package ua.telecomplus.app;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class CallDetail {
    final String cost;
    final String dateTime;
    final String destination;
    final String direction;
    final String duration;

    public CallDetail(String str, String str2, String str3, String str4, String str5) {
        this.dateTime = safe(str);
        this.direction = safe(str2);
        this.destination = safe(str3);
        this.duration = safe(str4);
        this.cost = safe(str5);
    }

    private static String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "—" : str.trim();
    }
}
