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

    private JSONObject command(String str, JSONObject jSONObject) throws Exception {
        String wsError;
        try {
            return commandWs(str, jSONObject);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (IllegalStateException e2) {
            throw e2;
        } catch (Exception e3) {
            wsError = String.valueOf(e3.getMessage());
            TelephonyLog.log("Provisioning WS: " + str + " не удался: " + wsError);
        }
        try {
            return commandHttp(str, jSONObject);
        } catch (IllegalStateException e4) {
            throw new IllegalStateException(e4.getMessage() + " (WebSocket: " + wsError + ")");
        }
    }

    private static boolean trustedRedirect(java.net.URI uri) {
        String h = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.ROOT);
        return "https".equalsIgnoreCase(uri.getScheme()) && (h.equals("ringotel.net") || h.endsWith(".ringotel.net") || h.equals("ringotel.co") || h.endsWith(".ringotel.co") || h.endsWith(".ringotel.com") || h.endsWith(".ringotel.io"));
    }

    private JSONObject commandWs(String str, JSONObject jSONObject) throws Exception {
        return commandWsAt(str, jSONObject, HOST, 443, "/", 0);
    }

    private JSONObject commandWsAt(String str, JSONObject jSONObject, String host, int port, String path, int depth) throws Exception {
        final java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        final String[] resp = {null};
        SipWebSocket ws = new SipWebSocket(host, port, "json.api.smile-soft.com", "Ringotel", new SipWebSocket.Listener() {
            @Override
            public void onClosed(String reason) {
                latch.countDown();
            }

            @Override
            public void onText(String text) {
                if (resp[0] == null) {
                    resp[0] = text;
                }
                latch.countDown();
            }
        });
        try {
            ws.setPath(path);
            TelephonyLog.log("Provisioning WS: подключение " + host + ":" + port + path + ", метод " + str);
            try {
                ws.connect();
            } catch (SipWebSocket.RedirectException re) {
                TelephonyLog.log("Provisioning WS: редирект на " + re.location);
                if (depth >= 4 || re.location.isEmpty()) {
                    throw re;
                }
                java.net.URI target = new java.net.URI("https://" + host + ":" + port + path).resolve(re.location);
                if (!trustedRedirect(target)) {
                    throw new java.io.IOException("редирект на неизвестный хост " + target.getHost());
                }
                String np = target.getRawPath() == null || target.getRawPath().isEmpty() ? "/" : target.getRawPath();
                if (target.getRawQuery() != null) {
                    np = np + "?" + target.getRawQuery();
                }
                return commandWsAt(str, jSONObject, target.getHost(), target.getPort() > 0 ? target.getPort() : 443, np, depth + 1);
            }
            JSONObject msg = new JSONObject();
            msg.put("method", str);
            msg.put("params", jSONObject);
            msg.put("id", 1);
            ws.sendText(msg.toString());
            if (!latch.await(25, java.util.concurrent.TimeUnit.SECONDS)) {
                throw new java.io.IOException("сервер не ответил за 25 секунд");
            }
            if (resp[0] == null || resp[0].trim().isEmpty()) {
                throw new java.io.IOException("соединение закрыто без ответа");
            }
            JSONObject root = new JSONObject(resp[0].trim());
            TelephonyLog.log("Provisioning WS: ответ на " + str + ", поля " + root.keys().hasNext() + " " + (root.has("error") ? "error" : "result"));
            JSONObject err = root.optJSONObject("error");
            if (err != null) {
                throw new IllegalStateException(serverMessage(err, "Ошибка сервера телефонии"));
            }
            JSONObject res = root.optJSONObject("result");
            return res != null ? res : root;
        } finally {
            ws.close();
        }
    }

    private JSONObject commandHttp(String str, JSONObject jSONObject) throws Exception {
        return commandHttpAt(str, jSONObject, REGISTRATION_ENDPOINT, 0);
    }

    private JSONObject commandHttpAt(String str, JSONObject jSONObject, String endpoint, int depth) throws Exception {
        byte[] bArr;
        JSONObject jSONObject2 = new JSONObject();
        jSONObject2.put("method", str);
        jSONObject2.put("params", jSONObject);
        String jSONObject3 = jSONObject2.toString();
        Charset charset = StandardCharsets.UTF_8;
        byte[] bytes = jSONObject3.getBytes(charset);
        HttpsURLConnection httpsURLConnection = (HttpsURLConnection) new URL(endpoint).openConnection();
        httpsURLConnection.setHostnameVerifier(RingotelHostnameVerifier.INSTANCE);
        httpsURLConnection.setConnectTimeout(15000);
        httpsURLConnection.setReadTimeout(25000);
        httpsURLConnection.setUseCaches(false);
        httpsURLConnection.setInstanceFollowRedirects(false);
        httpsURLConnection.setRequestMethod("POST");
        httpsURLConnection.setRequestProperty("Accept", "application/json");
        httpsURLConnection.setRequestProperty("Content-Type", "application/json");
        httpsURLConnection.setRequestProperty("User-Agent", "TelecomPlus/0.2.24-alpha26.5");
        httpsURLConnection.setFixedLengthStreamingMode(bytes.length);
        httpsURLConnection.setDoOutput(true);
        try {
            OutputStream outputStream = httpsURLConnection.getOutputStream();
            try {
                outputStream.write(bytes);
                outputStream.close();
                int responseCode = httpsURLConnection.getResponseCode();
                if (responseCode >= 301 && responseCode <= 308 && responseCode != 304) {
                    String loc = httpsURLConnection.getHeaderField("Location");
                    TelephonyLog.log("Provisioning HTTP: редирект " + responseCode + " на " + loc);
                    if (loc != null && depth < 4) {
                        java.net.URI target = new java.net.URI(endpoint).resolve(loc);
                        if (trustedRedirect(target)) {
                            httpsURLConnection.disconnect();
                            return commandHttpAt(str, jSONObject, target.toString(), depth + 1);
                        }
                    }
                }
                InputStream errorStream = (responseCode < 200 || responseCode >= 400) ? httpsURLConnection.getErrorStream() : httpsURLConnection.getInputStream();
                if (errorStream == null) {
                    bArr = new byte[0];
                } else {
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(errorStream);
                    try {
                        byte[] readLimited = readLimited(bufferedInputStream);
                        bufferedInputStream.close();
                        bArr = readLimited;
                    } finally {
                    }
                }
                String trim = new String(bArr, charset).trim();
                TelephonyLog.log("Provisioning HTTP: " + str + " -> HTTP " + responseCode + ", " + bArr.length + " байт");
                if (trim.isEmpty()) {
                    throw new IllegalStateException("Сервер телефонии вернул пустой ответ (HTTP " + responseCode + ")");
                }
                JSONObject jSONObject4 = new JSONObject(trim);
                if (responseCode < 200 || responseCode >= 300) {
                    throw new IllegalStateException(serverMessage(jSONObject4, "HTTP " + responseCode));
                }
                JSONObject optJSONObject = jSONObject4.optJSONObject("error");
                if (optJSONObject != null) {
                    throw new IllegalStateException(serverMessage(optJSONObject, "Ошибка сервера телефонии"));
                }
                JSONObject optJSONObject2 = jSONObject4.optJSONObject("result");
                if (optJSONObject2 != null) {
                    jSONObject4 = optJSONObject2;
                }
                httpsURLConnection.disconnect();
                return jSONObject4;
            } finally {
            }
        } catch (Throwable th) {
            httpsURLConnection.disconnect();
            throw th;
        }
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
        jSONObject.put("termid", terminalId());
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
        jSONObject.put("oauth", true);
        jSONObject.put("termid", terminalId());
        jSONObject.put("country", Locale.getDefault().getCountry());
        return command("registrationComplete", jSONObject);
    }
}
