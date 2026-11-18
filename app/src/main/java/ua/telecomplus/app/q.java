package ua.telecomplus.app;

import ua.telecomplus.app.PortalWebAuthBridge;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class q implements PortalWebAuthBridge.PageCallback {
    public final /* synthetic */ int a;
    public final /* synthetic */ PortalWebAuthBridge b;

    public /* synthetic */ q(PortalWebAuthBridge portalWebAuthBridge, int i) {
        this.a = i;
        this.b = portalWebAuthBridge;
    }

    @Override // ua.telecomplus.app.PortalWebAuthBridge.PageCallback
    public final void onPage(String str, String str2) {
        int i = this.a;
        PortalWebAuthBridge portalWebAuthBridge = this.b;
        switch (i) {
            case 0:
                portalWebAuthBridge.lambda$inspectDataPage$13(str, str2);
                break;
            case 1:
                portalWebAuthBridge.lambda$inspectOtpPage$10(str, str2);
                break;
            default:
                portalWebAuthBridge.lambda$inspectLoginResult$11(str, str2);
                break;
        }
    }
}
