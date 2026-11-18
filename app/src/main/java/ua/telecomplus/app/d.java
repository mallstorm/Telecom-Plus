package ua.telecomplus.app;

import java.util.concurrent.atomic.AtomicBoolean;
import ua.telecomplus.app.MainActivity;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class d implements Runnable {
    public final /* synthetic */ int a;
    public final /* synthetic */ Object b;
    public final /* synthetic */ Object c;

    public /* synthetic */ d(Object obj, Object obj2, int i) {
        this.a = i;
        this.b = obj;
        this.c = obj2;
    }

    @Override // java.lang.Runnable
    public final void run() {
        int i = this.a;
        Object obj = this.c;
        Object obj2 = this.b;
        switch (i) {
            case 0:
                ((MainActivity) obj2).lambda$onCreate$0((TelephonySnapshot) obj);
                break;
            case 1:
                ((MainActivity.PortalCallback) obj2).accept((PortalResult) obj);
                break;
            case 2:
                ((MainActivity.TelephonyCallback) obj2).accept((TelephonyResult) obj);
                break;
            default:
                PortalWebAuthBridge.lambda$clearBrowserSession$6((AtomicBoolean) obj2, (Runnable) obj);
                break;
        }
    }
}
