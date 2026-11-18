package ua.telecomplus.app;

import android.webkit.ValueCallback;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class t implements ValueCallback {
    public final /* synthetic */ int a;
    public final /* synthetic */ PortalWebAuthBridge b;

    public /* synthetic */ t(PortalWebAuthBridge portalWebAuthBridge, int i) {
        this.a = i;
        this.b = portalWebAuthBridge;
    }

    @Override // android.webkit.ValueCallback
    public final void onReceiveValue(Object obj) {
        int i = this.a;
        PortalWebAuthBridge portalWebAuthBridge = this.b;
        switch (i) {
            case 0:
                portalWebAuthBridge.lambda$submitNumber$8((String) obj);
                break;
            case 1:
                portalWebAuthBridge.lambda$confirmOtp$5((String) obj);
                break;
            case 2:
                portalWebAuthBridge.lambda$inspectDataPage$12((String) obj);
                break;
            case 3:
                portalWebAuthBridge.lambda$requestOtp$3((Boolean) obj);
                break;
            case 4:
                portalWebAuthBridge.lambda$loginWithPassword$4((Boolean) obj);
                break;
            default:
                portalWebAuthBridge.lambda$submitPassword$9((String) obj);
                break;
        }
    }
}
