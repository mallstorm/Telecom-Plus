package ua.telecomplus.app;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class PortalHtmlParser {
    private static final int FLAGS = 34;
    private static final Pattern FORM = Pattern.compile("<form\\b([^>]*)>(.*?)</form\\s*>", FLAGS);
    private static final Pattern CONTROL = Pattern.compile("<input\\b[^>]*>|<button\\b[^>]*>.*?</button\\s*>", FLAGS);
    private static final Pattern ATTR = Pattern.compile("([A-Za-z_:][-A-Za-z0-9_:.]*)(?:\\s*=\\s*(?:([\\\"'])(.*?)\\2|([^\\s>]+)))?", FLAGS);

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class Control {
        final Map<String, String> attrs;
        final boolean button;
        final String text;

        public Control(Map<String, String> map, String str, boolean z) {
            this.attrs = map;
            this.text = PortalHtmlParser.nz(str);
            this.button = z;
        }

        public boolean isDisabled() {
            return this.attrs.containsKey("disabled");
        }

        public boolean isSubmit() {
            String type = type();
            return "submit".equals(type) || "image".equals(type);
        }

        public String type() {
            String trim = PortalHtmlParser.nz(this.attrs.get("type")).toLowerCase(Locale.ROOT).trim();
            return trim.isEmpty() ? this.button ? "submit" : "text" : trim;
        }
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class FormCandidate {
        final Map<String, String> attrs;
        final String body;
        final List<Control> controls;

        public FormCandidate(Map<String, String> map, String str, List<Control> list) {
            this.attrs = map;
            this.body = str;
            this.controls = list;
        }

        public boolean containsAction(String str) {
            for (Control control : this.controls) {
                if (str.equalsIgnoreCase(PortalHtmlParser.nz(control.attrs.get("value"))) || str.equalsIgnoreCase(PortalHtmlParser.nz(control.attrs.get("name")))) {
                    return true;
                }
            }
            return false;
        }

        public boolean containsPasswordField() {
            Iterator<Control> it = this.controls.iterator();
            while (it.hasNext()) {
                if ("password".equalsIgnoreCase(it.next().type())) {
                    return true;
                }
            }
            return false;
        }

        public String findOtpField() {
            int i = Integer.MIN_VALUE;
            String str = "";
            for (Control control : this.controls) {
                String nz = PortalHtmlParser.nz(control.attrs.get("name"));
                if (!nz.trim().isEmpty()) {
                    String nz2 = PortalHtmlParser.nz(control.attrs.get("id"));
                    String type = control.type();
                    String nz3 = PortalHtmlParser.nz(control.attrs.get("placeholder"));
                    Locale locale = Locale.ROOT;
                    String lowerCase = nz3.toLowerCase(locale);
                    String lowerCase2 = PortalHtmlParser.nz(control.attrs.get("autocomplete")).toLowerCase(locale);
                    String lowerCase3 = (nz + " " + nz2).toLowerCase(locale);
                    int i2 = lowerCase3.contains("one_time_password") ? 120 : 0;
                    if (lowerCase3.contains("ongoing_password")) {
                        i2 += 115;
                    }
                    if (lowerCase3.contains("current_otp") || lowerCase3.contains("temporary_password")) {
                        i2 += 105;
                    }
                    if (lowerCase3.contains("otp")) {
                        i2 += 90;
                    }
                    if (lowerCase3.contains("code") || lowerCase3.contains("verify")) {
                        i2 += 40;
                    }
                    if ("one-time-code".equals(lowerCase2)) {
                        i2 += 80;
                    }
                    if (lowerCase.contains("четыр") || lowerCase.contains("чотир") || lowerCase.contains("код")) {
                        i2 += 35;
                    }
                    if ("password".equals(type)) {
                        i2 += 10;
                    }
                    if (lowerCase3.contains("subscriber") || lowerCase3.contains("phone") || lowerCase3.contains("token") || lowerCase3.contains("csrf") || "hidden".equals(type)) {
                        i2 -= 100;
                    }
                    if (i2 > i) {
                        str = nz;
                        i = i2;
                    }
                }
            }
            return i > 20 ? str : "";
        }

        public Control findPasswordSubmitControl() {
            Control control = null;
            int i = Integer.MIN_VALUE;
            for (Control control2 : this.controls) {
                if (!control2.isDisabled() && control2.isSubmit()) {
                    String nz = PortalHtmlParser.nz(control2.attrs.get("name"));
                    Locale locale = Locale.ROOT;
                    String lowerCase = nz.toLowerCase(locale);
                    String lowerCase2 = PortalHtmlParser.nz(control2.attrs.get("value")).toLowerCase(locale);
                    String lowerCase3 = PortalHtmlParser.nz(control2.attrs.get("id")).toLowerCase(locale);
                    String lowerCase4 = PortalHtmlParser.nz(control2.text).toLowerCase(locale);
                    int i2 = ("password_check".equals(lowerCase2) || "password_check".equals(lowerCase)) ? 240 : 0;
                    if ((lowerCase + " " + lowerCase3).contains("password_check")) {
                        i2 += 180;
                    }
                    if (lowerCase4.equals("войти") || lowerCase4.equals("увійти") || lowerCase4.contains("личному паролю") || lowerCase4.contains("особистим паролем")) {
                        i2 += 100;
                    }
                    if (lowerCase2.contains("one_time_password") || lowerCase4.contains("однораз")) {
                        i2 -= 180;
                    }
                    if (i2 > i) {
                        control = control2;
                        i = i2;
                    }
                }
            }
            return control;
        }

        public String findPersonalPasswordField() {
            boolean isPersonalPasswordContext = isPersonalPasswordContext();
            int i = Integer.MIN_VALUE;
            String str = "";
            for (Control control : this.controls) {
                String nz = PortalHtmlParser.nz(control.attrs.get("name"));
                if (!nz.trim().isEmpty()) {
                    String nz2 = PortalHtmlParser.nz(control.attrs.get("id"));
                    String type = control.type();
                    String nz3 = PortalHtmlParser.nz(control.attrs.get("placeholder"));
                    Locale locale = Locale.ROOT;
                    String lowerCase = nz3.toLowerCase(locale);
                    String lowerCase2 = PortalHtmlParser.nz(control.attrs.get("autocomplete")).toLowerCase(locale);
                    String lowerCase3 = (nz + " " + nz2).toLowerCase(locale);
                    int i2 = (lowerCase3.matches(".*(?:^|[_-])passwordform\\d*.*") || lowerCase3.startsWith("passwordform")) ? 150 : 0;
                    if (lowerCase3.contains("personal_password") || lowerCase3.contains("current_password")) {
                        i2 += 130;
                    }
                    if (lowerCase3.contains("password")) {
                        i2 += 80;
                    }
                    if ("password".equals(type)) {
                        i2 += isPersonalPasswordContext ? 130 : 70;
                    }
                    if ("current-password".equals(lowerCase2)) {
                        i2 += 90;
                    }
                    if (lowerCase.contains("личн") || lowerCase.contains("особист") || lowerCase.contains("парол")) {
                        i2 += 65;
                    }
                    if (lowerCase3.contains("ongoing_password")) {
                        i2 += isPersonalPasswordContext ? 90 : -220;
                    }
                    if (lowerCase3.contains("one_time") || lowerCase3.contains("otp") || lowerCase3.contains("temporary") || "one-time-code".equals(lowerCase2)) {
                        i2 -= 240;
                    }
                    if (lowerCase3.contains("token") || lowerCase3.contains("csrf") || "hidden".equals(type)) {
                        i2 -= 220;
                    }
                    if (i2 > i) {
                        str = nz;
                        i = i2;
                    }
                }
            }
            return i > 40 ? str : "";
        }

        public Control findSubmitControl(boolean z) {
            String str = z ? "one_time_password_check" : "number_check";
            Control control = null;
            int i = Integer.MIN_VALUE;
            for (Control control2 : this.controls) {
                if (!control2.isDisabled() && control2.isSubmit()) {
                    String nz = PortalHtmlParser.nz(control2.attrs.get("name"));
                    Locale locale = Locale.ROOT;
                    String lowerCase = nz.toLowerCase(locale);
                    String lowerCase2 = PortalHtmlParser.nz(control2.attrs.get("value")).toLowerCase(locale);
                    String lowerCase3 = PortalHtmlParser.nz(control2.attrs.get("id")).toLowerCase(locale);
                    String lowerCase4 = PortalHtmlParser.nz(control2.text).toLowerCase(locale);
                    int i2 = (str.equals(lowerCase2) || str.equals(lowerCase)) ? 220 : 0;
                    if ((lowerCase + " " + lowerCase3).contains(str)) {
                        i2 += 180;
                    }
                    if (!z && (lowerCase4.contains("получить однораз") || lowerCase4.contains("отримати однораз"))) {
                        i2 += 120;
                    }
                    if (z && (lowerCase4.equals("войти") || lowerCase4.equals("увійти") || lowerCase4.contains("подтверд"))) {
                        i2 += 100;
                    }
                    if (lowerCase4.contains("личному паролю") || lowerCase2.contains("password_check")) {
                        i2 -= 180;
                    }
                    if (i2 > i) {
                        control = control2;
                        i = i2;
                    }
                }
            }
            return control;
        }

        public String findSubscriberField() {
            int i = Integer.MIN_VALUE;
            String str = "";
            for (Control control : this.controls) {
                String nz = PortalHtmlParser.nz(control.attrs.get("name"));
                if (!nz.trim().isEmpty()) {
                    String nz2 = PortalHtmlParser.nz(control.attrs.get("id"));
                    String type = control.type();
                    String nz3 = PortalHtmlParser.nz(control.attrs.get("placeholder"));
                    Locale locale = Locale.ROOT;
                    String lowerCase = nz3.toLowerCase(locale);
                    String lowerCase2 = PortalHtmlParser.nz(control.attrs.get("autocomplete")).toLowerCase(locale);
                    String lowerCase3 = (nz + " " + nz2).toLowerCase(locale);
                    int i2 = lowerCase3.contains("subscriber_number") ? 100 : 0;
                    if (lowerCase3.contains("subscriber") || lowerCase3.contains("msisdn")) {
                        i2 += 65;
                    }
                    if (lowerCase3.contains("phone") || lowerCase3.contains("number") || lowerCase3.contains("login")) {
                        i2 += 35;
                    }
                    if ("tel".equals(type)) {
                        i2 += 45;
                    }
                    if ("username".equals(lowerCase2) || "tel".equals(lowerCase2)) {
                        i2 += 25;
                    }
                    if (lowerCase.contains("номер") || lowerCase.contains("телефон") || lowerCase.contains("ідентифікатор")) {
                        i2 += 30;
                    }
                    if (lowerCase3.contains("otp") || lowerCase3.contains("password") || lowerCase3.contains("token") || lowerCase3.contains("csrf") || "hidden".equals(type)) {
                        i2 -= 100;
                    }
                    if (i2 > i) {
                        str = nz;
                        i = i2;
                    }
                }
            }
            return i > 0 ? str : "";
        }

        public boolean isPersonalPasswordContext() {
            String lowerCase = PortalHtmlParser.clean(this.body).toLowerCase(Locale.ROOT);
            return containsAction("password_check") || lowerCase.contains("вход по личному паролю") || lowerCase.contains("личный пароль") || lowerCase.contains("увійти за особистим паролем") || lowerCase.contains("особистий пароль");
        }

        public int passwordScore() {
            String lowerCase = PortalHtmlParser.clean(this.body).toLowerCase(Locale.ROOT);
            String findSubscriberField = findSubscriberField();
            String findPersonalPasswordField = findPersonalPasswordField();
            int i = !findSubscriberField.isEmpty() ? 30 : 0;
            if (!findPersonalPasswordField.isEmpty()) {
                i += 80;
            }
            if (containsAction("password_check")) {
                i += 180;
            }
            if (containsAction("one_time_password_check")) {
                i -= 180;
            }
            if (containsAction("number_check")) {
                i -= 100;
            }
            if (lowerCase.contains("вход по личному паролю") || lowerCase.contains("увійти за особистим паролем") || lowerCase.contains("личный пароль") || lowerCase.contains("особистий пароль")) {
                i += 90;
            }
            return (lowerCase.contains("последние четыре") || lowerCase.contains("останні чотири") || lowerCase.contains("однораз")) ? i - 70 : i;
        }

        public int score(boolean z) {
            String lowerCase = PortalHtmlParser.clean(this.body).toLowerCase(Locale.ROOT);
            String findSubscriberField = findSubscriberField();
            String findOtpField = findOtpField();
            int i = !findSubscriberField.isEmpty() ? 20 : 0;
            if (!findOtpField.isEmpty()) {
                i += z ? 45 : -12;
            }
            if (z && containsAction("one_time_password_check")) {
                i += 140;
            }
            if (z && containsAction("number_check")) {
                i -= 80;
            }
            if (z && containsAction("password_check")) {
                i -= 120;
            }
            if (z && (lowerCase.contains("однораз") || lowerCase.contains("otp") || lowerCase.contains("останні чотири") || lowerCase.contains("последние четыре"))) {
                i += 80;
            }
            if (z && lowerCase.contains("вход по личному паролю")) {
                i -= 70;
            }
            if (!z && containsAction("number_check")) {
                i += 140;
            }
            if (!z && (lowerCase.contains("получить однораз") || lowerCase.contains("отримати однораз"))) {
                i += 60;
            }
            if (!z && containsAction("one_time_password_check")) {
                i -= 100;
            }
            if (!z && containsAction("password_check")) {
                i -= 120;
            }
            if (!z && lowerCase.contains("вход по личному паролю")) {
                i -= 40;
            }
            return (z || !containsPasswordField() || containsAction("number_check")) ? i : i - 30;
        }

        public Map<String, String> successfulNonSubmitFields() {
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            for (Control control : this.controls) {
                if (!control.isDisabled() && !control.isSubmit()) {
                    String trim = PortalHtmlParser.nz(control.attrs.get("name")).trim();
                    if (!trim.isEmpty()) {
                        String type = control.type();
                        if (!"button".equals(type) && !"reset".equals(type) && !"file".equals(type) && ((!"checkbox".equals(type) && !"radio".equals(type)) || control.attrs.containsKey("checked"))) {
                            linkedHashMap.put(trim, PortalHtmlParser.decode(PortalHtmlParser.nz(control.attrs.get("value"))));
                        }
                    }
                }
            }
            return linkedHashMap;
        }
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class FundsStatementBlock {
        final String closing;
        final String credits;
        final String debits;
        final String opening;
        final boolean recognized;
        final List<FundsTransaction> transactions;

        public FundsStatementBlock(boolean z, String str, String str2, String str3, String str4, List<FundsTransaction> list) {
            this.recognized = z;
            this.opening = PortalHtmlParser.nz(str);
            this.credits = PortalHtmlParser.nz(str2);
            this.debits = PortalHtmlParser.nz(str3);
            this.closing = PortalHtmlParser.nz(str4);
            this.transactions = list == null ? new ArrayList<>() : list;
        }
    }

    private PortalHtmlParser() {
    }

    private static String amountAfterLabel(List<String> list, String... strArr) {
        for (int i = 0; i < list.size(); i++) {
            String lowerCase = list.get(i).toLowerCase(Locale.ROOT);
            int length = strArr.length;
            int i2 = 0;
            while (true) {
                if (i2 >= length) {
                    break;
                }
                if (lowerCase.contains(strArr[i2])) {
                    List<String> extractMoneyTokens = extractMoneyTokens(list.get(i));
                    if (!extractMoneyTokens.isEmpty()) {
                        return extractMoneyTokens.get(extractMoneyTokens.size() - 1);
                    }
                    for (int i3 = i + 1; i3 < list.size() && i3 <= i + 3; i3++) {
                        List<String> extractMoneyTokens2 = extractMoneyTokens(list.get(i3));
                        if (!extractMoneyTokens2.isEmpty()) {
                            return extractMoneyTokens2.get(0);
                        }
                    }
                } else {
                    i2++;
                }
            }
        }
        return "";
    }

    private static Map<String, String> attributes(String str) {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        Matcher matcher = ATTR.matcher(nz(str));
        while (matcher.find()) {
            linkedHashMap.put(nz(matcher.group(1)).toLowerCase(Locale.US), decode(nz(matcher.group(matcher.group(3) == null ? 4 : 3))));
        }
        return linkedHashMap;
    }

    private static boolean blank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String buildSafeStructureReport(String str, String str2) {
        String nz = nz(str2);
        StringBuilder sb = new StringBuilder("Telecom+ safe DOM report\nPath: ");
        sb.append(safeReportPath(str));
        sb.append('\n');
        String firstTagText = firstTagText(nz, "title");
        if (!blank(firstTagText)) {
            sb.append("Title: ");
            sb.append(maskReportValue(firstTagText));
            sb.append('\n');
        }
        sb.append("Forms: ");
        sb.append(countMatches(nz, "<form\\b"));
        sb.append("\nTables: ");
        sb.append(countMatches(nz, "<table\\b"));
        sb.append("\nRows: ");
        sb.append(countMatches(nz, "<tr\\b"));
        sb.append('\n');
        Matcher matcher = Pattern.compile("<table\\b([^>]*)>(.*?)</table\\s*>", FLAGS).matcher(nz);
        int i = 0;
        int i2 = 0;
        while (matcher.find() && i2 < 8) {
            Map<String, String> attributes = attributes(matcher.group(1));
            ArrayList arrayList = new ArrayList();
            Matcher matcher2 = Pattern.compile("<th\\b[^>]*>(.*?)</th\\s*>", FLAGS).matcher(matcher.group(2));
            while (matcher2.find() && arrayList.size() < 16) {
                arrayList.add(maskReportValue(clean(matcher2.group(1))));
            }
            sb.append("Table ");
            i2++;
            sb.append(i2);
            sb.append(": id=");
            sb.append(maskReportValue(nz(attributes.get("id"))));
            sb.append(" class=");
            sb.append(maskReportValue(nz(attributes.get("class"))));
            sb.append(" headers=");
            sb.append(arrayList);
            sb.append(" rows=");
            sb.append(countMatches(matcher.group(2), "<tr\\b"));
            sb.append('\n');
        }
        Matcher matcher3 = Pattern.compile("<form\\b([^>]*)>(.*?)</form\\s*>", FLAGS).matcher(nz);
        while (matcher3.find() && i < 6) {
            Map<String, String> attributes2 = attributes(matcher3.group(1));
            ArrayList arrayList2 = new ArrayList();
            Matcher matcher4 = Pattern.compile("<(?:input|select|button)\\b([^>]*)>", FLAGS).matcher(matcher3.group(2));
            while (matcher4.find() && arrayList2.size() < 20) {
                Map<String, String> attributes3 = attributes(matcher4.group(1));
                arrayList2.add(maskReportValue(firstNonBlank(attributes3.get("name"), attributes3.get("id"), attributes3.get("type"))));
            }
            sb.append("Form ");
            i++;
            sb.append(i);
            sb.append(": method=");
            sb.append(maskReportValue(nz(attributes2.get("method"))));
            sb.append(" action=");
            sb.append(safeReportPath(nz(attributes2.get("action"))));
            sb.append(" controls=");
            sb.append(arrayList2);
            sb.append('\n');
        }
        sb.append("No cookies, passwords, OTP codes or raw row values are included.\n");
        return sb.toString();
    }

    private static CallDetail callFromCells(List<String> list) {
        if (list != null && list.size() >= 2) {
            ArrayList arrayList = new ArrayList();
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String normalizePortalText = normalizePortalText(it.next());
                if (!blank(normalizePortalText)) {
                    arrayList.add(normalizePortalText);
                }
            }
            if (arrayList.size() >= 2 && !isCallHeaderRow(arrayList)) {
                int size = arrayList.size();
                int i = 0;
                String str = "";
                int i2 = 0;
                String str2 = "";
                String str3 = str2;
                String str4 = str3;
                String str5 = str4;
                while (i2 < size) {
                    Object obj = arrayList.get(i2);
                    i2++;
                    String str6 = (String) obj;
                    if (blank(str2)) {
                        str2 = extractDateTime(str6);
                    }
                    if (blank(str3) && isCallDuration(str6)) {
                        str3 = normalizeCallDuration(str6);
                    }
                    if (blank(str4)) {
                        str4 = extractCallCost(str6);
                    }
                    if (blank(str5)) {
                        str5 = extractDestination(str6);
                    }
                }
                int size2 = arrayList.size();
                int i3 = 0;
                while (i3 < size2) {
                    Object obj2 = arrayList.get(i3);
                    i3++;
                    String str7 = (String) obj2;
                    if (!str7.equals(str2) && !str7.equals(str3) && !str7.equals(str4) && !str7.equals(str5)) {
                        String lowerCase = str7.toLowerCase(Locale.ROOT);
                        if (lowerCase.contains("исход") || lowerCase.contains("вихід") || lowerCase.contains("украин") || lowerCase.contains("україн") || lowerCase.contains("моб") || lowerCase.contains("город") || lowerCase.contains("міськ") || lowerCase.contains("международ") || lowerCase.contains("міжнарод")) {
                            str = str7;
                            break;
                        }
                    }
                }
                if (blank(str)) {
                    int size3 = arrayList.size();
                    while (true) {
                        if (i >= size3) {
                            break;
                        }
                        Object obj3 = arrayList.get(i);
                        i++;
                        String str8 = (String) obj3;
                        if (!str8.equals(str2) && !str8.equals(str3) && !str8.equals(str4) && !str8.equals(str5) && str8.length() >= 3 && str8.length() <= 100) {
                            str = str8;
                            break;
                        }
                    }
                }
                if (blank(str2) && blank(str3)) {
                    return null;
                }
                return new CallDetail(orDash(str2), orDash(str), orDash(str5), orDash(str3), orDash(str4));
            }
        }
        return null;
    }

    private static CallDetail callFromMappedCells(List<String> list, List<String> list2) {
        int findHeader = findHeader(list, "дата", "date");
        int findHeader2 = findHeader(list, "время", "час", "time");
        int findHeader3 = findHeader(list, "направ", "тип", "категор", "зона", "direction");
        int findHeader4 = findHeader(list, "номер", "абонент", "назнач", "куда", "телефон", "destination");
        int findHeader5 = findHeader(list, "длитель", "тривал", "продолж", "duration");
        int findHeader6 = findHeader(list, "стоим", "варт", "сумма", "сума", "cost");
        String joinNonBlank = joinNonBlank(cell(list2, findHeader), cell(list2, findHeader2));
        String cell = cell(list2, findHeader3);
        String cell2 = cell(list2, findHeader4);
        String cell3 = cell(list2, findHeader5);
        String extractCallCost = extractCallCost(cell(list2, findHeader6));
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = list2.iterator();
        if (it.hasNext()) {
            while (true) {
                sb.append((CharSequence) it.next());
                if (!it.hasNext()) {
                    break;
                }
                sb.append((CharSequence) " ");
            }
        }
        String sb2 = sb.toString();
        if (blank(joinNonBlank)) {
            joinNonBlank = extractDateTime(sb2);
        }
        if (blank(cell2)) {
            cell2 = extractDestination(sb2);
        }
        if (blank(cell3)) {
            Iterator<String> it2 = list2.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                String next = it2.next();
                if (isCallDuration(next)) {
                    cell3 = normalizeCallDuration(next);
                    break;
                }
            }
        } else {
            cell3 = normalizeCallDuration(cell3);
        }
        if (blank(extractCallCost)) {
            Iterator<String> it3 = list2.iterator();
            while (it3.hasNext()) {
                extractCallCost = extractCallCost(it3.next());
                if (!blank(extractCallCost)) {
                    break;
                }
            }
        }
        if (blank(cell)) {
            cell = "Исходящий звонок";
        }
        if (blank(joinNonBlank) && blank(cell3)) {
            return null;
        }
        return new CallDetail(orDash(joinNonBlank), orDash(cell), orDash(cell2), orDash(cell3), orDash(extractCallCost));
    }

    private static String cell(List<String> list, int i) {
        return (i < 0 || i >= list.size()) ? "" : normalizePortalText(list.get(i));
    }

    private static String chooseTransactionAmount(List<String> list, String str) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        int size = list.size() >= 2 ? list.size() - 2 : 0;
        for (int i = 0; i <= size; i++) {
            String str2 = list.get(i);
            if (str2.trim().startsWith("+") || str2.trim().startsWith("-") || str2.trim().startsWith("−")) {
                return str2;
            }
        }
        for (int i2 = 0; i2 <= size; i2++) {
            if (Math.abs(moneyNumber(list.get(i2))) > 1.0E-4d) {
                return list.get(i2);
            }
        }
        return list.get(size);
    }

    public static String clean(String str) {
        return sanitizeDisplayText(decode(nz(str).replaceAll("(?is)<!--.*?-->", " ").replaceAll("(?is)<script\\b.*?</script>", " ").replaceAll("(?is)<style\\b.*?</style>", " ").replaceAll("(?is)<noscript\\b.*?</noscript>", " ").replaceAll("(?i)<(?:br\\s*/?|/p|/div|/li|/tr|/td|/th|/dt|/dd|/label|/h[1-6]|/section|/article|/option)[^>]*>", "\n").replaceAll("(?i)<(?:p|div|li|tr|td|th|dt|dd|label|h[1-6]|section|article|option)\\b[^>]*>", "\n").replaceAll("<[^>]+>", " "))).replace((char) 160, ' ').replaceAll("[ \\t\\x0B\\f\\r]+", " ").replaceAll(" *\\n *", "\n").replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static String cleanServiceTitle(String str) {
        return sanitizeDisplayText(str).replaceAll("^[\\-•]+\\s*", "").trim();
    }

    private static boolean containsCaptchaControl(List<Control> list) {
        for (Control control : list) {
            String lowerCase = (nz(control.attrs.get("name")) + " " + nz(control.attrs.get("id")) + " " + nz(control.attrs.get("class"))).toLowerCase(Locale.ROOT);
            if (lowerCase.contains("captcha") || lowerCase.contains("recaptcha") || lowerCase.contains("h-captcha")) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsIgnoreCase(String str, String str2) {
        String nz = nz(str);
        Locale locale = Locale.ROOT;
        return nz.toLowerCase(locale).contains(nz(str2).toLowerCase(locale));
    }

    private static int countMatches(String str, String str2) {
        Matcher matcher = Pattern.compile(str2, 2).matcher(nz(str));
        int i = 0;
        while (matcher.find()) {
            i++;
        }
        return i;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String decode(String str) {
        int i;
        Matcher matcher = Pattern.compile("&#(\\d{1,7});").matcher(nz(str).replace("&nbsp;", " ").replace("&#160;", " ").replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&apos;", "'").replace("&lt;", "<").replace("&gt;", ">"));
        StringBuffer stringBuffer = new StringBuffer();
        while (matcher.find()) {
            try {
                i = Integer.parseInt(matcher.group(1));
            } catch (Exception unused) {
                i = -1;
            }
            matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement((i < 0 || !Character.isValidCodePoint(i)) ? matcher.group() : new String(Character.toChars(i))));
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    public static String diagnoseAuthPage(String str, String str2) {
        String nz = nz(str2);
        int i = 0;
        int i2 = 0;
        while (FORM.matcher(nz).find()) {
            i2++;
        }
        List<Control> parseControls = parseControls(nz);
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        Iterator<Control> it = parseControls.iterator();
        while (it.hasNext()) {
            String trim = nz(it.next().attrs.get("name")).trim();
            if (!trim.isEmpty()) {
                linkedHashSet.add(trim);
            }
        }
        Matcher matcher = Pattern.compile("<title[^>]*>(.*?)</title>", FLAGS).matcher(nz);
        String str3 = "";
        String clean = matcher.find() ? clean(matcher.group(1)) : "";
        Locale locale = Locale.ROOT;
        String lowerCase = nz.toLowerCase(locale);
        String lowerCase2 = clean(nz).toLowerCase(locale);
        if (lowerCase.contains("cloudflare") || lowerCase.contains("cf-chl")) {
            str3 = "защита Cloudflare";
        } else if (containsCaptchaControl(parseControls) || lowerCase2.contains("введите код с картинки") || lowerCase2.contains("підтвердіть, що ви не робот") || lowerCase2.contains("подтвердите, что вы не робот")) {
            str3 = "CAPTCHA";
        } else if (lowerCase.contains("access denied") || lowerCase.contains("доступ запрещ")) {
            str3 = "страница блокировки";
        } else if (lowerCase2.contains("enable javascript") || lowerCase2.contains("включите javascript")) {
            str3 = "требуется JavaScript";
        }
        StringBuilder sb = new StringBuilder("URL: ");
        sb.append(limit(nz(str), 120));
        if (!clean.isEmpty()) {
            sb.append("; title: ");
            sb.append(limit(clean, 80));
        }
        sb.append("; forms: ");
        sb.append(i2);
        sb.append("; controls: ");
        sb.append(parseControls.size());
        if (!linkedHashSet.isEmpty()) {
            sb.append("; names: ");
            Iterator it2 = linkedHashSet.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                String str4 = (String) it2.next();
                int i3 = i + 1;
                if (i >= 12) {
                    sb.append("…");
                    break;
                }
                if (i3 > 1) {
                    sb.append(", ");
                }
                sb.append(limit(str4, 40));
                i = i3;
            }
        }
        if (!str3.isEmpty()) {
            sb.append("; detected: ");
            sb.append(str3);
        }
        return sb.toString();
    }

    private static boolean equalsAnyIgnoreCase(String str, String... strArr) {
        for (String str2 : strArr) {
            if (nz(str).equalsIgnoreCase(str2)) {
                return true;
            }
        }
        return false;
    }

    private static String extractCallCost(String str) {
        Matcher matcher = Pattern.compile("(?iu)[+\\-−]?\\s*\\d+[.,]\\d{1,2}\\s*(?:грн|₴|uah)").matcher(nz(str));
        if (matcher.find()) {
            return normalizePortalText(matcher.group()).replace((char) 8722, '-').replace(',', '.');
        }
        Matcher matcher2 = Pattern.compile("(?u)^\\s*[+\\-−]?\\s*\\d+[.,]\\d{1,2}\\s*$").matcher(nz(str));
        if (!matcher2.find()) {
            return "";
        }
        return normalizePortalText(matcher2.group()).replace((char) 8722, '-').replace(',', '.') + " грн";
    }

    private static String extractCallPeriod(String str) {
        List<String> meaningfulLines = meaningfulLines(str);
        for (int i = 0; i < meaningfulLines.size(); i++) {
            String lowerCase = meaningfulLines.get(i).toLowerCase(Locale.ROOT);
            if (lowerCase.contains("текущ") || lowerCase.contains("поточн") || lowerCase.contains("предыдущ") || lowerCase.contains("попередн") || lowerCase.contains("период") || lowerCase.contains("період")) {
                String str2 = meaningfulLines.get(i);
                int i2 = i + 1;
                if (i2 < meaningfulLines.size() && meaningfulLines.get(i2).matches(".*\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}.*")) {
                    str2 = str2 + " · " + meaningfulLines.get(i2);
                }
                return limit(str2, 120);
            }
        }
        Matcher matcher = Pattern.compile("(?u)(?:до|по)\\s+\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}(?:\\s+\\d{1,2}:\\d{2})?").matcher(nz(str));
        return matcher.find() ? normalizePortalText(matcher.group()) : "Текущий период";
    }

    private static String extractDateTime(String str) {
        Matcher matcher = Pattern.compile("\\b\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}(?:\\s+\\d{1,2}:\\d{2}(?::\\d{2})?)?\\b").matcher(nz(str));
        if (matcher.find()) {
            return matcher.group();
        }
        Matcher matcher2 = Pattern.compile("\\b\\d{1,2}:\\d{2}(?::\\d{2})?\\b").matcher(nz(str));
        return matcher2.find() ? matcher2.group() : "";
    }

    private static String extractDestination(String str) {
        Matcher matcher = Pattern.compile("(?:\\+?38)?0\\d(?:[\\s()\\-]*\\d){8,9}").matcher(nz(str));
        return matcher.find() ? normalizePortalText(matcher.group()) : "";
    }

    private static String extractDurationPhrase(String str) {
        Matcher matcher = Pattern.compile("(?iu)(?:\\d+\\s*(?:час(?:а|ов)?|год(?:ина|ини|ин)?)[ ,]*)?(?:\\d+\\s*(?:минут(?:а|ы)?|хвилин(?:а|и)?)[ ,]*)?\\d+\\s*(?:секунд(?:а|ы)?|секунди?)").matcher(normalizePortalText(str));
        return matcher.find() ? normalizeDuration(matcher.group()) : "";
    }

    public static String extractError(String str) {
        String[] strArr = {"Неверный", "Помилка", "Ошибка", "истек", "закінчився", "неправиль", "подождите", "зачекайте", "повторите через", "спробуйте через", "слишком много попыток", "забагато спроб", "кількість спроб", "количество попыток"};
        for (String str2 : clean(str).split("\\R")) {
            String trim = str2.trim();
            for (int i = 0; i < 14; i++) {
                if (containsIgnoreCase(trim, strArr[i])) {
                    return trim.substring(0, Math.min(220, trim.length()));
                }
            }
        }
        return null;
    }

    private static String extractFee(String str) {
        List<String> meaningfulLines = meaningfulLines(str);
        for (int i = 0; i < meaningfulLines.size(); i++) {
            if (equalsAnyIgnoreCase(meaningfulLines.get(i), "Абонентная плата", "Абонентська плата")) {
                String nextValueLine = nextValueLine(meaningfulLines, i + 1, 4);
                return blank(nextValueLine) ? "—" : nextValueLine;
            }
        }
        return "—";
    }

    private static String extractFundsPeriod(String str) {
        Matcher matcher = Pattern.compile("(?iu)движение\\s+средств\\s+за\\s+([а-яіїєґ]+\\s+\\d{4})").matcher(nz(str));
        if (matcher.find()) {
            return normalizePortalText(matcher.group(1));
        }
        Matcher matcher2 = Pattern.compile("(?iu)рух\\s+коштів\\s+за\\s+([а-яіїєґ]+\\s+\\d{4})").matcher(nz(str));
        if (matcher2.find()) {
            return normalizePortalText(matcher2.group(1));
        }
        List<String> meaningfulLines = meaningfulLines(str);
        for (int i = 0; i < meaningfulLines.size(); i++) {
            String lowerCase = meaningfulLines.get(i).toLowerCase(Locale.ROOT);
            if (lowerCase.contains("период") || lowerCase.contains("період") || lowerCase.contains("за месяц") || lowerCase.contains("за місяць") || lowerCase.contains("с даты") || lowerCase.contains("з дати")) {
                String str2 = meaningfulLines.get(i);
                int i2 = i + 1;
                if (i2 < meaningfulLines.size() && meaningfulLines.get(i2).matches(".*\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}.*")) {
                    str2 = str2 + " · " + meaningfulLines.get(i2);
                }
                return limit(str2, 140);
            }
        }
        Matcher matcher3 = Pattern.compile("(?u)\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}\\s*(?:—|-|по|до)\\s*\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}").matcher(nz(str));
        return matcher3.find() ? normalizePortalText(matcher3.group()) : "Текущий период";
    }

    private static List<String> extractMoneyTokens(String str) {
        ArrayList arrayList = new ArrayList();
        Matcher matcher = Pattern.compile("(?iu)[+\\-−]?\\s*\\d{1,9}(?:[ \\u00A0]\\d{3})*[.,]\\d{1,2}\\s*(?:грн|₴|uah)").matcher(nz(str));
        while (matcher.find()) {
            arrayList.add(normalizePortalText(matcher.group()).replace((char) 8722, '-').replace(',', '.').replaceAll("\\s+", " ").trim());
        }
        if (arrayList.isEmpty()) {
            Matcher matcher2 = Pattern.compile("(?u)(?<![\\d./:])[+\\-−]?\\s*\\d{1,9}(?:[ \\u00A0]\\d{3})*[.,]\\d{1,2}(?!\\d)").matcher(nz(str));
            while (matcher2.find()) {
                arrayList.add(normalizePortalText(matcher2.group()).replace((char) 8722, '-').replace(',', '.').replaceAll("\\s+", " ").trim() + " грн");
            }
        }
        return arrayList;
    }

    public static int extractOtpLifetimeSeconds(String str) {
        Matcher matcher = Pattern.compile("(\\d{1,2})\\s*(?:хвилин|минут)", 2).matcher(clean(str));
        if (!matcher.find()) {
            return 600;
        }
        try {
            int parseInt = Integer.parseInt(matcher.group(1));
            if (parseInt < 1 || parseInt > 30) {
                return 600;
            }
            return parseInt * 60;
        } catch (Exception unused) {
            return 600;
        }
    }

    public static int extractRetryAfterSeconds(String str) {
        String clean = clean(nz(str));
        Matcher matcher = Pattern.compile("(?<!\\d)(\\d{1,2}):(\\d{2})(?!\\d)").matcher(clean);
        if (matcher.find()) {
            try {
                int parseInt = Integer.parseInt(matcher.group(1));
                int parseInt2 = Integer.parseInt(matcher.group(2));
                if (parseInt <= 60 && parseInt2 < 60) {
                    return (parseInt * 60) + parseInt2;
                }
            } catch (Exception unused) {
            }
        }
        Matcher matcher2 = Pattern.compile("(\\d{1,3})\\s*(секунд|секунди|сек\\.?|минут|минуты|минута|хвилин|хвилини|хвилина)", 2).matcher(clean);
        if (matcher2.find()) {
            try {
                int parseInt3 = Integer.parseInt(matcher2.group(1));
                return matcher2.group(2).toLowerCase(Locale.ROOT).startsWith("сек") ? parseInt3 : parseInt3 * 60;
            } catch (Exception unused2) {
            }
        }
        return isRateLimitError(clean) ? 600 : 0;
    }

    private static String extractServiceStatus(String str) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        return (lowerCase.contains("подключена") || lowerCase.contains("подключено") || lowerCase.contains("підключена") || lowerCase.contains("підключено")) ? "Подключена" : (lowerCase.contains("доступна") || lowerCase.contains("доступно") || lowerCase.contains("подключить") || lowerCase.contains("підключити")) ? "Доступна" : (lowerCase.contains("отключена") || lowerCase.contains("відключена") || lowerCase.contains("не подключена") || lowerCase.contains("не підключена")) ? "Отключена" : (lowerCase.contains("отключить") || lowerCase.contains("відключити")) ? "Подключена" : "—";
    }

    public static String findFundsFlowPath(String str) {
        Matcher matcher = Pattern.compile("<a\\b([^>]*)>(.*?)</a\\s*>", FLAGS).matcher(nz(str));
        String str2 = "";
        while (matcher.find()) {
            boolean z = true;
            String trim = decode(nz(attributes(matcher.group(1)).get("href"))).trim();
            String sanitizeDisplayText = sanitizeDisplayText(clean(matcher.group(2)));
            Locale locale = Locale.ROOT;
            String lowerCase = sanitizeDisplayText.toLowerCase(locale);
            if (!trim.isEmpty() && !trim.startsWith("javascript:") && !trim.startsWith("#")) {
                String normalizePortalHref = normalizePortalHref(trim);
                if (normalizePortalHref.isEmpty()) {
                    continue;
                } else {
                    String lowerCase2 = normalizePortalHref.toLowerCase(locale);
                    boolean z2 = lowerCase.contains("движение средств") || lowerCase.contains("рух коштів") || lowerCase.contains("история платеж") || lowerCase.contains("історія платеж") || lowerCase.contains("расход") || lowerCase.contains("витрат") || lowerCase.contains("финанс") || lowerCase.contains("фінанс");
                    if (!lowerCase2.contains("fund") && !lowerCase2.contains("money") && !lowerCase2.contains("payment") && !lowerCase2.contains("balance_history") && !lowerCase2.contains("account_history")) {
                        z = false;
                    }
                    if (z2 && z) {
                        return normalizePortalHref;
                    }
                    if (str2.isEmpty() && z) {
                        str2 = normalizePortalHref;
                    }
                }
            }
        }
        return str2;
    }

    private static int findHeader(List<String> list, String... strArr) {
        for (int i = 0; i < list.size(); i++) {
            String lowerCase = nz(list.get(i)).toLowerCase(Locale.ROOT);
            for (String str : strArr) {
                if (lowerCase.contains(str)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static String findPhone(String str) {
        Matcher matcher = Pattern.compile("(?:\\+?38)?0?94\\s*\\d{3}\\s*\\d{2}\\s*\\d{2}").matcher(nz(str));
        return matcher.find() ? matcher.group() : "";
    }

    public static String findServicesPath(String str) {
        Matcher matcher = Pattern.compile("<a\\b([^>]*)>(.*?)</a\\s*>", FLAGS).matcher(nz(str));
        String str2 = "";
        while (matcher.find()) {
            boolean z = true;
            String trim = decode(nz(attributes(matcher.group(1)).get("href"))).trim();
            String sanitizeDisplayText = sanitizeDisplayText(clean(matcher.group(2)));
            Locale locale = Locale.ROOT;
            String lowerCase = sanitizeDisplayText.toLowerCase(locale);
            if (!trim.isEmpty() && !trim.startsWith("javascript:") && !trim.startsWith("#")) {
                String normalizePortalHref = normalizePortalHref(trim);
                if (normalizePortalHref.isEmpty()) {
                    continue;
                } else {
                    String lowerCase2 = normalizePortalHref.toLowerCase(locale);
                    boolean z2 = lowerCase.contains("управление услуг") || lowerCase.contains("керування послуг") || lowerCase.equals("услуги") || lowerCase.equals("послуги");
                    if (!lowerCase2.contains("service") && !lowerCase2.contains("uslug") && !lowerCase2.contains("poslug")) {
                        z = false;
                    }
                    if (z2 && z) {
                        return normalizePortalHref;
                    }
                    if (str2.isEmpty() && z) {
                        str2 = normalizePortalHref;
                    }
                }
            }
        }
        return str2;
    }

    private static String firstAttributeText(String str, String str2) {
        Matcher matcher = Pattern.compile("<[^>]+" + Pattern.quote(str2) + "(?:=[\\\"'][^\\\"']*[\\\"'])?[^>]*>(.*?)</[^>]+>", FLAGS).matcher(nz(str));
        return matcher.find() ? clean(matcher.group(1)) : "";
    }

    private static String firstClassText(String str, String str2) {
        Matcher matcher = Pattern.compile("<[^>]+class=[\\\"'][^\\\"']*" + Pattern.quote(str2) + "[^\\\"']*[\\\"'][^>]*>(.*?)</[^>]+>", FLAGS).matcher(nz(str));
        return matcher.find() ? clean(matcher.group(1)) : "";
    }

    private static String firstHeadingOrLink(String str) {
        Matcher matcher = Pattern.compile("<h[1-6][^>]*>(.*?)</h[1-6]>", FLAGS).matcher(nz(str));
        if (matcher.find()) {
            return sanitizeDisplayText(clean(matcher.group(1)));
        }
        Matcher matcher2 = Pattern.compile("<a\\b[^>]*>(.*?)</a\\s*>", FLAGS).matcher(nz(str));
        while (matcher2.find()) {
            String sanitizeDisplayText = sanitizeDisplayText(clean(matcher2.group(1)));
            if (isLikelyServiceTitle(sanitizeDisplayText)) {
                return sanitizeDisplayText;
            }
        }
        return "";
    }

    private static String firstMoney(String str) {
        List<String> extractMoneyTokens = extractMoneyTokens(str);
        return extractMoneyTokens.isEmpty() ? "" : extractMoneyTokens.get(0);
    }

    private static String firstNonBlank(String... strArr) {
        for (String str : strArr) {
            if (!blank(str) && !"—".equals(str.trim())) {
                return str.trim();
            }
        }
        return "";
    }

    private static String firstTagText(String str, String str2) {
        Matcher matcher = Pattern.compile("<" + str2 + "\\b[^>]*>(.*?)</" + str2 + "\\s*>", FLAGS).matcher(nz(str));
        return matcher.find() ? normalizePortalText(clean(matcher.group(1))) : "";
    }

    private static FundsTransaction fundsFromCells(List<String> list) {
        if (list != null && list.size() >= 2) {
            ArrayList arrayList = new ArrayList();
            Iterator<String> it = list.iterator();
            while (it.hasNext()) {
                String normalizePortalText = normalizePortalText(it.next());
                if (!blank(normalizePortalText)) {
                    arrayList.add(normalizePortalText);
                }
            }
            if (arrayList.size() >= 2 && !isFundsHeaderRow(arrayList)) {
                StringBuilder sb = new StringBuilder();
                Iterator it2 = arrayList.iterator();
                if (it2.hasNext()) {
                    while (true) {
                        sb.append((CharSequence) it2.next());
                        if (!it2.hasNext()) {
                            break;
                        }
                        sb.append((CharSequence) " ");
                    }
                }
                String sb2 = sb.toString();
                int size = arrayList.size();
                int i = 0;
                int i2 = 0;
                String str = "";
                while (i2 < size) {
                    Object obj = arrayList.get(i2);
                    i2++;
                    String str2 = (String) obj;
                    if (blank(str)) {
                        str = extractDateTime(str2);
                    }
                }
                ArrayList arrayList2 = new ArrayList();
                int size2 = arrayList.size();
                int i3 = 0;
                while (i3 < size2) {
                    Object obj2 = arrayList.get(i3);
                    i3++;
                    arrayList2.addAll(extractMoneyTokens((String) obj2));
                }
                if (arrayList2.isEmpty()) {
                    return null;
                }
                String inferFundsType = inferFundsType(sb2, arrayList2);
                String str3 = arrayList2.size() >= 2 ? (String) arrayList2.get(arrayList2.size() - 1) : "";
                String chooseTransactionAmount = chooseTransactionAmount(arrayList2, inferFundsType);
                if (blank(chooseTransactionAmount)) {
                    chooseTransactionAmount = (String) arrayList2.get(0);
                }
                String normalizeSignedAmount = normalizeSignedAmount(chooseTransactionAmount, inferFundsType);
                int size3 = arrayList.size();
                String str4 = "";
                while (i < size3) {
                    Object obj3 = arrayList.get(i);
                    i++;
                    String str5 = (String) obj3;
                    if (blank(extractDateTime(str5)) && (extractMoneyTokens(str5).isEmpty() || !sanitizeDisplayText(str5).replaceAll("[+\\-]?\\d+[.,]\\d{1,2}\\s*(?:грн|₴|uah)", "").trim().isEmpty())) {
                        String lowerCase = str5.toLowerCase(Locale.ROOT);
                        if (!lowerCase.equals("приход") && !lowerCase.equals("расход") && !lowerCase.equals("надходження") && !lowerCase.equals("витрати") && !lowerCase.equals("дебет") && !lowerCase.equals("кредит") && str5.length() > str4.length() && str5.length() <= 260) {
                            str4 = str5;
                        }
                    }
                }
                if (blank(str4)) {
                    str4 = "Операция по лицевому счёту";
                }
                if (!isFundsUiNoise(str4) && !isFundsStatementSummaryLabel(str4.toLowerCase(Locale.ROOT))) {
                    if ("credit".equals(inferFundsType) && !isExplicitFundsCredit(str4) && blank(str)) {
                        return null;
                    }
                    String inferFundsCategory = inferFundsCategory(str4, inferFundsType);
                    if (blank(str) && "Операция по лицевому счёту".equals(str4)) {
                        return null;
                    }
                    return new FundsTransaction(orDash(str), limit(str4, 260), inferFundsCategory, orDash(normalizeSignedAmount), orDash(str3), inferFundsType);
                }
            }
        }
        return null;
    }

    private static FundsTransaction fundsFromMappedCells(List<String> list, List<String> list2) {
        int i;
        int findHeader = findHeader(list, "дата", "date");
        int findHeader2 = findHeader(list, "время", "час", "time");
        int findHeader3 = findHeader(list, "операц", "описан", "назнач", "признач", "услуг", "послуг", "комментар");
        int findHeader4 = findHeader(list, "списан", "расход", "витрат", "дебет", "debit");
        int findHeader5 = findHeader(list, "пополн", "приход", "надход", "зачисл", "кредит", "credit");
        int findHeader6 = findHeader(list, "сумма", "сума", "amount");
        int findHeader7 = findHeader(list, "остаток", "залишок", "баланс", "balance");
        String joinNonBlank = joinNonBlank(cell(list2, findHeader), cell(list2, findHeader2));
        String cell = cell(list2, findHeader3);
        String firstMoney = firstMoney(cell(list2, findHeader4));
        String str = blank(firstMoney) ? "neutral" : "debit";
        if (blank(firstMoney)) {
            firstMoney = firstMoney(cell(list2, findHeader5));
            if (!blank(firstMoney)) {
                str = "credit";
            }
        }
        if (blank(firstMoney)) {
            firstMoney = firstMoney(cell(list2, findHeader6));
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = list2.iterator();
        if (it.hasNext()) {
            while (true) {
                sb.append((CharSequence) it.next());
                if (!it.hasNext()) {
                    break;
                }
                sb.append((CharSequence) " ");
            }
        }
        String sb2 = sb.toString();
        if ("neutral".equals(str)) {
            str = inferFundsType(sb2, extractMoneyTokens(sb2));
        }
        if (blank(cell)) {
            int i2 = 0;
            while (i2 < list2.size()) {
                if (i2 == findHeader || i2 == findHeader2 || i2 == findHeader4 || i2 == findHeader5 || i2 == findHeader6 || i2 == findHeader7) {
                    i = findHeader;
                } else {
                    String normalizePortalText = normalizePortalText(list2.get(i2));
                    i = findHeader;
                    if (normalizePortalText.length() > cell.length() && normalizePortalText.length() <= 260) {
                        cell = normalizePortalText;
                    }
                }
                i2++;
                findHeader = i;
            }
        }
        if (blank(joinNonBlank)) {
            joinNonBlank = extractDateTime(sb2);
        }
        if (blank(firstMoney)) {
            return null;
        }
        if (blank(cell)) {
            cell = "Операция по лицевому счёту";
        }
        if (isFundsUiNoise(cell) || isFundsStatementSummaryLabel(cell.toLowerCase(Locale.ROOT))) {
            return null;
        }
        if ("credit".equals(str) && !isExplicitFundsCredit(cell) && blank(joinNonBlank)) {
            return null;
        }
        return new FundsTransaction(orDash(joinNonBlank), limit(cell, 260), inferFundsCategory(cell, str), orDash(normalizeSignedAmount(firstMoney, str)), orDash(firstMoney(cell(list2, findHeader7))), str);
    }

    public static boolean hasDynamicDetailRows(String str) {
        String nz = nz(str);
        String lowerCase = nz.toLowerCase(Locale.ROOT);
        Matcher matcher = Pattern.compile("<tr\\b", 2).matcher(nz);
        int i = 0;
        while (matcher.find() && i < 4) {
            i++;
        }
        boolean z = lowerCase.contains("списан") || lowerCase.contains("пополн") || lowerCase.contains("надход") || lowerCase.contains("витрат") || lowerCase.contains("операц") || lowerCase.contains("длитель") || lowerCase.contains("тривал") || lowerCase.contains("стоим") || lowerCase.contains("исходящ") || lowerCase.contains("вихідн") || lowerCase.contains("destination") || lowerCase.contains("duration") || lowerCase.contains("debit") || lowerCase.contains("credit");
        return (i >= 2 && z) || lowerCase.contains("transaction-row") || lowerCase.contains("operation-row") || lowerCase.contains("call-detail-row") || lowerCase.contains("history-item") || lowerCase.contains("funds-flow-row") || (lowerCase.contains("datatables_wrapper") && z);
    }

    public static boolean hasOtpCallConfirmation(String str) {
        String lowerCase = clean(str).toLowerCase(Locale.ROOT);
        return (lowerCase.contains("короткий виклик") || lowerCase.contains("короткого виклику") || lowerCase.contains("короткий вызов") || lowerCase.contains("короткого вызова") || lowerCase.contains("короткий звонок") || lowerCase.contains("короткого звонка") || ((lowerCase.contains("коротк") || lowerCase.contains("коротк")) && (lowerCase.contains("виклик") || lowerCase.contains("вызов") || lowerCase.contains("звон")))) && (lowerCase.contains("останні чотири") || lowerCase.contains("последние четыре") || lowerCase.contains("одноразовим паролем") || lowerCase.contains("одноразовым паролем")) && (lowerCase.contains("було здійснено") || lowerCase.contains("было осуществлено") || lowerCase.contains("был осуществлен") || lowerCase.contains("выполнен") || lowerCase.contains("совершен"));
    }

    private static List<String> htmlCells(String str) {
        ArrayList arrayList = new ArrayList();
        Matcher matcher = Pattern.compile("<t[dh]\\b[^>]*>(.*?)</t[dh]\\s*>", FLAGS).matcher(nz(str));
        while (matcher.find()) {
            String normalizePortalText = normalizePortalText(clean(matcher.group(1)));
            if (!blank(normalizePortalText)) {
                arrayList.add(normalizePortalText);
            }
        }
        return arrayList;
    }

    private static List<String> htmlCellsPreserve(String str) {
        ArrayList arrayList = new ArrayList();
        Matcher matcher = Pattern.compile("<t[dh]\\b[^>]*>(.*?)</t[dh]\\s*>", FLAGS).matcher(nz(str));
        while (matcher.find()) {
            arrayList.add(normalizePortalText(clean(matcher.group(1))));
        }
        return arrayList;
    }

    private static int indexOfIgnoreCase(String str, String str2) {
        String nz = nz(str);
        Locale locale = Locale.ROOT;
        return nz.toLowerCase(locale).indexOf(nz(str2).toLowerCase(locale));
    }

    private static String inferFundsCategory(String str, String str2) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        return (lowerCase.contains("пополн") || lowerCase.contains("зачисл") || lowerCase.contains("надход") || lowerCase.contains("банк")) ? "Пополнение" : (lowerCase.contains("абонент") || lowerCase.contains("тариф")) ? "Тариф" : (lowerCase.contains("звон") || lowerCase.contains("дзвін") || lowerCase.contains("вызов")) ? "Звонки" : (lowerCase.contains("интернет") || lowerCase.contains("послуг") || lowerCase.contains("услуг") || lowerCase.contains("sms")) ? "Услуги" : (lowerCase.contains("перевод") || lowerCase.contains("переказ")) ? "Перевод" : (lowerCase.contains("коррект") || lowerCase.contains("коригув") || lowerCase.contains("возврат") || lowerCase.contains("повернен")) ? "Корректировка" : "credit".equals(str2) ? "Поступление" : "debit".equals(str2) ? "Списание" : "Операция";
    }

    private static String inferFundsType(String str, List<String> list) {
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String trim = it.next().trim();
            if (trim.startsWith("+")) {
                return "credit";
            }
            if (trim.startsWith("-") || trim.startsWith("−")) {
                return "debit";
            }
        }
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        String[] strArr = {"пополн", "зачисл", "надход", "зарах", "возврат", "повернен", "внесен", "кредит"};
        for (int i = 0; i < 8; i++) {
            if (lowerCase.contains(strArr[i])) {
                return "credit";
            }
        }
        String[] strArr2 = {"списан", "расход", "витрат", "абонент", "пакет", "оплата услуг", "дзвін", "звон", "дебет", "удерж", "утрим"};
        for (int i2 = 0; i2 < 11; i2++) {
            if (lowerCase.contains(strArr2[i2])) {
                return "debit";
            }
        }
        return "neutral";
    }

    private static String inferPackageTitle(String str, int i) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        if (lowerCase.contains("неогранич") || lowerCase.contains("необмеж") || lowerCase.contains("интернет")) {
            return "Интернет";
        }
        if (lowerCase.contains("час") || lowerCase.contains("год") || lowerCase.contains("минут") || lowerCase.contains("хвилин")) {
            return "Звонки по Украине";
        }
        return "Пакет " + (i + 1);
    }

    private static int inferPercent(String str) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        return (lowerCase.contains("неогранич") || lowerCase.contains("необмеж")) ? 100 : 0;
    }

    private static boolean isCallDuration(String str) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        if (lowerCase.matches(".*\\d+\\s*(?:час|год|мин|хв|сек).*")) {
            return true;
        }
        return lowerCase.matches("^\\s*\\d{1,3}:\\d{2}:\\d{2}\\s*$");
    }

    private static boolean isCallHeaderRow(List<String> list) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = list.iterator();
        if (it.hasNext()) {
            while (true) {
                sb.append((CharSequence) it.next());
                if (!it.hasNext()) {
                    break;
                }
                sb.append((CharSequence) " ");
            }
        }
        String lowerCase = sb.toString().toLowerCase(Locale.ROOT);
        String[] strArr = {"дата", "время", "час", "направление", "напрямок", "номер", "длительность", "тривалість", "стоимость", "вартість"};
        int i = 0;
        for (int i2 = 0; i2 < 10; i2++) {
            if (lowerCase.contains(strArr[i2])) {
                i++;
            }
        }
        return i >= 2 && !lowerCase.matches(".*\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}.*");
    }

    private static boolean isConsentText(String str) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        if (lowerCase.contains("я согласен") || lowerCase.contains("я згоден")) {
            return true;
        }
        return (lowerCase.contains("контактн") && lowerCase.contains("данн")) || lowerCase.contains("условиями") || lowerCase.contains("умовами");
    }

    private static boolean isDurationLine(String str) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        if (lowerCase.length() > 100) {
            return false;
        }
        return lowerCase.matches(".*\\d+\\s*(?:час(?:а|ов)?|год(?:ина|ини|ин)?|минут(?:а|ы)?|хвилин(?:а|и)?|секунд(?:а|ы)?|секунди?).*");
    }

    private static boolean isExplicitFundsCredit(String str) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        return lowerCase.contains("пополн") || lowerCase.contains("поповн") || lowerCase.contains("зачисл") || lowerCase.contains("зарах") || lowerCase.contains("надход") || lowerCase.contains("приход") || lowerCase.contains("прийом грошей") || lowerCase.contains("прием денег") || lowerCase.contains("банк") || lowerCase.contains("online") || lowerCase.contains("онлайн");
    }

    private static boolean isFundsHeaderRow(List<String> list) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = list.iterator();
        if (it.hasNext()) {
            while (true) {
                sb.append((CharSequence) it.next());
                if (!it.hasNext()) {
                    break;
                }
                sb.append((CharSequence) " ");
            }
        }
        String lowerCase = sb.toString().toLowerCase(Locale.ROOT);
        String[] strArr = {"дата", "время", "час", "операц", "описан", "призначення", "приход", "расход", "дебет", "кредит", "сумма", "сума", "остаток", "залишок"};
        int i = 0;
        for (int i2 = 0; i2 < 14; i2++) {
            if (lowerCase.contains(strArr[i2])) {
                i++;
            }
        }
        return i >= 2 && !lowerCase.matches(".*\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}.*");
    }

    private static boolean isFundsStatementSummaryLabel(String str) {
        String nz = nz(str);
        return nz.contains("остаток на счету") || nz.contains("остаток на счёте") || nz.contains("залишок на рахунку") || nz.contains("всего снято") || nz.contains("всего списано") || nz.contains("всего зачислено") || nz.contains("всего пополнено") || nz.contains("усього знято") || nz.contains("усього списано") || nz.contains("усього зараховано");
    }

    private static boolean isFundsUiNoise(String str) {
        String trim = nz(str).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        return trim.isEmpty() || trim.equals("сумма, грн.") || trim.equals("сума, грн.") || trim.equals("сумма") || trim.equals("сума") || trim.equals("грн.") || trim.equals("просмотр") || trim.equals("перегляд") || trim.equals("отменить") || trim.equals("скасувати") || trim.equals("показать") || trim.equals("показати") || trim.contains("фискальные чеки") || trim.contains("фіскальні чеки") || trim.matches("(?iu)^\\d{1,2}[./-]\\d{1,2}(?:[./-]\\d{2,4})?$");
    }

    private static boolean isLikelyPackage(String str, String str2) {
        String lowerCase = (nz(str) + " " + nz(str2)).toLowerCase(Locale.ROOT);
        if (isConsentText(lowerCase)) {
            return false;
        }
        return lowerCase.contains("до ") || lowerCase.contains("неогранич") || lowerCase.contains("необмеж") || lowerCase.matches(".*\\d+\\s*(?:час|год|минут|хвилин|секунд).*");
    }

    private static boolean isLikelyServiceTitle(String str) {
        String sanitizeDisplayText = sanitizeDisplayText(str);
        if (sanitizeDisplayText.length() >= 3 && sanitizeDisplayText.length() <= 120) {
            String lowerCase = sanitizeDisplayText.toLowerCase(Locale.ROOT);
            String[] strArr = {"абонентная плата", "абонентська плата", "подключенные услуги", "підключені послуги", "доступные услуги", "доступні послуги", "управление услугами", "керування послугами", "подробнее", "детальніше", "настроить", "налаштувати", "бесплатно", "безкоштовно", "подключена", "підключена", "доступна", "отключена", "відключена"};
            for (int i = 0; i < 19; i++) {
                if (lowerCase.equals(strArr[i])) {
                    return false;
                }
            }
            if (!lowerCase.startsWith("номер:") && !lowerCase.startsWith("состояние:")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isPackageNoise(String str) {
        String lowerCase = sanitizeDisplayText(str).toLowerCase(Locale.ROOT);
        if (lowerCase.isEmpty()) {
            return true;
        }
        String[] strArr = {"предоплаченные пакетные услуги", "передплачені пакетні послуги", "пополнить", "поповнити", "перевести", "переказати", "остаток на счету", "залишок на рахунку", "доступно для перевода", "данные абонента", "дані абонента", "тарифный план", "тарифний план", "состояние", "стан", "активный", "активний", "подробнее", "детальніше"};
        for (int i = 0; i < 19; i++) {
            String str2 = strArr[i];
            if (!lowerCase.equals(str2)) {
                if (!lowerCase.startsWith(str2 + ":")) {
                }
            }
            return true;
        }
        return lowerCase.contains("number_type=") || lowerCase.contains("class=") || lowerCase.contains("style=") || isConsentText(lowerCase);
    }

    private static boolean isPackageUsageLine(String str) {
        String lowerCase = normalizePortalText(str).toLowerCase(Locale.ROOT);
        if (lowerCase.length() < 4 || lowerCase.length() > 220) {
            return false;
        }
        if (lowerCase.contains("неогранич") || lowerCase.contains("необмеж")) {
            return true;
        }
        return lowerCase.matches(".*\\d+\\s*(?:час|год|минут|хвилин|секунд|секунди).*") && (lowerCase.contains(" до ") || lowerCase.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*"));
    }

    public static boolean isRateLimitError(String str) {
        String lowerCase = nz(str).toLowerCase(Locale.ROOT);
        return lowerCase.contains("подожд") || lowerCase.contains("зачека") || lowerCase.contains("повторите через") || lowerCase.contains("спробуйте через") || lowerCase.contains("слишком много попыток") || lowerCase.contains("забагато спроб") || lowerCase.contains("количество попыток") || lowerCase.contains("кількість спроб");
    }

    private static String joinNonBlank(String str, String str2) {
        String normalizePortalText = normalizePortalText(str);
        String normalizePortalText2 = normalizePortalText(str2);
        if (blank(normalizePortalText)) {
            return normalizePortalText2;
        }
        if (blank(normalizePortalText2) || normalizePortalText.contains(normalizePortalText2)) {
            return normalizePortalText;
        }
        return normalizePortalText + " " + normalizePortalText2;
    }

    private static String joinRange(List<String> list, int i, int i2) {
        StringBuilder sb = new StringBuilder();
        while (i < i2) {
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(list.get(i));
            i++;
        }
        return sb.toString();
    }

    private static String labeled(String str, String... strArr) {
        int i;
        String[] split = nz(str).split("\\R");
        for (int i2 = 0; i2 < split.length; i2++) {
            String trim = split[i2].trim();
            for (String str2 : strArr) {
                if (!(trim.endsWith(":") ? trim.substring(0, trim.length() - 1) : trim).equalsIgnoreCase(str2)) {
                    Locale locale = Locale.ROOT;
                    if (!trim.toLowerCase(locale).startsWith((str2 + ":").toLowerCase(locale))) {
                    }
                }
                int indexOf = trim.indexOf(58);
                if (indexOf >= 0 && (i = indexOf + 1) < trim.length()) {
                    String trim2 = trim.substring(i).trim();
                    if (!trim2.isEmpty()) {
                        return trim2;
                    }
                }
                for (int i3 = i2 + 1; i3 < split.length && i3 <= i2 + 4; i3++) {
                    String trim3 = split[i3].trim();
                    if (!trim3.isEmpty()) {
                        return trim3;
                    }
                }
                return "";
            }
        }
        return "";
    }

    private static String limit(String str, int i) {
        String nz = nz(str);
        return nz.substring(0, Math.min(i, nz.length()));
    }

    public static boolean looksUnauthorized(String str, String str2) {
        if (nz(str).contains("/login/")) {
            return true;
        }
        return containsIgnoreCase(str2, "Вход в систему") && containsIgnoreCase(str2, "однораз");
    }

    private static String maskReportValue(String str) {
        return limit(normalizePortalText(str).replaceAll("(?u)[+]?\\d", "#").replaceAll("\\s+", " ").trim(), 180);
    }

    private static List<String> meaningfulLines(String str) {
        ArrayList arrayList = new ArrayList();
        for (String str2 : nz(str).split("\\R")) {
            String trim = sanitizeDisplayText(str2).replaceAll("\\s+", " ").trim();
            if (!trim.isEmpty()) {
                arrayList.add(trim);
            }
        }
        return arrayList;
    }

    private static String money(String str) {
        Matcher matcher = Pattern.compile("-?\\d+[.,]\\d{1,2}").matcher(nz(str));
        String group = matcher.find() ? matcher.group() : null;
        if (group == null) {
            Matcher matcher2 = Pattern.compile("-?\\d+").matcher(nz(str));
            if (matcher2.find()) {
                group = matcher2.group();
            }
        }
        if (group == null) {
            return orDash(str);
        }
        return group.replace(',', '.') + " грн";
    }

    private static double moneyNumber(String str) {
        try {
            return Double.parseDouble(nz(str).replace((char) 8722, '-').replace(',', '.').replaceAll("[^0-9+\\-.]", ""));
        } catch (Exception unused) {
            return 0.0d;
        }
    }

    private static String moneyOrDash(String str) {
        String money = money(str);
        return (blank(money) || "—".equals(money)) ? "—" : (!nz(str).trim().startsWith("+") || money.startsWith("+") || money.startsWith("-")) ? money : "+".concat(money);
    }

    private static String nearestServiceTitle(List<String> list, int i) {
        for (int i2 = i; i2 >= 0 && i2 >= i - 6; i2--) {
            String str = list.get(i2);
            if (isLikelyServiceTitle(str)) {
                return str;
            }
        }
        return "";
    }

    private static String nextServiceStatus(List<String> list, int i, int i2) {
        for (int i3 = i; i3 < list.size() && i3 < i + i2; i3++) {
            String extractServiceStatus = extractServiceStatus(list.get(i3));
            if (!"—".equals(extractServiceStatus)) {
                return extractServiceStatus;
            }
        }
        return "";
    }

    private static String nextValueLine(List<String> list, int i, int i2) {
        for (int i3 = i; i3 < list.size() && i3 < i + i2; i3++) {
            String str = list.get(i3);
            if (!str.isEmpty() && !equalsAnyIgnoreCase(str, "Подробнее", "Детальніше", "Настроить", "Налаштувати") && extractServiceStatus(str).equals("—")) {
                return str;
            }
        }
        return "";
    }

    private static String normalizeAccountId(String str) {
        String normalizePortalText = normalizePortalText(str);
        Matcher matcher = Pattern.compile("\\b\\d{4,12}\\b").matcher(normalizePortalText);
        return matcher.find() ? matcher.group() : orDash(normalizePortalText);
    }

    private static String normalizeAction(String str) {
        String trim = decode(nz(str)).trim();
        String str2 = "";
        if (trim.isEmpty() || "#".equals(trim)) {
            return "";
        }
        try {
            URI uri = new URI(trim);
            if (uri.isAbsolute()) {
                if ("https".equalsIgnoreCase(uri.getScheme()) && "services.intertelecom.ua".equalsIgnoreCase(uri.getHost())) {
                    String rawQuery = uri.getRawQuery();
                    StringBuilder sb = new StringBuilder();
                    sb.append(nz(uri.getRawPath()));
                    if (rawQuery != null) {
                        str2 = "?".concat(rawQuery);
                    }
                    sb.append(str2);
                    return sb.toString();
                }
                return "";
            }
        } catch (Exception unused) {
        }
        return !trim.startsWith("/") ? "/".concat(trim) : trim;
    }

    private static String normalizeCallDuration(String str) {
        String extractDurationPhrase = extractDurationPhrase(str);
        if (!blank(extractDurationPhrase)) {
            return extractDurationPhrase;
        }
        Matcher matcher = Pattern.compile("\\b\\d{1,3}:\\d{2}:\\d{2}\\b").matcher(nz(str));
        return matcher.find() ? matcher.group() : normalizePortalText(str);
    }

    private static String normalizeDuration(String str) {
        return sanitizeDisplayText(str).replaceAll("(?i)^.*?(?=\\d)", "").trim();
    }

    private static String normalizePackageTitle(String str, String str2, int i) {
        String normalizePortalText = normalizePortalText(str);
        String lowerCase = normalizePortalText.toLowerCase(Locale.ROOT);
        return (lowerCase.equals("сеть ит") || lowerCase.equals("мережа іт")) ? "Сеть ИТ" : (lowerCase.contains("украина") || lowerCase.contains("україна")) ? normalizePortalText : (blank(normalizePortalText) || isPackageNoise(normalizePortalText)) ? inferPackageTitle(str2, i) : normalizePortalText;
    }

    private static String normalizePortalHref(String str) {
        String replace = nz(str).trim().replace("&amp;", "&");
        if (replace.startsWith("//")) {
            replace = "https:".concat(replace);
        }
        String str2 = replace;
        if (str2.startsWith("https://")) {
            if (!str2.regionMatches(true, 0, "https://services.intertelecom.ua", 0, 32)) {
                return "";
            }
            str2 = str2.substring(32);
        } else if (str2.startsWith("http://")) {
            return "";
        }
        return !str2.startsWith("/") ? "" : str2;
    }

    private static String normalizePortalText(String str) {
        return sanitizeDisplayText(str).replaceAll("(?i)(?<![a-z])n{2,}(?![a-z])", " ").replaceAll("\\s+", " ").replaceAll("^[\\\"'=:;,-]+", "").replaceAll("[\\\"'=:;,-]+$", "").trim();
    }

    private static String normalizeSignedAmount(String str, String str2) {
        String trim = nz(str).replace((char) 8722, '-').replace(',', '.').trim();
        return trim.isEmpty() ? trim : (!"credit".equals(str2) || trim.startsWith("+") || trim.startsWith("-")) ? (!"debit".equals(str2) || trim.startsWith("-") || trim.startsWith("+")) ? trim : "-".concat(trim) : "+".concat(trim);
    }

    private static String normalizeSourcePath(String str) {
        String trim = nz(str).trim();
        String str2 = "";
        if (trim.isEmpty()) {
            return "";
        }
        try {
            URI uri = new URI(trim);
            if (uri.isAbsolute()) {
                if ("https".equalsIgnoreCase(uri.getScheme()) && "services.intertelecom.ua".equalsIgnoreCase(uri.getHost())) {
                    String nz = nz(uri.getRawPath());
                    if (nz.isEmpty()) {
                        nz = "/";
                    }
                    if (uri.getRawQuery() != null) {
                        str2 = "?" + uri.getRawQuery();
                    }
                    return nz.concat(str2);
                }
                return "";
            }
        } catch (Exception unused) {
        }
        return !trim.startsWith("/") ? "/".concat(trim) : trim;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String nz(String str) {
        return str == null ? "" : str;
    }

    private static String orDash(String str) {
        return blank(str) ? "—" : str.trim();
    }

    private static int packageSectionStart(String str) {
        Matcher matcher = Pattern.compile("(?iu)предоплаченные пакетные услуги|передплачені пакетні послуги").matcher(clean(str));
        if (!matcher.find()) {
            return 0;
        }
        String group = matcher.group();
        Locale locale = Locale.ROOT;
        return Math.max(0, str.toLowerCase(locale).indexOf(group.toLowerCase(locale)));
    }

    public static AuthForm parseAuthForm(String str, boolean z, String str2) {
        String str3;
        String str4;
        if (str == null || str.trim().isEmpty()) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        Matcher matcher = FORM.matcher(str);
        while (matcher.find()) {
            Map<String, String> attributes = attributes(matcher.group(1));
            String group = matcher.group(2);
            arrayList.add(new FormCandidate(attributes, group, parseControls(group)));
        }
        if (arrayList.isEmpty()) {
            arrayList.add(new FormCandidate(new LinkedHashMap(), str, parseControls(str)));
        }
        int size = arrayList.size();
        int i = Integer.MIN_VALUE;
        int i2 = 0;
        FormCandidate formCandidate = null;
        while (i2 < size) {
            Object obj = arrayList.get(i2);
            i2++;
            FormCandidate formCandidate2 = (FormCandidate) obj;
            int score = formCandidate2.score(z);
            if (score > i) {
                formCandidate = formCandidate2;
                i = score;
            }
        }
        if (formCandidate == null) {
            return null;
        }
        String findSubscriberField = formCandidate.findSubscriberField();
        String findOtpField = formCandidate.findOtpField();
        if (findSubscriberField.isEmpty()) {
            return null;
        }
        if (z && findOtpField.isEmpty()) {
            return null;
        }
        Map<String, String> successfulNonSubmitFields = formCandidate.successfulNonSubmitFields();
        Control findSubmitControl = formCandidate.findSubmitControl(z);
        String normalizeAction = normalizeAction(formCandidate.attrs.get("action"));
        String nz = nz(formCandidate.attrs.get("method"));
        if (nz.trim().isEmpty() && (formCandidate.containsAction("number_check") || formCandidate.containsAction("one_time_password_check") || formCandidate.containsAction("password_check"))) {
            nz = "post";
        }
        String lowerCase = nz(formCandidate.attrs.get("enctype")).toLowerCase(Locale.ROOT);
        if (findSubmitControl != null) {
            String normalizeAction2 = normalizeAction(findSubmitControl.attrs.get("formaction"));
            if (!normalizeAction2.isEmpty()) {
                normalizeAction = normalizeAction2;
            }
            if (!nz(findSubmitControl.attrs.get("formmethod")).trim().isEmpty()) {
                nz = findSubmitControl.attrs.get("formmethod");
            }
            if (!nz(findSubmitControl.attrs.get("formenctype")).trim().isEmpty()) {
                lowerCase = findSubmitControl.attrs.get("formenctype");
            }
            String trim = nz(findSubmitControl.attrs.get("name")).trim();
            str4 = decode(nz(findSubmitControl.attrs.get("value")));
            str3 = trim;
        } else {
            str3 = "";
            str4 = str3;
        }
        return new AuthForm(normalizeAction, normalizeSourcePath(str2), nz, lowerCase, successfulNonSubmitFields, findSubscriberField, findOtpField, "", str3, str4);
    }

    public static CallHistorySnapshot parseCallHistory(String str) {
        String nz = nz(str);
        String extractCallPeriod = extractCallPeriod(clean(nz));
        String parseCallSummary = parseCallSummary(nz);
        List<CallDetail> parseCallTablesWithHeaders = parseCallTablesWithHeaders(nz);
        if (parseCallTablesWithHeaders.isEmpty()) {
            Matcher matcher = Pattern.compile("<tr\\b[^>]*>(.*?)</tr\\s*>", FLAGS).matcher(nz);
            while (matcher.find()) {
                CallDetail callFromCells = callFromCells(htmlCells(matcher.group(1)));
                if (callFromCells != null) {
                    parseCallTablesWithHeaders.add(callFromCells);
                }
            }
        }
        if (parseCallTablesWithHeaders.isEmpty()) {
            Matcher matcher2 = Pattern.compile("<(?:li|div|article|section)\\b[^>]*class=[\\\"'][^\\\"']*(?:call|history|detail|item)[^\\\"']*[\\\"'][^>]*>(.*?)</(?:li|div|article|section)\\s*>", FLAGS).matcher(nz);
            while (matcher2.find()) {
                CallDetail callFromCells2 = callFromCells(meaningfulLines(clean(matcher2.group(1))));
                if (callFromCells2 != null) {
                    parseCallTablesWithHeaders.add(callFromCells2);
                }
            }
        }
        ArrayList arrayList = new ArrayList();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        for (CallDetail callDetail : parseCallTablesWithHeaders) {
            String trim = (callDetail.dateTime + "|" + callDetail.destination + "|" + callDetail.duration).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
            if (!trim.replace("—", "").replace("|", "").trim().isEmpty() && linkedHashSet.add(trim)) {
                arrayList.add(callDetail);
            }
        }
        return new CallHistorySnapshot(orDash(extractCallPeriod), orDash(parseCallSummary), arrayList);
    }

    public static String parseCallSummary(String str) {
        String parseOutgoingSummary = parseOutgoingSummary(clean(str));
        if (!blank(parseOutgoingSummary) && !"—".equals(parseOutgoingSummary)) {
            return parseOutgoingSummary;
        }
        Matcher matcher = Pattern.compile("(?iu)(?:\\d+\\s*(?:час(?:а|ов)?|год(?:ина|ини|ин)?)[ ,]*)?(?:\\d+\\s*(?:минут(?:а|ы)?|хвилин(?:а|и)?)[ ,]*)?\\d+\\s*(?:секунд(?:а|ы)?|секунди?)").matcher(clean(str));
        return matcher.find() ? normalizeDuration(matcher.group()) : "—";
    }

    private static List<CallDetail> parseCallTablesWithHeaders(String str) {
        CallDetail callFromMappedCells;
        ArrayList arrayList = new ArrayList();
        Matcher matcher = Pattern.compile("<table\\b[^>]*>(.*?)</table\\s*>", FLAGS).matcher(nz(str));
        while (matcher.find()) {
            Matcher matcher2 = Pattern.compile("<tr\\b[^>]*>(.*?)</tr\\s*>", FLAGS).matcher(matcher.group(1));
            List<String> list = null;
            while (matcher2.find()) {
                String group = matcher2.group(1);
                List<String> htmlCellsPreserve = htmlCellsPreserve(group);
                if (!htmlCellsPreserve.isEmpty()) {
                    if (list == null && (group.toLowerCase(Locale.ROOT).contains("<th") || isCallHeaderRow(htmlCellsPreserve))) {
                        list = htmlCellsPreserve;
                    } else if (list != null && list.size() >= 2 && (callFromMappedCells = callFromMappedCells(list, htmlCellsPreserve)) != null) {
                        arrayList.add(callFromMappedCells);
                    }
                }
            }
        }
        return arrayList;
    }

    private static List<Control> parseControls(String str) {
        String str2;
        int indexOf;
        ArrayList arrayList = new ArrayList();
        Matcher matcher = CONTROL.matcher(nz(str));
        while (matcher.find()) {
            String group = matcher.group();
            boolean regionMatches = group.regionMatches(true, 0, "<button", 0, 7);
            str2 = "";
            if (regionMatches && (indexOf = group.indexOf(62)) >= 0) {
                int i = indexOf + 1;
                String substring = group.substring(0, i);
                int lastIndexOf = group.toLowerCase(Locale.ROOT).lastIndexOf("</button");
                str2 = lastIndexOf > indexOf ? clean(group.substring(i, lastIndexOf)) : "";
                group = substring;
            }
            arrayList.add(new Control(attributes(group), str2, regionMatches));
        }
        return arrayList;
    }

    public static DashboardSnapshot parseDashboard(String str) {
        String clean = clean(str);
        String firstNonBlank = firstNonBlank(firstClassText(str, "account-balance-span"), structuredLabeled(str, "Остаток на счету", "Остаток на счёте", "Баланс"), labeled(clean, "Остаток на счету", "Остаток на счёте", "Баланс"));
        String firstNonBlank2 = firstNonBlank(firstClassText(str, "account-transfer-balance-span"), structuredLabeled(str, "Доступно для перевода", "Доступно для переказу"), labeled(clean, "Доступно для перевода", "Доступно для переказу"));
        String firstNonBlank3 = firstNonBlank(firstAttributeText(str, "tariff_id"), structuredLabeled(str, "Тарифный план", "Тарифний план"), labeled(clean, "Тарифный план", "Тарифний план"));
        String findPhone = findPhone(clean);
        String firstNonBlank4 = firstNonBlank(structuredLabeled(str, "Состояние", "Стан"), labeled(clean, "Состояние", "Стан"));
        if (blank(firstNonBlank4) && (containsIgnoreCase(clean, "Активний") || containsIgnoreCase(clean, "Активный"))) {
            firstNonBlank4 = "Активный";
        }
        String firstNonBlank5 = firstNonBlank(structuredLabeled(str, "Лицевой счет", "Лицевой счёт", "Особовий рахунок"), labeled(clean, "Лицевой счет", "Лицевой счёт", "Особовий рахунок"));
        String registrationFromNumberLine = registrationFromNumberLine(firstNonBlank(structuredLabeled(str, "Номер"), labeled(clean, "Номер")));
        String firstNonBlank6 = firstNonBlank(structuredLabeled(str, "ITPhone ID", "IT Phone ID"), labeled(clean, "ITPhone ID", "IT Phone ID"));
        String firstNonBlank7 = firstNonBlank(structuredLabeled(str, "Контактный номер телефона", "Контактний номер телефону", "Контактный номер"), labeled(clean, "Контактный номер телефона", "Контактний номер телефону", "Контактный номер"));
        String firstNonBlank8 = firstNonBlank(structuredLabeled(str, "Адрес электронной почты", "Адрес електронної пошти", "Электронная почта", "Електронна пошта", "Email"), labeled(clean, "Адрес электронной почты", "Адрес електронної пошти", "Электронная почта", "Електронна пошта", "Email"));
        String firstNonBlank9 = firstNonBlank(structuredLabeled(str, "Дата подключения", "Дата підключення"), labeled(clean, "Дата подключения", "Дата підключення"));
        String firstNonBlank10 = firstNonBlank(structuredLabeled(str, "Номер действителен до (мм.гггг)", "Номер действителен до", "Номер дійсний до"), labeled(clean, "Номер действителен до (мм.гггг)", "Номер действителен до", "Номер дійсний до"));
        String firstNonBlank11 = firstNonBlank(structuredLabeled(str, "Абонентский стаж", "Абонентський стаж"), labeled(clean, "Абонентский стаж", "Абонентський стаж"));
        String firstNonBlank12 = firstNonBlank(structuredLabeled(str, "Лояльный стаж", "Лояльний стаж"), labeled(clean, "Лояльный стаж", "Лояльний стаж"));
        List<PackageBalance> parsePackages = parsePackages(str);
        String parseOutgoingSummary = parseOutgoingSummary(clean);
        return new DashboardSnapshot(orDash(findPhone), orDash(firstNonBlank4), money(firstNonBlank), money(firstNonBlank2), orDash(firstNonBlank3), parsePackages, new SubscriberProfile(normalizeAccountId(firstNonBlank5), suffixAfterComma(firstNonBlank5), orDash(findPhone), orDash(registrationFromNumberLine), orDash(firstNonBlank3), orDash(firstNonBlank6), orDash(firstNonBlank7), orDash(firstNonBlank8), orDash(firstNonBlank9), orDash(firstNonBlank10), orDash(firstNonBlank11), orDash(firstNonBlank12)), orDash(parseOutgoingSummary));
    }

    public static FundsFlowSnapshot parseFundsFlow(String str) {
        String nz = nz(str);
        String clean = clean(nz);
        String extractFundsPeriod = extractFundsPeriod(clean);
        String firstNonBlank = firstNonBlank(structuredLabeled(nz, "Начальный остаток", "Остаток на начало", "Баланс на начало", "Остаток на счету на начало месяца", "Остаток на счёте на начало месяца", "Початковий залишок", "Залишок на початок", "Залишок на рахунку на початок місяця"), labeled(clean, "Начальный остаток", "Остаток на начало", "Баланс на начало", "Остаток на счету на начало месяца", "Остаток на счёте на начало месяца", "Початковий залишок", "Залишок на початок", "Залишок на рахунку на початок місяця"));
        String firstNonBlank2 = firstNonBlank(structuredLabeled(nz, "Пополнения", "Зачисления", "Приход", "Надходження", "Зараховано", "Всего зачислено", "Всего пополнено", "Усього зараховано"), labeled(clean, "Пополнения", "Зачисления", "Приход", "Надходження", "Зараховано", "Всего зачислено", "Всего пополнено", "Усього зараховано"));
        String firstNonBlank3 = firstNonBlank(structuredLabeled(nz, "Списания", "Расходы", "Расход", "Витрати", "Списано", "Всего снято", "Всего списано", "Усього знято", "Усього списано"), labeled(clean, "Списания", "Расходы", "Расход", "Витрати", "Списано", "Всего снято", "Всего списано", "Усього знято", "Усього списано"));
        String firstNonBlank4 = firstNonBlank(structuredLabeled(nz, "Конечный остаток", "Остаток на конец", "Баланс на конец", "Остаток на счету на конец месяца", "Остаток на счёте на конец месяца", "Кінцевий залишок", "Залишок на кінець", "Залишок на рахунку на кінець місяця", "Текущий баланс"), labeled(clean, "Конечный остаток", "Остаток на конец", "Баланс на конец", "Остаток на счету на конец месяца", "Остаток на счёте на конец месяца", "Кінцевий залишок", "Залишок на кінець", "Залишок на рахунку на кінець місяця", "Текущий баланс"));
        FundsStatementBlock parseFundsStatementBlock = parseFundsStatementBlock(clean, extractFundsPeriod);
        if (blank(firstNonBlank)) {
            firstNonBlank = parseFundsStatementBlock.opening;
        }
        if (blank(firstNonBlank2)) {
            firstNonBlank2 = parseFundsStatementBlock.credits;
        }
        if (blank(firstNonBlank3)) {
            firstNonBlank3 = parseFundsStatementBlock.debits;
        }
        if (blank(firstNonBlank4)) {
            firstNonBlank4 = parseFundsStatementBlock.closing;
        }
        ArrayList arrayList = new ArrayList();
        if (parseFundsStatementBlock.recognized) {
            arrayList.addAll(parseFundsStatementBlock.transactions);
        } else {
            arrayList.addAll(parseFundsTablesWithHeaders(nz));
            if (arrayList.isEmpty()) {
                Matcher matcher = Pattern.compile("<tr\\b[^>]*>(.*?)</tr\\s*>", FLAGS).matcher(nz);
                while (matcher.find()) {
                    FundsTransaction fundsFromCells = fundsFromCells(htmlCells(matcher.group(1)));
                    if (fundsFromCells != null) {
                        arrayList.add(fundsFromCells);
                    }
                }
            }
            if (arrayList.isEmpty()) {
                Matcher matcher2 = Pattern.compile("<(?:li|div|article|section)\\b[^>]*class=[\\\"'][^\\\"']*(?:transaction|payment|fund|money|operation|history|item)[^\\\"']*[\\\"'][^>]*>(.*?)</(?:li|div|article|section)\\s*>", FLAGS).matcher(nz);
                while (matcher2.find()) {
                    FundsTransaction fundsFromCells2 = fundsFromCells(meaningfulLines(clean(matcher2.group(1))));
                    if (fundsFromCells2 != null) {
                        arrayList.add(fundsFromCells2);
                    }
                }
            }
        }
        ArrayList arrayList2 = new ArrayList();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        int size = arrayList.size();
        int i = 0;
        while (i < size) {
            Object obj = arrayList.get(i);
            i++;
            FundsTransaction fundsTransaction = (FundsTransaction) obj;
            String trim = (fundsTransaction.dateTime + "|" + fundsTransaction.description + "|" + fundsTransaction.amount + "|" + fundsTransaction.balanceAfter).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
            if (!trim.replace("—", "").replace("|", "").trim().isEmpty() && linkedHashSet.add(trim)) {
                arrayList2.add(fundsTransaction);
            }
        }
        if (blank(firstNonBlank2)) {
            firstNonBlank2 = sumFunds(arrayList2, true);
        }
        if (blank(firstNonBlank3)) {
            firstNonBlank3 = sumFunds(arrayList2, false);
        }
        return new FundsFlowSnapshot(orDash(extractFundsPeriod), moneyOrDash(firstNonBlank), moneyOrDash(firstNonBlank2), moneyOrDash(firstNonBlank3), moneyOrDash(firstNonBlank4), arrayList2);
    }

    private static FundsStatementBlock parseFundsStatementBlock(String str, String str2) {
        String nz = nz(str);
        int indexOfIgnoreCase = indexOfIgnoreCase(nz, "Движение средств за");
        if (indexOfIgnoreCase < 0) {
            indexOfIgnoreCase = indexOfIgnoreCase(nz, "Рух коштів за");
        }
        int i = 0;
        boolean z = indexOfIgnoreCase >= 0;
        if (indexOfIgnoreCase >= 0) {
            nz = nz.substring(indexOfIgnoreCase);
        }
        int indexOfIgnoreCase2 = indexOfIgnoreCase(nz, "Фискальные чеки");
        if (indexOfIgnoreCase2 < 0) {
            indexOfIgnoreCase2 = indexOfIgnoreCase(nz, "Фіскальні чеки");
        }
        if (indexOfIgnoreCase2 > 0) {
            nz = nz.substring(0, indexOfIgnoreCase2);
        }
        int indexOfIgnoreCase3 = indexOfIgnoreCase(nz, "ИНФОРМАЦИОННАЯ ПОДДЕРЖКА");
        if (indexOfIgnoreCase3 < 0) {
            indexOfIgnoreCase3 = indexOfIgnoreCase(nz, "ІНФОРМАЦІЙНА ПІДТРИМКА");
        }
        if (indexOfIgnoreCase3 > 0) {
            nz = nz.substring(0, indexOfIgnoreCase3);
        }
        List<String> meaningfulLines = meaningfulLines(nz);
        String amountAfterLabel = amountAfterLabel(meaningfulLines, "остаток на счету на начало месяца", "остаток на счёте на начало месяца", "залишок на рахунку на початок місяця");
        String amountAfterLabel2 = amountAfterLabel(meaningfulLines, "всего зачислено", "всего пополнено", "усього зараховано", "усього поповнено", "всего начислено");
        String amountAfterLabel3 = amountAfterLabel(meaningfulLines, "всего снято", "всего списано", "усього знято", "усього списано");
        String amountAfterLabel4 = amountAfterLabel(meaningfulLines, "остаток на счету на конец месяца", "остаток на счёте на конец месяца", "залишок на рахунку на кінець місяця");
        ArrayList arrayList = new ArrayList();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        int i2 = 1;
        while (i2 < meaningfulLines.size()) {
            List<String> extractMoneyTokens = extractMoneyTokens(meaningfulLines.get(i2));
            if (!extractMoneyTokens.isEmpty()) {
                String str3 = meaningfulLines.get(i2 - 1);
                Locale locale = Locale.ROOT;
                String lowerCase = str3.toLowerCase(locale);
                if (!isFundsStatementSummaryLabel(lowerCase) && !lowerCase.contains("движение средств за") && !lowerCase.contains("рух коштів за") && !lowerCase.equals("за период:") && !lowerCase.equals("за період:") && !str3.matches("(?iu).*(?:остаток|залишок|всего|усього).*") && !isFundsUiNoise(str3)) {
                    String str4 = extractMoneyTokens.get(i);
                    String inferFundsType = inferFundsType(str3 + " " + str4, extractMoneyTokens);
                    if ("neutral".equals(inferFundsType)) {
                        inferFundsType = moneyNumber(str4) < 0.0d ? "debit" : "credit";
                    }
                    if (!"credit".equals(inferFundsType) || isExplicitFundsCredit(str3)) {
                        String normalizeSignedAmount = normalizeSignedAmount(str4, inferFundsType);
                        if (linkedHashSet.add((str3 + "|" + normalizeSignedAmount).toLowerCase(locale))) {
                            arrayList.add(new FundsTransaction(orDash(str2), limit(str3, 260), inferFundsCategory(str3, inferFundsType), normalizeSignedAmount, "—", inferFundsType));
                        }
                    }
                }
            }
            i2++;
            i = 0;
        }
        if (blank(amountAfterLabel2)) {
            amountAfterLabel2 = sumFunds(arrayList, true);
        }
        String str5 = amountAfterLabel2;
        if (blank(amountAfterLabel3)) {
            amountAfterLabel3 = sumFunds(arrayList, false);
        }
        return new FundsStatementBlock(z, amountAfterLabel, str5, amountAfterLabel3, amountAfterLabel4, arrayList);
    }

    private static List<FundsTransaction> parseFundsTablesWithHeaders(String str) {
        FundsTransaction fundsFromMappedCells;
        ArrayList arrayList = new ArrayList();
        Matcher matcher = Pattern.compile("<table\\b[^>]*>(.*?)</table\\s*>", FLAGS).matcher(nz(str));
        while (matcher.find()) {
            Matcher matcher2 = Pattern.compile("<tr\\b[^>]*>(.*?)</tr\\s*>", FLAGS).matcher(matcher.group(1));
            List<String> list = null;
            while (matcher2.find()) {
                String group = matcher2.group(1);
                List<String> htmlCellsPreserve = htmlCellsPreserve(group);
                if (!htmlCellsPreserve.isEmpty()) {
                    if (list == null && (group.toLowerCase(Locale.ROOT).contains("<th") || isFundsHeaderRow(htmlCellsPreserve))) {
                        list = htmlCellsPreserve;
                    } else if (list != null && list.size() >= 2 && (fundsFromMappedCells = fundsFromMappedCells(list, htmlCellsPreserve)) != null) {
                        arrayList.add(fundsFromMappedCells);
                    }
                }
            }
        }
        return arrayList;
    }

    private static String parseOutgoingSummary(String str) {
        List<String> meaningfulLines = meaningfulLines(str);
        int i = 0;
        while (true) {
            if (i >= meaningfulLines.size()) {
                i = -1;
                break;
            }
            String lowerCase = meaningfulLines.get(i).toLowerCase(Locale.ROOT);
            if (lowerCase.contains("исходящие звонки") || lowerCase.contains("вихідні дзвінки")) {
                break;
            }
            i++;
        }
        int i2 = i >= 0 ? i + 1 : 0;
        int min = i >= 0 ? Math.min(meaningfulLines.size(), i + 40) : meaningfulLines.size();
        for (int i3 = i2; i3 < min; i3++) {
            String lowerCase2 = meaningfulLines.get(i3).toLowerCase(Locale.ROOT);
            if (lowerCase2.contains("украина (моб") || lowerCase2.contains("україна (моб")) {
                for (int i4 = i3 + 1; i4 < Math.min(min, i3 + 6); i4++) {
                    String extractDurationPhrase = extractDurationPhrase(meaningfulLines.get(i4));
                    if (!blank(extractDurationPhrase)) {
                        return extractDurationPhrase;
                    }
                }
            }
        }
        while (i2 < min) {
            String extractDurationPhrase2 = extractDurationPhrase(meaningfulLines.get(i2));
            if (!blank(extractDurationPhrase2) && !meaningfulLines.get(i2).toLowerCase(Locale.ROOT).contains(" до ")) {
                return extractDurationPhrase2;
            }
            i2++;
        }
        return "—";
    }

    private static List<PackageBalance> parsePackages(String str) {
        String str2;
        ArrayList arrayList = new ArrayList();
        String nz = nz(str);
        Matcher matcher = Pattern.compile("<(?:div|span)\\b[^>]*class=[\\\"'][^\\\"']*progress-bar[^\\\"']*[\\\"'][^>]*>", FLAGS).matcher(nz);
        int packageSectionStart = packageSectionStart(nz);
        int i = 0;
        int i2 = 0;
        while (true) {
            String str3 = "";
            if (!matcher.find()) {
                break;
            }
            int progressPercent = progressPercent(attributes(matcher.group()));
            String substring = nz.substring(Math.max(packageSectionStart, Math.max(i, matcher.start() - 1400)), matcher.start());
            int end = matcher.end();
            List<String> meaningfulLines = meaningfulLines(clean(substring));
            int size = meaningfulLines.size() - 1;
            while (true) {
                if (size < 0) {
                    size = -1;
                    break;
                }
                String normalizePortalText = normalizePortalText(meaningfulLines.get(size));
                if (isPackageUsageLine(normalizePortalText) && !isConsentText(normalizePortalText)) {
                    break;
                }
                size--;
            }
            if (size < 0) {
                i2++;
                packageSectionStart = end;
            } else {
                String normalizePortalText2 = normalizePortalText(meaningfulLines.get(size));
                int i3 = size - 1;
                while (true) {
                    if (i3 < 0 || i3 < size - 6) {
                        break;
                    }
                    String normalizePortalText3 = normalizePortalText(meaningfulLines.get(i3));
                    if (!isPackageNoise(normalizePortalText3) && !isPackageUsageLine(normalizePortalText3) && normalizePortalText3.length() <= 100) {
                        str3 = normalizePortalText3;
                        break;
                    }
                    i3--;
                }
                String normalizePackageTitle = normalizePackageTitle(str3, normalizePortalText2, i2);
                if (isLikelyPackage(normalizePackageTitle, normalizePortalText2)) {
                    if (!isConsentText(normalizePackageTitle + " " + normalizePortalText2)) {
                        arrayList.add(new PackageBalance(limit(normalizePackageTitle, 90), limit(normalizePortalText2, 180), progressPercent));
                    }
                }
                i2++;
                packageSectionStart = end;
                i = 0;
            }
        }
        if (arrayList.isEmpty()) {
            List<String> meaningfulLines2 = meaningfulLines(clean(nz));
            int i4 = 0;
            for (int i5 = 0; i5 < meaningfulLines2.size() && i4 < 4; i5++) {
                String normalizePortalText4 = normalizePortalText(meaningfulLines2.get(i5));
                if (isPackageUsageLine(normalizePortalText4) && !isConsentText(normalizePortalText4)) {
                    for (int i6 = i5 - 1; i6 >= 0 && i6 >= i5 - 5; i6--) {
                        str2 = normalizePortalText(meaningfulLines2.get(i6));
                        if (!isPackageNoise(str2) && !isPackageUsageLine(str2)) {
                            break;
                        }
                    }
                    str2 = "";
                    arrayList.add(new PackageBalance(limit(normalizePackageTitle(str2, normalizePortalText4, i4), 90), limit(normalizePortalText4, 180), inferPercent(normalizePortalText4)));
                    i4++;
                }
            }
        }
        ArrayList arrayList2 = new ArrayList();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        int size2 = arrayList.size();
        int i7 = 0;
        while (i7 < size2) {
            Object obj = arrayList.get(i7);
            i7++;
            PackageBalance packageBalance = (PackageBalance) obj;
            if (linkedHashSet.add((packageBalance.title + "|" + packageBalance.subtitle).toLowerCase(Locale.ROOT).replaceAll("\\s+", " "))) {
                arrayList2.add(packageBalance);
            }
        }
        return arrayList2;
    }

    public static AuthForm parsePasswordAuthForm(String str, String str2) {
        String str3;
        String str4;
        if (str != null && !str.trim().isEmpty()) {
            ArrayList arrayList = new ArrayList();
            Matcher matcher = FORM.matcher(str);
            while (matcher.find()) {
                Map<String, String> attributes = attributes(matcher.group(1));
                String group = matcher.group(2);
                arrayList.add(new FormCandidate(attributes, group, parseControls(group)));
            }
            if (arrayList.isEmpty()) {
                arrayList.add(new FormCandidate(new LinkedHashMap(), str, parseControls(str)));
            }
            int size = arrayList.size();
            int i = Integer.MIN_VALUE;
            int i2 = 0;
            FormCandidate formCandidate = null;
            while (i2 < size) {
                Object obj = arrayList.get(i2);
                i2++;
                FormCandidate formCandidate2 = (FormCandidate) obj;
                int passwordScore = formCandidate2.passwordScore();
                if (passwordScore > i) {
                    formCandidate = formCandidate2;
                    i = passwordScore;
                }
            }
            if (formCandidate == null) {
                return null;
            }
            String findSubscriberField = formCandidate.findSubscriberField();
            String findPersonalPasswordField = formCandidate.findPersonalPasswordField();
            if (!findSubscriberField.isEmpty() && !findPersonalPasswordField.isEmpty()) {
                Map<String, String> successfulNonSubmitFields = formCandidate.successfulNonSubmitFields();
                Control findPasswordSubmitControl = formCandidate.findPasswordSubmitControl();
                String normalizeAction = normalizeAction(formCandidate.attrs.get("action"));
                String nz = nz(formCandidate.attrs.get("method"));
                if (nz.trim().isEmpty() && formCandidate.containsAction("password_check")) {
                    nz = "post";
                }
                String lowerCase = nz(formCandidate.attrs.get("enctype")).toLowerCase(Locale.ROOT);
                if (findPasswordSubmitControl != null) {
                    String normalizeAction2 = normalizeAction(findPasswordSubmitControl.attrs.get("formaction"));
                    if (!normalizeAction2.isEmpty()) {
                        normalizeAction = normalizeAction2;
                    }
                    if (!nz(findPasswordSubmitControl.attrs.get("formmethod")).trim().isEmpty()) {
                        nz = findPasswordSubmitControl.attrs.get("formmethod");
                    }
                    if (!nz(findPasswordSubmitControl.attrs.get("formenctype")).trim().isEmpty()) {
                        lowerCase = findPasswordSubmitControl.attrs.get("formenctype");
                    }
                    String trim = nz(findPasswordSubmitControl.attrs.get("name")).trim();
                    str4 = decode(nz(findPasswordSubmitControl.attrs.get("value")));
                    str3 = trim;
                } else {
                    str3 = "";
                    str4 = str3;
                }
                return new AuthForm(normalizeAction, normalizeSourcePath(str2), nz, lowerCase, successfulNonSubmitFields, findSubscriberField, "", findPersonalPasswordField, str3, str4);
            }
        }
        return null;
    }

    private static int parsePercent(String str) {
        try {
            return Math.max(0, Math.min(100, (int) Math.round(Double.parseDouble(nz(str).replace(',', '.')))));
        } catch (Exception unused) {
            return 0;
        }
    }

    public static List<ServiceItem> parseServices(String str) {
        ArrayList arrayList = new ArrayList();
        String nz = nz(str);
        Matcher matcher = Pattern.compile("<tr\\b[^>]*>(.*?)</tr\\s*>", FLAGS).matcher(nz);
        int i = 0;
        int i2 = 0;
        while (matcher.find()) {
            List<String> htmlCells = htmlCells(matcher.group(1));
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = htmlCells.iterator();
            if (it.hasNext()) {
                while (true) {
                    sb.append((CharSequence) it.next());
                    if (!it.hasNext()) {
                        break;
                    }
                    sb.append((CharSequence) "\n");
                }
            }
            String sb2 = sb.toString();
            if (containsIgnoreCase(sb2, "Абонентная плата") || containsIgnoreCase(sb2, "Абонентська плата") || !"—".equals(extractServiceStatus(sb2))) {
                String nearestServiceTitle = nearestServiceTitle(htmlCells, Math.max(0, htmlCells.size() - 1));
                if (!isLikelyServiceTitle(nearestServiceTitle)) {
                    nearestServiceTitle = titleBeforeFee(sb2);
                }
                if (isLikelyServiceTitle(nearestServiceTitle)) {
                    arrayList.add(new ServiceItem("table-service-" + i2, cleanServiceTitle(nearestServiceTitle), extractFee(sb2), extractServiceStatus(sb2), limit(sb2, 420)));
                    i2++;
                }
            }
        }
        Matcher matcher2 = Pattern.compile("<(?:div|section|article|li)[^>]+class=[\\\"'][^\\\"']*(?:thumbnail|service|packet|well)[^\\\"']*[\\\"'][^>]*>(.*?)</(?:div|section|article|li)\\s*>", FLAGS).matcher(nz);
        int i3 = 0;
        while (matcher2.find()) {
            String group = matcher2.group(1);
            String sanitizeDisplayText = sanitizeDisplayText(clean(group));
            String firstHeadingOrLink = firstHeadingOrLink(group);
            if (blank(firstHeadingOrLink)) {
                firstHeadingOrLink = titleBeforeFee(sanitizeDisplayText);
            }
            if (isLikelyServiceTitle(firstHeadingOrLink)) {
                Matcher matcher3 = Pattern.compile("(?:service[_-]?id|data-service-id|id)\\s*=\\s*[\\\"']?([0-9]+)", 2).matcher(group);
                arrayList.add(new ServiceItem(matcher3.find() ? matcher3.group(1) : "service-" + i3, cleanServiceTitle(firstHeadingOrLink), extractFee(sanitizeDisplayText), extractServiceStatus(sanitizeDisplayText), limit(sanitizeDisplayText, 420)));
            }
            i3++;
        }
        if (arrayList.isEmpty()) {
            List<String> meaningfulLines = meaningfulLines(clean(nz));
            int i4 = 0;
            for (int i5 = 0; i5 < meaningfulLines.size(); i5++) {
                if (equalsAnyIgnoreCase(meaningfulLines.get(i5), "Абонентная плата", "Абонентська плата")) {
                    String nearestServiceTitle2 = nearestServiceTitle(meaningfulLines, i5 - 1);
                    int i6 = i5 + 1;
                    String nextValueLine = nextValueLine(meaningfulLines, i6, 4);
                    String nextServiceStatus = nextServiceStatus(meaningfulLines, i6, 8);
                    if (isLikelyServiceTitle(nearestServiceTitle2)) {
                        String joinRange = joinRange(meaningfulLines, Math.max(0, i5 - 2), Math.min(meaningfulLines.size(), i5 + 8));
                        StringBuilder sb3 = new StringBuilder("service-");
                        int i7 = i4 + 1;
                        sb3.append(i4);
                        arrayList.add(new ServiceItem(sb3.toString(), cleanServiceTitle(nearestServiceTitle2), blank(nextValueLine) ? "—" : nextValueLine, blank(nextServiceStatus) ? "—" : nextServiceStatus, limit(joinRange, 420)));
                        i4 = i7;
                    }
                }
            }
        }
        ArrayList arrayList2 = new ArrayList();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        int size = arrayList.size();
        while (i < size) {
            Object obj = arrayList.get(i);
            i++;
            ServiceItem serviceItem = (ServiceItem) obj;
            String trim = serviceItem.title.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
            if (!trim.isEmpty() && linkedHashSet.add(trim)) {
                arrayList2.add(serviceItem);
            }
        }
        return arrayList2;
    }

    public static SubscriberProfile parseSubscriberProfile(String str, String str2, String str3) {
        String clean = clean(str);
        String firstNonBlank = firstNonBlank(structuredLabeled(str, "Лицевой счет", "Лицевой счёт", "Особовий рахунок"), labeled(clean, "Лицевой счет", "Лицевой счёт", "Особовий рахунок"));
        String firstNonBlank2 = firstNonBlank(structuredLabeled(str, "Номер"), labeled(clean, "Номер"));
        String findPhone = findPhone(firstNonBlank2);
        if (blank(findPhone)) {
            findPhone = findPhone(clean);
        }
        if (blank(findPhone)) {
            findPhone = str2;
        }
        String registrationFromNumberLine = registrationFromNumberLine(firstNonBlank2);
        if (blank(registrationFromNumberLine)) {
            registrationFromNumberLine = firstNonBlank(structuredLabeled(str, "Регистрация", "Реєстрація", "Статус регистрации"), labeled(clean, "Регистрация", "Реєстрація", "Статус регистрации"));
        }
        String firstNonBlank3 = firstNonBlank(structuredLabeled(str, "Тарифный план", "Тарифний план"), labeled(clean, "Тарифный план", "Тарифний план"));
        if (blank(firstNonBlank3)) {
            firstNonBlank3 = str3;
        }
        return new SubscriberProfile(normalizeAccountId(firstNonBlank), suffixAfterComma(firstNonBlank), orDash(findPhone), orDash(registrationFromNumberLine), orDash(firstNonBlank3), orDash(firstNonBlank(structuredLabeled(str, "ITPhone ID", "IT Phone ID"), labeled(clean, "ITPhone ID", "IT Phone ID"))), orDash(firstNonBlank(structuredLabeled(str, "Контактный номер телефона", "Контактний номер телефону", "Контактный номер"), labeled(clean, "Контактный номер телефона", "Контактний номер телефону", "Контактный номер"))), orDash(firstNonBlank(structuredLabeled(str, "Адрес электронной почты", "Адрес електронної пошти", "Электронная почта", "Електронна пошта", "Email"), labeled(clean, "Адрес электронной почты", "Адрес електронної пошти", "Электронная почта", "Електронна пошта", "Email"))), orDash(firstNonBlank(structuredLabeled(str, "Дата подключения", "Дата підключення"), labeled(clean, "Дата подключения", "Дата підключення"))), orDash(firstNonBlank(structuredLabeled(str, "Номер действителен до (мм.гггг)", "Номер действителен до", "Номер дійсний до"), labeled(clean, "Номер действителен до (мм.гггг)", "Номер действителен до", "Номер дійсний до"))), orDash(firstNonBlank(structuredLabeled(str, "Абонентский стаж", "Абонентський стаж"), labeled(clean, "Абонентский стаж", "Абонентський стаж"))), orDash(firstNonBlank(structuredLabeled(str, "Лояльный стаж", "Лояльний стаж"), labeled(clean, "Лояльный стаж", "Лояльний стаж"))));
    }

    private static int progressPercent(Map<String, String> map) {
        String firstNonBlank = firstNonBlank(map.get("aria-valuenow"), map.get("data-value"), map.get("data-percent"));
        if (!blank(firstNonBlank)) {
            return parsePercent(firstNonBlank.replace("%", ""));
        }
        Matcher matcher = Pattern.compile("(?i)width\\s*:\\s*([0-9]+(?:[.,][0-9]+)?)%").matcher(nz(map.get("style")));
        if (matcher.find()) {
            return parsePercent(matcher.group(1));
        }
        return 0;
    }

    private static String registrationFromNumberLine(String str) {
        int i;
        String normalizePortalText = normalizePortalText(str);
        int indexOf = normalizePortalText.indexOf(44);
        return (indexOf < 0 || (i = indexOf + 1) >= normalizePortalText.length()) ? "" : normalizePortalText.substring(i).trim();
    }

    private static String safeReportPath(String str) {
        String trim = nz(str).trim();
        try {
            URI uri = new URI(trim);
            if (uri.isAbsolute()) {
                return nz(uri.getRawPath());
            }
        } catch (Exception unused) {
        }
        int indexOf = trim.indexOf(63);
        return indexOf >= 0 ? trim.substring(0, indexOf) : limit(trim, 120);
    }

    private static String sanitizeDisplayText(String str) {
        return nz(str).replace("\\r", " ").replace("\\n", "\n").replaceAll("(?i)(?<![a-z])n{2,}(?![a-z])", "\n").replaceAll("(?i)\\b(?:class|style|title|href|id|name|value|type|role|number_type|data-[a-z0-9_-]+|aria-[a-z0-9_-]+)\\s*=\\s*(?:[\\\"'][^\\\"']*[\\\"']|[^\\s>]+)", " ").replaceAll("(?i)\\b(?:onclick|onchange|onsubmit)\\s*=\\s*(?:[\\\"'][^\\\"']*[\\\"']|[^\\s>]+)", " ").replaceAll("(?i)^[\\s\\\"']*(?:all|div|span|input|button|a)[\\s\\\"']+", "").replaceAll("[<>]", " ").replaceAll("[ \\t\\x0B\\f\\r]+", " ").replaceAll(" *\\n *", "\n").replaceAll("\\n{3,}", "\n\n").trim();
    }

    private static String structuredLabeled(String str, String... strArr) {
        String nz = nz(str);
        for (String str2 : strArr) {
            String quote = Pattern.quote(str2);
            Matcher matcher = Pattern.compile("(?is)<[^>]+>\\s*" + quote + "\\s*:?[\\s\\u00a0]*</[^>]+>\\s*<[^>]+>(.*?)</[^>]+>").matcher(nz);
            if (matcher.find()) {
                String normalizePortalText = normalizePortalText(clean(matcher.group(1)));
                if (!blank(normalizePortalText)) {
                    return normalizePortalText;
                }
            }
            Matcher matcher2 = Pattern.compile("(?iu)" + quote + "\\s*:\\s*([^\\n<]{1,180})").matcher(clean(nz));
            if (matcher2.find()) {
                String normalizePortalText2 = normalizePortalText(matcher2.group(1));
                if (!blank(normalizePortalText2)) {
                    return normalizePortalText2;
                }
            }
        }
        return "";
    }

    private static String suffixAfterComma(String str) {
        int i;
        String normalizePortalText = normalizePortalText(str);
        int indexOf = normalizePortalText.indexOf(44);
        return (indexOf < 0 || (i = indexOf + 1) >= normalizePortalText.length()) ? "—" : orDash(normalizePortalText.substring(i).trim());
    }

    private static String sumFunds(List<FundsTransaction> list, boolean z) {
        double d = 0.0d;
        boolean z2 = false;
        for (FundsTransaction fundsTransaction : list) {
            if ((z && fundsTransaction.isCredit()) || (!z && fundsTransaction.isDebit())) {
                z2 = true;
                d = Math.abs(moneyNumber(fundsTransaction.amount)) + d;
            }
        }
        if (z2) {
            return String.format(Locale.US, "%s%.2f грн", z ? "+" : "-", Double.valueOf(d));
        }
        return "";
    }

    private static String titleBeforeFee(String str) {
        List<String> meaningfulLines = meaningfulLines(str);
        for (int i = 0; i < meaningfulLines.size(); i++) {
            if (equalsAnyIgnoreCase(meaningfulLines.get(i), "Абонентная плата", "Абонентська плата")) {
                return nearestServiceTitle(meaningfulLines, i - 1);
            }
        }
        return "";
    }

    private static String value(Map<String, Map<String, String>> map, String str) {
        Map<String, String> map2 = map.get(str);
        return map2 == null ? "" : nz(map2.get("value"));
    }

    public static AuthForm parseAuthForm(String str, boolean z) {
        return parseAuthForm(str, z, "");
    }

    public static AuthForm parseAuthForm(String str) {
        return parseAuthForm(str, false, "");
    }
}
