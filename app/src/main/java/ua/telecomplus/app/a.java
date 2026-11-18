package ua.telecomplus.app;

import ua.telecomplus.app.MainActivity;
import ua.telecomplus.app.PortalWebAuthBridge;
import ua.telecomplus.app.TelephonyEngine;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final /* synthetic */ class a implements MainActivity.PortalCallback, PortalWebAuthBridge.OtpCallback, MainActivity.TelephonyCallback, PortalWebAuthBridge.LoginCallback, TelephonyEngine.Listener {
    public final /* synthetic */ int a;
    public final /* synthetic */ MainActivity b;

    public /* synthetic */ a(MainActivity mainActivity, int i) {
        this.a = i;
        this.b = mainActivity;
    }

    @Override // ua.telecomplus.app.MainActivity.PortalCallback
    public void accept(PortalResult portalResult) {
        int i = this.a;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 0:
                mainActivity.lambda$showNumberLogin$7(portalResult);
                break;
            case 1:
            default:
                mainActivity.lambda$submitPortalOtp$27(portalResult);
                break;
            case 2:
                mainActivity.lambda$showOtpLogin$21(portalResult);
                break;
            case 3:
                mainActivity.lambda$continueStartup$3(portalResult);
                break;
            case 4:
                mainActivity.lambda$showPasswordLogin$13(portalResult);
                break;
        }
    }

    @Override // ua.telecomplus.app.PortalWebAuthBridge.OtpCallback, ua.telecomplus.app.PortalWebAuthBridge.LoginCallback
    public void onResult(PortalResult portalResult) {
        int i = this.a;
        MainActivity mainActivity = this.b;
        switch (i) {
            case 1:
                mainActivity.lambda$showOtpLogin$19(portalResult);
                break;
            case 6:
                mainActivity.lambda$submitPortalOtp$25(portalResult);
                break;
            default:
                mainActivity.lambda$showNumberLogin$5(portalResult);
                break;
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine.Listener
    public void onTelephonyChanged(TelephonySnapshot telephonySnapshot) {
        this.b.lambda$onCreate$1(telephonySnapshot);
    }

    @Override // ua.telecomplus.app.MainActivity.TelephonyCallback
    public void accept(TelephonyResult telephonyResult) {
        this.b.showTelephonyResult(telephonyResult);
    }
}
