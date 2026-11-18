package ua.telecomplus.app;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class PackageBalance {
    final int progressPercent;
    final String subtitle;
    final String title;

    public PackageBalance(String str, String str2, int i) {
        this.title = value(str);
        this.subtitle = value(str2);
        this.progressPercent = Math.max(0, Math.min(100, i));
    }

    private static String value(String str) {
        return (str == null || str.trim().isEmpty()) ? "—" : str.trim();
    }
}
