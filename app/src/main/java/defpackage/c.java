package defpackage;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import ua.telecomplus.app.MainActivity;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class c implements View.OnClickListener {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ EditText c;
    public final /* synthetic */ Button d;

    public /* synthetic */ c(MainActivity mainActivity, EditText editText, Button button, int i) {
        this.a = i;
        this.b = mainActivity;
        this.c = editText;
        this.d = button;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        Button button = this.d;
        EditText editText = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$showOtpLogin$16(editText, button, view);
                break;
            case 1:
                mainActivity.lambda$showNumberLogin$8(editText, button, view);
                break;
            case 2:
                mainActivity.lambda$renderPhone$51(editText, button, view);
                break;
            default:
                mainActivity.lambda$renderPhone$53(editText, button, view);
                break;
        }
    }
}
