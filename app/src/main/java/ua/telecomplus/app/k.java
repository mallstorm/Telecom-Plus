package ua.telecomplus.app;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class k implements Runnable {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ PortalPageSnapshot c;
    public final /* synthetic */ Runnable d;

    public /* synthetic */ k(MainActivity mainActivity, PortalPageSnapshot portalPageSnapshot, Runnable runnable, int i) {
        this.a = i;
        this.b = mainActivity;
        this.c = portalPageSnapshot;
        this.d = runnable;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        Runnable runnable = this.d;
        PortalPageSnapshot portalPageSnapshot = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$loadFundsDomIfNeeded$34(portalPageSnapshot, runnable);
                break;
            default:
                mainActivity.lambda$loadCallsDomIfNeeded$38(portalPageSnapshot, runnable);
                break;
        }
    }
}
