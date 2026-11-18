package ua.telecomplus.app;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.net.ssl.HttpsURLConnection;
import org.json.JSONObject;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class RingotelProvisioningClient {
    static final String HOST = "intertelecom.ringotel.net";
    private static final int MAX_RESPONSE_BYTES = 1048576;
    static final String REGISTRATION_ENDPOINT = "https://intertelecom.ringotel.net:443/";
    private final Context context;

    public RingotelProvisioningClient(Context context) {
        this.context = context.getApplicationContext();
    }

    private JSONObject baseParams() throws Exception {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("termid", terminalId());
        jSONObject.put("devinfo", "Android " + Build.VERSION.RELEASE + " " + Build.MANUFACTURER + " " + Build.MODEL + " (Telecom+ 0.2.24-alpha26.5)");
        jSONObject.put("language", Locale.getDefault().getLanguage());
        jSONObject.put("country", Locale.getDefault().getCountry());
        return jSONObject;
    }

    /**
     * Так регистрируется оригинальный IT Phone: обычный HTTPS POST /public (JSON {"method","params"}),
     * соединение по IP без SNI, Host = intertelecom.ringotel.net:443, Connection: close.
     */
    private JSONObject command(String str, JSONObject jSONObject) throws Exception {
        int saved = SipWebSocket.savedDirectMode();
        int[] order = saved == 0 ? new int[]{0, 1} : new int[]{1, 0};
        Exception last = null;
        for (int mode : order) {
            try {
                TelephonyLog.log("Provisioning: " + str + ", режим " + (mode == 0 ? "IP без SNI" : "имя хоста (SNI)"));
                JSONObject r = postPublic(str, jSONObject, mode == 0);
                SipWebSocket.saveDirectMode(mode == 0 ? 0 : 1);
                return r;
            } catch (IllegalStateException e) {
                SipWebSocket.saveDirectMode(mode == 0 ? 0 : 1);
                throw e;
            } catch (Exception e2) {
                last = e2;
                TelephonyLog.log("Provisioning: режим не подошёл: " + e2.getMessage());
            }
        }
        throw last == null ? new java.io.IOException("нет соединения") : last;
    }

    private JSONObject postPublic(String method, JSONObject params, boolean noSni) throws Exception {
        JSONObject req = new JSONObject();
        req.put("method", method);
        req.put("params", params);
        byte[] body = req.toString().getBytes(StandardCharsets.UTF_8);
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
        ctx.init(null, null, null);
        javax.net.ssl.SSLSocket socket = (javax.net.ssl.SSLSocket) ctx.getSocketFactory().createSocket();
        try {
            if (noSni) {
                String[] o = SipWebSocket.resolveRingotelIp().split("\\.");
                byte[] b = new byte[]{(byte) Integer.parseInt(o[0]), (byte) Integer.parseInt(o[1]), (byte) Integer.parseInt(o[2]), (byte) Integer.parseInt(o[3])};
                socket.connect(new java.net.InetSocketAddress(java.net.InetAddress.getByAddress(b), 443), 15000);
            } else {
                socket.connect(new java.net.InetSocketAddress(HOST, 443), 15000);
            }
            socket.setSoTimeout(25000);
            socket.startHandshake();
            if (!RingotelHostnameVerifier.INSTANCE.verify(HOST, socket.getSession())) {
                throw new java.io.IOException("Сертификат сервера телефонии не подходит для " + HOST);
            }
            String head = "POST /public HTTP/1.1\r\nHost: " + HOST + ":443\r\nConnection: close\r\nContent-Type: application/json\r\nContent-Length: " + body.length + "\r\n\r\n";
            OutputStream out = socket.getOutputStream();
            out.write(head.getBytes(StandardCharsets.UTF_8));
            out.write(body);
            out.write("\r\n".getBytes(StandardCharsets.UTF_8));
            out.flush();
            byte[] all = readLimited(new BufferedInputStream(socket.getInputStream()));
            String text = new String(all, StandardCharsets.UTF_8);
            int split = text.indexOf("\r\n\r\n");
            if (split < 0) {
                throw new java.io.IOException("соединение закрыто без ответа");
            }
            String header = text.substring(0, split);
            String payload = text.substring(split + 4);
            String status = header.split("\r\n", 2)[0];
            int code = 0;
            try {
                code = Integer.parseInt(status.split(" ")[1]);
            } catch (Exception ignored) {
            }
            if (header.toLowerCase(Locale.ROOT).contains("transfer-encoding: chunked")) {
                payload = dechunk(payload);
            }
            payload = payload.trim();
            TelephonyLog.log("Provisioning: " + method + " -> HTTP " + code + ", " + payload.length() + " байт");
            if (code >= 300 && code < 400) {
                throw new java.io.IOException("сервер перенаправил запрос (HTTP " + code + ")");
            }
            if (payload.isEmpty()) {
                throw new IllegalStateException("Сервер телефонии вернул пустой ответ (HTTP " + code + ")");
            }
            JSONObject root = new JSONObject(payload);
            JSONObject err = root.optJSONObject("error");
            JSONObject res = root.optJSONObject("result");
            if (res != null) {
                return res;
            }
            if (err != null) {
                throw new IllegalStateException(serverMessage(err, "Ошибка сервера телефонии"));
            }
            if (code != 200) {
                throw new IllegalStateException(serverMessage(root, "HTTP " + code));
            }
            return root;
        } finally {
            try {
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }

    private static String dechunk(String s) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            int e = s.indexOf("\r\n", i);
            if (e < 0) {
                break;
            }
            int n;
            try {
                n = Integer.parseInt(s.substring(i, e).trim().split(";")[0], 16);
            } catch (Exception ex) {
                break;
            }
            if (n == 0) {
                break;
            }
            i = e + 2;
            sb.append(s, i, Math.min(s.length(), i + n));
            i += n + 2;
        }
        return sb.toString();
    }

    private static String digits(String str) {
        return str == null ? "" : str.replaceAll("\\D+", "");
    }

    public static boolean isEmailIdentity(String str) {
        return str != null && str.trim().contains("@");
    }

    public static String normalizePhone(String str) {
        String digits = digits(str);
        if (digits.length() == 9) {
            digits = "380".concat(digits);
        } else if (digits.length() == 10 && digits.startsWith("0")) {
            digits = "38".concat(digits);
        }
        if (digits.length() == 12 && digits.startsWith("380")) {
            return "+".concat(digits);
        }
        throw new IllegalArgumentException("Введите контактный номер в формате +380XXXXXXXXX");
    }

    public static String normalizeRegistrationIdentity(String str) {
        String trim = str == null ? "" : str.trim();
        if (!isEmailIdentity(trim)) {
            return normalizePhone(trim);
        }
        if (trim.length() > 254 || !trim.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Введите корректный e-mail профиля IT Phone");
        }
        return trim;
    }

    private static byte[] readLimited(BufferedInputStream bufferedInputStream) throws java.io.IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] bArr = new byte[8192];
        int i = 0;
        while (true) {
            int read = bufferedInputStream.read(bArr);
            if (read == -1) {
                return byteArrayOutputStream.toByteArray();
            }
            i += read;
            if (i > MAX_RESPONSE_BYTES) {
                throw new IllegalStateException("Ответ сервера телефонии превышает допустимый размер");
            }
            byteArrayOutputStream.write(bArr, 0, read);
        }
    }

    private static String registrationKey(String str) {
        return isEmailIdentity(str) ? "email" : "phone";
    }

    private static String serverMessage(JSONObject jSONObject, String str) {
        String trim = jSONObject.optString("message", "").trim();
        if ("Phone number does not exist".equalsIgnoreCase(trim)) {
            return "Номер не найден на сервере телефонии Интертелекома";
        }
        if (!trim.isEmpty()) {
            return trim;
        }
        int optInt = jSONObject.optInt("rcode", jSONObject.optInt("code", -1));
        if (optInt < 0) {
            return str;
        }
        return str + " (код " + optInt + ")";
    }

    String terminalId() {
        String string = Settings.Secure.getString(this.context.getContentResolver(), "android_id");
        return (string == null || string.trim().isEmpty()) ? "telecom-plus" : string.trim();
    }

    public JSONObject complete(String str, String str2) throws Exception {
        String normalizeRegistrationIdentity = normalizeRegistrationIdentity(str);
        String digits = digits(str2);
        if (digits.length() > 4) {
            digits = digits.substring(digits.length() - 4);
        }
        if (digits.length() != 4) {
            throw new IllegalArgumentException("Введите четырёхзначный код IT Phone");
        }
        JSONObject jSONObject = new JSONObject();
        jSONObject.put(registrationKey(normalizeRegistrationIdentity), normalizeRegistrationIdentity);
        jSONObject.put("code", digits);
        jSONObject.put("country", Locale.getDefault().getCountry());
        return command("registrationComplete", jSONObject);
    }

    public JSONObject requestCode(String str) throws Exception {
        String normalizeRegistrationIdentity = normalizeRegistrationIdentity(str);
        JSONObject baseParams = baseParams();
        baseParams.put(registrationKey(normalizeRegistrationIdentity), normalizeRegistrationIdentity);
        return command("registrationRequest", baseParams);
    }

    public JSONObject requestOAuth(String str) throws Exception {
        JSONObject baseParams = baseParams();
        baseParams.put("service", str);
        return command("registrationRequest", baseParams);
    }

    public JSONObject completeOAuth() throws Exception {
        JSONObject jSONObject = new JSONObject();
        jSONObject.put("oauth", "true");
        jSONObject.put("termid", terminalId());
        jSONObject.put("country", Locale.getDefault().getCountry());
        return command("registrationComplete", jSONObject);
    }
}
