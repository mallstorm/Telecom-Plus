package ua.telecomplus.app;

import java.util.Comparator;
import ua.telecomplus.app.PersistentCookieStore;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class p implements Comparator {
    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        int lambda$header$0;
        lambda$header$0 = PersistentCookieStore.lambda$header$0((PersistentCookieStore.StoredCookie) obj, (PersistentCookieStore.StoredCookie) obj2);
        return lambda$header$0;
    }
}
