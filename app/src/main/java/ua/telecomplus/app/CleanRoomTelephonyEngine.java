package ua.telecomplus.app;

import android.content.Context;
import android.content.pm.PackageManager;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.JSONObject;
import ua.telecomplus.app.TelephonyEngine;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class CleanRoomTelephonyEngine implements TelephonyEngine {
    private static final String PENDING_PHONE_KEY = "telephony_pending_phone_v1";
    private static final String PROFILE_KEY = "telephony_profile_v1";
    private final Context context;
    private volatile SipUserAgent agent;
    private boolean incoming;
    private volatile RingotelControl control;
    private final CopyOnWriteArrayList<TelephonyEngine.Listener> observers = new CopyOnWriteArrayList<>();
    private int callDir = -1;
    private String callNumber = "";
    private long connectedAt;
    private TelephonyEngine.Listener listener;
    private final RingotelProvisioningClient provisioning;
    private TelephonySnapshot snapshot;
    private final SecureVault vault;

    public CleanRoomTelephonyEngine(Context context, SecureVault secureVault) {
        this.vault = secureVault;
        this.context = context.getApplicationContext();
        this.provisioning = new RingotelProvisioningClient(context);
        this.snapshot = restoreSnapshot();
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
            JSONObject stored = new JSONObject(str);
            if (!this.provisioning.terminalId().equals(stored.optString("termid_used", ""))) {
                this.vault.remove(PROFILE_KEY);
                return TelephonySnapshot.unprovisioned();
            }
            return snapshotFromProfile(stored, TelephonyRegistrationState.PROVISIONED, "Телефонный профиль сохранён и зашифрован");
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
        return new TelephonySnapshot(telephonyRegistrationState, TelecomCallState.IDLE, RingotelProvisioningClient.isEmailIdentity(optString2) ? "" : optString2, jSONObject.optString("login", jSONObject.optString("userid", jSONObject.optString("name", jSONObject.optString("displayname", optString)))), "", str, false, false, AudioRoute.EARPIECE, 0L, Arrays.asList("PCMA", "PCMU"));
    }

    private synchronized void update(TelephonySnapshot telephonySnapshot) {
        this.snapshot = telephonySnapshot;
        TelephonyEngine.Listener listener = this.listener;
        if (listener != null) {
            listener.onTelephonyChanged(telephonySnapshot);
        }
        for (TelephonyEngine.Listener observer : this.observers) {
            try {
                observer.onTelephonyChanged(telephonySnapshot);
            } catch (Exception unused) {
            }
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public void addObserver(TelephonyEngine.Listener listener) {
        this.observers.addIfAbsent(listener);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public void removeObserver(TelephonyEngine.Listener listener) {
        this.observers.remove(listener);
    }

    private void updateError(Exception exc) {
        update(copy(TelephonyRegistrationState.ERROR, safeMessage(exc)));
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> answer() {
        SipUserAgent ua = this.agent;
        if (ua == null) {
            return callUnavailable();
        }
        ua.setMicGranted(micGranted());
        try {
            ua.answer();
            return TelephonyResult.success(null);
        } catch (Exception e) {
            return TelephonyResult.failure("ANSWER_FAILED", safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public void close() {
        stopAgent();
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
            jSONObject.put("termid_used", this.provisioning.terminalId());
            jSONObject.put("server", "intertelecom.ringotel.net:443");
            this.vault.put(PROFILE_KEY, jSONObject.toString());
            this.vault.remove(PENDING_PHONE_KEY);
            update(snapshotFromProfile(jSONObject, TelephonyRegistrationState.PROVISIONED, "Телефонный профиль активирован"));
            connect();
            return TelephonyResult.success(null);
        } catch (Exception e) {
            update(copy(TelephonyRegistrationState.OTP_REQUESTED, safeMessage(e)));
            return TelephonyResult.failure("PROVISIONING_CONFIRM_FAILED", safeMessage(e));
        }
    }


    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<String> beginGoogleLogin() {
        try {
            JSONObject r = this.provisioning.requestOAuth("Google");
            TelephonyLog.log("OAuth request: " + r);
            int code = r.optInt("rcode", r.optInt("code", 0));
            if (code > 0) {
                return TelephonyResult.failure("PROVISIONING_REJECTED", serverText(r, code));
            }
            String location = r.optString("location", "").trim();
            if (location.isEmpty()) {
                return TelephonyResult.failure("NO_LOCATION", "Сервер не вернул страницу входа Google");
            }
            return TelephonyResult.success(location);
        } catch (Exception e) {
            return TelephonyResult.failure("OAUTH_REQUEST_FAILED", safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> finishGoogleLogin() {
        try {
            JSONObject complete = this.provisioning.completeOAuth();
            TelephonyLog.log("OAuth complete: " + complete.toString().replaceAll("\"termpass\":\"[^\"]*\"", "\"termpass\":\"***\""));
            int code = complete.optInt("rcode", complete.optInt("code", 0));
            if (code > 0) {
                return TelephonyResult.failure("PROVISIONING_REJECTED", serverText(complete, code));
            }
            if (complete.optString("termpass", "").trim().isEmpty()) {
                return TelephonyResult.failure("NO_TERMINAL_PASSWORD", "Сервер подтвердил вход, но не выдал терминальный пароль");
            }
            JSONObject profile = new JSONObject(complete.toString());
            String identity = complete.optString("email", complete.optString("login", complete.optString("name", "Google")));
            profile.put("registration_identity", identity);
            profile.put("termid_used", this.provisioning.terminalId());
            profile.put("server", "intertelecom.ringotel.net:443");
            this.vault.put(PROFILE_KEY, profile.toString());
            this.vault.remove(PENDING_PHONE_KEY);
            update(snapshotFromProfile(profile, TelephonyRegistrationState.PROVISIONED, "Вход через Google выполнен"));
            connect();
            return TelephonyResult.success(null);
        } catch (Exception e) {
            return TelephonyResult.failure("OAUTH_COMPLETE_FAILED", safeMessage(e));
        }
    }

    private static volatile long probedAt;

    /** Однократная диагностика: перебираем варианты логина/режима и смотрим, какой принимает сервер. */
    private void probeAuth(final JSONObject profile, final String termpass) {
        if (System.currentTimeMillis() - probedAt < 120000L) {
            return;
        }
        probedAt = System.currentTimeMillis();
        TelephonyLog.pin("probe: старт (diag-8), логин/пароль перебираем");
        final String termid = this.provisioning.terminalId();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    runProbe();
                } catch (Throwable t) {
                    TelephonyLog.log("probe: сбой " + t);
                }
            }

            private void runProbe() {
                String[] users = {termid, profile.optString("userid", ""), profile.optString("registration_identity", ""), android.text.TextUtils.join("", new String[]{})};
                try {
                    JSONObject info = profile.optJSONObject("info");
                    if (info != null && info.optJSONArray("mobile") != null && info.optJSONArray("mobile").length() > 0) {
                        users[3] = info.optJSONArray("mobile").getJSONObject(0).optString("value", "");
                    }
                } catch (Exception ignored) {
                }
                try {
                    java.util.ArrayList<String> ips = new java.util.ArrayList<>(SipWebSocket.resolveAllRingotelIps());
                    if (!ips.contains(SipWebSocket.FALLBACK_IP)) {
                        ips.add(SipWebSocket.FALLBACK_IP);
                    }
                    TelephonyLog.pin("DNS=" + SipWebSocket.resolveAllRingotelIps() + " выбран=" + SipWebSocket.resolveRingotelIp());
                    for (String ip : ips) {
                        SipWebSocket w = new SipWebSocket("intertelecom.ringotel.net", 443, "json.api.smile-soft.com", "Ringotel", new SipWebSocket.Listener() {
                            @Override
                            public void onClosed(String str) {
                            }

                            @Override
                            public void onText(String str) {
                            }
                        });
                        try {
                            w.setDirect(ip, "intertelecom.ringotel.net");
                            w.setBasicAuth(users[0], termpass);
                            w.connect();
                            TelephonyLog.pin("probe ip " + ip + " -> 101 OK");
                        } catch (Exception e) {
                            String m = String.valueOf(e.getMessage());
                            TelephonyLog.pin("probe ip " + ip + " -> " + (m.indexOf(" | ") > 0 ? m.substring(0, m.indexOf(" | ")) : m));
                        } finally {
                            try {
                                w.close();
                            } catch (Exception ignored) {
                            }
                        }
                    }
                } catch (Throwable t) {
                    TelephonyLog.pin("probe ip: сбой " + t);
                }
                String[] uas = {"Ringotel"};
                for (int mode = 1; mode >= 0; mode--) {
                    for (String ua : uas) {
                        for (int i = 0; i < users.length; i++) {
                            if (users[i].isEmpty()) {
                                continue;
                            }
                            String tag = "probe[" + (mode == 1 ? "IP" : "SNI") + " ua=" + ua + " user#" + i + "]";
                            SipWebSocket ws = new SipWebSocket("intertelecom.ringotel.net", 443, "json.api.smile-soft.com", ua, new SipWebSocket.Listener() {
                                @Override
                                public void onClosed(String str) {
                                }

                                @Override
                                public void onText(String str) {
                                }
                            });
                            try {
                                ws.applyDirectMode(mode);
                                ws.setBasicAuth(users[i], termpass);
                                ws.connect();
                                TelephonyLog.pin(tag + " -> 101 OK");
                            } catch (Exception e) {
                                String m = String.valueOf(e.getMessage());
                                int k = m.indexOf(" | ");
                                TelephonyLog.pin(tag + " -> " + (k > 0 ? m.substring(0, k) : m));
                            } finally {
                                try {
                                    ws.close();
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }
                }
            }
        }, "probe-auth").start();
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> connect() {
        if (!this.snapshot.isProvisioned()) {
            return TelephonyResult.failure("NOT_PROVISIONED", "Сначала активируйте телефонию по короткому звонку");
        }
        SipUserAgent running = this.agent;
        if (running != null && running.isRegistered()) {
            return TelephonyResult.success(null);
        }
        String str = this.vault.get(PROFILE_KEY);
        if (str == null || str.trim().isEmpty()) {
            return TelephonyResult.failure("NOT_PROVISIONED", "Профиль телефонии не найден");
        }
        try {
            JSONObject jSONObject = new JSONObject(str);
            String termpass = jSONObject.optString("termpass", "").trim();
            if (termpass.isEmpty()) {
                return TelephonyResult.failure("NO_TERMINAL_PASSWORD", "В профиле нет терминального пароля — активируйте телефонию заново");
            }
            String server = jSONObject.optString("server", "intertelecom.ringotel.net:443");
            String host = server;
            int port = 443;
            int idx = server.lastIndexOf(':');
            if (idx > 0) {
                host = server.substring(0, idx);
                try {
                    port = Integer.parseInt(server.substring(idx + 1));
                } catch (NumberFormatException unused) {
                }
            }
            probeAuth(jSONObject, termpass);
            stopAgent();
            TelephonyLog.log("connect (diag-8): server=" + host + ":" + port + " termid=" + this.provisioning.terminalId());
            try {
                JSONObject dump = new JSONObject(jSONObject.toString());
                if (dump.has("termpass")) {
                    dump.put("termpass", "***(" + termpass.length() + ")");
                }
                TelephonyLog.log("профиль: " + dump);
            } catch (Exception ignored) {
            }
            final SipUserAgent ua = new SipUserAgent(this.context, host, port, this.provisioning.terminalId(), termpass, "Ringotel", new SipUserAgent.Events() {
                @Override
                public void onRegistration(boolean z, String str2, String str3) {
                    CleanRoomTelephonyEngine.this.onRegistration(z, str2, str3);
                }

                @Override
                public void onCallState(TelecomCallState telecomCallState, String str2) {
                    CleanRoomTelephonyEngine.this.onCallState(telecomCallState, str2);
                }

                @Override
                public void onIncomingCall(String str2, String str3) {
                    CleanRoomTelephonyEngine.this.onIncomingCall(str2, str3);
                }
            });
            synchronized (this) {
                this.agent = ua;
                update(copy(TelephonyRegistrationState.CONNECTING, "Подключение к серверу телефонии…"));
            }
            ua.start();
            RingotelControl rc = new RingotelControl(this.context, PhoneData.get(this.vault), host, port, this.provisioning.terminalId(), termpass);
            this.control = rc;
            rc.start();
            return TelephonyResult.success(null);
        } catch (Exception e) {
            TelephonyLog.log("connect: " + e);
            synchronized (this) {
                update(copy(TelephonyRegistrationState.OFFLINE, safeMessage(e)));
            }
            return TelephonyResult.failure("CONNECT_FAILED", safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> disconnect() {
        stopAgent();
        update(copy(this.snapshot.isProvisioned() ? TelephonyRegistrationState.PROVISIONED : TelephonyRegistrationState.UNPROVISIONED, "Телефония отключена"));
        return TelephonyResult.success(null);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> forgetProvisioning() {
        stopAgent();
        this.vault.remove(PROFILE_KEY);
        this.vault.remove(PENDING_PHONE_KEY);
        update(TelephonySnapshot.unprovisioned());
        return TelephonyResult.success(null);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> hangup() {
        SipUserAgent ua = this.agent;
        if (ua == null || !ua.hasCall()) {
            return callUnavailable();
        }
        ua.hangup();
        return TelephonyResult.success(null);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> placeCall(String str, boolean z) {
        if (!this.snapshot.isProvisioned()) {
            return TelephonyResult.failure("NOT_PROVISIONED", "Сначала активируйте телефонию");
        }
        if (SipUserAgent.normalizeNumber(str).isEmpty()) {
            return TelephonyResult.failure("EMPTY_NUMBER", "Введите номер");
        }
        SipUserAgent ua = this.agent;
        if (ua == null || !ua.isRegistered()) {
            connect();
            return TelephonyResult.failure("NOT_CONNECTED", "Телефония ещё подключается. Повторите через несколько секунд.");
        }
        ua.setMicGranted(micGranted());
        try {
            this.incoming = false;
            synchronized (this) {
                this.callDir = PhoneData.CALL_OUT;
                this.callNumber = SipUserAgent.normalizeNumber(str);
                this.connectedAt = 0;
            }
            ua.placeCall(str);
            return TelephonyResult.success(null);
        } catch (Exception e) {
            return TelephonyResult.failure("CALL_FAILED", safeMessage(e));
        }
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
        SipUserAgent ua = this.agent;
        if (ua == null || !ua.hasCall()) {
            return callUnavailable();
        }
        ua.sendDtmf(c);
        return TelephonyResult.success(null);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> setAudioRoute(AudioRoute audioRoute) {
        SipUserAgent ua = this.agent;
        if (ua == null || !ua.hasCall()) {
            return callUnavailable();
        }
        ua.setSpeaker(audioRoute == AudioRoute.SPEAKER);
        TelephonySnapshot s = this.snapshot;
        update(new TelephonySnapshot(s.registrationState, s.callState, s.phoneNumber, s.displayName, s.remoteNumber, s.statusMessage, s.muted, s.held, audioRoute, s.callStartedAt, s.supportedCodecs));
        return TelephonyResult.success(null);
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public TelephonyResult<Void> setHeld(boolean z) {
        SipUserAgent ua = this.agent;
        if (ua == null || !ua.hasCall()) {
            return callUnavailable();
        }
        try {
            ua.setHeld(z);
            return TelephonyResult.success(null);
        } catch (Exception e) {
            return TelephonyResult.failure("HOLD_FAILED", safeMessage(e));
        }
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
        SipUserAgent ua = this.agent;
        if (ua == null || !ua.hasCall()) {
            return callUnavailable();
        }
        ua.setMuted(z);
        TelephonySnapshot s = this.snapshot;
        update(new TelephonySnapshot(s.registrationState, s.callState, s.phoneNumber, s.displayName, s.remoteNumber, s.statusMessage, z, s.held, s.audioRoute, s.callStartedAt, s.supportedCodecs));
        return TelephonyResult.success(null);
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
        SipUserAgent ua = this.agent;
        if (ua == null || !ua.hasCall()) {
            return callUnavailable();
        }
        try {
            ua.transfer(str);
            return TelephonyResult.success(null);
        } catch (Exception e) {
            return TelephonyResult.failure("TRANSFER_FAILED", safeMessage(e));
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public synchronized boolean incomingRinging() {
        return this.incoming && this.snapshot.callState == TelecomCallState.RINGING;
    }

    private boolean micGranted() {
        return this.context.checkSelfPermission("android.permission.RECORD_AUDIO") == PackageManager.PERMISSION_GRANTED;
    }

    private void stopAgent() {
        SipUserAgent ua;
        synchronized (this) {
            ua = this.agent;
            this.agent = null;
            this.incoming = false;
        }
        RingotelControl rc = this.control;
        this.control = null;
        if (rc != null) {
            rc.stop();
        }
        if (ua != null) {
            ua.stop();
        }
    }

    @Override // ua.telecomplus.app.TelephonyEngine
    public RingotelControl control() {
        return this.control;
    }

    private synchronized void onRegistration(boolean z, String str, String str2) {
        if (this.agent == null || !this.snapshot.isProvisioned()) {
            return;
        }
        TelephonySnapshot s = this.snapshot;
        String number = s.phoneNumber;
        update(new TelephonySnapshot(z ? TelephonyRegistrationState.ONLINE : TelephonyRegistrationState.OFFLINE, s.callState, number, s.displayName, s.remoteNumber, str, s.muted, s.held, s.audioRoute, s.callStartedAt, s.supportedCodecs));
    }

    private synchronized void onIncomingCall(String str, String str2) {
        if (PhoneData.get(this.vault).isBlocked(str)) {
            TelephonyLog.log("Входящий от заблокированного номера отклонён: " + str);
            final SipUserAgent blockedAgent = this.agent;
            if (blockedAgent != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        blockedAgent.hangup();
                    }
                }).start();
            }
            return;
        }
        this.incoming = true;
        this.callDir = PhoneData.CALL_IN;
        this.callNumber = str == null ? "" : str;
        this.connectedAt = 0;
        TelephonySnapshot s = this.snapshot;
        String known = PhoneData.get(this.vault).nameFor(str);
        if (known != null) {
            str2 = known;
        }
        String who = (str2 == null || str2.isEmpty()) ? str : str2 + " (" + str + ")";
        update(new TelephonySnapshot(s.registrationState, TelecomCallState.RINGING, s.phoneNumber, s.displayName, str, "Входящий вызов: " + who, false, false, AudioRoute.EARPIECE, 0L, s.supportedCodecs));
    }

    private synchronized void onCallState(TelecomCallState telecomCallState, String str) {
        TelephonySnapshot s = this.snapshot;
        if (telecomCallState == TelecomCallState.ENDED || telecomCallState == TelecomCallState.FAILED) {
            if (this.callDir >= 0 && !this.callNumber.isEmpty()) {
                long now = System.currentTimeMillis();
                if (this.connectedAt > 0) {
                    PhoneData.get(this.vault).addCall(this.callNumber, this.callDir, this.connectedAt, (int) ((now - this.connectedAt) / 1000));
                } else {
                    PhoneData.get(this.vault).addCall(this.callNumber, this.callDir == PhoneData.CALL_IN ? PhoneData.CALL_MISSED : PhoneData.CALL_OUT, now, 0);
                }
            }
            this.callDir = -1;
            this.callNumber = "";
            this.connectedAt = 0;
            this.incoming = false;
            update(new TelephonySnapshot(s.registrationState, TelecomCallState.IDLE, s.phoneNumber, s.displayName, "", str, false, false, AudioRoute.EARPIECE, 0L, s.supportedCodecs));
            return;
        }
        long started = s.callStartedAt;
        if ((telecomCallState == TelecomCallState.ACTIVE || telecomCallState == TelecomCallState.HELD) && started == 0) {
            started = System.currentTimeMillis();
            this.connectedAt = started;
        }
        String remote = s.remoteNumber;
        if (telecomCallState == TelecomCallState.DIALING && str.startsWith("Вызов ")) {
            remote = str.substring(6);
        }
        update(new TelephonySnapshot(s.registrationState, telecomCallState, s.phoneNumber, s.displayName, remote, str, s.muted, telecomCallState == TelecomCallState.HELD, s.audioRoute, started, s.supportedCodecs));
    }
}
