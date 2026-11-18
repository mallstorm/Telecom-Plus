package defpackage;

import java.util.concurrent.Callable;
import ua.telecomplus.app.MainActivity;
import ua.telecomplus.app.PortalResult;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class b implements Callable {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;

    public /* synthetic */ b(MainActivity mainActivity, int i) {
        this.a = i;
        this.b = mainActivity;
    }

    @Override // java.util.concurrent.Callable
    public final Object call() {
        PortalResult lambda$showOtpLogin$20;
        PortalResult lambda$continueStartup$2;
        PortalResult lambda$showNumberLogin$6;
        int i = this.a;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                lambda$showOtpLogin$20 = mainActivity.lambda$showOtpLogin$20();
                return lambda$showOtpLogin$20;
            case 1:
                lambda$continueStartup$2 = mainActivity.lambda$continueStartup$2();
                return lambda$continueStartup$2;
            default:
                lambda$showNumberLogin$6 = mainActivity.lambda$showNumberLogin$6();
                return lambda$showNumberLogin$6;
        }
    }
}
