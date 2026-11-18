package ua.telecomplus.app;

import java.util.Locale;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class ServiceItem {
    final String details;
    final String fee;
    final String id;
    final String status;
    final String title;

    public ServiceItem(String str, String str2, String str3, String str4, String str5) {
        this.id = safe(str);
        this.title = safe(str2);
        this.fee = safe(str3);
        this.status = safe(str4);
        this.details = str5 == null ? "" : str5.trim();
    }

    private static String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "—" : str.trim();
    }

    public boolean isAvailable() {
        String lowerCase = this.status.toLowerCase(Locale.ROOT);
        return lowerCase.contains("доступ") || lowerCase.contains("отключ") || lowerCase.contains("відключ") || lowerCase.contains("не подключ") || lowerCase.contains("не підключ");
    }

    public boolean isConnected() {
        String lowerCase = this.status.toLowerCase(Locale.ROOT);
        return ((!lowerCase.contains("подключ") && !lowerCase.contains("підключ") && !lowerCase.contains("актив")) || lowerCase.contains("не подключ") || lowerCase.contains("не підключ") || lowerCase.contains("отключ")) ? false : true;
    }

    public ServiceItem(String str, String str2, String str3, String str4) {
        this(str, str2, str3, str4, "");
    }
}
