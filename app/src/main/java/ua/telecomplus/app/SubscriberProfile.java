package ua.telecomplus.app;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class SubscriberProfile {
    final String accountId;
    final String connectionDate;
    final String contactPhone;
    final String contractStatus;
    final String email;
    final String itPhoneId;
    final String loyaltyTenure;
    final String phoneNumber;
    final String registrationStatus;
    final String subscriberTenure;
    final String tariffName;
    final String validUntil;

    public SubscriberProfile(String str, String str2, String str3, String str4, String str5, String str6, String str7, String str8, String str9, String str10, String str11, String str12) {
        this.accountId = safe(str);
        this.phoneNumber = safe(str3);
        this.contractStatus = safe(str2);
        this.registrationStatus = safe(str4);
        this.tariffName = safe(str5);
        this.itPhoneId = safe(str6);
        this.contactPhone = safe(str7);
        this.email = safe(str8);
        this.connectionDate = safe(str9);
        this.validUntil = safe(str10);
        this.subscriberTenure = safe(str11);
        this.loyaltyTenure = safe(str12);
    }

    private static boolean present(String str) {
        return (str == null || str.trim().isEmpty() || "—".equals(str.trim())) ? false : true;
    }

    private static String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "—" : str.trim();
    }

    public int completeness() {
        String[] f = {this.accountId, this.phoneNumber, this.registrationStatus, this.tariffName, this.itPhoneId, this.contactPhone, this.email, this.connectionDate, this.validUntil, this.subscriberTenure, this.loyaltyTenure};
        int n = 0;
        for (String x : f) {
            if (present(x)) {
                n++;
            }
        }
        return n;
    }

    public SubscriberProfile(String str, String str2, String str3, String str4, String str5, String str6) {
        this(str, "—", str2, str3, str4, str5, str6, "—", "—", "—", "—", "—");
    }

    public SubscriberProfile() {
        this("—", "—", "—", "—", "—", "—", "—", "—", "—", "—", "—", "—");
    }
}
