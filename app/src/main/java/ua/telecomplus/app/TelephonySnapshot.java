package ua.telecomplus.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class TelephonySnapshot {
    final AudioRoute audioRoute;
    final long callStartedAt;
    final TelecomCallState callState;
    final String displayName;
    final boolean held;
    final boolean muted;
    final String phoneNumber;
    final TelephonyRegistrationState registrationState;
    final String remoteNumber;
    final String statusMessage;
    final List<String> supportedCodecs;

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    /* renamed from: ua.telecomplus.app.TelephonySnapshot$1, reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState;

        static {
            int[] iArr = new int[TelephonyRegistrationState.values().length];
            $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState = iArr;
            try {
                iArr[TelephonyRegistrationState.PROVISIONED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[TelephonyRegistrationState.CONNECTING.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[TelephonyRegistrationState.ONLINE.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[TelephonyRegistrationState.OFFLINE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
        }
    }

    public TelephonySnapshot(TelephonyRegistrationState telephonyRegistrationState, TelecomCallState telecomCallState, String str, String str2, String str3, String str4, boolean z, boolean z2, AudioRoute audioRoute, long j, List<String> list) {
        this.registrationState = telephonyRegistrationState == null ? TelephonyRegistrationState.UNPROVISIONED : telephonyRegistrationState;
        this.callState = telecomCallState == null ? TelecomCallState.IDLE : telecomCallState;
        this.phoneNumber = clean(str);
        this.displayName = clean(str2);
        this.remoteNumber = clean(str3);
        this.statusMessage = clean(str4);
        this.muted = z;
        this.held = z2;
        this.audioRoute = audioRoute == null ? AudioRoute.EARPIECE : audioRoute;
        this.callStartedAt = j;
        this.supportedCodecs = list == null ? Collections.EMPTY_LIST : Collections.unmodifiableList(new ArrayList(list));
    }

    private static String clean(String str) {
        return str == null ? "" : str.trim();
    }

    public static TelephonySnapshot unprovisioned() {
        return new TelephonySnapshot(TelephonyRegistrationState.UNPROVISIONED, TelecomCallState.IDLE, "", "", "", "Телефония ещё не активирована", false, false, AudioRoute.EARPIECE, 0L, Collections.EMPTY_LIST);
    }

    public boolean hasActiveCall() {
        TelecomCallState telecomCallState = this.callState;
        return telecomCallState == TelecomCallState.DIALING || telecomCallState == TelecomCallState.RINGING || telecomCallState == TelecomCallState.ACTIVE || telecomCallState == TelecomCallState.HELD;
    }

    public boolean isProvisioned() {
        int i = AnonymousClass1.$SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[this.registrationState.ordinal()];
        return i == 1 || i == 2 || i == 3 || i == 4;
    }
}
