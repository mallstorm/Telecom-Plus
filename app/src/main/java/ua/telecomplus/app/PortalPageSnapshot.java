package ua.telecomplus.app;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class PortalPageSnapshot {
    final String cookieHeader;
    final String finalUrl;
    final String html;
    final String safeReport;

    public PortalPageSnapshot(String str, String str2, String str3, String str4) {
        this.finalUrl = str == null ? "" : str;
        this.html = str2 == null ? "" : str2;
        this.safeReport = str3 == null ? "" : str3;
        this.cookieHeader = str4 == null ? "" : str4;
    }
}
