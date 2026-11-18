package ua.telecomplus.app;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Минимальная модель SIP-сообщения (запрос/ответ) с разбором и сериализацией. */
final class SipMessage {
    boolean isRequest;
    String method = "";
    String uri = "";
    int status;
    String reason = "";
    /** Ключ — имя заголовка в нижнем регистре; значения в порядке получения. */
    final LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();
    final Map<String, String> names = new LinkedHashMap<>();
    String body = "";

    static SipMessage request(String str, String str2) {
        SipMessage m = new SipMessage();
        m.isRequest = true;
        m.method = str;
        m.uri = str2;
        return m;
    }

    static SipMessage response(int i, String str) {
        SipMessage m = new SipMessage();
        m.isRequest = false;
        m.status = i;
        m.reason = str;
        return m;
    }

    static SipMessage parse(String str) {
        int split = str.indexOf("\r\n\r\n");
        String head = split >= 0 ? str.substring(0, split) : str;
        String body = split >= 0 ? str.substring(split + 4) : "";
        String[] lines = head.split("\r\n");
        if (lines.length == 0 || lines[0].isEmpty()) {
            return null;
        }
        SipMessage m = new SipMessage();
        String first = lines[0];
        if (first.startsWith("SIP/2.0")) {
            String[] parts = first.split(" ", 3);
            if (parts.length < 2) {
                return null;
            }
            try {
                m.status = Integer.parseInt(parts[1].trim());
            } catch (NumberFormatException e) {
                return null;
            }
            m.reason = parts.length > 2 ? parts[2] : "";
        } else {
            String[] parts = first.split(" ");
            if (parts.length < 3) {
                return null;
            }
            m.isRequest = true;
            m.method = parts[0].toUpperCase(Locale.ROOT);
            m.uri = parts[1];
        }
        String lastKey = null;
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isEmpty()) {
                continue;
            }
            if ((line.charAt(0) == ' ' || line.charAt(0) == '\t') && lastKey != null) {
                List<String> list = m.headers.get(lastKey);
                list.set(list.size() - 1, list.get(list.size() - 1) + " " + line.trim());
                continue;
            }
            int colon = line.indexOf(':');
            if (colon <= 0) {
                continue;
            }
            String name = canonical(line.substring(0, colon).trim());
            String value = line.substring(colon + 1).trim();
            m.add(name, value);
            lastKey = name.toLowerCase(Locale.ROOT);
        }
        m.body = body;
        return m;
    }

    private static String canonical(String str) {
        switch (str.toLowerCase(Locale.ROOT)) {
            case "v":
                return "Via";
            case "f":
                return "From";
            case "t":
                return "To";
            case "i":
                return "Call-ID";
            case "m":
                return "Contact";
            case "l":
                return "Content-Length";
            case "c":
                return "Content-Type";
            default:
                return str;
        }
    }

    SipMessage add(String str, String str2) {
        String key = str.toLowerCase(Locale.ROOT);
        List<String> list = this.headers.get(key);
        if (list == null) {
            list = new ArrayList<>();
            this.headers.put(key, list);
            this.names.put(key, str);
        }
        list.add(str2);
        return this;
    }

    SipMessage set(String str, String str2) {
        this.headers.remove(str.toLowerCase(Locale.ROOT));
        return add(str, str2);
    }

    String get(String str) {
        List<String> list = this.headers.get(str.toLowerCase(Locale.ROOT));
        return (list == null || list.isEmpty()) ? null : list.get(0);
    }

    List<String> getAll(String str) {
        List<String> list = this.headers.get(str.toLowerCase(Locale.ROOT));
        return list == null ? new ArrayList<String>() : list;
    }

    String callId() {
        String v = get("Call-ID");
        return v == null ? "" : v;
    }

    String cseqMethod() {
        String v = get("CSeq");
        if (v == null) {
            return "";
        }
        String[] parts = v.trim().split("\\s+");
        return parts.length > 1 ? parts[1].toUpperCase(Locale.ROOT) : "";
    }

    int cseqNumber() {
        String v = get("CSeq");
        if (v == null) {
            return 0;
        }
        try {
            return Integer.parseInt(v.trim().split("\\s+")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    /** Значение параметра заголовка, например tag из From/To. */
    static String param(String str, String str2) {
        if (str == null) {
            return null;
        }
        int semi = str.indexOf(';');
        // для адресов в <...> параметры могут быть и внутри скобок, и после
        String rest = str.substring(str.indexOf('>') >= 0 ? str.indexOf('>') : 0);
        for (String part : rest.split(";")) {
            int eq = part.indexOf('=');
            if (eq > 0 && part.substring(0, eq).trim().equalsIgnoreCase(str2)) {
                return part.substring(eq + 1).trim().replace("\"", "");
            }
        }
        if (semi < 0) {
            return null;
        }
        return null;
    }

    /** Извлекает URI из значения адресного заголовка: «Имя» <sip:user@host> -> sip:user@host. */
    static String uriOf(String str) {
        if (str == null) {
            return "";
        }
        int lt = str.indexOf('<');
        int gt = str.indexOf('>');
        if (lt >= 0 && gt > lt) {
            return str.substring(lt + 1, gt).trim();
        }
        int semi = str.indexOf(';');
        return (semi >= 0 ? str.substring(0, semi) : str).trim();
    }

    /** Пользовательская часть sip:user@host. */
    static String userOf(String str) {
        String u = uriOf(str);
        int colon = u.indexOf(':');
        if (colon >= 0) {
            u = u.substring(colon + 1);
        }
        int at = u.indexOf('@');
        return at >= 0 ? u.substring(0, at) : "";
    }

    static String displayNameOf(String str) {
        if (str == null) {
            return "";
        }
        int lt = str.indexOf('<');
        if (lt <= 0) {
            return "";
        }
        return str.substring(0, lt).trim().replace("\"", "");
    }

    byte[] toBytes() {
        return toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return serialize(false);
    }

    /** Версия для журнала: учётные данные скрыты. */
    String redacted() {
        return serialize(true);
    }

    private String serialize(boolean redact) {
        StringBuilder sb = new StringBuilder();
        if (this.isRequest) {
            sb.append(this.method).append(' ').append(this.uri).append(" SIP/2.0\r\n");
        } else {
            sb.append("SIP/2.0 ").append(this.status).append(' ').append(this.reason).append("\r\n");
        }
        byte[] bodyBytes = this.body.getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<String, List<String>> entry : this.headers.entrySet()) {
            String key = entry.getKey();
            if (key.equals("content-length")) {
                continue;
            }
            String name = this.names.get(key);
            for (String value : entry.getValue()) {
                boolean secret = key.equals("authorization") || key.equals("proxy-authorization");
                sb.append(name).append(": ").append(redact && secret ? "<скрыто>" : value).append("\r\n");
            }
        }
        sb.append("Content-Length: ").append(bodyBytes.length).append("\r\n\r\n");
        sb.append(this.body);
        return sb.toString();
    }
}
