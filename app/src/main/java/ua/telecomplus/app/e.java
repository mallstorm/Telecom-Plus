package ua.telecomplus.app;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class e implements Runnable {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ PortalResult c;
    public final /* synthetic */ Runnable d;

    public /* synthetic */ e(MainActivity mainActivity, PortalResult portalResult, Runnable runnable, int i) {
        this.a = i;
        this.b = mainActivity;
        this.c = portalResult;
        this.d = runnable;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        Runnable runnable = this.d;
        PortalResult portalResult = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$loadCallsDomIfNeeded$39(portalResult, runnable);
                break;
            case 1:
                mainActivity.lambda$loadCallsDomIfNeeded$37(portalResult, runnable);
                break;
            case 2:
                mainActivity.lambda$loadFundsDomIfNeeded$33(portalResult, runnable);
                break;
            default:
                mainActivity.lambda$loadFundsDomIfNeeded$35(portalResult, runnable);
                break;
        }
    }
}
