package ua.telecomplus.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class DashboardSnapshot {
    final String balance;
    final String outgoingSummary;
    final List<PackageBalance> packages;
    final String phoneNumber;
    final SubscriberProfile profile;
    final String status;
    final String tariffName;
    final String transferableBalance;
    final long updatedAt;

    public DashboardSnapshot(String str, String str2, String str3, String str4, String str5, List<PackageBalance> list, SubscriberProfile subscriberProfile, String str6, long j) {
        this.phoneNumber = safe(str);
        this.status = safe(str2);
        this.balance = safe(str3);
        this.transferableBalance = safe(str4);
        this.tariffName = safe(str5);
        this.packages = list == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(new ArrayList(list));
        this.profile = subscriberProfile == null ? new SubscriberProfile() : subscriberProfile;
        this.outgoingSummary = safe(str6);
        this.updatedAt = j;
    }

    private static String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "—" : str.trim();
    }

    public DashboardSnapshot(String str, String str2, String str3, String str4, String str5, List<PackageBalance> list, SubscriberProfile subscriberProfile, String str6) {
        this(str, str2, str3, str4, str5, list, subscriberProfile, str6, System.currentTimeMillis());
    }
}
