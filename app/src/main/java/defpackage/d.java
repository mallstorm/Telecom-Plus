package defpackage;

import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import ua.telecomplus.app.MainActivity;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class d implements View.OnClickListener {
    public final /* synthetic */ int a;
    public final /* synthetic */ KeyEvent.Callback b;

    public /* synthetic */ d(KeyEvent.Callback callback, int i) {
        this.a = i;
        this.b = callback;
    }

    @Override // android.view.View.OnClickListener
    public final void onClick(View view) {
        int i = this.a;
        KeyEvent.Callback callback = this.b;
        switch (i) {
            case 0:
                ((MainActivity) callback).lambda$showOtpLogin$17(view);
                break;
            case 1:
                ((MainActivity) callback).lambda$showOtpLogin$23(view);
                break;
            case 2:
                ((MainActivity) callback).lambda$renderHome$45(view);
                break;
            case 3:
                ((MainActivity) callback).lambda$renderHome$46(view);
                break;
            case 4:
                ((MainActivity) callback).lambda$renderHome$47(view);
                break;
            case 5:
                ((MainActivity) callback).lambda$renderHome$48(view);
                break;
            case 6:
                ((MainActivity) callback).lambda$renderHome$49(view);
                break;
            case 7:
                ((MainActivity) callback).lambda$renderProfile$79(view);
                break;
            case 8:
                ((MainActivity) callback).lambda$renderProfile$80(view);
                break;
            case 9:
                ((MainActivity) callback).lambda$showLoadFailure$41(view);
                break;
            case 10:
                ((MainActivity) callback).lambda$showLoadFailure$42(view);
                break;
            case 11:
                ((MainActivity) callback).lambda$renderPhone$58(view);
                break;
            case 12:
                ((MainActivity) callback).lambda$renderPhone$59(view);
                break;
            case 13:
                ((MainActivity) callback).lambda$renderFunds$76(view);
                break;
            case 14:
                ((MainActivity) callback).lambda$topBar$43(view);
                break;
            case 15:
                ((MainActivity) callback).lambda$renderCalls$78(view);
                break;
            default:
                MainActivity.lambda$renderPhone$55((EditText) callback, view);
                break;
        }
    }
}
