package ua.telecomplus.app;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class AuthForm {
    final String actionPath;
    final String enctype;
    final Map<String, String> fields;
    final String method;
    final String otpName;
    final String passwordName;
    final String sourcePath;
    final String submitName;
    final String submitValue;
    final String subscriberName;

    public AuthForm(String str, String str2, String str3, String str4, Map<String, String> map, String str5, String str6, String str7, String str8, String str9) {
        this.actionPath = nz(str);
        this.sourcePath = nz(str2);
        this.method = normalizeMethod(str3);
        this.enctype = nz(str4);
        this.fields = map == null ? Collections.EMPTY_MAP : Collections.unmodifiableMap(new LinkedHashMap(map));
        this.subscriberName = nz(str5);
        this.otpName = nz(str6);
        this.passwordName = nz(str7);
        this.submitName = nz(str8);
        this.submitValue = nz(str9);
    }

    private static String normalizeMethod(String str) {
        return "post".equalsIgnoreCase(nz(str).trim()) ? "POST" : "GET";
    }

    private static String nz(String str) {
        return str == null ? "" : str;
    }

    public String field(String str) {
        String str2 = this.fields.get(str);
        return str2 == null ? "" : str2;
    }

    public String targetPath() {
        return this.actionPath.trim().isEmpty() ? this.sourcePath : this.actionPath;
    }
}
