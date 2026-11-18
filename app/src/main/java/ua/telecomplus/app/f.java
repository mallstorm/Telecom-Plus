package ua.telecomplus.app;

import java.util.List;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class f implements Runnable {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ PortalResult c;
    public final /* synthetic */ List d;
    public final /* synthetic */ List e;
    public final /* synthetic */ int[] f;
    public final /* synthetic */ List g;
    public final /* synthetic */ int h;

    public /* synthetic */ f(MainActivity mainActivity, PortalResult portalResult, List list, List list2, int[] iArr, List list3, int i, int i2) {
        this.a = i2;
        this.b = mainActivity;
        this.c = portalResult;
        this.d = list;
        this.e = list2;
        this.f = iArr;
        this.g = list3;
        this.h = i;
    }

    @Override // java.lang.Runnable
    public final void run() {
        switch (this.a) {
            case 0:
                List list = this.g;
                int i = this.h;
                this.b.lambda$loadFundsMonth$70(this.c, this.f, this.d, this.e, list, i);
                break;
            case 1:
                List list2 = this.g;
                int i2 = this.h;
                this.b.lambda$loadCallsMonth$74(this.c, this.d, this.e, this.f, list2, i2);
                break;
            case 2:
                List list3 = this.g;
                int i3 = this.h;
                this.b.lambda$loadFundsMonth$68(this.c, this.f, this.d, this.e, list3, i3);
                break;
            default:
                List list4 = this.g;
                int i4 = this.h;
                this.b.lambda$loadCallsMonth$72(this.c, this.d, this.e, this.f, list4, i4);
                break;
        }
    }

    public /* synthetic */ f(MainActivity mainActivity, PortalResult portalResult, int[] iArr, List list, List list2, List list3, int i, int i2) {
        this.a = i2;
        this.b = mainActivity;
        this.c = portalResult;
        this.f = iArr;
        this.d = list;
        this.e = list2;
        this.g = list3;
        this.h = i;
    }
}
