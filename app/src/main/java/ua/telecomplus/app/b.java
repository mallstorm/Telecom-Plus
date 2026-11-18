package ua.telecomplus.app;

import ua.telecomplus.app.PortalWebAuthBridge;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class b implements PortalWebAuthBridge.DataPageCallback, PortalWebAuthBridge.LoginCallback {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ Object c;

    public /* synthetic */ b(MainActivity mainActivity, Object obj, int i) {
        this.a = i;
        this.b = mainActivity;
        this.c = obj;
    }

    @Override // ua.telecomplus.app.PortalWebAuthBridge.DataPageCallback, ua.telecomplus.app.PortalWebAuthBridge.LoginCallback
    public final void onResult(PortalResult portalResult) {
        int i = this.a;
        Object obj = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$loadFundsDomIfNeeded$36((Runnable) obj, portalResult);
                break;
            case 1:
                mainActivity.lambda$loadCallsDomIfNeeded$40((Runnable) obj, portalResult);
                break;
            default:
                mainActivity.lambda$showPasswordLogin$11((String) obj, portalResult);
                break;
        }
    }
}
