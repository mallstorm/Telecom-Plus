package ua.telecomplus.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import ua.telecomplus.app.PortalResult;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class IntertelecomPortalClient implements SubscriberPortalGateway {
    private static final String KEY_CALLS_CACHE = "portal_calls_cache_v1";
    private static final String KEY_DASHBOARD_CACHE = "portal_dashboard_cache_v1";
    private static final String KEY_FUNDS_CACHE = "portal_funds_cache_v1";
    private static final String KEY_PHONE = "portal_phone_v3";
    private static final String KEY_SERVICES_CACHE = "portal_services_cache_v1";
    private volatile AuthForm authForm;
    private final PersistentCookieStore cookieStore;
    private volatile String currentPhone;
    private final SecureVault vault;

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class MatcherLike {
        final boolean found;
        final String value;

        public MatcherLike(boolean z, String str) {
            this.found = z;
            this.value = str;
        }
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class Response {
        final String body;
        final int code;
        final Exception error;
        final String finalUrl;
        final String location;

        public Response(int i, String str, String str2, String str3, Exception exc) {
            this.code = i;
            this.body = IntertelecomPortalClient.nz(str);
            this.finalUrl = IntertelecomPortalClient.nz(str2);
            this.location = str3;
            this.error = exc;
        }
    }

    public IntertelecomPortalClient(SecureVault secureVault) {
        this.vault = secureVault;
        this.cookieStore = new PersistentCookieStore(secureVault);
        String str = secureVault.get(KEY_PHONE);
        this.currentPhone = str == null ? "" : str;
    }

    private static String absolutePortalUrl(String str) {
        String trim = nz(str).trim();
        if (trim.isEmpty()) {
            return null;
        }
        if (trim.startsWith("https://")) {
            return trim;
        }
        if (!trim.startsWith("/")) {
            trim = "/".concat(trim);
        }
        return "https://services.intertelecom.ua".concat(trim);
    }

    private static void addSubmitControl(Map<String, String> map, AuthForm authForm) {
        if (authForm == null || authForm.submitName.trim().isEmpty()) {
            return;
        }
        map.put(authForm.submitName, authForm.submitValue);
    }

    private static void addUnique(List<String> list, String str) {
        String trim = str == null ? "" : str.trim();
        if (trim.isEmpty() || list.contains(trim)) {
            return;
        }
        list.add(trim);
    }

    private static String digits(String str) {
        return nz(str).replaceAll("\\D+", "");
    }

    private String encodeFields(Map<String, String> map) throws java.io.UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getKey() != null && !entry.getKey().trim().isEmpty()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                sb.append('=');
                sb.append(URLEncoder.encode(nz(entry.getValue()), "UTF-8"));
            }
        }
        return sb.toString();
    }

    <T> PortalResult<T> failure(Response response) {
        String str;
        Exception exc = response.error;
        if (exc != null) {
            String name = exc.getClass().getName();
            String safeMessage = safeMessage(response.error);
            return (name.contains("UnknownHost") || safeMessage.contains("Unable to resolve host") || safeMessage.contains("No address associated with hostname")) ? PortalResult.failure("Портал не найден через текущий DNS. Проверьте, что Telecom+ добавлен в список приложений австрийского VPN, затем повторите вход.") : PortalResult.failure("Сеть недоступна: ".concat(safeMessage));
        }
        int i = response.code;
        boolean z = i == 401 || i == 403 || PortalHtmlParser.looksUnauthorized(response.finalUrl, response.body);
        String extractError = PortalHtmlParser.extractError(response.body);
        if (extractError == null) {
            if (response.code < 0) {
                str = "Ошибка сети";
            } else {
                str = "Портал вернул HTTP " + response.code;
            }
            extractError = str;
        }
        return PortalResult.failure(extractError, z);
    }

    private static MatcherLike findCharset(String str) {
        if (str == null) {
            return new MatcherLike(false, "");
        }
        Matcher matcher = Pattern.compile("charset=([^; ]+)", 2).matcher(str);
        return matcher.find() ? new MatcherLike(true, matcher.group(1).trim().replace("\"", "")) : new MatcherLike(false, "");
    }

    private static List<String> headerValues(Map<String, List<String>> map, String str) {
        if (map == null) {
            return Collections.EMPTY_LIST;
        }
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(str)) {
                return entry.getValue() == null ? Collections.EMPTY_LIST : entry.getValue();
            }
        }
        return Collections.EMPTY_LIST;
    }

    private static boolean isRedirect(int i) {
        return i == 301 || i == 302 || i == 303 || i == 307 || i == 308;
    }

    private Response multipart(String str, Map<String, String> map, String str2, boolean z) {
        String boundary = "----TelecomPlus" + UUID.randomUUID().toString().replace("-", "");
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Charset charset = StandardCharsets.UTF_8;
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey() != null && !entry.getKey().trim().isEmpty()) {
                    out.write(("--" + boundary + "\r\n").getBytes(charset));
                    out.write(("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n").getBytes(charset));
                    out.write(nz(entry.getValue()).getBytes(charset));
                    out.write("\r\n".getBytes(charset));
                }
            }
            out.write(("--" + boundary + "--\r\n").getBytes(charset));
            return request("POST", str, out.toByteArray(), "multipart/form-data; boundary=" + boundary, z, str2);
        } catch (Exception e) {
            return new Response(-1, "", "https://services.intertelecom.ua" + str, null, e);
        }
    }

    static String normalizeNumber(String str) {
        String digits = digits(str);
        if (digits.startsWith("380")) {
            digits = digits.substring(3);
        }
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        if (digits.length() == 9) {
            return digits;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String nz(String str) {
        return str == null ? "" : str;
    }

    private static String prefer(String str, String str2) {
        String trim = nz(str).trim();
        return (trim.isEmpty() || "—".equals(trim)) ? nz(str2) : trim;
    }

    private static SubscriberProfile preferProfile(SubscriberProfile subscriberProfile, SubscriberProfile subscriberProfile2) {
        if (subscriberProfile == null) {
            subscriberProfile = new SubscriberProfile();
        }
        if (subscriberProfile2 == null) {
            subscriberProfile2 = new SubscriberProfile();
        }
        return new SubscriberProfile(prefer(subscriberProfile.accountId, subscriberProfile2.accountId), prefer(subscriberProfile.contractStatus, subscriberProfile2.contractStatus), prefer(subscriberProfile.phoneNumber, subscriberProfile2.phoneNumber), prefer(subscriberProfile.registrationStatus, subscriberProfile2.registrationStatus), prefer(subscriberProfile.tariffName, subscriberProfile2.tariffName), prefer(subscriberProfile.itPhoneId, subscriberProfile2.itPhoneId), prefer(subscriberProfile.contactPhone, subscriberProfile2.contactPhone), prefer(subscriberProfile.email, subscriberProfile2.email), prefer(subscriberProfile.connectionDate, subscriberProfile2.connectionDate), prefer(subscriberProfile.validUntil, subscriberProfile2.validUntil), prefer(subscriberProfile.subscriberTenure, subscriberProfile2.subscriberTenure), prefer(subscriberProfile.loyaltyTenure, subscriberProfile2.loyaltyTenure));
    }

    private static void putIfBlank(Map<String, String> map, String str, String str2) {
        String str3 = map.get(str);
        if (str3 == null || str3.trim().isEmpty()) {
            map.put(str, nz(str2));
        }
    }

    private static void putNonBlank(Map<String, String> map, String str, String str2) {
        if (str == null || str.trim().isEmpty()) {
            return;
        }
        map.put(str, nz(str2));
    }

    private Response query(String str, Map<String, String> map, String str2, boolean z) {
        String str3;
        try {
            String encodeFields = encodeFields(map);
            if (encodeFields.isEmpty()) {
                str3 = str;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append(str);
                sb.append(str.contains("?") ? "&" : "?");
                sb.append(encodeFields);
                str3 = sb.toString();
            }
            return request("GET", str3, null, null, z, str2);
        } catch (Exception e) {
            return new Response(-1, "", "https://services.intertelecom.ua" + str, null, e);
        }
    }

    private static byte[] readAll(InputStream inputStream) throws IOException {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try {
                byte[] bArr = new byte[16384];
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read < 0) {
                        byte[] byteArray = byteArrayOutputStream.toByteArray();
                        byteArrayOutputStream.close();
                        inputStream.close();
                        return byteArray;
                    }
                    if (read > 0) {
                        byteArrayOutputStream.write(bArr, 0, read);
                    }
                }
            } finally {
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Throwable th2) {
                    th.addSuppressed(th2);
                }
            }
            throw th;
        }
    }

    private Response request(String str, String str2, byte[] bArr, String str3, boolean z, String str4) {
        String method = str;
        String path = str2;
        byte[] body = bArr;
        String contentType = str3;
        String referer = str4;
        for (int i = 0; i <= 6; i++) {
            Response response = requestOnce(method, path, body, contentType, referer);
            if (response.error != null || !z || !isRedirect(response.code) || nz(response.location).trim().isEmpty()) {
                return response;
            }
            try {
                URI resolve = new URI("https://services.intertelecom.ua" + path).resolve(response.location);
                if (!"https".equalsIgnoreCase(resolve.getScheme()) || !"services.intertelecom.ua".equalsIgnoreCase(resolve.getHost())) {
                    return new Response(-1, "", response.finalUrl, response.location, new SecurityException("Портал попытался перенаправить на внешний адрес"));
                }
                String rawPath = nz(resolve.getRawPath());
                if (rawPath.isEmpty()) {
                    rawPath = "/";
                }
                path = resolve.getRawQuery() != null ? rawPath + "?" + resolve.getRawQuery() : rawPath;
                referer = response.finalUrl;
                int code = response.code;
                if (code == 303 || ((code == 301 || code == 302) && !"GET".equals(method))) {
                    body = null;
                    contentType = null;
                    method = "GET";
                }
            } catch (Exception e) {
                return new Response(-1, "", response.finalUrl, response.location, e);
            }
        }
        return new Response(-1, "", "https://services.intertelecom.ua" + nz(path), null, new IOException("Слишком много перенаправлений портала"));
    }

    private Response requestOnce(String str, String str2, byte[] bArr, String str3, String str4) {
        URL url = null;
        HttpsURLConnection conn = null;
        try {
            if (str2 == null || !str2.startsWith("/")) {
                throw new SecurityException("Неверный путь портала");
            }
            url = new URL("https://services.intertelecom.ua".concat(str2));
            if (!"https".equals(url.getProtocol()) || !"services.intertelecom.ua".equals(url.getHost())) {
                throw new SecurityException("Заблокирован внешний адрес");
            }
            conn = (HttpsURLConnection) url.openConnection();
            conn.setRequestMethod(str);
            conn.setInstanceFollowRedirects(false);
            conn.setConnectTimeout(20000);
            conn.setReadTimeout(25000);
            conn.setUseCaches(false);
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,uk;q=0.8");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Mobile Safari/537.36");
            conn.setRequestProperty("Cache-Control", "no-cache");
            conn.setRequestProperty("Pragma", "no-cache");
            conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
            conn.setRequestProperty("Sec-Fetch-Dest", "document");
            conn.setRequestProperty("Sec-Fetch-Mode", "navigate");
            conn.setRequestProperty("Sec-Fetch-Site", str4 == null ? "none" : "same-origin");
            conn.setRequestProperty("Sec-Fetch-User", "?1");
            if (str4 != null && !str4.trim().isEmpty()) {
                conn.setRequestProperty("Referer", str4);
            } else if ("POST".equals(str)) {
                conn.setRequestProperty("Referer", "https://services.intertelecom.ua/login/number?lang=ru");
            }
            if ("POST".equals(str)) {
                conn.setRequestProperty("Origin", "https://services.intertelecom.ua");
            }
            String cookieHeader = this.cookieStore.header(url.toURI());
            if (!cookieHeader.isEmpty()) {
                conn.setRequestProperty("Cookie", cookieHeader);
            }
            if (bArr != null) {
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", str3 == null ? "application/octet-stream" : str3);
                conn.setFixedLengthStreamingMode(bArr.length);
                OutputStream outputStream = conn.getOutputStream();
                try {
                    outputStream.write(bArr);
                } finally {
                    outputStream.close();
                }
            }
            int responseCode = conn.getResponseCode();
            this.cookieStore.capture(url.toURI(), headerValues(conn.getHeaderFields(), "Set-Cookie"));
            InputStream stream = responseCode >= 400 ? conn.getErrorStream() : conn.getInputStream();
            byte[] data = stream == null ? new byte[0] : readAll(stream);
            MatcherLike charsetInfo = findCharset(conn.getContentType());
            String text;
            try {
                text = new String(data, Charset.forName(charsetInfo.found ? charsetInfo.value : "UTF-8"));
            } catch (Exception unused) {
                text = new String(data, StandardCharsets.UTF_8);
            }
            return new Response(responseCode, text, url.toString(), conn.getHeaderField("Location"), null);
        } catch (Exception e) {
            String finalUrl = url == null ? "https://services.intertelecom.ua" + nz(str2) : url.toString();
            return new Response(-1, "", finalUrl, null, e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private static String safeMessage(Throwable th) {
        String message = th.getMessage();
        return (message == null || message.trim().isEmpty()) ? th.getClass().getSimpleName() : message;
    }

    private static String safePath(String str) {
        String trim = nz(str).trim();
        if (trim.isEmpty()) {
            return "/";
        }
        try {
            URI uri = new URI(trim);
            if (uri.isAbsolute()) {
                String nz = nz(uri.getRawPath());
                return nz.isEmpty() ? "/" : nz;
            }
        } catch (Exception unused) {
        }
        int indexOf = trim.indexOf(63);
        return indexOf >= 0 ? trim.substring(0, indexOf) : trim;
    }

    private Response submitForm(AuthForm authForm, Map<String, String> map, boolean z) {
        String targetPath = authForm == null ? "" : authForm.targetPath();
        if (targetPath.trim().isEmpty()) {
            return new Response(-1, "", "https://services.intertelecom.ua", null, new IOException("Форма портала не содержит адрес отправки"));
        }
        String absolutePortalUrl = absolutePortalUrl(authForm.sourcePath);
        return "GET".equals(authForm.method) ? query(targetPath, map, absolutePortalUrl, z) : nz(authForm.enctype).toLowerCase(Locale.ROOT).contains("multipart/form-data") ? multipart(targetPath, map, absolutePortalUrl, z) : urlEncoded(targetPath, map, absolutePortalUrl, z);
    }

    private Response urlEncoded(String str, Map<String, String> map, String str2, boolean z) {
        try {
            return request("POST", str, encodeFields(map).getBytes(StandardCharsets.UTF_8), "application/x-www-form-urlencoded", z, str2);
        } catch (Exception e) {
            return new Response(-1, "", "https://services.intertelecom.ua" + str, null, e);
        }
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<CallHistorySnapshot> acceptCallHistoryHtml(String str) {
        try {
            CallHistorySnapshot parseCallHistory = PortalHtmlParser.parseCallHistory(str);
            CallHistorySnapshot cachedCallHistory = getCachedCallHistory();
            boolean z = false;
            boolean z2 = (parseCallHistory == null || parseCallHistory.calls.isEmpty()) ? false : true;
            if (parseCallHistory != null && !"—".equals(parseCallHistory.totalDuration)) {
                z = true;
            }
            if (!z2 && !z && cachedCallHistory != null && !cachedCallHistory.calls.isEmpty()) {
                return PortalResult.success(cachedCallHistory);
            }
            this.vault.put(KEY_CALLS_CACHE, PortalCacheCodec.encodeCallHistory(parseCallHistory));
            return PortalResult.success(parseCallHistory);
        } catch (Exception e) {
            return PortalResult.failure("Ошибка разбора детализации звонков: " + safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<FundsFlowSnapshot> acceptFundsFlowHtml(String str) {
        try {
            FundsFlowSnapshot parseFundsFlow = PortalHtmlParser.parseFundsFlow(str);
            FundsFlowSnapshot cachedFundsFlow = getCachedFundsFlow();
            boolean z = false;
            boolean z2 = (parseFundsFlow == null || parseFundsFlow.transactions.isEmpty()) ? false : true;
            if (parseFundsFlow != null && (!"—".equals(parseFundsFlow.openingBalance) || !"—".equals(parseFundsFlow.creditsTotal) || !"—".equals(parseFundsFlow.debitsTotal) || !"—".equals(parseFundsFlow.closingBalance))) {
                z = true;
            }
            if (!z2 && !z && cachedFundsFlow != null && !cachedFundsFlow.transactions.isEmpty()) {
                return PortalResult.success(cachedFundsFlow);
            }
            this.vault.put(KEY_FUNDS_CACHE, PortalCacheCodec.encodeFundsFlow(parseFundsFlow));
            return PortalResult.success(parseFundsFlow);
        } catch (Exception e) {
            return PortalResult.failure("Ошибка разбора движения средств: " + safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> confirmOtp(String str, String str2) {
        String normalizeNumber = normalizeNumber(str);
        if (normalizeNumber == null) {
            return PortalResult.failure("Неверный формат номера");
        }
        String digits = digits(str2);
        if (digits.length() != 4) {
            return PortalResult.failure("Введите последние четыре цифры входящего номера");
        }
        AuthForm authForm = this.authForm;
        if (authForm == null) {
            return PortalResult.failure("Сначала запросите одноразовый пароль");
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap(authForm.fields);
        putIfBlank(linkedHashMap, "action", "one_time_password_check");
        if (!linkedHashMap.containsKey("lang")) {
            linkedHashMap.put("lang", "ru");
        }
        putNonBlank(linkedHashMap, authForm.subscriberName, normalizeNumber);
        putNonBlank(linkedHashMap, authForm.otpName, digits);
        addSubmitControl(linkedHashMap, authForm);
        Response submitForm = submitForm(authForm, linkedHashMap, false);
        int i = submitForm.code;
        if ((i == 301 || i == 302 || i == 303) && nz(submitForm.location).contains("/pages/overview")) {
            this.authForm = null;
            this.vault.put(KEY_PHONE, normalizeNumber);
            return PortalResult.success(null);
        }
        int i2 = submitForm.code;
        if (i2 >= 200 && i2 <= 299) {
            String extractError = PortalHtmlParser.extractError(submitForm.body);
            if (extractError != null) {
                return PortalResult.failure(extractError);
            }
            if (!PortalHtmlParser.looksUnauthorized(submitForm.finalUrl, submitForm.body)) {
                return PortalResult.success(null);
            }
        }
        return failure(submitForm);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public CallHistorySnapshot getCachedCallHistory() {
        return PortalCacheCodec.decodeCallHistory(this.vault.get(KEY_CALLS_CACHE));
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public DashboardSnapshot getCachedDashboard() {
        return PortalCacheCodec.decodeDashboard(this.vault.get(KEY_DASHBOARD_CACHE));
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public FundsFlowSnapshot getCachedFundsFlow() {
        return PortalCacheCodec.decodeFundsFlow(this.vault.get(KEY_FUNDS_CACHE));
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public List<ServiceItem> getCachedServices() {
        return PortalCacheCodec.decodeServices(this.vault.get(KEY_SERVICES_CACHE));
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<CallHistorySnapshot> getCallHistory() {
        Response request = request("GET", "/pages/call_details/?lang=ru", null, null, true);
        int i = request.code;
        return (i < 200 || i > 299) ? failure(request) : PortalHtmlParser.looksUnauthorized(request.finalUrl, request.body) ? PortalResult.failure("Требуется повторный вход", true) : acceptCallHistoryHtml(request.body);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<DashboardSnapshot> getDashboard() {
        Response request = request("GET", "/pages/overview/?lang=ru", null, null, true);
        int i = request.code;
        if (i < 200 || i > 299) {
            return failure(request);
        }
        if (PortalHtmlParser.looksUnauthorized(request.finalUrl, request.body)) {
            return PortalResult.failure("Требуется повторный вход", true);
        }
        try {
            DashboardSnapshot parseDashboard = PortalHtmlParser.parseDashboard(request.body);
            SubscriberProfile subscriberProfile = parseDashboard.profile;
            Response request2 = request("GET", "/pages/subscriber_data/?lang=ru", null, null, true);
            int i2 = request2.code;
            if (i2 >= 200 && i2 <= 299 && !PortalHtmlParser.looksUnauthorized(request2.finalUrl, request2.body)) {
                subscriberProfile = preferProfile(PortalHtmlParser.parseSubscriberProfile(request2.body, parseDashboard.phoneNumber, parseDashboard.tariffName), subscriberProfile);
            }
            DashboardSnapshot dashboardSnapshot = new DashboardSnapshot(parseDashboard.phoneNumber, parseDashboard.status, parseDashboard.balance, parseDashboard.transferableBalance, parseDashboard.tariffName, parseDashboard.packages, subscriberProfile, parseDashboard.outgoingSummary, System.currentTimeMillis());
            this.vault.put(KEY_DASHBOARD_CACHE, PortalCacheCodec.encodeDashboard(dashboardSnapshot));
            return PortalResult.success(dashboardSnapshot);
        } catch (Exception e) {
            return PortalResult.failure("Ошибка разбора главной страницы: " + safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<FundsFlowSnapshot> getFundsFlow() {
        ArrayList arrayList = new ArrayList();
        Response request = request("GET", "/pages/overview/?lang=ru", null, null, true);
        int i = request.code;
        if (i >= 200 && i <= 299 && !PortalHtmlParser.looksUnauthorized(request.finalUrl, request.body)) {
            addUnique(arrayList, PortalHtmlParser.findFundsFlowPath(request.body));
        }
        addUnique(arrayList, "/pages/funds_flow/?lang=ru");
        addUnique(arrayList, "/pages/money_flow/?lang=ru");
        addUnique(arrayList, "/pages/account_history/?lang=ru");
        addUnique(arrayList, "/pages/balance_history/?lang=ru");
        addUnique(arrayList, "/pages/payments/?lang=ru");
        int size = arrayList.size();
        int i2 = 0;
        while (i2 < size) {
            int i3 = i2 + 1;
            request = request("GET", (String) arrayList.get(i2), null, null, true);
            int i4 = request.code;
            if (i4 >= 200 && i4 <= 299) {
                if (PortalHtmlParser.looksUnauthorized(request.finalUrl, request.body)) {
                    return PortalResult.failure("Требуется повторный вход", true);
                }
                try {
                    PortalResult<FundsFlowSnapshot> acceptFundsFlowHtml = acceptFundsFlowHtml(request.body);
                    if (acceptFundsFlowHtml instanceof PortalResult.Success) {
                        return acceptFundsFlowHtml;
                    }
                } catch (Exception unused) {
                    continue;
                }
            }
            i2 = i3;
        }
        int i5 = request.code;
        return (i5 < 200 || i5 > 299) ? failure(request) : PortalResult.failure("Страница движения средств не найдена в меню портала");
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<List<ServiceItem>> getServices() {
        ArrayList arrayList = new ArrayList();
        Response request = request("GET", "/pages/overview/?lang=ru", null, null, true);
        int i = request.code;
        if (i >= 200 && i <= 299 && !PortalHtmlParser.looksUnauthorized(request.finalUrl, request.body)) {
            addUnique(arrayList, PortalHtmlParser.findServicesPath(request.body));
        }
        addUnique(arrayList, "/pages/services/?lang=ru");
        addUnique(arrayList, "/pages/service_management/?lang=ru");
        addUnique(arrayList, "/pages/services_management/?lang=ru");
        addUnique(arrayList, "/pages/service_control/?lang=ru");
        int size = arrayList.size();
        int i2 = 0;
        while (i2 < size) {
            int i3 = i2 + 1;
            request = request("GET", (String) arrayList.get(i2), null, null, true);
            int i4 = request.code;
            if (i4 >= 200 && i4 <= 299) {
                if (PortalHtmlParser.looksUnauthorized(request.finalUrl, request.body)) {
                    return PortalResult.failure("Требуется повторный вход", true);
                }
                try {
                    List<ServiceItem> parseServices = PortalHtmlParser.parseServices(request.body);
                    if (!parseServices.isEmpty()) {
                        this.vault.put(KEY_SERVICES_CACHE, PortalCacheCodec.encodeServices(parseServices));
                        return PortalResult.success(parseServices);
                    }
                    continue;
                } catch (Exception unused) {
                    continue;
                }
            }
            i2 = i3;
        }
        int i5 = request.code;
        return (i5 < 200 || i5 > 299) ? failure(request) : PortalResult.success(Collections.EMPTY_LIST);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public String getSessionCookieHeader() {
        try {
            return this.cookieStore.header(new URI("https://services.intertelecom.ua/pages/overview/?lang=ru"));
        } catch (Exception unused) {
            return "";
        }
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public boolean hasSavedSession() {
        return this.cookieStore.hasSession();
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> importBrowserSession(String str, String str2) {
        String normalizeNumber = normalizeNumber(str);
        if (normalizeNumber == null) {
            return PortalResult.failure("Неверный формат номера");
        }
        try {
            this.cookieStore.replaceFromCookieHeader(new URI("https://services.intertelecom.ua/pages/overview/?lang=ru"), str2);
            if (!this.cookieStore.hasSession()) {
                return PortalResult.failure("Портал не выдал сессионный cookie после входа");
            }
            this.authForm = null;
            this.currentPhone = normalizeNumber;
            this.vault.put(KEY_PHONE, normalizeNumber);
            return PortalResult.success(null);
        } catch (Exception e) {
            return PortalResult.failure("Не удалось перенести защищённую сессию портала: " + safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> loginWithPassword(String str, String str2) {
        String normalizeNumber = normalizeNumber(str);
        if (normalizeNumber == null) {
            return PortalResult.failure("Введите номер или цифровой идентификатор: 9 цифр без начального нуля");
        }
        if (str2 == null) {
            str2 = "";
        }
        if (str2.isEmpty()) {
            return PortalResult.failure("Введите личный пароль");
        }
        if (str2.length() > 256) {
            return PortalResult.failure("Пароль слишком длинный");
        }
        this.cookieStore.clear();
        this.authForm = null;
        Response request = request("GET", "/login/number?lang=ru", null, null, true);
        int i = request.code;
        if (i < 200 || i > 299) {
            return failure(request);
        }
        AuthForm parsePasswordAuthForm = PortalHtmlParser.parsePasswordAuthForm(request.body, request.finalUrl);
        if (parsePasswordAuthForm == null) {
            request = request("GET", "/login/password/?lang=ru", null, null, true);
            int i2 = request.code;
            if (i2 < 200 || i2 > 299) {
                return failure(request);
            }
            parsePasswordAuthForm = PortalHtmlParser.parsePasswordAuthForm(request.body, request.finalUrl);
        }
        AuthForm authForm = parsePasswordAuthForm;
        if (authForm == null) {
            return PortalResult.failure("Портал загрузился, но форма входа по личному паролю не распознана. " + PortalHtmlParser.diagnoseAuthPage(request.finalUrl, request.body));
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap(authForm.fields);
        putIfBlank(linkedHashMap, "action", "password_check");
        if (!linkedHashMap.containsKey("lang")) {
            linkedHashMap.put("lang", "ru");
        }
        putNonBlank(linkedHashMap, authForm.subscriberName, normalizeNumber);
        putNonBlank(linkedHashMap, authForm.passwordName, str2);
        addSubmitControl(linkedHashMap, authForm);
        Response submitForm = submitForm(authForm, linkedHashMap, true);
        linkedHashMap.put(authForm.passwordName, "");
        int i3 = submitForm.code;
        if (i3 < 200 || i3 > 299) {
            this.cookieStore.clear();
            return failure(submitForm);
        }
        String extractError = PortalHtmlParser.extractError(submitForm.body);
        if (extractError != null) {
            this.cookieStore.clear();
            return PortalResult.failure(extractError);
        }
        String str3 = submitForm.finalUrl;
        Response request2 = request("GET", "/pages/overview/?lang=ru", null, null, true, (str3 == null || str3.trim().isEmpty()) ? request.finalUrl : submitForm.finalUrl);
        int i4 = request2.code;
        if (i4 >= 200 && i4 <= 299 && !PortalHtmlParser.looksUnauthorized(request2.finalUrl, request2.body)) {
            if (!this.cookieStore.hasSession()) {
                this.cookieStore.clear();
                return PortalResult.failure("Вход подтверждён кабинетом, но портал не выдал сохраняемый сессионный cookie");
            }
            this.currentPhone = normalizeNumber;
            this.vault.put(KEY_PHONE, normalizeNumber);
            return PortalResult.success(null);
        }
        String extractError2 = PortalHtmlParser.extractError(request2.body);
        this.cookieStore.clear();
        if (extractError2 != null) {
            return PortalResult.failure(extractError2);
        }
        return PortalResult.failure("Логин или личный пароль не принят порталом. Форма: " + authForm.method + " " + safePath(authForm.targetPath()) + "; ответ: HTTP " + submitForm.code + " " + safePath(submitForm.finalUrl) + "; проверка кабинета: HTTP " + request2.code + " " + safePath(request2.finalUrl) + ". Пароль в диагностике не сохраняется.");
    }

    /* JADX WARN: Unreachable blocks removed: 1, instructions: 1 */
    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> logout(boolean z) {
        try {
            request("GET", "/pages/overview/?logout=Y&lang=ru", null, null, true);
        } catch (Exception unused) {
        }
        this.cookieStore.clear();
        this.authForm = null;
        SecureVault secureVault = this.vault;
        if (z) {
            secureVault.clear();
            this.currentPhone = "";
        } else {
            secureVault.remove(KEY_PHONE);
            this.vault.remove(KEY_DASHBOARD_CACHE);
            this.vault.remove(KEY_SERVICES_CACHE);
            this.vault.remove(KEY_CALLS_CACHE);
            this.vault.remove(KEY_FUNDS_CACHE);
        }
        return PortalResult.success(null);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Integer> requestOtp(String str) {
        String normalizeNumber = normalizeNumber(str);
        if (normalizeNumber == null) {
            return PortalResult.failure("Введите номер Интертелекома: 9 цифр без начального нуля");
        }
        this.cookieStore.clear();
        this.authForm = null;
        Response request = request("GET", "/login/number?lang=ru", null, null, true);
        int i = request.code;
        if (i < 200 || i > 299) {
            return failure(request);
        }
        AuthForm parseAuthForm = PortalHtmlParser.parseAuthForm(request.body, false, request.finalUrl);
        if (parseAuthForm == null) {
            return PortalResult.failure("Форма входа загружена, но поле номера не распознано. " + PortalHtmlParser.diagnoseAuthPage(request.finalUrl, request.body));
        }
        LinkedHashMap linkedHashMap = new LinkedHashMap(parseAuthForm.fields);
        putIfBlank(linkedHashMap, "action", "number_check");
        if (!linkedHashMap.containsKey("lang")) {
            linkedHashMap.put("lang", "ru");
        }
        putNonBlank(linkedHashMap, parseAuthForm.subscriberName, normalizeNumber);
        addSubmitControl(linkedHashMap, parseAuthForm);
        Response submitForm = submitForm(parseAuthForm, linkedHashMap, true);
        int i2 = submitForm.code;
        if (i2 < 200 || i2 > 299) {
            return failure(submitForm);
        }
        String extractError = PortalHtmlParser.extractError(submitForm.body);
        if (extractError != null) {
            return PortalResult.failure(extractError);
        }
        AuthForm parseAuthForm2 = PortalHtmlParser.parseAuthForm(submitForm.body, true, submitForm.finalUrl);
        if (parseAuthForm2 == null) {
            return PortalResult.failure("Портал не вернул распознаваемую форму OTP. " + PortalHtmlParser.diagnoseAuthPage(submitForm.finalUrl, submitForm.body));
        }
        if (PortalHtmlParser.hasOtpCallConfirmation(submitForm.body)) {
            this.authForm = parseAuthForm2;
            this.currentPhone = normalizeNumber;
            this.vault.put(KEY_PHONE, normalizeNumber);
            return PortalResult.success(Integer.valueOf(PortalHtmlParser.extractOtpLifetimeSeconds(submitForm.body)));
        }
        return PortalResult.failure("Портал показал поле OTP, но не подтвердил отправку короткого звонка. Форма была отправлена как " + parseAuthForm.method + " " + safePath(parseAuthForm.targetPath()) + "; ответ: HTTP " + submitForm.code + " " + safePath(submitForm.finalUrl) + ". Повторите запрос один раз. Если звонка снова нет — пришлите скрин этого сообщения.");
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalResult<Void> validateSession() {
        if (!this.cookieStore.hasSession()) {
            return PortalResult.failure("Сохранённая сессия отсутствует", true);
        }
        Response request = request("GET", "/pages/overview/?lang=ru", null, null, true);
        int i = request.code;
        if (i < 200 || i > 299) {
            return failure(request);
        }
        if (!PortalHtmlParser.looksUnauthorized(request.finalUrl, request.body)) {
            return PortalResult.success(null);
        }
        this.cookieStore.clear();
        return PortalResult.failure("Серверная сессия истекла", true);
    }

    private Response request(String str, String str2, byte[] bArr, String str3, boolean z) {
        return request(str, str2, bArr, str3, z, null);
    }

    private volatile PortalLoginFlow loginFlow;

    static String digitsOf(String str) {
        return digits(str);
    }

    @Override // ua.telecomplus.app.SubscriberPortalGateway
    public PortalLoginFlow loginFlow() {
        PortalLoginFlow flow = this.loginFlow;
        if (flow == null) {
            flow = new PortalLoginFlow(this);
            this.loginFlow = flow;
        }
        return flow;
    }

    void resetSession() {
        this.cookieStore.clear();
        this.authForm = null;
    }

    Response send(String str, String str2, byte[] bArr, String str3, boolean z, String str4) {
        return request(str, str2, bArr, str3, z, str4);
    }

    Response sendMultipart(String str, Map<String, String> map, String str2) {
        return multipart(str, map, str2, false);
    }

    boolean completeLogin(String str) {
        if (!this.cookieStore.hasSession()) {
            this.cookieStore.clear();
            return false;
        }
        this.authForm = null;
        this.currentPhone = str;
        this.vault.put(KEY_PHONE, str);
        return true;
    }
}
