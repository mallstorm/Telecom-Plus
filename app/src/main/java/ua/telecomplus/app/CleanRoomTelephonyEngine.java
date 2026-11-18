package ua.telecomplus.app;

import android.content.Context;
import java.util.Arrays;
import org.json.JSONObject;
import ua.telecomplus.app.TelephonyEngine;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class CleanRoomTelephonyEngine implements TelephonyEngine {
    private static final String MEDIA_BLOCK_MESSAGE = "Телефонный профиль активирован. Голосовой медиамодуль пока не включён в alpha26: нет проверенной сборки DSP/Opus/G.722/G.729A для ABI устройства. Приложение не имитирует успешный звонок.";
    private static final String PENDING_PHONE_KEY = "telephony_pending_phone_v1";
    private static final String PROFILE_KEY = "telephony_profile_v1";
    private TelephonyEngine.Listener listener;
    private final RingotelProvisioningClient provisioning;
    private TelephonySnapshot snapshot = restoreSnapshot();
    private final SecureVault vault;

    public CleanRoomTelephonyEngine(Context context, SecureVault secureVault) {
        this.vault = secureVault;
        this.provisioning = new RingotelProvisioningClient(context);
    }

    private TelephonyResult<Void> callUnavailable() {
        return TelephonyResult.failure("NO_ACTIVE_CALL", "Нет активного звонка");
    }

    private TelephonySnapshot copy(TelephonyRegistrationState telephonyRegistrationState, String str) {
        TelephonySnapshot telephonySnapshot = this.snapshot;
        return new TelephonySnapshot(telephonyRegistrationState, telephonySnapshot.callState, telephonySnapshot.phoneNumber, telephonySnapshot.displayName, telephonySnapshot.remoteNumber, str, telephonySnapshot.muted, telephonySnapshot.held, telephonySnapshot.audioRoute, telephonySnapshot.callStartedAt, telephonySnapshot.supportedCodecs);
    }

    private TelephonySnapshot restoreSnapshot() {
        String str = this.vault.get(PROFILE_KEY);
        if (str == null || str.trim().isEmpty()) {
            return TelephonySnapshot.unprovisioned();
        }
        try {
            return snapshotFromProfile(new JSONObject(str), TelephonyRegistrationState.PROVISIONED, "Телефонный профиль сохранён и зашифрован");
        } catch (Exception unused) {
            this.vault.remove(PROFILE_KEY);
            return TelephonySnapshot.unprovisioned();
        }
    }

    private static String safeMessage(Exception exc) {
        String message = exc == null ? null : exc.getMessage();
        return (message == null || message.trim().isEmpty()) ? "Ошибка соединения с сервером телефонии" : message.trim();
    }

    private static String serverText(JSONObject jSONObject, int i) {
        String trim = jSONObject.optString("message", "").trim();
        if ("InvalidCode".equals(trim)) {
            return "Неверный код активации";
        }
        if ("Phone number does not exist".equalsIgnoreCase(trim)) {
            return "Номер не найден на сервере телефонии Интертелекома";
        }
        if (!trim.isEmpty()) {
            return trim;
        }
        return "Сервер телефонии отклонил запрос (код " + i + ")";
    }

    private static TelephonySnapshot snapshotFromProfile(JSONObject jSONObject, TelephonyRegistrationState telephonyRegistrationState, String str) {
        String optString = jSONObject.optString("registration_identity", "");
        String optString2 = jSONObject.optString("phone", jSONObject.optString("number", ""));
        return new TelephonySnapshot(telephonyRegistrationState, TelecomCallState.IDLE, RingotelProvisioningClient.isEmailIdentity(optString2) ? "" : optString2, jSONObject.optString("login", jSONObject.optString("userid", jSONObject.optString("name", jSONObject.optString("displayname", optString)))), "", str, false, false, AudioRoute.EARPIECE, 0L, Arrays.asList("Opus", "G.722", "G.729A", "PCMA", "PCMU"));
    }

    private synchronized void update(TelephonySnapshot telephonySnapshot) {
        this.snapshot = telephonySnapshot;
        TelephonyEngine.Listener listener = this.listener;
        if (listener != null) {
            listener.onTelephonyChanged(telephonySnapshot);
        }
    }

    private void updateError(Exception exc) {
        update(copy(TelephonyRegistrationState.ERROR, safeMessage(exc)));
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> answer() {
        return callUnavailable();
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public void close() {
        setListener(null);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> confirmProvisioningOtp(String str) {
        String str2 = this.vault.get(PENDING_PHONE_KEY);
        if (str2 == null || str2.isEmpty()) {
            return TelephonyResult.failure("NO_PENDING_PROVISIONING", "Сначала запросите код входа IT Phone");
        }
        try {
            JSONObject complete = this.provisioning.complete(str2, str);
            int optInt = complete.optInt("rcode", complete.optInt("code", 0));
            if (optInt > 0) {
                return TelephonyResult.failure("PROVISIONING_REJECTED", serverText(complete, optInt));
            }
            if (complete.optString("termpass", "").trim().isEmpty()) {
                return TelephonyResult.failure("NO_TERMINAL_PASSWORD", "Сервер подтвердил код, но не выдал терминальный пароль");
            }
            JSONObject jSONObject = new JSONObject(complete.toString());
            jSONObject.put("registration_identity", str2);
            jSONObject.put("server", "intertelecom.ringotel.net:443");
            this.vault.put(PROFILE_KEY, jSONObject.toString());
            this.vault.remove(PENDING_PHONE_KEY);
            update(snapshotFromProfile(jSONObject, TelephonyRegistrationState.PROVISIONED, "Телефонный профиль активирован"));
            return TelephonyResult.success(null);
        } catch (Exception e) {
            update(copy(TelephonyRegistrationState.OTP_REQUESTED, safeMessage(e)));
            return TelephonyResult.failure("PROVISIONING_CONFIRM_FAILED", safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> connect() {
        if (!this.snapshot.isProvisioned()) {
            return TelephonyResult.failure("NOT_PROVISIONED", "Сначала активируйте телефонию по короткому звонку");
        }
        update(copy(TelephonyRegistrationState.OFFLINE, MEDIA_BLOCK_MESSAGE));
        return TelephonyResult.failure("MEDIA_COMPONENT_MISSING", MEDIA_BLOCK_MESSAGE);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> disconnect() {
        update(copy(this.snapshot.isProvisioned() ? TelephonyRegistrationState.PROVISIONED : TelephonyRegistrationState.UNPROVISIONED, "Телефония отключена"));
        return TelephonyResult.success(null);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> forgetProvisioning() {
        this.vault.remove(PROFILE_KEY);
        this.vault.remove(PENDING_PHONE_KEY);
        update(TelephonySnapshot.unprovisioned());
        return TelephonyResult.success(null);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> hangup() {
        return callUnavailable();
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> placeCall(String str, boolean z) {
        if (this.snapshot.isProvisioned()) {
            return (str != null ? str.replaceAll("[^0-9*#+]", "") : "").isEmpty() ? TelephonyResult.failure("EMPTY_NUMBER", "Введите номер") : TelephonyResult.failure("MEDIA_COMPONENT_MISSING", MEDIA_BLOCK_MESSAGE);
        }
        return TelephonyResult.failure("NOT_PROVISIONED", "Сначала активируйте телефонию");
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Integer> requestProvisioningOtp(String str) {
        try {
            String normalizeRegistrationIdentity = RingotelProvisioningClient.normalizeRegistrationIdentity(str);
            JSONObject requestCode = this.provisioning.requestCode(normalizeRegistrationIdentity);
            int optInt = requestCode.optInt("rcode", requestCode.optInt("code", 0));
            if (optInt > 0) {
                return TelephonyResult.failure("PROVISIONING_REJECTED", serverText(requestCode, optInt));
            }
            this.vault.put(PENDING_PHONE_KEY, normalizeRegistrationIdentity);
            update(new TelephonySnapshot(TelephonyRegistrationState.OTP_REQUESTED, TelecomCallState.IDLE, normalizeRegistrationIdentity, "", "", RingotelProvisioningClient.isEmailIdentity(normalizeRegistrationIdentity) ? "Код входа IT Phone отправлен на e-mail" : "Короткий звонок на контактный номер запрошен", false, false, AudioRoute.EARPIECE, 0L, Arrays.asList("PCMA", "PCMU")));
            return TelephonyResult.success(Integer.valueOf(requestCode.optInt("expires", 600)));
        } catch (Exception e) {
            updateError(e);
            return TelephonyResult.failure("PROVISIONING_REQUEST_FAILED", safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> sendDtmf(char c) {
        return callUnavailable();
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> setAudioRoute(AudioRoute audioRoute) {
        return callUnavailable();
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> setHeld(boolean z) {
        return callUnavailable();
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public synchronized void setListener(TelephonyEngine.Listener listener) {
        this.listener = listener;
        if (listener != null) {
            listener.onTelephonyChanged(this.snapshot);
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> setMuted(boolean z) {
        return callUnavailable();
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public synchronized TelephonySnapshot snapshot() {
        return this.snapshot;
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> startConference(String str) {
        return callUnavailable();
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> transfer(String str) {
        return callUnavailable();
    }
}
