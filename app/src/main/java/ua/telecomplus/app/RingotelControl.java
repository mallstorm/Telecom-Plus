package ua.telecomplus.app;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Канал управления Ringotel (WSS, подпротокол json.api.smile-soft.com, Basic termid:termpass,
 * JSON-RPC {method, params, id}). Синхронизирует контакты, переписку (чат/SMS) и историю вызовов
 * в локальное хранилище PhoneData. Формат сообщений восстановлен по официальному клиенту; сырые
 * ответы сервера пишутся в журнал телефонии для уточнения.
 */
final class RingotelControl implements SipWebSocket.Listener {
    private static final String SUBPROTOCOL = "json.api.smile-soft.com";
    private static final int T_TEXT = 1;
    private static final int T_SMS = 20;
    private static final int T_CALL = 8;

    private static final class Pending {
        final CountDownLatch latch = new CountDownLatch(1);
        Object result;
        String error;
    }

    private final Context context;
    private final PhoneData data;
    private final String host;
    private final int port;
    private final String termId;
    private final String termPass;
    private final ConcurrentHashMap<Long, Pending> pending = new ConcurrentHashMap<>();
    private final Map<String, String> userNumbers = new HashMap<>();
    private final Map<String, String> sessionPeers = new HashMap<>();
    private final Map<String, String> threadSessions = new HashMap<>();
    private volatile SipWebSocket socket;
    private volatile boolean stopped = true;
    private volatile boolean online;
    private volatile String myUserId = "";
    private volatile String myName = "";
    private volatile JSONObject profileJson = new JSONObject();
    private volatile JSONArray trunksJson = new JSONArray();
    private volatile JSONArray providersJson = new JSONArray();
    private final Map<String, String> userNames = new HashMap<>();
    private long nextId = System.currentTimeMillis() & 0x7fffffffL;
    private Thread worker;

    RingotelControl(Context context, PhoneData phoneData, String str, int i, String str2, String str3) {
        this.context = context.getApplicationContext();
        this.data = phoneData;
        this.host = str;
        this.port = i;
        this.termId = str2;
        this.termPass = str3;
    }

    boolean isOnline() {
        return this.online;
    }

    void start() {
        if (!this.stopped) {
            return;
        }
        this.stopped = false;
        this.worker = new Thread(new Runnable() {
            @Override
            public void run() {
                RingotelControl.this.loop();
            }
        }, "ringotel-control");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    void stop() {
        this.stopped = true;
        this.online = false;
        SipWebSocket ws = this.socket;
        if (ws != null) {
            ws.close();
        }
        if (this.worker != null) {
            this.worker.interrupt();
        }
    }

    private void loop() {
        int backoff = 3000;
        while (!this.stopped) {
            try {
                connect();
                backoff = 3000;
                long last = System.currentTimeMillis();
                while (!this.stopped && this.socket != null && this.socket.isOpen()) {
                    Thread.sleep(1000);
                    if (System.currentTimeMillis() - last >= 30000) {
                        last = System.currentTimeMillis();
                        this.socket.sendPing();
                    }
                }
            } catch (InterruptedException unused) {
                return;
            } catch (Exception e) {
                TelephonyLog.log("Control: " + String.valueOf(e.getMessage()).split(" \\| ")[0]);
            }
            this.online = false;
            if (this.stopped) {
                return;
            }
            try {
                Thread.sleep(backoff);
            } catch (InterruptedException unused2) {
                return;
            }
            backoff = Math.min(backoff * 2, 60000);
        }
    }

    private void connect() throws Exception {
        TelephonyLog.log("Control: подключение (" + SUBPROTOCOL + ")");
        SipWebSocket ws = new SipWebSocket(this.host, this.port, SUBPROTOCOL, "Ringotel", this);
        if (RingotelHostnameVerifier.HOST.equalsIgnoreCase(this.host)) {
            ws.applyDirectMode(SipWebSocket.savedDirectMode());
        }
        ws.setBasicAuth(this.termId, this.termPass);
        this.socket = ws;
        ws.connect();
        this.online = true;
        TelephonyLog.log("Control: канал открыт");
        sync();
    }

    // ---------- RPC ----------

    Object call(String str, JSONObject jSONObject, long j) throws Exception {
        SipWebSocket ws = this.socket;
        if (ws == null || !ws.isOpen()) {
            throw new IllegalStateException("Канал управления не подключён");
        }
        long id;
        synchronized (this) {
            id = ++this.nextId;
        }
        Pending p = new Pending();
        this.pending.put(Long.valueOf(id), p);
        JSONObject msg = new JSONObject();
        msg.put("method", str);
        msg.put("params", jSONObject == null ? new JSONObject() : jSONObject);
        msg.put("id", id);
        ws.sendText(msg.toString());
        try {
            if (!p.latch.await(j, TimeUnit.MILLISECONDS)) {
                throw new Exception("Сервер не ответил на " + str);
            }
        } finally {
            this.pending.remove(Long.valueOf(id));
        }
        if (p.error != null) {
            throw new Exception(p.error);
        }
        return p.result;
    }

    void indicate(String str, JSONObject jSONObject) throws Exception {
        SipWebSocket ws = this.socket;
        if (ws == null || !ws.isOpen()) {
            throw new IllegalStateException("Канал управления не подключён");
        }
        JSONObject msg = new JSONObject();
        msg.put("method", str);
        msg.put("params", jSONObject == null ? new JSONObject() : jSONObject);
        ws.sendText(msg.toString());
    }

    @Override
    public void onText(String str) {
        try {
            JSONObject o = new JSONObject(str);
            Object id = o.opt("id");
            if (id != null && id != JSONObject.NULL) {
                Pending p = this.pending.get(Long.valueOf(((Number) id).longValue()));
                if (p == null) {
                    return;
                }
                if (o.has("result") && !o.isNull("result")) {
                    p.result = o.get("result");
                } else if (o.has("error") && !o.isNull("error")) {
                    JSONObject e = o.optJSONObject("error");
                    p.error = e != null ? e.optString("message", "Ошибка сервера") : String.valueOf(o.get("error"));
                }
                p.latch.countDown();
                return;
            }
            final String method = o.optString("method");
            final JSONObject params = o.optJSONObject("params");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    RingotelControl.this.onCommand(method, params == null ? new JSONObject() : params);
                }
            }, "control-cmd").start();
        } catch (Exception e) {
            TelephonyLog.log("Control: разбор: " + e);
        }
    }

    @Override
    public void onClosed(String str) {
        TelephonyLog.log("Control: закрыт: " + str);
        this.online = false;
        for (Pending p : this.pending.values()) {
            p.error = "канал закрыт";
            p.latch.countDown();
        }
    }

    // ---------- синхронизация ----------

    private static String clip(Object o) {
        String s = String.valueOf(o);
        return s.length() > 1500 ? s.substring(0, 1500) + "…" : s;
    }

    private void sync() {
        boolean loaded = false;
        try {
            JSONObject lp = new JSONObject();
            lp.put("since", 0);
            lp.put("termid", this.termId);
            lp.put("devinfo", "Android " + Build.VERSION.RELEASE + " " + Build.MANUFACTURER + " " + Build.MODEL + " (Telecom+)");
            lp.put("signin", true);
            Object objects = call("loadObjects", lp, 45000);
            TelephonyLog.log("Control loadObjects: " + clip(objects));
            if (objects instanceof JSONObject) {
                applyObjects((JSONObject) objects);
                loaded = true;
            }
        } catch (Exception e) {
            TelephonyLog.log("Control loadObjects: " + e);
        }
        if (!loaded) {
            try {
                Object profile = call("getProfile", new JSONObject(), 15000);
                TelephonyLog.log("Control getProfile: " + clip(profile));
                if (profile instanceof JSONObject) {
                    applyProfile((JSONObject) profile);
                }
            } catch (Exception e) {
                TelephonyLog.log("Control getProfile: " + e);
            }
        }
        try {
            JSONObject params = new JSONObject();
            params.put("since", 0);
            params.put("now", System.currentTimeMillis());
            params.put("termid", this.termId);
            Object events = call("getEvents", params, 30000);
            TelephonyLog.log("Control getEvents: " + clip(events));
            if (events instanceof JSONObject) {
                processEvents((JSONObject) events);
            }
        } catch (Exception e) {
            TelephonyLog.log("Control getEvents: " + e);
        }
        loadHistories();
        try {
            refreshTrunks();
        } catch (Exception unused) {
        }
    }

    private void applyProfile(JSONObject p) {
        this.profileJson = p;
        this.myUserId = p.optString("userid", p.optString("id", this.myUserId));
        this.myName = p.optString("name", p.optString("displayname", this.myName));
    }

    private void applyObjects(JSONObject o) {
        JSONObject p = o.optJSONObject("profile");
        if (p != null) {
            applyProfile(p);
        }
        JSONArray t = o.optJSONArray("trunks");
        if (t != null) {
            this.trunksJson = t;
        }
        JSONArray pr = o.optJSONArray("providers");
        if (pr != null) {
            this.providersJson = pr;
        }
        processEvents(o);
    }

    private void loadHistories() {
        java.util.List<String> ids = new java.util.ArrayList<>(this.sessionPeers.keySet());
        int n = 0;
        for (String id : ids) {
            if (this.stopped || n++ >= 40) {
                return;
            }
            try {
                JSONObject params = new JSONObject();
                params.put("sessionid", id);
                params.put("since", 0);
                params.put("count", 100);
                Object res = call("getMessages", params, 15000);
                JSONArray arr = res instanceof JSONArray ? (JSONArray) res : (res instanceof JSONObject ? ((JSONObject) res).optJSONArray("messages") : null);
                if (arr == null) {
                    continue;
                }
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject m = arr.optJSONObject(i);
                    if (m != null) {
                        if (!m.has("sessionid")) {
                            m.put("sessionid", id);
                        }
                        processMessage(m, false);
                    }
                }
            } catch (Exception e) {
                TelephonyLog.log("Control getMessages " + id + ": " + e);
            }
        }
    }

    // ---------- аккаунт, номера, баланс ----------

    JSONObject profile() {
        return this.profileJson;
    }

    String userId() {
        return this.myUserId;
    }

    JSONArray trunks() {
        return this.trunksJson;
    }

    JSONArray providers() {
        return this.providersJson;
    }

    /** Идентификатор провайдера Интертелеком в списке провайдеров аккаунта. */
    String providerId() {
        JSONArray a = this.providersJson;
        String fallback = null;
        for (int i = 0; i < a.length(); i++) {
            JSONObject p = a.optJSONObject(i);
            if (p == null) {
                continue;
            }
            String id = p.optString("id", p.optString("provider", ""));
            String name = p.optString("name", "").toLowerCase(java.util.Locale.ROOT);
            if (name.contains("intertelecom") || name.contains("интертелеком") || name.contains("інтертелеком")) {
                return id;
            }
            if (fallback == null && !id.isEmpty() && !id.equals("-1")) {
                fallback = id;
            }
        }
        return fallback;
    }

    private static JSONObject asObject(Object o) {
        return o instanceof JSONObject ? (JSONObject) o : new JSONObject();
    }

    JSONArray refreshTrunks() throws Exception {
        Object res = call("getSIPAccounts", new JSONObject(), 20000);
        TelephonyLog.log("Control getSIPAccounts: " + clip(res));
        if (res instanceof JSONArray) {
            this.trunksJson = (JSONArray) res;
        }
        return this.trunksJson;
    }

    JSONObject phoneBalance(String provider, String phone) throws Exception {
        JSONObject p = new JSONObject();
        p.put("provider", provider);
        p.put("phone_number", phone);
        Object res = call("getPhoneNumberBalance", p, 20000);
        TelephonyLog.log("Control balance " + phone + ": " + clip(res));
        return asObject(res);
    }

    JSONObject offer(String provider) throws Exception {
        JSONObject p = new JSONObject();
        p.put("provider", provider);
        p.put("language", java.util.Locale.getDefault().getLanguage());
        p.put("country", java.util.Locale.getDefault().getCountry());
        Object res = call("getOffer", p, 25000);
        TelephonyLog.log("Control getOffer: " + clip(res));
        return asObject(res);
    }

    /** Запрос покупки номера: сервер возвращает ссылку на оплату (payment_url), списания без оплаты нет. */
    JSONObject requestNumberPurchase(String provider, String phone, String packageId) throws Exception {
        JSONObject p = new JSONObject();
        p.put("provider", provider);
        p.put("phone_number", phone);
        p.put("package_id", packageId);
        Object res = call("getPhoneNumber", p, 30000);
        TelephonyLog.log("Control getPhoneNumber: " + clip(res));
        return asObject(res);
    }

    /** Привязка существующего номера: без кода сервер звонит на номер, с кодом подтверждает привязку. */
    JSONObject connectNumber(String provider, String phone, String code) throws Exception {
        JSONObject p = new JSONObject();
        p.put("provider", provider);
        p.put("phone_number", phone);
        p.put("package_id", JSONObject.NULL);
        p.put("code", code == null ? JSONObject.NULL : code);
        Object res = call("connectPhoneNumber", p, 40000);
        TelephonyLog.log("Control connectPhoneNumber: " + clip(res));
        refreshTrunksQuietly();
        return asObject(res);
    }

    void deleteTrunk(String oid) throws Exception {
        JSONObject p = new JSONObject();
        p.put("oid", oid);
        call("deleteSIPAccount", p, 20000);
        refreshTrunksQuietly();
    }

    private void refreshTrunksQuietly() {
        try {
            refreshTrunks();
        } catch (Exception unused) {
        }
    }

    /** «Перезагрузить объекты»: контакты, чаты и история заново с сервера. */
    void reloadObjects() throws Exception {
        this.data.clearForReload();
        this.sessionPeers.clear();
        this.threadSessions.clear();
        sync();
    }

    private void onCommand(String str, JSONObject jSONObject) {
        TelephonyLog.log("Control ← " + str + " " + clip(jSONObject));
        try {
            if (str.equals("events")) {
                processEvents(jSONObject);
            } else if (str.equals("message")) {
                processMessage(jSONObject, true);
            } else if (str.equals("delivery")) {
                String sid = this.sessionPeers.get(jSONObject.optString("sessionid"));
                String mid = jSONObject.optString("messageid", jSONObject.optString("id"));
                if (sid != null && !mid.isEmpty()) {
                    this.data.setMessageStatus(sid, mid, 2);
                }
            } else if (str.equals("sessionUpdated") || str.equals("sessionCreated")) {
                processSession(jSONObject);
            } else if (str.equals("contactCreated") || str.equals("contactUpdated")) {
                processContact(jSONObject);
            }
        } catch (Exception e) {
            TelephonyLog.log("Control cmd " + str + ": " + e);
        }
    }

    private void processEvents(JSONObject o) {
        JSONArray contacts = o.optJSONArray("contacts");
        if (contacts != null) {
            for (int i = 0; i < contacts.length(); i++) {
                processContact(contacts.optJSONObject(i));
            }
        }
        JSONArray sessions = o.optJSONArray("sessions");
        if (sessions != null) {
            for (int i = 0; i < sessions.length(); i++) {
                processSession(sessions.optJSONObject(i));
            }
        }
        JSONArray messages = o.optJSONArray("messages");
        if (messages != null) {
            for (int i = 0; i < messages.length(); i++) {
                processMessage(messages.optJSONObject(i), false);
            }
        }
    }

    private static String phoneOf(JSONObject o) {
        String[] keys = {"phone", "mobile", "number", "tel", "msisdn", "work", "home"};
        for (String k : keys) {
            Object v = o.opt(k);
            String s = firstString(v);
            if (s != null && s.replaceAll("[^0-9]", "").length() >= 7) {
                return s;
            }
        }
        Iterator<String> it = o.keys();
        while (it.hasNext()) {
            String k = it.next();
            if (k.toLowerCase().contains("phone") || k.toLowerCase().contains("number")) {
                String s = firstString(o.opt(k));
                if (s != null && s.replaceAll("[^0-9]", "").length() >= 7) {
                    return s;
                }
            }
        }
        return null;
    }

    private static String firstString(Object v) {
        if (v == null || v == JSONObject.NULL) {
            return null;
        }
        if (v instanceof JSONArray) {
            JSONArray a = (JSONArray) v;
            for (int i = 0; i < a.length(); i++) {
                String s = firstString(a.opt(i));
                if (s != null) {
                    return s;
                }
            }
            return null;
        }
        if (v instanceof JSONObject) {
            return firstString(((JSONObject) v).opt("value"));
        }
        return String.valueOf(v);
    }

    private void processContact(JSONObject o) {
        if (o == null) {
            return;
        }
        String userid = o.optString("userid", o.optString("id", ""));
        String number = phoneOf(o);
        String name = o.optString("name", "").trim();
        if (name.isEmpty()) {
            name = (o.optString("firstname", "") + " " + o.optString("lastname", "")).trim();
        }
        if (name.isEmpty()) {
            name = o.optString("displayname", "").trim();
        }
        if (!userid.isEmpty() && number != null) {
            synchronized (this.userNumbers) {
                this.userNumbers.put(userid, number);
            }
        }
        if (!userid.isEmpty() && number == null && !name.isEmpty()) {
            synchronized (this.userNames) {
                this.userNames.put(userid, name);
            }
        }
        if (number != null && !name.isEmpty() && !userid.equals(this.myUserId)) {
            this.data.addContact(name, number);
        }
    }

    private void processSession(JSONObject o) {
        if (o == null) {
            return;
        }
        String id = o.optString("id", "");
        JSONArray parties = o.optJSONArray("parties");
        if (id.isEmpty() || parties == null) {
            return;
        }
        for (int i = 0; i < parties.length(); i++) {
            String party = parties.optString(i);
            if (party.isEmpty() || party.equals(this.myUserId)) {
                continue;
            }
            String number;
            synchronized (this.userNumbers) {
                number = this.userNumbers.get(party);
            }
            if (number == null) {
                synchronized (this.userNames) {
                    number = this.userNames.get(party);
                }
            }
            if (number == null) {
                number = o.optString("name", party);
            }
            this.sessionPeers.put(id, number);
            this.threadSessions.put(PhoneData.key(number), id);
            return;
        }
    }

    private static long millis(long t) {
        return t > 0 && t < 100000000000L ? t * 1000L : t;
    }

    private void processMessage(JSONObject o, boolean live) {
        if (o == null) {
            return;
        }
        String sessionId = o.optString("sessionid", "");
        int type = o.optInt("type", T_TEXT);
        int status = o.optInt("status", 0);
        String sender = o.optString("userid", o.optString("senderid", o.optString("from", "")));
        String content = o.optString("content", "");
        long time = millis(o.optLong("time", o.optLong("timestamp", System.currentTimeMillis())));
        String id = o.optString("id", "");
        if (type == T_CALL) {
            processCallRecord(o, content, time, id);
            return;
        }
        if (type != T_TEXT && type != T_SMS && type != 0) {
            return;
        }
        if (content.isEmpty()) {
            return;
        }
        boolean outgoing = (!this.myUserId.isEmpty() && sender.equals(this.myUserId)) || status == 0 || status == 3 || status == 4 || status == -1;
        String number = this.sessionPeers.get(sessionId);
        if (number == null) {
            String s;
            synchronized (this.userNumbers) {
                s = this.userNumbers.get(sender);
            }
            number = s != null ? s : (sender.isEmpty() ? sessionId : sender);
        }
        PhoneData.Msg existing = null;
        PhoneData.Thread t = this.data.thread(number);
        if (t != null && !id.isEmpty()) {
            for (PhoneData.Msg m : t.messages) {
                if (id.equals(m.id)) {
                    existing = m;
                }
            }
        }
        if (existing != null || this.data.isBlocked(number)) {
            return;
        }
        boolean wasForeground = TpEnv.foreground;
        this.data.addMessage(number, id.isEmpty() ? null : id, outgoing, content, time, outgoing ? (status == 4 ? 2 : 1) : 2);
        if (live && !outgoing && !wasForeground) {
            notifyMessage(number, content);
        }
    }

    private void processCallRecord(JSONObject o, String content, long time, String id) {
        try {
            JSONObject c = new JSONObject(content);
            int status = o.optInt("status", 5);
            String caller = c.optString("caller", "");
            String called = c.optString("called", "");
            int duration = c.optInt("duration", 0);
            boolean incoming = status == 5 || status == 12 || status == 13;
            String number = incoming ? caller : called;
            if (number.isEmpty()) {
                number = incoming ? called : caller;
            }
            if (number.isEmpty()) {
                return;
            }
            int dir = status == 12 || status == 13 || status == 14 ? PhoneData.CALL_MISSED : (incoming ? PhoneData.CALL_IN : PhoneData.CALL_OUT);
            this.data.addCall(number, dir, time, duration);
        } catch (Exception unused) {
        }
    }

    private void notifyMessage(String number, String text) {
        try {
            NotificationManager nm = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent intent = new Intent(this.context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("open_chat", number);
            PendingIntent pi = PendingIntent.getActivity(this.context, PhoneData.key(number).hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            Notification.Builder b = Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this.context, TelephonyService.CH_MESSAGES) : new Notification.Builder(this.context);
            b.setSmallIcon(Res.d("ic_telecom_plus")).setContentTitle(this.data.title(number)).setContentText(text).setStyle(new Notification.BigTextStyle().bigText(text)).setAutoCancel(true).setContentIntent(pi);
            nm.notify(PhoneData.key(number).hashCode(), b.build());
        } catch (Exception e) {
            TelephonyLog.log("notifyMessage: " + e);
        }
    }

    // ---------- отправка ----------

    /** Отправляет сообщение (чат/SMS) на номер. Локально сообщение сохраняется сразу. */
    void sendText(final String str, final String str2) {
        final String id = UUID.randomUUID().toString();
        this.data.addMessage(str, id, true, str2, System.currentTimeMillis(), 0);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String sessionId = RingotelControl.this.threadSessions.get(PhoneData.key(str));
                    String dest = PhoneData.dialable(str);
                    if (sessionId == null) {
                        JSONObject cs = new JSONObject();
                        cs.put("parties", new JSONArray().put(dest));
                        cs.put("status", 0);
                        cs.put("myname", RingotelControl.this.myName);
                        Object res = RingotelControl.this.call("createSession", cs, 20000);
                        TelephonyLog.log("Control createSession: " + clip(res));
                        if (res instanceof JSONObject) {
                            sessionId = ((JSONObject) res).optString("id", null);
                        }
                        if (sessionId == null || sessionId.isEmpty()) {
                            throw new Exception("Сервер не создал диалог");
                        }
                        RingotelControl.this.threadSessions.put(PhoneData.key(str), sessionId);
                        RingotelControl.this.sessionPeers.put(sessionId, dest);
                    }
                    JSONObject m = new JSONObject();
                    m.put("id", id);
                    m.put("type", T_TEXT);
                    m.put("content", str2);
                    m.put("lifetime", 0);
                    m.put("code", 0);
                    m.put("sessionid", sessionId);
                    RingotelControl.this.indicate("sendMessage_", m);
                    RingotelControl.this.data.setMessageStatus(str, id, 1);
                } catch (Exception e) {
                    TelephonyLog.log("Control sendText: " + e);
                    RingotelControl.this.data.setMessageStatus(str, id, -1);
                }
            }
        }, "control-send").start();
    }

    /** Сообщает серверу, что чат прочитан. */
    void markRead(String str) {
        try {
            String sessionId = this.threadSessions.get(PhoneData.key(str));
            if (sessionId != null && this.online) {
                JSONObject m = new JSONObject();
                m.put("sessionid", sessionId);
                indicate("messageRead", m);
            }
        } catch (Exception unused) {
        }
    }
}
