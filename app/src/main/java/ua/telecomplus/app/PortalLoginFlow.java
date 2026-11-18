package ua.telecomplus.app;

import android.text.Html;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Нативный вход в кабинет Интертелекома (services.intertelecom.ua):
 * 1) GET /login/number — форма, csrf_token и ключ Cloudflare Turnstile;
 * 2) POST /login/password/ (ongoing_password_check) — номер + личный пароль + токен Turnstile;
 *    портал отвечает формой одноразового пароля (короткий звонок, последние 4 цифры номера);
 * 3) POST /login/password/ (one_time_password_check) — 4-значный код → 302 на /pages/overview.
 * Повторная отправка кода: POST number_check с microcall_type (1 — звонок на номер,
 * 2 — на контактный номер, 3 — SMS). Поля формы описаны по реальному обмену, без угадывания.
 */
final class PortalLoginFlow {
    static final class Challenge {
        final String formId;
        final String subscriberField;
        final String subscriberValue;
        final String otpField;
        final String hint;

        Challenge(String str, String str2, String str3, String str4, String str5) {
            this.formId = str;
            this.subscriberField = str2;
            this.subscriberValue = str3;
            this.otpField = str4;
            this.hint = str5;
        }
    }

    static final String ACTION_PASSWORD = "ongoing_password_check";
    private static final String HOST = "https://services.intertelecom.ua";
    private static final String LOGIN_PAGE = "/login/number?lang=ru";
    private static final String LOGIN_POST = "/login/password/";

    private final IntertelecomPortalClient client;
    private volatile String csrf = "";
    private volatile String phone = "";
    private volatile Challenge challenge;

    PortalLoginFlow(IntertelecomPortalClient intertelecomPortalClient) {
        this.client = intertelecomPortalClient;
    }

    Challenge challenge() {
        return this.challenge;
    }

    /** Загружает форму входа с чистой сессией и возвращает sitekey Turnstile. */
    PortalResult<String> start() {
        this.client.resetSession();
        this.challenge = null;
        IntertelecomPortalClient.Response response = this.client.send("GET", LOGIN_PAGE, null, null, true, null);
        if (response.error != null || response.code < 200 || response.code > 299) {
            return this.client.failure(response);
        }
        String csrfToken = inputValue(response.body, "csrf_token");
        String sitekey = first(response.body, "TURNSTILE_SITEKEY\\s*=\\s*\"([^\"]+)\"");
        if (csrfToken.isEmpty() || sitekey.isEmpty()) {
            return PortalResult.failure("Страница входа изменилась: не найден csrf_token или ключ проверки безопасности");
        }
        this.csrf = csrfToken;
        return PortalResult.success(sitekey);
    }

    /**
     * Шаг 2. Успех с null — портал впустил сразу (одноразовый код не потребовался),
     * иначе вернётся challenge с формой ввода четырёх цифр.
     */
    PortalResult<Challenge> submitCredentials(String str, String str2, String str3) {
        String number = IntertelecomPortalClient.normalizeNumber(str);
        if (number == null) {
            return PortalResult.failure("Введите номер или идентификатор: 9 цифр без начального нуля");
        }
        if (str2 == null || str2.length() < 3) {
            return PortalResult.failure("Введите личный пароль");
        }
        if (str3 == null || str3.isEmpty()) {
            return PortalResult.failure("Не пройдена проверка безопасности. Подтвердите, что вы реальный человек, и повторите попытку");
        }
        if (this.csrf.isEmpty()) {
            return PortalResult.failure("Сессия входа не подготовлена, повторите попытку");
        }
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("action", ACTION_PASSWORD);
        fields.put("form_id", "");
        fields.put("form_token", str3);
        fields.put("lang", "ru");
        fields.put("back_page", "");
        fields.put("csrf_token", this.csrf);
        fields.put("subscriber_number", number);
        fields.put("ongoing_password", str2);
        fields.put("cf-turnstile-response", str3);
        IntertelecomPortalClient.Response response = this.client.sendMultipart(LOGIN_POST, fields, HOST + LOGIN_PAGE);
        this.phone = number;
        return handleStepResponse(response, true);
    }

    /** Повторно запрашивает одноразовый пароль: 1 — звонок на номер, 2 — на контактный, 3 — SMS. */
    PortalResult<Challenge> resend(int i) {
        Challenge current = this.challenge;
        if (current == null) {
            return PortalResult.failure("Сначала войдите по логину и паролю");
        }
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("action", "number_check");
        fields.put("form_id", current.formId);
        fields.put("lang", "ru");
        fields.put("back_page", "");
        fields.put(current.subscriberField, current.subscriberValue);
        fields.put("microcall_type", String.valueOf(i));
        IntertelecomPortalClient.Response response = this.client.sendMultipart(LOGIN_POST, fields, HOST + LOGIN_POST);
        PortalResult<Challenge> result = handleStepResponse(response, false);
        if (result instanceof PortalResult.Success && ((PortalResult.Success<Challenge>) result).value == null) {
            return PortalResult.failure("Портал не подтвердил отправку одноразового пароля");
        }
        return result;
    }

    /** Шаг 3. Проверяет четыре цифры и завершает вход. */
    PortalResult<Void> confirm(String str) {
        Challenge current = this.challenge;
        if (current == null) {
            return PortalResult.failure("Сначала войдите по логину и паролю");
        }
        String code = IntertelecomPortalClient.digitsOf(str);
        if (code.length() != 4) {
            return PortalResult.failure("Введите ровно четыре цифры");
        }
        LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        fields.put("action", "one_time_password_check");
        fields.put("form_id", current.formId);
        fields.put("lang", "ru");
        fields.put("back_page", "");
        fields.put(current.subscriberField, current.subscriberValue);
        fields.put(current.otpField, code);
        IntertelecomPortalClient.Response response = this.client.sendMultipart(LOGIN_POST, fields, HOST + LOGIN_POST);
        if (response.error != null) {
            return this.client.failure(response);
        }
        if (isRedirect(response.code) && response.location != null && response.location.contains("/pages/")) {
            return finish();
        }
        if (response.code >= 200 && response.code <= 299) {
            Challenge next = parseChallenge(response.body);
            if (next != null) {
                this.challenge = next;
            }
            String error = alert(response.body);
            return PortalResult.failure(error != null ? error : "Одноразовый пароль не принят. Проверьте четыре цифры.");
        }
        return this.client.failure(response);
    }

    private PortalResult<Challenge> handleStepResponse(IntertelecomPortalClient.Response response, boolean z) {
        if (response.error != null) {
            return this.client.failure(response);
        }
        if (isRedirect(response.code) && response.location != null && response.location.contains("/pages/") && z) {
            PortalResult<Void> finished = finish();
            if (finished instanceof PortalResult.Failure) {
                return PortalResult.failure(((PortalResult.Failure<Void>) finished).message);
            }
            this.challenge = null;
            return PortalResult.success(null);
        }
        if (response.code < 200 || response.code > 299) {
            return this.client.failure(response);
        }
        Challenge parsed = parseChallenge(response.body);
        if (parsed != null) {
            this.challenge = parsed;
            return PortalResult.success(parsed);
        }
        String error = alert(response.body);
        return PortalResult.failure(error != null ? error : "Портал не принял логин или пароль");
    }

    private PortalResult<Void> finish() {
        IntertelecomPortalClient.Response overview = this.client.send("GET", "/pages/overview/?lang=ru", null, null, true, HOST + LOGIN_POST);
        if (overview.error != null || overview.code < 200 || overview.code > 299 || PortalHtmlParser.looksUnauthorized(overview.finalUrl, overview.body)) {
            return PortalResult.failure("Портал не открыл кабинет после входа");
        }
        if (!this.client.completeLogin(this.phone)) {
            return PortalResult.failure("Портал не выдал сессионный cookie после входа");
        }
        return PortalResult.success(null);
    }

    // ---------- разбор HTML ----------

    private static boolean isRedirect(int i) {
        return i == 301 || i == 302 || i == 303 || i == 307 || i == 308;
    }

    static Challenge parseChallenge(String str) {
        Matcher forms = Pattern.compile("(?is)<form\\b[^>]*>.*?</form>").matcher(str);
        while (forms.find()) {
            String form = forms.group();
            if (!Pattern.compile("(?i)name=\"action\"[^>]*value=\"one_time_password_check\"").matcher(form).find()) {
                continue;
            }
            String formId = inputValue(form, "form_id");
            String subscriberField = "";
            String subscriberValue = "";
            Matcher hidden = Pattern.compile("(?is)<input\\b[^>]*>").matcher(form);
            String otpField = "";
            while (hidden.find()) {
                String tag = hidden.group();
                String name = attr(tag, "name");
                if (name.startsWith("subscriber_number")) {
                    subscriberField = name;
                    subscriberValue = attr(tag, "value");
                } else if (name.startsWith("one_time_password") && !name.equals("action")) {
                    otpField = name;
                }
            }
            if (subscriberField.isEmpty()) {
                subscriberField = "subscriber_number" + formId;
            }
            if (otpField.isEmpty()) {
                otpField = "one_time_password" + formId;
            }
            String hint = first(form, "(?is)help-block text-center\">(.*?)</span>\\s*</div>");
            return new Challenge(formId, subscriberField, subscriberValue, otpField, text(hint));
        }
        return null;
    }

    static String alert(String str) {
        Matcher m = Pattern.compile("(?is)<div class=\"alert alert-danger[^\"]*\">(.*?)</div>").matcher(str);
        if (m.find()) {
            String t = text(m.group(1));
            if (!t.isEmpty()) {
                return t;
            }
        }
        return null;
    }

    private static String inputValue(String str, String str2) {
        Matcher m = Pattern.compile("(?is)<input\\b[^>]*>").matcher(str);
        while (m.find()) {
            String tag = m.group();
            if (str2.equals(attr(tag, "name"))) {
                return attr(tag, "value");
            }
        }
        return "";
    }

    private static String attr(String str, String str2) {
        Matcher m = Pattern.compile("(?is)\\b" + Pattern.quote(str2) + "\\s*=\\s*\"([^\"]*)\"").matcher(str);
        return m.find() ? Html.fromHtml(m.group(1), 0).toString() : "";
    }

    private static String first(String str, String str2) {
        Matcher m = Pattern.compile(str2).matcher(str);
        return m.find() ? m.group(1) : "";
    }

    private static String text(String str) {
        if (str == null) {
            return "";
        }
        return Html.fromHtml(str.replaceAll("(?is)<[^>]+>", " "), 0).toString().replaceAll("\\s+", " ").trim();
    }
}
