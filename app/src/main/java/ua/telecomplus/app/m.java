package ua.telecomplus.app;

import java.util.List;
import ua.telecomplus.app.PortalWebAuthBridge;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class m implements PortalWebAuthBridge.DataPageCallback {
    public final /* synthetic */ int a = 0;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ int[] c;
    public final /* synthetic */ List d;
    public final /* synthetic */ List e;
    public final /* synthetic */ List f;
    public final /* synthetic */ int g;

    public /* synthetic */ m(MainActivity mainActivity, List list, List list2, int[] iArr, List list3, int i) {
        this.b = mainActivity;
        this.d = list;
        this.e = list2;
        this.c = iArr;
        this.f = list3;
        this.g = i;
    }

    @Override // ua.telecomplus.app.PortalWebAuthBridge.DataPageCallback, ua.telecomplus.app.PortalWebAuthBridge.LoginCallback
    public final void onResult(PortalResult portalResult) {
        switch (this.a) {
            case 0:
                List list = this.f;
                int i = this.g;
                this.b.lambda$loadCallsMonth$75(this.d, this.e, this.c, list, i, portalResult);
                break;
            default:
                List list2 = this.f;
                int i2 = this.g;
                this.b.lambda$loadFundsMonth$71(this.c, this.d, this.e, list2, i2, portalResult);
                break;
        }
    }

    public /* synthetic */ m(MainActivity mainActivity, int[] iArr, List list, List list2, List list3, int i) {
        this.b = mainActivity;
        this.c = iArr;
        this.d = list;
        this.e = list2;
        this.f = list3;
        this.g = i;
    }
}
