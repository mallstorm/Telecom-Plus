package ua.telecomplus.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class PortalCacheCodec {
    private static final String CALLS_VERSION = "C1";
    private static final String DASHBOARD_VERSION = "D2";
    private static final String FUNDS_VERSION = "F1";
    private static final String SERVICES_VERSION = "S1";

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class Reader {
        private int offset;
        private final String source;

        public Reader(String str) {
            this.source = str == null ? "" : str;
        }

        public String next() {
            int indexOf = this.source.indexOf(58, this.offset);
            if (indexOf < 0) {
                throw new IllegalArgumentException("cache field");
            }
            int parseInt = Integer.parseInt(this.source.substring(this.offset, indexOf));
            int i = indexOf + 1;
            int i2 = i + parseInt;
            if (parseInt < 0 || i2 > this.source.length()) {
                throw new IllegalArgumentException("cache length");
            }
            this.offset = i2;
            return this.source.substring(i, i2);
        }
    }

    private PortalCacheCodec() {
    }

    public static CallHistorySnapshot decodeCallHistory(String str) {
        try {
            Reader reader = new Reader(str);
            if (!CALLS_VERSION.equals(reader.next())) {
                return null;
            }
            String next = reader.next();
            String next2 = reader.next();
            long parseLong = parseLong(reader.next(), System.currentTimeMillis());
            int max = Math.max(0, Math.min(500, parseInt(reader.next(), 0)));
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < max; i++) {
                arrayList.add(new CallDetail(reader.next(), reader.next(), reader.next(), reader.next(), reader.next()));
            }
            return new CallHistorySnapshot(next, next2, arrayList, parseLong);
        } catch (Exception unused) {
            return null;
        }
    }

    public static DashboardSnapshot decodeDashboard(String str) {
        DashboardSnapshot dashboardSnapshot;
        DashboardSnapshot dashboardSnapshot2 = null;
        try {
            Reader reader = new Reader(str);
            String next = reader.next();
            if (!DASHBOARD_VERSION.equals(next) && !"D1".equals(next)) {
                return null;
            }
            String next2 = reader.next();
            String next3 = reader.next();
            String next4 = reader.next();
            String next5 = reader.next();
            String next6 = reader.next();
            String next7 = reader.next();
            long parseLong = parseLong(reader.next(), System.currentTimeMillis());
            SubscriberProfile subscriberProfile = "D1".equals(next) ? new SubscriberProfile(reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next()) : new SubscriberProfile(reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next());
            ArrayList arrayList = new ArrayList();
            int i = 0;
            for (int max = Math.max(0, Math.min(20, parseInt(reader.next(), 0))); i < max; max = max) {
                dashboardSnapshot = dashboardSnapshot2;
                try {
                    Reader reader2 = reader;
                    arrayList.add(new PackageBalance(reader.next(), reader.next(), parseInt(reader2.next(), 0)));
                    i++;
                    dashboardSnapshot2 = dashboardSnapshot;
                    reader = reader2;
                } catch (Exception unused) {
                    return dashboardSnapshot;
                }
            }
            dashboardSnapshot = dashboardSnapshot2;
            return new DashboardSnapshot(next2, next3, next4, next5, next6, arrayList, subscriberProfile, next7, parseLong);
        } catch (Exception unused2) {
            return dashboardSnapshot2;
        }
    }

    public static FundsFlowSnapshot decodeFundsFlow(String str) {
        try {
            Reader reader = new Reader(str);
            if (!FUNDS_VERSION.equals(reader.next())) {
                return null;
            }
            String next = reader.next();
            String next2 = reader.next();
            String next3 = reader.next();
            String next4 = reader.next();
            String next5 = reader.next();
            long parseLong = parseLong(reader.next(), System.currentTimeMillis());
            int max = Math.max(0, Math.min(1000, parseInt(reader.next(), 0)));
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < max; i++) {
                arrayList.add(new FundsTransaction(reader.next(), reader.next(), reader.next(), reader.next(), reader.next(), reader.next()));
            }
            return new FundsFlowSnapshot(next, next2, next3, next4, next5, arrayList, parseLong);
        } catch (Exception unused) {
            return null;
        }
    }

    public static List<ServiceItem> decodeServices(String str) {
        try {
            Reader reader = new Reader(str);
            if (!SERVICES_VERSION.equals(reader.next())) {
                return Collections.EMPTY_LIST;
            }
            int max = Math.max(0, Math.min(100, parseInt(reader.next(), 0)));
            ArrayList arrayList = new ArrayList();
            for (int i = 0; i < max; i++) {
                arrayList.add(new ServiceItem(reader.next(), reader.next(), reader.next(), reader.next(), reader.next()));
            }
            return arrayList;
        } catch (Exception unused) {
            return Collections.EMPTY_LIST;
        }
    }

    public static String encodeCallHistory(CallHistorySnapshot callHistorySnapshot) {
        if (callHistorySnapshot == null) {
            return "";
        }
        Writer writer = new Writer(0);
        writer.put(CALLS_VERSION);
        writer.put(callHistorySnapshot.periodLabel);
        writer.put(callHistorySnapshot.totalDuration);
        writer.put(Long.toString(callHistorySnapshot.updatedAt));
        writer.put(Integer.toString(callHistorySnapshot.calls.size()));
        for (CallDetail callDetail : callHistorySnapshot.calls) {
            writer.put(callDetail.dateTime);
            writer.put(callDetail.direction);
            writer.put(callDetail.destination);
            writer.put(callDetail.duration);
            writer.put(callDetail.cost);
        }
        return writer.value();
    }

    public static String encodeDashboard(DashboardSnapshot dashboardSnapshot) {
        if (dashboardSnapshot == null) {
            return "";
        }
        Writer writer = new Writer(0);
        writer.put(DASHBOARD_VERSION);
        writer.put(dashboardSnapshot.phoneNumber);
        writer.put(dashboardSnapshot.status);
        writer.put(dashboardSnapshot.balance);
        writer.put(dashboardSnapshot.transferableBalance);
        writer.put(dashboardSnapshot.tariffName);
        writer.put(dashboardSnapshot.outgoingSummary);
        writer.put(Long.toString(dashboardSnapshot.updatedAt));
        SubscriberProfile subscriberProfile = dashboardSnapshot.profile;
        if (subscriberProfile == null) {
            subscriberProfile = new SubscriberProfile();
        }
        writer.put(subscriberProfile.accountId);
        writer.put(subscriberProfile.contractStatus);
        writer.put(subscriberProfile.phoneNumber);
        writer.put(subscriberProfile.registrationStatus);
        writer.put(subscriberProfile.tariffName);
        writer.put(subscriberProfile.itPhoneId);
        writer.put(subscriberProfile.contactPhone);
        writer.put(subscriberProfile.email);
        writer.put(subscriberProfile.connectionDate);
        writer.put(subscriberProfile.validUntil);
        writer.put(subscriberProfile.subscriberTenure);
        writer.put(subscriberProfile.loyaltyTenure);
        writer.put(Integer.toString(dashboardSnapshot.packages.size()));
        for (PackageBalance packageBalance : dashboardSnapshot.packages) {
            writer.put(packageBalance.title);
            writer.put(packageBalance.subtitle);
            writer.put(Integer.toString(packageBalance.progressPercent));
        }
        return writer.value();
    }

    public static String encodeFundsFlow(FundsFlowSnapshot fundsFlowSnapshot) {
        if (fundsFlowSnapshot == null) {
            return "";
        }
        Writer writer = new Writer(0);
        writer.put(FUNDS_VERSION);
        writer.put(fundsFlowSnapshot.periodLabel);
        writer.put(fundsFlowSnapshot.openingBalance);
        writer.put(fundsFlowSnapshot.creditsTotal);
        writer.put(fundsFlowSnapshot.debitsTotal);
        writer.put(fundsFlowSnapshot.closingBalance);
        writer.put(Long.toString(fundsFlowSnapshot.updatedAt));
        writer.put(Integer.toString(fundsFlowSnapshot.transactions.size()));
        for (FundsTransaction fundsTransaction : fundsFlowSnapshot.transactions) {
            writer.put(fundsTransaction.dateTime);
            writer.put(fundsTransaction.description);
            writer.put(fundsTransaction.category);
            writer.put(fundsTransaction.amount);
            writer.put(fundsTransaction.balanceAfter);
            writer.put(fundsTransaction.type);
        }
        return writer.value();
    }

    public static String encodeServices(List<ServiceItem> list) {
        if (list == null) {
            list = Collections.EMPTY_LIST;
        }
        Writer writer = new Writer(0);
        writer.put(SERVICES_VERSION);
        writer.put(Integer.toString(list.size()));
        for (ServiceItem serviceItem : list) {
            writer.put(serviceItem.id);
            writer.put(serviceItem.title);
            writer.put(serviceItem.fee);
            writer.put(serviceItem.status);
            writer.put(serviceItem.details);
        }
        return writer.value();
    }

    private static int parseInt(String str, int i) {
        try {
            return Integer.parseInt(str);
        } catch (Exception unused) {
            return i;
        }
    }

    private static long parseLong(String str, long j) {
        try {
            return Long.parseLong(str);
        } catch (Exception unused) {
            return j;
        }
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public static final class Writer {
        private final StringBuilder out;

        private Writer() {
            this.out = new StringBuilder();
        }

        public void put(String str) {
            if (str == null) {
                str = "";
            }
            StringBuilder sb = this.out;
            sb.append(str.length());
            sb.append(':');
            sb.append(str);
        }

        public String value() {
            return this.out.toString();
        }

        public /* synthetic */ Writer(int i) {
            this();
        }
    }
}
