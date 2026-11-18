package defpackage;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import ua.telecomplus.app.MainActivity;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class e implements View.OnClickListener {
    public final /* synthetic */ int a;
    public final /* synthetic */ KeyEvent.Callback b;
    public final /* synthetic */ Object c;

    public /* synthetic */ e(KeyEvent.Callback callback, Object obj, int i) {
        this.a = i;
        this.b = callback;
        this.c = obj;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        Object obj = this.c;
        KeyEvent.Callback callback = this.b;
        switch (i) {
            case 0:
                ((MainActivity) callback).lambda$showOtpLogin$22((Button) obj, view);
                break;
            default:
                ((EditText) callback).append((String) obj);
                break;
        }
    }
}
