package ua.telecomplus.app;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
interface TelephonyEngine {

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public interface Listener {
        void onTelephonyChanged(TelephonySnapshot telephonySnapshot);
    }

    TelephonyResult<Void> answer();

    void close();

    TelephonyResult<Void> confirmProvisioningOtp(String str);

    TelephonyResult<Void> connect();

    TelephonyResult<Void> disconnect();

    TelephonyResult<Void> forgetProvisioning();

    TelephonyResult<Void> hangup();

    TelephonyResult<Void> placeCall(String str, boolean z);

    TelephonyResult<Integer> requestProvisioningOtp(String str);

    TelephonyResult<Void> sendDtmf(char c);

    TelephonyResult<Void> setAudioRoute(AudioRoute audioRoute);

    TelephonyResult<Void> setHeld(boolean z);

    void setListener(Listener listener);

    TelephonyResult<Void> setMuted(boolean z);

    TelephonySnapshot snapshot();

    TelephonyResult<Void> startConference(String str);

    TelephonyResult<Void> transfer(String str);
}
