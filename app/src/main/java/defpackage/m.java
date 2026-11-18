package defpackage;

import android.widget.EditText;
import java.util.concurrent.Callable;
import ua.telecomplus.app.MainActivity;
import ua.telecomplus.app.PortalResult;
import ua.telecomplus.app.TelephonyResult;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class m implements Callable {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ Object c;

    public /* synthetic */ m(MainActivity mainActivity, Object obj, int i) {
        this.a = i;
        this.b = mainActivity;
        this.c = obj;
    }

    @Override // java.util.concurrent.Callable
    public final Object call() {
        PortalResult lambda$submitPortalOtp$26;
        TelephonyResult lambda$requestTelephonyActivation$60;
        TelephonyResult lambda$renderPhone$50;
        TelephonyResult lambda$renderPhone$56;
        int i = this.a;
        Object obj = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                lambda$submitPortalOtp$26 = mainActivity.lambda$submitPortalOtp$26((String) obj);
                return lambda$submitPortalOtp$26;
            case 1:
                lambda$requestTelephonyActivation$60 = mainActivity.lambda$requestTelephonyActivation$60((String) obj);
                return lambda$requestTelephonyActivation$60;
            case 2:
                lambda$renderPhone$50 = mainActivity.lambda$renderPhone$50((String) obj);
                return lambda$renderPhone$50;
            default:
                lambda$renderPhone$56 = mainActivity.lambda$renderPhone$56((EditText) obj);
                return lambda$renderPhone$56;
        }
    }
}
