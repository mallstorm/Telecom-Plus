package ua.telecomplus.app;

import android.app.Application;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final class TelecomPlusApplication extends Application {
    SubscriberPortalGateway gateway;
    TelephonyEngine telephony;
    SecureVault vault;
    static volatile boolean foreground;
    static volatile TelecomPlusApplication instance;

    @Override // android.app.Application
    public void onCreate() {
        super.onCreate();
        instance = this;
        TpEnv.init(this);
        this.vault = TpEnv.vault;
        this.gateway = TpEnv.gateway;
        this.telephony = TpEnv.telephony;
    }

    @Override // android.app.Application
    public void onTerminate() {
        TelephonyEngine telephonyEngine = this.telephony;
        if (telephonyEngine != null) {
            telephonyEngine.close();
        }
        super.onTerminate();
    }
}
