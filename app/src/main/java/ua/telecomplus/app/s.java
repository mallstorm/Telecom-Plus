package ua.telecomplus.app;

import ua.telecomplus.app.PortalWebAuthBridge;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class s implements Runnable {
    public final /* synthetic */ int a;
    public final /* synthetic */ PortalWebAuthBridge b;

    public /* synthetic */ s(PortalWebAuthBridge portalWebAuthBridge, int i) {
        this.a = i;
        this.b = portalWebAuthBridge;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        PortalWebAuthBridge portalWebAuthBridge = this.b;
        switch (i) {
            case 0:
                portalWebAuthBridge.inspectOtpPage();
                break;
            case 1:
                portalWebAuthBridge.inspectDataPage();
                break;
            case 2:
                portalWebAuthBridge.lambda$new$0();
                break;
            case 3:
                portalWebAuthBridge.lambda$new$1();
                break;
            case 4:
                portalWebAuthBridge.lambda$new$2();
                break;
            case 5:
                portalWebAuthBridge.inspectLoginResult();
                break;
            case 6:
                portalWebAuthBridge.submitPassword();
                break;
            case 7:
                int i2 = PortalWebAuthBridge.AuthClient.a;
                portalWebAuthBridge.submitNumber();
                break;
            case 8:
                int i3 = PortalWebAuthBridge.AuthClient.a;
                portalWebAuthBridge.submitPassword();
                break;
            case 9:
                int i4 = PortalWebAuthBridge.AuthClient.a;
                portalWebAuthBridge.inspectOtpPage();
                break;
            case 10:
                int i5 = PortalWebAuthBridge.AuthClient.a;
                portalWebAuthBridge.inspectLoginResult();
                break;
            default:
                int i6 = PortalWebAuthBridge.AuthClient.a;
                portalWebAuthBridge.inspectDataPage();
                break;
        }
    }
}
