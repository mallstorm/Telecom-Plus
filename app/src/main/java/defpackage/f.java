package defpackage;

import android.view.View;
import ua.telecomplus.app.MainActivity;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class f implements View.OnClickListener {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ int c;

    public /* synthetic */ f(MainActivity mainActivity, int i, int i2) {
        this.a = i2;
        this.b = mainActivity;
        this.c = i;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        int i2 = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$bottomNav$44(i2, view);
                break;
            case 1:
                mainActivity.lambda$historyModeButton$61(i2, view);
                break;
            default:
                mainActivity.lambda$fundsFilterButton$77(i2, view);
                break;
        }
    }
}
