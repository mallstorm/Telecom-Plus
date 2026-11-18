package ua.telecomplus.app;

import android.util.Base64;
import java.net.HttpCookie;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class PersistentCookieStore {
    private static final String KEY = "portal_cookies_v3";
    private static final long SESSION_COOKIE_RETENTION_MS = 2592000000L;
    private final Map<String, StoredCookie> cookies = new LinkedHashMap();
    private final SecureVault vault;

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class StoredCookie {
        final String domain;
        final long expiresAt;
        final boolean hostOnly;
        final String name;
        final String path;
        final boolean secure;
        final String value;

        public StoredCookie(String str, String str2, String str3, String str4, long j, boolean z, boolean z2) {
            this.name = PersistentCookieStore.nz(str);
            this.value = PersistentCookieStore.nz(str2);
            this.domain = PersistentCookieStore.nz(str3);
            this.path = PersistentCookieStore.nz(str4);
            this.expiresAt = j;
            this.secure = z;
            this.hostOnly = z2;
        }
    }

    public PersistentCookieStore(SecureVault secureVault) {
        this.vault = secureVault;
        restore();
    }

    private static String cookieKey(String str, String str2, String str3, boolean z) {
        return nz(str) + "|" + nz(str2) + "|" + nz(str3) + "|" + z;
    }

    private static String defaultCookiePath(String str) {
        int lastIndexOf;
        String nz = nz(str);
        return (!nz.startsWith("/") || nz.equals("/") || (lastIndexOf = nz.lastIndexOf(47)) <= 0) ? "/" : nz.substring(0, lastIndexOf);
    }

    private static boolean domainMatches(String str, String str2) {
        String nz = nz(str);
        Locale locale = Locale.US;
        String lowerCase = nz.toLowerCase(locale);
        String lowerCase2 = nz(str2).replaceFirst("^\\.", "").toLowerCase(locale);
        if (lowerCase2.isEmpty()) {
            return false;
        }
        return lowerCase.equals(lowerCase2) || lowerCase.endsWith(".".concat(lowerCase2));
    }

    private static String join(List<String> list, String str) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) {
                sb.append(str);
            }
            sb.append(list.get(i));
        }
        return sb.toString();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ int lambda$header$0(StoredCookie storedCookie, StoredCookie storedCookie2) {
        return Integer.compare(storedCookie2.path.length(), storedCookie.path.length());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String nz(String str) {
        return str == null ? "" : str;
    }

    private static boolean pathMatches(String str, String str2) {
        if (nz(str).isEmpty()) {
            str = "/";
        }
        if (nz(str2).isEmpty()) {
            str2 = "/";
        }
        if (str.equals(str2)) {
            return true;
        }
        if (str.startsWith(str2)) {
            return str2.endsWith("/") || (str.length() > str2.length() && str.charAt(str2.length()) == '/');
        }
        return false;
    }

    private void persist() {
        ArrayList arrayList = new ArrayList();
        for (StoredCookie storedCookie : this.cookies.values()) {
            String[] strArr = {storedCookie.name, storedCookie.value, storedCookie.domain, storedCookie.path, String.valueOf(storedCookie.expiresAt), String.valueOf(storedCookie.secure), String.valueOf(storedCookie.hostOnly)};
            ArrayList arrayList2 = new ArrayList();
            for (int i = 0; i < 7; i++) {
                arrayList2.add(Base64.encodeToString(strArr[i].getBytes(StandardCharsets.UTF_8), 10));
            }
            arrayList.add(join(arrayList2, "\t"));
        }
        String join = join(arrayList, "\n");
        boolean isEmpty = join.trim().isEmpty();
        SecureVault secureVault = this.vault;
        if (isEmpty) {
            secureVault.remove(KEY);
        } else {
            secureVault.put(KEY, join);
        }
    }

    private void purge(long j) {
        ArrayList arrayList = new ArrayList();
        for (Map.Entry<String, StoredCookie> entry : this.cookies.entrySet()) {
            if (entry.getValue().expiresAt <= j) {
                arrayList.add(entry.getKey());
            }
        }
        int size = arrayList.size();
        int i = 0;
        while (i < size) {
            Object obj = arrayList.get(i);
            i++;
            this.cookies.remove((String) obj);
        }
    }

    private void restore() {
        String str = this.vault.get(KEY);
        if (str == null) {
            return;
        }
        for (String str2 : str.split("\\R")) {
            try {
                String[] split = str2.split("\\t");
                if (split.length == 6 || split.length == 7) {
                    String[] strArr = new String[split.length];
                    for (int i = 0; i < split.length; i++) {
                        strArr[i] = new String(Base64.decode(split[i], 10), StandardCharsets.UTF_8);
                    }
                    StoredCookie storedCookie = new StoredCookie(strArr[0], strArr[1], strArr[2], strArr[3], Long.parseLong(strArr[4]), Boolean.parseBoolean(strArr[5]), split.length == 7 ? Boolean.parseBoolean(strArr[6]) : "services.intertelecom.ua".equalsIgnoreCase(strArr[2]));
                    this.cookies.put(cookieKey(storedCookie.name, storedCookie.path, storedCookie.domain, storedCookie.hostOnly), storedCookie);
                }
            } catch (Exception unused) {
            }
        }
        purge(System.currentTimeMillis());
    }

    public synchronized void capture(URI uri, List<String> list) {
        long now = System.currentTimeMillis();
        String host = nz(uri.getHost()).toLowerCase(Locale.US);
        List<String> headers = list == null ? Collections.<String>emptyList() : list;
        for (String header : headers) {
            List<HttpCookie> parsed;
            try {
                parsed = HttpCookie.parse(header);
            } catch (Exception unused) {
                parsed = Collections.emptyList();
            }
            for (HttpCookie httpCookie : parsed) {
                boolean hostOnly = httpCookie.getDomain() == null || httpCookie.getDomain().trim().isEmpty();
                String domain = hostOnly ? host : httpCookie.getDomain().trim().replaceFirst("^\\.", "").toLowerCase(Locale.US);
                if (!domainMatches(host, domain)) {
                    continue;
                }
                String path = httpCookie.getPath();
                if (path == null || path.trim().isEmpty() || !path.startsWith("/")) {
                    path = defaultCookiePath(uri.getPath());
                }
                long maxAge = httpCookie.getMaxAge();
                long expiresAt = 0;
                if (maxAge != 0) {
                    if (maxAge > 0) {
                        long millis = maxAge > 9223372036854775L ? Long.MAX_VALUE : maxAge * 1000;
                        expiresAt = (millis != Long.MAX_VALUE && now <= Long.MAX_VALUE - millis) ? now + millis : Long.MAX_VALUE;
                    } else {
                        expiresAt = now + SESSION_COOKIE_RETENTION_MS;
                    }
                }
                String key = cookieKey(httpCookie.getName(), path, domain, hostOnly);
                if (expiresAt <= now) {
                    this.cookies.remove(key);
                } else {
                    this.cookies.put(key, new StoredCookie(httpCookie.getName(), httpCookie.getValue(), domain, path, expiresAt, httpCookie.getSecure(), hostOnly));
                }
            }
        }
        purge(now);
        persist();
    }

    public synchronized void clear() {
        this.cookies.clear();
        this.vault.remove(KEY);
    }

    public synchronized boolean hasSession() {
        purge(System.currentTimeMillis());
        Iterator<StoredCookie> it = this.cookies.values().iterator();
        while (it.hasNext()) {
            if ("ITlaravelLKsess".equalsIgnoreCase(it.next().name)) {
                return true;
            }
        }
        return false;
    }

    public synchronized String header(URI uri) {
        try {
            long currentTimeMillis = System.currentTimeMillis();
            purge(currentTimeMillis);
            String lowerCase = nz(uri.getHost()).toLowerCase(Locale.US);
            if ("https".equalsIgnoreCase(uri.getScheme()) && !lowerCase.isEmpty()) {
                String nz = nz(uri.getPath());
                if (nz.isEmpty()) {
                    nz = "/";
                }
                ArrayList arrayList = new ArrayList();
                for (StoredCookie storedCookie : this.cookies.values()) {
                    boolean z = storedCookie.hostOnly;
                    String str = storedCookie.domain;
                    boolean equals = z ? lowerCase.equals(str) : domainMatches(lowerCase, str);
                    if (storedCookie.expiresAt > currentTimeMillis && equals && pathMatches(nz, storedCookie.path) && (!storedCookie.secure || "https".equalsIgnoreCase(uri.getScheme()))) {
                        arrayList.add(storedCookie);
                    }
                }
                Collections.sort(arrayList, new p());
                ArrayList arrayList2 = new ArrayList();
                int size = arrayList.size();
                int i = 0;
                while (i < size) {
                    Object obj = arrayList.get(i);
                    i++;
                    StoredCookie storedCookie2 = (StoredCookie) obj;
                    arrayList2.add(storedCookie2.name + "=" + storedCookie2.value);
                }
                return join(arrayList2, "; ");
            }
            return "";
        } finally {
        }
    }

    public synchronized void replaceFromCookieHeader(URI uri, String str) {
        try {
            this.cookies.clear();
            long currentTimeMillis = System.currentTimeMillis();
            String lowerCase = nz(uri.getHost()).toLowerCase(Locale.US);
            for (String str2 : nz(str).split(";")) {
                String trim = str2.trim();
                int indexOf = trim.indexOf(61);
                if (indexOf > 0) {
                    String trim2 = trim.substring(0, indexOf).trim();
                    String trim3 = trim.substring(indexOf + 1).trim();
                    if (!trim2.isEmpty()) {
                        StoredCookie storedCookie = new StoredCookie(trim2, trim3, lowerCase, "/", SESSION_COOKIE_RETENTION_MS + currentTimeMillis, true, true);
                        this.cookies.put(cookieKey(storedCookie.name, storedCookie.path, storedCookie.domain, storedCookie.hostOnly), storedCookie);
                    }
                }
            }
            purge(currentTimeMillis);
            persist();
        } catch (Throwable th) {
            throw th;
        }
    }
}
