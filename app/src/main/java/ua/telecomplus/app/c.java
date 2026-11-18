package ua.telecomplus.app;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class c implements Runnable {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ PortalResult c;

    public /* synthetic */ c(MainActivity mainActivity, PortalResult portalResult, int i) {
        this.a = i;
        this.b = mainActivity;
        this.c = portalResult;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        PortalResult portalResult = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$showOtpLogin$18(portalResult);
                break;
            case 1:
                mainActivity.lambda$showNumberLogin$4(portalResult);
                break;
            default:
                mainActivity.lambda$submitPortalOtp$24(portalResult);
                break;
        }
    }
}
