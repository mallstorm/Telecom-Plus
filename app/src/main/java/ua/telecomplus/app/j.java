package ua.telecomplus.app;

import java.util.List;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class j implements Runnable {
    public final /* synthetic */ int a = 0;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ PortalPageSnapshot c;
    public final /* synthetic */ int[] d;
    public final /* synthetic */ List e;
    public final /* synthetic */ List f;
    public final /* synthetic */ List g;
    public final /* synthetic */ int h;

    public /* synthetic */ j(MainActivity mainActivity, PortalPageSnapshot portalPageSnapshot, List list, List list2, int[] iArr, List list3, int i) {
        this.b = mainActivity;
        this.c = portalPageSnapshot;
        this.e = list;
        this.f = list2;
        this.d = iArr;
        this.g = list3;
        this.h = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                this.b.lambda$loadCallsMonth$73(this.c, this.e, this.f, this.d, this.g, this.h);
                break;
            default:
                List list = this.g;
                int i = this.h;
                this.b.lambda$loadFundsMonth$69(this.c, this.d, this.e, this.f, list, i);
                break;
        }
    }

    public /* synthetic */ j(MainActivity mainActivity, PortalPageSnapshot portalPageSnapshot, int[] iArr, List list, List list2, List list3, int i) {
        this.b = mainActivity;
        this.c = portalPageSnapshot;
        this.d = iArr;
        this.e = list;
        this.f = list2;
        this.g = list3;
        this.h = i;
    }
}
