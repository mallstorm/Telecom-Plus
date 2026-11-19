package ua.telecomplus.app;

import android.content.Context;

/** Общие объекты приложения; не зависят от класса Application (внутри IT Phone он чужой). */
final class TpEnv {
    static volatile SecureVault vault;
    static volatile SubscriberPortalGateway gateway;
    static volatile TelephonyEngine telephony;
    static volatile boolean foreground;
    static volatile Context app;

    private TpEnv() {
    }

    static synchronized void init(Context context) {
        if (app != null) {
            return;
        }
        Context c = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        app = c;
        Res.init(c);
        vault = new SecureVault(c);
        gateway = new IntertelecomPortalClient(vault);
        telephony = new CleanRoomTelephonyEngine(c, vault);
        SessionKeepAliveJob.schedule(c);
    }
}
