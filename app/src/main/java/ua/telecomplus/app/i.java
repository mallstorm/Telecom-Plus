package ua.telecomplus.app;

import java.util.concurrent.Callable;
import ua.telecomplus.app.MainActivity;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class i implements Runnable {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ Object c;
    public final /* synthetic */ Object d;

    public /* synthetic */ i(MainActivity mainActivity, Object obj, Object obj2, int i) {
        this.a = i;
        this.b = mainActivity;
        this.c = obj;
        this.d = obj2;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        Object obj = this.d;
        Object obj2 = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$showPasswordLogin$10((PortalResult) obj2, (String) obj);
                break;
            case 1:
                mainActivity.lambda$runTelephony$88((Callable) obj2, (MainActivity.TelephonyCallback) obj);
                break;
            default:
                mainActivity.lambda$runPortal$86((Callable) obj2, (MainActivity.PortalCallback) obj);
                break;
        }
    }
}
