package defpackage;

import android.view.View;
import android.widget.EditText;
import ua.telecomplus.app.MainActivity;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class h implements View.OnClickListener {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;
    public final /* synthetic */ EditText c;

    public /* synthetic */ h(MainActivity mainActivity, EditText editText, int i) {
        this.a = i;
        this.b = mainActivity;
        this.c = editText;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        EditText editText = this.c;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$showPasswordLogin$15(editText, view);
                break;
            case 1:
                mainActivity.lambda$showNumberLogin$9(editText, view);
                break;
            default:
                mainActivity.lambda$renderPhone$57(editText, view);
                break;
        }
    }
}
