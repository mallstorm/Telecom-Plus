package ua.telecomplus.app;

import android.content.Context;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * SIP-клиент поверх WSS для сервера телефонии Интертелекома (Ringotel).
 * Регистрация: REGISTER с Authorization: Basic termid:termpass (как у официального IT Phone),
 * Digest-аутентификация — как запасной вариант. Один вызов одновременно.
 */
final class SipUserAgent implements SipWebSocket.Listener {
    interface Events {
        void onCallState(TelecomCallState telecomCallState, String str);

        void onIncomingCall(String str, String str2);

        void onRegistration(boolean z, String str, String str2);
    }

    private static final String SUBPROTOCOL = "sip.smile-soft.com";
    private static final String ALLOWED = "INVITE, ACK, OPTIONS, CANCEL, BYE, UPDATE, REFER, NOTIFY, INFO, SUBSCRIBE, MESSAGE";
    private static final String LOCAL = "127.0.0.1";
    private static final int LOCAL_PORT = 5060;
    private static final int REGISTER_EXPIRES = 600;

    /** Состояние одного вызова. */
    private final class Call {
        boolean outgoing;
        String callId;
        String number = "";
        String display = "";
        String localTag;
        String remoteTag = "";
        String fromHeader;
        String toHeader;
        String remoteTarget;
        List<String> routeSet = new ArrayList<>();
        int cseq = 1;
        SipMessage invite;
        String inviteBranch;
        SipMessage incomingInvite;
        Sdp remoteSdp;
        RtpSession rtp;
        boolean answered;
        boolean canceled;
        boolean held;
        boolean muted;
        boolean authTried;
        long sessionId = System.currentTimeMillis() / 1000;
        int sdpVersion = 1;
        int pendingHoldTarget = -1;
    }

    private final Context context;
    private final String host;
    private final int port;
    private final String domain;
    private final String termId;
    private final String termPass;
    private final String userAgent;
    private final Events events;
    private final SecureRandom random = new SecureRandom();
    private final Object lock = new Object();
    private volatile boolean stopped = true;
    private volatile boolean registered;
    private volatile String sipUser;
    private SipWebSocket socket;
    private Thread worker;
    private int regCseq = 1;
    private String regCallId = newId();
    private String regTag = newToken();
    private String regAuth;
    private boolean regAuthTried;
    private int regExpires = REGISTER_EXPIRES;
    private long lastRegister;
    private Call call;
    private boolean micGranted = true;

    SipUserAgent(Context context, String str, int i, String str2, String str3, String str4, Events events) {
        this.context = context.getApplicationContext();
        this.host = str;
        this.port = i;
        this.domain = str + ":" + i;
        this.termId = str2;
        this.termPass = str3;
        this.userAgent = str4;
        this.events = events;
    }

    // ---------- публичный API ----------

    void start() {
        synchronized (this.lock) {
            if (!this.stopped) {
                return;
            }
            this.stopped = false;
        }
        this.worker = new Thread(this::workerLoop, "sip-worker");
        this.worker.setDaemon(true);
        this.worker.start();
    }

    void stop() {
        synchronized (this.lock) {
            if (this.stopped) {
                return;
            }
            this.stopped = true;
        }
        try {
            if (this.call != null) {
                hangup();
            }
            if (this.registered && this.socket != null && this.socket.isOpen()) {
                sendRegister(0);
                Thread.sleep(150);
            }
        } catch (Exception unused) {
        }
        this.registered = false;
        SipWebSocket ws = this.socket;
        if (ws != null) {
            ws.close();
        }
        if (this.worker != null) {
            this.worker.interrupt();
        }
    }

    boolean isRegistered() {
        return this.registered;
    }

    String sipUser() {
        return this.sipUser;
    }

    void setMicGranted(boolean z) {
        this.micGranted = z;
    }

    boolean hasCall() {
        return this.call != null;
    }

    void placeCall(String str) throws Exception {
        synchronized (this.lock) {
            if (!this.registered || this.socket == null || !this.socket.isOpen()) {
                throw new IllegalStateException("Телефония не подключена");
            }
            if (this.call != null) {
                throw new IllegalStateException("Уже идёт вызов");
            }
            String number = normalizeNumber(str);
            if (number.isEmpty()) {
                throw new IllegalArgumentException("Введите номер");
            }
            Call c = new Call();
            c.outgoing = true;
            c.number = number;
            c.callId = newId();
            c.localTag = newToken();
            String user = this.sipUser == null ? "" : this.sipUser + "@";
            c.fromHeader = "<sip:" + user + this.domain + ">;tag=" + c.localTag;
            c.toHeader = "<sip:" + number + "@" + this.domain + ">";
            c.remoteTarget = "sip:" + number + "@" + this.domain + ";transport=wss";
            c.rtp = new RtpSession(this.context);
            this.call = c;
            sendInvite(c);
            this.events.onCallState(TelecomCallState.DIALING, "Вызов " + number);
        }
    }

    void answer() throws Exception {
        synchronized (this.lock) {
            Call c = this.call;
            if (c == null || c.outgoing || c.answered || c.incomingInvite == null) {
                throw new IllegalStateException("Нет входящего вызова");
            }
            int codec = c.remoteSdp.chooseCodec();
            c.rtp = new RtpSession(this.context);
            String body = Sdp.answer(this.sipUser, c.rtp.localPort(), c.sessionId, c.sdpVersion, "sendrecv", codec, c.remoteSdp.dtmfPayload >= 0 ? c.remoteSdp.dtmfPayload : 101);
            SipMessage ok = responseTo(c.incomingInvite, 200, "OK", c.localTag);
            ok.add("Contact", contact());
            ok.add("Allow", ALLOWED);
            ok.add("User-Agent", this.userAgent);
            ok.add("Content-Type", "application/sdp");
            ok.body = body;
            send(ok);
            c.answered = true;
            startMedia(c, codec);
            this.events.onCallState(TelecomCallState.ACTIVE, "Разговор");
        }
    }

    void hangup() {
        synchronized (this.lock) {
            Call c = this.call;
            if (c == null) {
                return;
            }
            try {
                if (!c.outgoing && !c.answered) {
                    send(responseTo(c.incomingInvite, 603, "Decline", c.localTag));
                    finishCall(TelecomCallState.ENDED, "Вызов отклонён");
                } else if (c.outgoing && !c.answered) {
                    c.canceled = true;
                    sendCancel(c);
                    // дальше ждём 487; на случай, если ответ не придёт — завершим локально
                    scheduleForceEnd(c, 4000);
                } else {
                    sendInDialog(c, "BYE", null, null);
                    finishCall(TelecomCallState.ENDED, "Вызов завершён");
                }
            } catch (Exception e) {
                TelephonyLog.log("hangup: " + e);
                finishCall(TelecomCallState.ENDED, "Вызов завершён");
            }
        }
    }

    void setMuted(boolean z) {
        synchronized (this.lock) {
            if (this.call != null) {
                this.call.muted = z;
                if (this.call.rtp != null) {
                    this.call.rtp.setMuted(z);
                }
            }
        }
    }

    void setSpeaker(boolean z) {
        synchronized (this.lock) {
            if (this.call != null && this.call.rtp != null) {
                this.call.rtp.setSpeaker(z);
            }
        }
    }

    void sendDtmf(char c) {
        synchronized (this.lock) {
            if (this.call != null && this.call.rtp != null && this.call.answered) {
                this.call.rtp.sendDtmf(c);
            }
        }
    }

    void setHeld(boolean z) throws Exception {
        synchronized (this.lock) {
            Call c = this.call;
            if (c == null || !c.answered) {
                throw new IllegalStateException("Нет активного вызова");
            }
            c.sdpVersion++;
            c.pendingHoldTarget = z ? 1 : 0;
            String body = Sdp.answer(this.sipUser, c.rtp.localPort(), c.sessionId, c.sdpVersion, z ? "sendonly" : "sendrecv", c.remoteSdp.chooseCodec(), c.remoteSdp.dtmfPayload >= 0 ? c.remoteSdp.dtmfPayload : 101);
            sendInDialog(c, "INVITE", "application/sdp", body);
        }
    }

    void transfer(String str) throws Exception {
        synchronized (this.lock) {
            Call c = this.call;
            if (c == null || !c.answered) {
                throw new IllegalStateException("Нет активного вызова");
            }
            String number = normalizeNumber(str);
            if (number.isEmpty()) {
                throw new IllegalArgumentException("Введите номер");
            }
            sendInDialog(c, "REFER", null, null, "Refer-To: <sip:" + number + "@" + this.domain + ">");
        }
    }

    // ---------- рабочий поток: подключение и регистрация ----------

    private void workerLoop() {
        int backoff = 2000;
        while (!this.stopped) {
            try {
                connectAndRegister();
                backoff = 2000;
                long lastPing = System.currentTimeMillis();
                while (!this.stopped && this.socket != null && this.socket.isOpen()) {
                    Thread.sleep(1000);
                    long now = System.currentTimeMillis();
                    if (now - lastPing >= 20000) {
                        lastPing = now;
                        this.socket.sendPing();
                    }
                    if (this.registered && now - this.lastRegister >= (long) (this.regExpires * 600)) {
                        synchronized (this.lock) {
                            sendRegister(this.regExpires);
                        }
                    }
                }
            } catch (InterruptedException unused) {
                break;
            } catch (Exception e) {
                TelephonyLog.log("Подключение: " + e);
                this.events.onRegistration(false, "Нет связи с сервером телефонии: " + e.getMessage(), null);
            }
            this.registered = false;
            if (this.stopped) {
                break;
            }
            this.events.onRegistration(false, "Соединение потеряно, переподключаюсь…", null);
            try {
                Thread.sleep(backoff);
            } catch (InterruptedException unused2) {
                break;
            }
            backoff = Math.min(backoff * 2, 30000);
        }
    }

    private void connectAndRegister() throws Exception {
        TelephonyLog.log("Подключение к wss://" + this.host + ":" + this.port + " (" + SUBPROTOCOL + ")");
        SipWebSocket ws = new SipWebSocket(this.host, this.port, SUBPROTOCOL, this.userAgent, this);
        this.socket = ws;
        ws.connect();
        TelephonyLog.log("WebSocket открыт");
        this.regAuth = null;
        this.regAuthTried = false;
        synchronized (this.lock) {
            sendRegister(REGISTER_EXPIRES);
        }
    }

    // ---------- формирование запросов ----------

    private SipMessage baseRequest(String str, String str2, String str3, String str4, String str5, int i) {
        SipMessage m = SipMessage.request(str, str2);
        m.add("Via", "SIP/2.0/WSS " + LOCAL + ":" + LOCAL_PORT + ";branch=z9hG4bK" + newToken());
        m.add("Max-Forwards", "70");
        m.add("From", str3);
        m.add("To", str4);
        m.add("Call-ID", str5);
        m.add("CSeq", i + " " + str);
        return m;
    }

    private String contact() {
        String user = this.sipUser == null ? "" : this.sipUser + "@";
        return "<sip:" + user + LOCAL + ";transport=wss>";
    }

    private void sendRegister(int i) {
        String user = this.sipUser == null ? "" : this.sipUser + "@";
        String addr = "<sip:" + user + this.domain + ">";
        SipMessage m = baseRequest("REGISTER", "sip:" + this.domain + ";transport=wss", addr + ";tag=" + this.regTag, addr, this.regCallId, this.regCseq++);
        m.add("Contact", contact());
        m.add("User-Agent", this.userAgent);
        m.add("Expires", String.valueOf(i));
        m.add("Allow", ALLOWED);
        m.add("Authorization", this.regAuth != null ? this.regAuth : "Basic " + Base64.encodeToString((this.termId + ":" + this.termPass).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP));
        this.lastRegister = System.currentTimeMillis();
        send(m);
    }

    private void sendInvite(Call c) {
        SipMessage m = baseRequest("INVITE", c.remoteTarget, c.fromHeader, c.toHeader, c.callId, c.cseq);
        c.inviteBranch = branchOf(m);
        m.add("Contact", contact());
        m.add("User-Agent", this.userAgent);
        m.add("Allow", ALLOWED);
        m.add("Content-Type", "application/sdp");
        m.body = Sdp.offer(this.sipUser, c.rtp.localPort(), c.sessionId, c.sdpVersion, "sendrecv");
        c.invite = m;
        send(m);
    }

    private void sendCancel(Call c) {
        SipMessage m = SipMessage.request("CANCEL", c.invite.uri);
        m.add("Via", c.invite.get("Via"));
        m.add("Max-Forwards", "70");
        m.add("From", c.fromHeader);
        m.add("To", c.toHeader);
        m.add("Call-ID", c.callId);
        m.add("CSeq", c.invite.cseqNumber() + " CANCEL");
        m.add("User-Agent", this.userAgent);
        send(m);
    }

    private void sendInDialog(Call c, String str, String str2, String str3, String... extra) {
        String from;
        String to;
        if (c.outgoing) {
            from = c.fromHeader;
            to = c.toHeader + (c.remoteTag.isEmpty() ? "" : ";tag=" + c.remoteTag);
        } else {
            from = withTag(c.toHeader, c.localTag);
            to = withTag(c.fromHeader, c.remoteTag);
        }
        SipMessage m = baseRequest(str, c.remoteTarget, from, to, c.callId, ++c.cseq);
        for (String route : c.routeSet) {
            m.add("Route", route);
        }
        m.add("Contact", contact());
        m.add("User-Agent", this.userAgent);
        for (String header : extra) {
            int i = header.indexOf(':');
            m.add(header.substring(0, i), header.substring(i + 1).trim());
        }
        if (str2 != null) {
            m.add("Content-Type", str2);
            m.body = str3;
        }
        send(m);
    }

    private static String withTag(String str, String str2) {
        if (str2 == null || str2.isEmpty() || str.toLowerCase(Locale.ROOT).contains(";tag=")) {
            return str;
        }
        return str + ";tag=" + str2;
    }

    private SipMessage responseTo(SipMessage req, int code, String reason, String toTag) {
        SipMessage m = SipMessage.response(code, reason);
        for (String via : req.getAll("Via")) {
            m.add("Via", via);
        }
        for (String rr : req.getAll("Record-Route")) {
            m.add("Record-Route", rr);
        }
        m.add("From", req.get("From"));
        String to = req.get("To");
        if (toTag != null && code > 100) {
            to = withTag(to, toTag);
        }
        m.add("To", to);
        m.add("Call-ID", req.callId());
        m.add("CSeq", req.get("CSeq"));
        return m;
    }

    private void send(SipMessage m) {
        SipWebSocket ws = this.socket;
        if (ws == null) {
            return;
        }
        try {
            TelephonyLog.log("→ " + firstLine(m) + (m.body.isEmpty() ? "" : " (SDP)"));
            ws.sendText(m.toString());
        } catch (Exception e) {
            TelephonyLog.log("Ошибка отправки: " + e);
        }
    }

    private static String firstLine(SipMessage m) {
        return m.isRequest ? m.method + " " + m.uri : m.status + " " + m.reason + " [" + m.cseqMethod() + "]";
    }

    // ---------- приём ----------

    @Override
    public void onText(String str) {
        String trimmed = str.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        SipMessage m = SipMessage.parse(str);
        if (m == null) {
            TelephonyLog.log("Не удалось разобрать SIP-сообщение");
            return;
        }
        TelephonyLog.log("← " + firstLine(m) + (m.body.isEmpty() ? "" : " (SDP)"));
        try {
            synchronized (this.lock) {
                if (m.isRequest) {
                    handleRequest(m);
                } else {
                    handleResponse(m);
                }
            }
        } catch (Exception e) {
            TelephonyLog.log("Ошибка обработки SIP: " + e);
        }
    }

    @Override
    public void onClosed(String str) {
        TelephonyLog.log("WebSocket закрыт: " + str);
        this.registered = false;
        synchronized (this.lock) {
            if (this.call != null) {
                finishCall(TelecomCallState.FAILED, "Связь с сервером потеряна");
            }
        }
    }

    private void handleResponse(SipMessage m) throws Exception {
        String method = m.cseqMethod();
        if (method.equals("REGISTER")) {
            handleRegisterResponse(m);
            return;
        }
        Call c = this.call;
        if (c == null || !m.callId().equals(c.callId)) {
            return;
        }
        if (method.equals("INVITE")) {
            handleInviteResponse(c, m);
        } else if (method.equals("CANCEL")) {
            // ждём 487 на INVITE
        } else if (method.equals("BYE") && c.answered) {
            finishCall(TelecomCallState.ENDED, "Вызов завершён");
        }
    }

    private void handleRegisterResponse(SipMessage m) throws Exception {
        int s = m.status;
        if (s == 200) {
            this.registered = true;
            String user = null;
            int expires = this.regExpires;
            for (String contact : m.getAll("Contact")) {
                String uri = SipMessage.uriOf(contact);
                if (uri.contains(LOCAL)) {
                    String u = SipMessage.userOf(contact);
                    if (!u.isEmpty()) {
                        user = u;
                    }
                    String e = SipMessage.param(contact, "expires");
                    if (e != null) {
                        try {
                            expires = Integer.parseInt(e);
                        } catch (NumberFormatException unused) {
                        }
                    }
                }
            }
            if (user != null) {
                this.sipUser = user;
            }
            String expHeader = m.get("Expires");
            if (expHeader != null) {
                try {
                    expires = Integer.parseInt(expHeader.trim());
                } catch (NumberFormatException unused2) {
                }
            }
            if (expires > 0) {
                this.regExpires = Math.max(30, expires);
            }
            TelephonyLog.log("Зарегистрирован: sipUser=" + this.sipUser + " expires=" + this.regExpires);
            this.events.onRegistration(true, "Телефония подключена", this.sipUser);
        } else if ((s == 401 || s == 407) && !this.regAuthTried) {
            String challenge = m.get(s == 407 ? "Proxy-Authenticate" : "WWW-Authenticate");
            if (challenge != null && challenge.toLowerCase(Locale.ROOT).startsWith("digest")) {
                this.regAuthTried = true;
                String user = this.sipUser == null ? "" : this.sipUser + "@";
                this.regAuth = digest(challenge, "REGISTER", "sip:" + this.domain + ";transport=wss");
                sendRegister(this.regExpires);
            } else {
                this.events.onRegistration(false, "Сервер отклонил учётные данные (" + s + ")", null);
            }
        } else if (s >= 200) {
            this.events.onRegistration(false, "Регистрация отклонена: " + s + " " + m.reason, null);
        }
    }

    private void handleInviteResponse(Call c, SipMessage m) throws Exception {
        int s = m.status;
        if (m.cseqNumber() != c.invite.cseqNumber()) {
            return;
        }
        String to = m.get("To");
        String tag = SipMessage.param(to, "tag");
        if (s >= 101 && s < 200) {
            if (tag != null) {
                c.remoteTag = tag;
            }
            this.events.onCallState(TelecomCallState.RINGING, s == 180 ? "Идёт вызов…" : "Соединение…");
            return;
        }
        if (s >= 200 && s < 300) {
            if (tag != null) {
                c.remoteTag = tag;
            }
            String contact = m.get("Contact");
            if (contact != null) {
                c.remoteTarget = SipMessage.uriOf(contact);
            }
            c.routeSet = new ArrayList<>();
            for (String rr : m.getAll("Record-Route")) {
                c.routeSet.add(rr);
            }
            Collections.reverse(c.routeSet);
            if (c.answered) {
                // ответ на re-INVITE (удержание)
                sendAck(c, m, true);
                if (c.pendingHoldTarget >= 0) {
                    c.held = c.pendingHoldTarget == 1;
                    c.pendingHoldTarget = -1;
                    this.events.onCallState(c.held ? TelecomCallState.HELD : TelecomCallState.ACTIVE, c.held ? "Удержание" : "Разговор");
                }
                return;
            }
            sendAck(c, m, true);
            Sdp sdp = Sdp.parse(m.body);
            c.remoteSdp = sdp;
            int codec = sdp.chooseCodec();
            if (codec < 0 || sdp.savp) {
                TelephonyLog.log("Несовместимый ответ: codec=" + codec + " savp=" + sdp.savp);
                c.answered = true;
                sendInDialog(c, "BYE", null, null);
                finishCall(TelecomCallState.FAILED, sdp.savp ? "Сервер требует SRTP (пока не поддерживается)" : "Нет общего кодека (поддерживаются PCMA/PCMU)");
                return;
            }
            c.answered = true;
            startMedia(c, codec);
            this.events.onCallState(TelecomCallState.ACTIVE, "Разговор");
            return;
        }
        // 3xx–6xx
        if ((s == 401 || s == 407) && !c.authTried) {
            String challenge = m.get(s == 407 ? "Proxy-Authenticate" : "WWW-Authenticate");
            if (challenge != null && challenge.toLowerCase(Locale.ROOT).startsWith("digest")) {
                c.authTried = true;
                sendAck(c, m, false);
                c.cseq++;
                SipMessage again = baseRequest("INVITE", c.remoteTarget, c.fromHeader, c.toHeader, c.callId, c.cseq);
                c.inviteBranch = branchOf(again);
                again.add("Contact", contact());
                again.add("User-Agent", this.userAgent);
                again.add("Allow", ALLOWED);
                again.add("Content-Type", "application/sdp");
                again.body = c.invite.body;
                again.add(s == 407 ? "Proxy-Authorization" : "Authorization", digest(challenge, "INVITE", c.remoteTarget));
                c.invite = again;
                send(again);
                return;
            }
        }
        if (tag != null) {
            c.remoteTag = tag;
        }
        sendAck(c, m, false);
        if (c.canceled || s == 487) {
            finishCall(TelecomCallState.ENDED, "Вызов отменён");
        } else {
            finishCall(TelecomCallState.FAILED, describeFailure(s, m.reason));
        }
    }

    private static String describeFailure(int i, String str) {
        switch (i) {
            case 404:
                return "Номер не найден (404)";
            case 480:
                return "Абонент недоступен (480)";
            case 486:
            case 600:
                return "Абонент занят (" + i + ")";
            case 403:
                return "Вызов запрещён (403)";
            case 402:
                return "Недостаточно средств (402)";
            case 488:
            case 606:
                return "Вызов не принят: несовместимые параметры (" + i + ")";
            case 603:
                return "Вызов отклонён (603)";
            default:
                return "Ошибка вызова: " + i + " " + str;
        }
    }

    private void sendAck(Call c, SipMessage resp, boolean twoXX) {
        SipMessage ack;
        if (twoXX) {
            ack = baseRequest("ACK", c.remoteTarget, c.fromHeader, c.toHeader + (c.remoteTag.isEmpty() ? "" : ";tag=" + c.remoteTag), c.callId, resp.cseqNumber());
            for (String route : c.routeSet) {
                ack.add("Route", route);
            }
        } else {
            ack = SipMessage.request("ACK", c.invite.uri);
            ack.add("Via", c.invite.get("Via"));
            ack.add("Max-Forwards", "70");
            ack.add("From", c.fromHeader);
            ack.add("To", resp.get("To"));
            ack.add("Call-ID", c.callId);
            ack.add("CSeq", resp.cseqNumber() + " ACK");
        }
        send(ack);
    }

    private void handleRequest(SipMessage m) throws Exception {
        String method = m.method;
        if (method.equals("INVITE")) {
            handleInvite(m);
        } else if (method.equals("ACK")) {
            // подтверждение нашего 200 OK — ничего не делаем
        } else if (method.equals("BYE")) {
            send(responseTo(m, 200, "OK", null));
            if (this.call != null && m.callId().equals(this.call.callId)) {
                finishCall(TelecomCallState.ENDED, "Собеседник завершил вызов");
            }
        } else if (method.equals("CANCEL")) {
            send(responseTo(m, 200, "OK", null));
            Call c = this.call;
            if (c != null && !c.outgoing && !c.answered && m.callId().equals(c.callId)) {
                send(responseTo(c.incomingInvite, 487, "Request Terminated", c.localTag));
                finishCall(TelecomCallState.ENDED, "Пропущенный вызов");
            }
        } else if (method.equals("OPTIONS")) {
            SipMessage ok = responseTo(m, 200, "OK", null);
            ok.add("Allow", ALLOWED);
            send(ok);
        } else if (method.equals("UPDATE")) {
            handleReinviteLike(m);
        } else if (method.equals("NOTIFY") || method.equals("INFO") || method.equals("MESSAGE") || method.equals("REFER") || method.equals("SUBSCRIBE")) {
            send(responseTo(m, method.equals("REFER") || method.equals("SUBSCRIBE") ? 501 : 200, method.equals("REFER") || method.equals("SUBSCRIBE") ? "Not Implemented" : "OK", null));
        } else {
            send(responseTo(m, 405, "Method Not Allowed", null));
        }
    }

    private void handleInvite(SipMessage m) throws Exception {
        Call c = this.call;
        if (c != null && m.callId().equals(c.callId)) {
            handleReinviteLike(m);
            return;
        }
        if (c != null) {
            send(responseTo(m, 486, "Busy Here", newToken()));
            return;
        }
        Sdp sdp = Sdp.parse(m.body);
        if (sdp.savp || sdp.chooseCodec() < 0) {
            TelephonyLog.log("Входящий вызов отклонён: codec=" + sdp.chooseCodec() + " savp=" + sdp.savp);
            send(responseTo(m, 488, "Not Acceptable Here", newToken()));
            return;
        }
        Call n = new Call();
        n.outgoing = false;
        n.callId = m.callId();
        n.incomingInvite = m;
        n.remoteSdp = sdp;
        n.localTag = newToken();
        n.remoteTag = SipMessage.param(m.get("From"), "tag") == null ? "" : SipMessage.param(m.get("From"), "tag");
        n.fromHeader = m.get("From");
        n.toHeader = m.get("To");
        n.number = SipMessage.userOf(m.get("From"));
        n.display = SipMessage.displayNameOf(m.get("From"));
        String contact = m.get("Contact");
        n.remoteTarget = contact == null ? "sip:" + n.number + "@" + this.domain : SipMessage.uriOf(contact);
        n.routeSet = new ArrayList<>(m.getAll("Record-Route"));
        n.cseq = (int) (Math.random() * 1000.0d) + 1;
        this.call = n;
        send(responseTo(m, 100, "Trying", null));
        SipMessage ringing = responseTo(m, 180, "Ringing", n.localTag);
        ringing.add("Contact", contact());
        send(ringing);
        this.events.onIncomingCall(n.number, n.display);
    }

    /** re-INVITE / UPDATE от сервера или собеседника (удержание, продление сессии). */
    private void handleReinviteLike(SipMessage m) throws Exception {
        Call c = this.call;
        if (c == null || !c.answered || c.rtp == null) {
            send(responseTo(m, 481, "Call/Transaction Does Not Exist", null));
            return;
        }
        Sdp sdp = m.body.isEmpty() ? c.remoteSdp : Sdp.parse(m.body);
        boolean remoteHold = sdp != null && sdp.isHold();
        String direction = remoteHold ? (sdp.direction.equals("inactive") ? "inactive" : "recvonly") : "sendrecv";
        if (c.held) {
            direction = remoteHold ? "inactive" : "sendonly";
        }
        c.sdpVersion++;
        SipMessage ok = responseTo(m, 200, "OK", c.localTag);
        ok.add("Contact", contact());
        ok.add("Allow", ALLOWED);
        if (!m.body.isEmpty() || m.method.equals("INVITE")) {
            ok.add("Content-Type", "application/sdp");
            ok.body = Sdp.answer(this.sipUser, c.rtp.localPort(), c.sessionId, c.sdpVersion, direction, c.remoteSdp.chooseCodec(), c.remoteSdp.dtmfPayload >= 0 ? c.remoteSdp.dtmfPayload : 101);
        }
        send(ok);
        if (sdp != null && !sdp.address.isEmpty() && sdp.port > 0 && !sdp.isHold()) {
            c.remoteSdp = sdp;
            c.rtp.retarget(sdp.address, sdp.port);
        }
        if (!c.held) {
            this.events.onCallState(remoteHold ? TelecomCallState.HELD : TelecomCallState.ACTIVE, remoteHold ? "Собеседник поставил на удержание" : "Разговор");
        }
    }

    // ---------- медиа и завершение ----------

    private void startMedia(Call c, int codec) {
        Sdp sdp = c.remoteSdp;
        String address = sdp.address.isEmpty() ? this.host : sdp.address;
        c.rtp.start(address, sdp.port, codec, sdp.dtmfPayload, this.micGranted);
        c.rtp.setMuted(c.muted);
    }

    private void finishCall(TelecomCallState telecomCallState, String str) {
        Call c = this.call;
        this.call = null;
        if (c != null && c.rtp != null) {
            c.rtp.discard();
        }
        this.events.onCallState(telecomCallState, str);
    }

    private void scheduleForceEnd(final Call c, final long j) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(j);
                } catch (InterruptedException unused) {
                    return;
                }
                synchronized (SipUserAgent.this.lock) {
                    if (SipUserAgent.this.call == c) {
                        SipUserAgent.this.finishCall(TelecomCallState.ENDED, "Вызов отменён");
                    }
                }
            }
        }, "sip-force-end");
        t.setDaemon(true);
        t.start();
    }

    // ---------- вспомогательное ----------

    static String normalizeNumber(String str) {
        String s = str == null ? "" : str.replaceAll("[^0-9*#+]", "");
        if (s.startsWith("+")) {
            s = s.substring(1);
        } else if (s.length() == 10 && s.startsWith("0")) {
            s = "38" + s;
        }
        return s;
    }

    private String newToken() {
        byte[] b = new byte[6];
        this.random.nextBytes(b);
        StringBuilder sb = new StringBuilder();
        for (byte x : b) {
            sb.append(String.format(Locale.ROOT, "%02x", x & 255));
        }
        return sb.toString();
    }

    private String newId() {
        return newToken() + newToken() + "@telecomplus";
    }

    private static String branchOf(SipMessage m) {
        String via = m.get("Via");
        int i = via == null ? -1 : via.indexOf("branch=");
        return i < 0 ? "" : via.substring(i + 7).split("[;,]")[0];
    }

    private String digest(String challenge, String method, String uri) throws Exception {
        String realm = quoted(challenge, "realm");
        String nonce = quoted(challenge, "nonce");
        String opaque = quoted(challenge, "opaque");
        String qop = quoted(challenge, "qop");
        String algorithm = quoted(challenge, "algorithm");
        if (algorithm != null && !algorithm.equalsIgnoreCase("MD5")) {
            throw new IllegalStateException("Алгоритм digest не поддерживается: " + algorithm);
        }
        String ha1 = md5(this.termId + ":" + realm + ":" + this.termPass);
        String ha2 = md5(method + ":" + uri);
        StringBuilder sb = new StringBuilder("Digest username=\"" + this.termId + "\", realm=\"" + realm + "\", nonce=\"" + nonce + "\", uri=\"" + uri + "\"");
        if (qop != null && qop.contains("auth")) {
            String cnonce = newToken();
            String nc = "00000001";
            String response = md5(ha1 + ":" + nonce + ":" + nc + ":" + cnonce + ":auth:" + ha2);
            sb.append(", response=\"").append(response).append("\", qop=auth, nc=").append(nc).append(", cnonce=\"").append(cnonce).append("\"");
        } else {
            sb.append(", response=\"").append(md5(ha1 + ":" + nonce + ":" + ha2)).append("\"");
        }
        if (opaque != null) {
            sb.append(", opaque=\"").append(opaque).append("\"");
        }
        sb.append(", algorithm=MD5");
        return sb.toString();
    }

    private static String quoted(String str, String str2) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("(?i)\\b" + str2 + "=(\"([^\"]*)\"|([^,\\s]+))").matcher(str);
        if (!m.find()) {
            return null;
        }
        return m.group(2) != null ? m.group(2) : m.group(3);
    }

    private static String md5(String str) throws Exception {
        byte[] d = MessageDigest.getInstance("MD5").digest(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) {
            sb.append(String.format(Locale.ROOT, "%02x", b & 255));
        }
        return sb.toString();
    }
}
