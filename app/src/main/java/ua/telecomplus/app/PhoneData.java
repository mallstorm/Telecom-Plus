package ua.telecomplus.app;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONArray;
import org.json.JSONObject;

/** Локальное зашифрованное хранилище телефонии: книга контактов, журнал вызовов, переписка. */
final class PhoneData {
    static final int CALL_IN = 0;
    static final int CALL_OUT = 1;
    static final int CALL_MISSED = 2;

    static final class Contact {
        String id;
        String name;
        String number;
        boolean favorite;
    }

    static final class CallEntry {
        String number;
        int dir;
        long time;
        int duration;
    }

    static final class Msg {
        String id;
        boolean outgoing;
        String text;
        long time;
        /** 0 — отправляется, 1 — отправлено, 2 — доставлено, -1 — ошибка */
        int status;
    }

    static final class Thread {
        String number;
        long updated;
        int unread;
        final List<Msg> messages = new ArrayList<>();
    }

    interface Listener {
        void onDataChanged();
    }

    private static final String K_CONTACTS = "pd_contacts_v1";
    private static final String K_CALLS = "pd_calls_v1";
    private static final String K_THREADS = "pd_threads_v1";
    private static PhoneData instance;

    private final SecureVault vault;
    private final List<Contact> contacts = new ArrayList<>();
    private final List<CallEntry> calls = new ArrayList<>();
    private final List<Thread> threads = new ArrayList<>();
    private final CopyOnWriteArrayList<Listener> listeners = new CopyOnWriteArrayList<>();

    static synchronized PhoneData get(SecureVault secureVault) {
        if (instance == null) {
            instance = new PhoneData(secureVault);
        }
        return instance;
    }

    private PhoneData(SecureVault secureVault) {
        this.vault = secureVault;
        load();
    }

    void addListener(Listener listener) {
        this.listeners.addIfAbsent(listener);
    }

    void removeListener(Listener listener) {
        this.listeners.remove(listener);
    }

    private void changed() {
        for (Listener l : this.listeners) {
            try {
                l.onDataChanged();
            } catch (Exception unused) {
            }
        }
    }

    // ---------- номера ----------

    static String key(String str) {
        String d = str == null ? "" : str.replaceAll("[^0-9]", "");
        if (d.length() < 5) {
            return str == null ? "" : str.trim().toLowerCase(Locale.ROOT);
        }
        return d.length() > 9 ? d.substring(d.length() - 9) : d;
    }

    /** Приводит номер к виду для набора: 380XXXXXXXXX для украинских. */
    static String dialable(String str) {
        String d = str == null ? "" : str.replaceAll("[^0-9*#]", "");
        if (d.length() < 5 && !str.matches("[0-9*#+ ()-]*")) {
            return str;
        }
        if (d.length() == 10 && d.startsWith("0")) {
            return "38" + d;
        }
        if (d.length() == 9) {
            return "380" + d;
        }
        return d;
    }

    static String pretty(String str) {
        String d = dialable(str);
        if (d.length() == 12 && d.startsWith("380")) {
            return "+380 " + d.substring(3, 5) + " " + d.substring(5, 8) + " " + d.substring(8, 10) + " " + d.substring(10);
        }
        return str == null ? "" : str;
    }

    // ---------- контакты ----------

    synchronized List<Contact> contacts() {
        List<Contact> copy = new ArrayList<>(this.contacts);
        Collections.sort(copy, new Comparator<Contact>() {
            @Override
            public int compare(Contact a, Contact b) {
                if (a.favorite != b.favorite) {
                    return a.favorite ? -1 : 1;
                }
                return a.name.toLowerCase(Locale.ROOT).compareTo(b.name.toLowerCase(Locale.ROOT));
            }
        });
        return copy;
    }

    synchronized String nameFor(String str) {
        String k = key(str);
        if (k.isEmpty()) {
            return null;
        }
        for (Contact c : this.contacts) {
            if (key(c.number).equals(k)) {
                return c.name;
            }
        }
        return null;
    }

    String title(String str) {
        String n = nameFor(str);
        return n != null ? n : pretty(str);
    }

    synchronized Contact addContact(String str, String str2) {
        String k = key(str2);
        for (Contact c : this.contacts) {
            if (key(c.number).equals(k)) {
                c.name = str.isEmpty() ? c.name : str;
                saveContacts();
                changed();
                return c;
            }
        }
        Contact c = new Contact();
        c.id = UUID.randomUUID().toString();
        c.name = str.isEmpty() ? pretty(str2) : str;
        c.number = dialable(str2);
        this.contacts.add(c);
        saveContacts();
        changed();
        return c;
    }

    synchronized void updateContact(Contact contact, String str, String str2, boolean z) {
        contact.name = str;
        contact.number = dialable(str2);
        contact.favorite = z;
        saveContacts();
        changed();
    }

    synchronized void removeContact(Contact contact) {
        this.contacts.remove(contact);
        saveContacts();
        changed();
    }

    /** Импорт из телефонной книги устройства (нужно разрешение READ_CONTACTS). Возвращает число добавленных. */
    int importDeviceContacts(Context context) {
        int added = 0;
        ContentResolver resolver = context.getContentResolver();
        try (Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null)) {
            if (cursor == null) {
                return 0;
            }
            while (cursor.moveToNext()) {
                String name = cursor.getString(0);
                String number = cursor.getString(1);
                if (number == null || key(number).length() < 5) {
                    continue;
                }
                synchronized (this) {
                    boolean exists = false;
                    String k = key(number);
                    for (Contact c : this.contacts) {
                        if (key(c.number).equals(k)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists) {
                        Contact c = new Contact();
                        c.id = UUID.randomUUID().toString();
                        c.name = name == null || name.isEmpty() ? pretty(number) : name;
                        c.number = dialable(number);
                        this.contacts.add(c);
                        added++;
                    }
                }
            }
        } catch (Exception e) {
            TelephonyLog.log("Импорт контактов: " + e);
        }
        synchronized (this) {
            saveContacts();
        }
        changed();
        return added;
    }

    // ---------- журнал вызовов ----------

    synchronized List<CallEntry> calls() {
        return new ArrayList<>(this.calls);
    }

    synchronized void addCall(String str, int i, long j, int i2) {
        for (CallEntry e : this.calls) {
            if (e.time == j && key(e.number).equals(key(str))) {
                return;
            }
        }
        CallEntry entry = new CallEntry();
        entry.number = str;
        entry.dir = i;
        entry.time = j;
        entry.duration = i2;
        this.calls.add(0, entry);
        Collections.sort(this.calls, new Comparator<CallEntry>() {
            @Override
            public int compare(CallEntry a, CallEntry b) {
                return Long.compare(b.time, a.time);
            }
        });
        while (this.calls.size() > 300) {
            this.calls.remove(this.calls.size() - 1);
        }
        saveCalls();
        changed();
    }

    synchronized void clearCalls() {
        this.calls.clear();
        saveCalls();
        changed();
    }

    // ---------- переписка ----------

    synchronized List<Thread> threads() {
        List<Thread> copy = new ArrayList<>(this.threads);
        Collections.sort(copy, new Comparator<Thread>() {
            @Override
            public int compare(Thread a, Thread b) {
                return Long.compare(b.updated, a.updated);
            }
        });
        return copy;
    }

    synchronized Thread thread(String str) {
        String k = key(str);
        for (Thread t : this.threads) {
            if (key(t.number).equals(k)) {
                return t;
            }
        }
        return null;
    }

    synchronized Thread openThread(String str) {
        Thread t = thread(str);
        if (t == null) {
            t = new Thread();
            t.number = dialable(str);
            t.updated = System.currentTimeMillis();
            this.threads.add(t);
            saveThreads();
            changed();
        }
        return t;
    }

    synchronized Msg addMessage(String str, String str2, boolean z, String str3, long j, int i) {
        Thread t = openThread(str);
        if (str2 != null) {
            for (Msg m : t.messages) {
                if (str2.equals(m.id)) {
                    m.status = i;
                    saveThreads();
                    changed();
                    return m;
                }
            }
        }
        Msg m = new Msg();
        m.id = str2 == null ? UUID.randomUUID().toString() : str2;
        m.outgoing = z;
        m.text = str3;
        m.time = j;
        m.status = i;
        t.messages.add(m);
        Collections.sort(t.messages, new Comparator<Msg>() {
            @Override
            public int compare(Msg a, Msg b) {
                return Long.compare(a.time, b.time);
            }
        });
        t.updated = Math.max(t.updated, j);
        if (!z) {
            t.unread++;
        }
        saveThreads();
        changed();
        return m;
    }

    synchronized void setMessageStatus(String str, String str2, int i) {
        Thread t = thread(str);
        if (t == null) {
            return;
        }
        for (Msg m : t.messages) {
            if (m.id.equals(str2)) {
                m.status = i;
            }
        }
        saveThreads();
        changed();
    }

    synchronized void markRead(String str) {
        Thread t = thread(str);
        if (t != null && t.unread != 0) {
            t.unread = 0;
            saveThreads();
            changed();
        }
    }

    synchronized int unreadTotal() {
        int total = 0;
        for (Thread t : this.threads) {
            total += t.unread;
        }
        return total;
    }

    synchronized void deleteThread(String str) {
        Thread t = thread(str);
        if (t != null) {
            this.threads.remove(t);
            saveThreads();
            changed();
        }
    }

    // ---------- хранение ----------

    private void load() {
        try {
            String s = this.vault.get(K_CONTACTS);
            if (s != null && !s.isEmpty()) {
                JSONArray a = new JSONArray(s);
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    Contact c = new Contact();
                    c.id = o.optString("id", UUID.randomUUID().toString());
                    c.name = o.optString("name");
                    c.number = o.optString("number");
                    c.favorite = o.optBoolean("fav");
                    this.contacts.add(c);
                }
            }
            s = this.vault.get(K_CALLS);
            if (s != null && !s.isEmpty()) {
                JSONArray a = new JSONArray(s);
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    CallEntry e = new CallEntry();
                    e.number = o.optString("n");
                    e.dir = o.optInt("d");
                    e.time = o.optLong("t");
                    e.duration = o.optInt("s");
                    this.calls.add(e);
                }
            }
            s = this.vault.get(K_THREADS);
            if (s != null && !s.isEmpty()) {
                JSONArray a = new JSONArray(s);
                for (int i = 0; i < a.length(); i++) {
                    JSONObject o = a.getJSONObject(i);
                    Thread t = new Thread();
                    t.number = o.optString("n");
                    t.updated = o.optLong("u");
                    t.unread = o.optInt("r");
                    JSONArray ms = o.optJSONArray("m");
                    if (ms != null) {
                        for (int j = 0; j < ms.length(); j++) {
                            JSONObject mo = ms.getJSONObject(j);
                            Msg m = new Msg();
                            m.id = mo.optString("i");
                            m.outgoing = mo.optBoolean("o");
                            m.text = mo.optString("x");
                            m.time = mo.optLong("t");
                            m.status = mo.optInt("s");
                            t.messages.add(m);
                        }
                    }
                    this.threads.add(t);
                }
            }
        } catch (Exception e) {
            TelephonyLog.log("PhoneData.load: " + e);
        }
    }

    private void saveContacts() {
        try {
            JSONArray a = new JSONArray();
            for (Contact c : this.contacts) {
                a.put(new JSONObject().put("id", c.id).put("name", c.name).put("number", c.number).put("fav", c.favorite));
            }
            this.vault.put(K_CONTACTS, a.toString());
        } catch (Exception e) {
            TelephonyLog.log("saveContacts: " + e);
        }
    }

    private void saveCalls() {
        try {
            JSONArray a = new JSONArray();
            for (CallEntry e : this.calls) {
                a.put(new JSONObject().put("n", e.number).put("d", e.dir).put("t", e.time).put("s", e.duration));
            }
            this.vault.put(K_CALLS, a.toString());
        } catch (Exception e) {
            TelephonyLog.log("saveCalls: " + e);
        }
    }

    private void saveThreads() {
        try {
            JSONArray a = new JSONArray();
            for (Thread t : this.threads) {
                JSONArray ms = new JSONArray();
                int from = Math.max(0, t.messages.size() - 200);
                for (int i = from; i < t.messages.size(); i++) {
                    Msg m = t.messages.get(i);
                    ms.put(new JSONObject().put("i", m.id).put("o", m.outgoing).put("x", m.text).put("t", m.time).put("s", m.status));
                }
                a.put(new JSONObject().put("n", t.number).put("u", t.updated).put("r", t.unread).put("m", ms));
            }
            this.vault.put(K_THREADS, a.toString());
        } catch (Exception e) {
            TelephonyLog.log("saveThreads: " + e);
        }
    }

    synchronized void clearAll() {
        this.contacts.clear();
        this.calls.clear();
        this.threads.clear();
        this.vault.remove(K_CONTACTS);
        this.vault.remove(K_CALLS);
        this.vault.remove(K_THREADS);
        changed();
    }
}
