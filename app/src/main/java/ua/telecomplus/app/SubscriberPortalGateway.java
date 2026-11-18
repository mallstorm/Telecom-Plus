package ua.telecomplus.app;

import java.util.List;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
interface SubscriberPortalGateway {
    PortalResult<CallHistorySnapshot> acceptCallHistoryHtml(String str);

    PortalResult<FundsFlowSnapshot> acceptFundsFlowHtml(String str);

    PortalResult<Void> confirmOtp(String str, String str2);

    CallHistorySnapshot getCachedCallHistory();

    DashboardSnapshot getCachedDashboard();

    FundsFlowSnapshot getCachedFundsFlow();

    List<ServiceItem> getCachedServices();

    PortalResult<CallHistorySnapshot> getCallHistory();

    PortalResult<DashboardSnapshot> getDashboard();

    PortalResult<FundsFlowSnapshot> getFundsFlow();

    PortalResult<List<ServiceItem>> getServices();

    String getSessionCookieHeader();

    boolean hasSavedSession();

    PortalResult<Void> importBrowserSession(String str, String str2);

    PortalLoginFlow loginFlow();

    PortalResult<Void> loginWithPassword(String str, String str2);

    PortalResult<Void> logout(boolean z);

    PortalResult<Integer> requestOtp(String str);

    PortalResult<Void> validateSession();
}
