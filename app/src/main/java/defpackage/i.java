package defpackage;

import ua.telecomplus.app.MainActivity;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class i implements Runnable {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;

    public /* synthetic */ i(MainActivity mainActivity, int i) {
        this.a = i;
        this.b = mainActivity;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$enterCabinet$30();
                break;
            default:
                mainActivity.lambda$startDomFallbackIfNeeded$31();
                break;
        }
    }
}
