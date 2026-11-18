package ua.telecomplus.app;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/** RTP-сессия G.711 (PCMA/PCMU, 8 кГц, 20 мс) с захватом/воспроизведением и RFC 2833 DTMF. */
final class RtpSession {
    private static final int RATE = 8000;
    private static final int FRAME = 160;

    private final Context context;
    private final DatagramSocket socket;
    private final int localPort;
    private final ArrayBlockingQueue<short[]> playQueue = new ArrayBlockingQueue<>(40);
    private final Object sendLock = new Object();
    private volatile boolean running;
    private volatile boolean muted;
    private volatile InetSocketAddress remote;
    private volatile boolean latched;
    private int payloadType = Sdp.PCMA;
    private int dtmfPayload = -1;
    private int seq;
    private long timestamp;
    private final int ssrc = (int) (Math.random() * 2.147483647E9d);
    private boolean firstPacket = true;
    private AudioRecord record;
    private AudioTrack track;
    private AcousticEchoCanceler aec;
    private NoiseSuppressor ns;
    private Thread sendThread;
    private Thread recvThread;
    private Thread playThread;
    private AudioManager audioManager;
    private int previousMode = AudioManager.MODE_NORMAL;
    private long rxPackets;
    private long txPackets;

    RtpSession(Context context) throws Exception {
        this.context = context.getApplicationContext();
        this.socket = new DatagramSocket(0);
        this.localPort = this.socket.getLocalPort();
        this.seq = (int) (Math.random() * 60000.0d);
        this.timestamp = (long) (Math.random() * 1.0E9d);
    }

    int localPort() {
        return this.localPort;
    }

    void start(String str, int i, int i2, int i3, boolean z) {
        this.remote = new InetSocketAddress(str, i);
        this.payloadType = i2;
        this.dtmfPayload = i3;
        this.running = true;
        this.audioManager = (AudioManager) this.context.getSystemService(Context.AUDIO_SERVICE);
        try {
            this.previousMode = this.audioManager.getMode();
            this.audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        } catch (Exception e) {
            TelephonyLog.log("AudioManager: " + e);
        }
        setupAudio(z);
        this.recvThread = new Thread(this::receiveLoop, "rtp-recv");
        this.sendThread = new Thread(this::sendLoop, "rtp-send");
        this.playThread = new Thread(this::playLoop, "rtp-play");
        this.recvThread.start();
        this.sendThread.start();
        this.playThread.start();
        TelephonyLog.log("RTP старт: local=" + this.localPort + " remote=" + str + ":" + i + " pt=" + i2 + " dtmf=" + i3 + " mic=" + (this.record != null));
    }

    void retarget(String str, int i) {
        this.remote = new InetSocketAddress(str, i);
        this.latched = false;
    }

    private void setupAudio(boolean z) {
        try {
            if (z) {
                int min = AudioRecord.getMinBufferSize(RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
                int size = Math.max(min, FRAME * 2 * 8);
                AudioRecord audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, size);
                if (audioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
                    this.record = audioRecord;
                    try {
                        if (AcousticEchoCanceler.isAvailable()) {
                            this.aec = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
                            if (this.aec != null) {
                                this.aec.setEnabled(true);
                            }
                        }
                        if (NoiseSuppressor.isAvailable()) {
                            this.ns = NoiseSuppressor.create(audioRecord.getAudioSessionId());
                            if (this.ns != null) {
                                this.ns.setEnabled(true);
                            }
                        }
                    } catch (Throwable th) {
                        TelephonyLog.log("AEC/NS: " + th);
                    }
                } else {
                    audioRecord.release();
                    TelephonyLog.log("AudioRecord не инициализирован (нет разрешения на микрофон?)");
                }
            } else {
                TelephonyLog.log("Нет разрешения на микрофон: отправляется тишина");
            }
        } catch (Throwable th2) {
            TelephonyLog.log("AudioRecord: " + th2);
            this.record = null;
        }
        try {
            int minTrack = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            this.track = new AudioTrack(AudioManager.STREAM_VOICE_CALL, RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, Math.max(minTrack, FRAME * 2 * 8), AudioTrack.MODE_STREAM);
            this.track.play();
        } catch (Throwable th3) {
            TelephonyLog.log("AudioTrack: " + th3);
            this.track = null;
        }
    }

    private void sendLoop() {
        short[] pcm = new short[FRAME];
        byte[] enc = new byte[FRAME];
        long next = System.nanoTime();
        AudioRecord rec = this.record;
        if (rec != null) {
            try {
                rec.startRecording();
            } catch (Throwable th) {
                TelephonyLog.log("startRecording: " + th);
                rec = null;
            }
        }
        while (this.running) {
            boolean alaw = this.payloadType == Sdp.PCMA;
            int got = 0;
            if (rec != null) {
                try {
                    while (got < FRAME && this.running) {
                        int n = rec.read(pcm, got, FRAME - got);
                        if (n <= 0) {
                            break;
                        }
                        got += n;
                    }
                } catch (Throwable th2) {
                    TelephonyLog.log("record.read: " + th2);
                    got = 0;
                }
            }
            if (got < FRAME) {
                for (int i = got; i < FRAME; i++) {
                    pcm[i] = 0;
                }
                if (rec == null || got == 0) {
                    next += 20_000_000L;
                    long wait = (next - System.nanoTime()) / 1_000_000L;
                    if (wait > 0) {
                        try {
                            Thread.sleep(wait);
                        } catch (InterruptedException unused) {
                        }
                    } else if (wait < -200) {
                        next = System.nanoTime();
                    }
                }
            }
            if (this.muted) {
                for (int i = 0; i < FRAME; i++) {
                    pcm[i] = 0;
                }
            }
            G711.encode(alaw, pcm, FRAME, enc);
            sendRtp(this.payloadType, false, this.timestamp, enc, FRAME);
            this.timestamp += FRAME;
        }
    }

    private void sendRtp(int i, boolean z, long j, byte[] bArr, int i2) {
        InetSocketAddress target = this.remote;
        if (target == null || this.socket.isClosed()) {
            return;
        }
        byte[] packet = new byte[i2 + 12];
        synchronized (this.sendLock) {
            packet[0] = (byte) 128;
            packet[1] = (byte) ((z || this.firstPacket ? 128 : 0) | (i & 127));
            this.firstPacket = false;
            packet[2] = (byte) (this.seq >> 8);
            packet[3] = (byte) this.seq;
            this.seq = (this.seq + 1) & 65535;
            packet[4] = (byte) (j >> 24);
            packet[5] = (byte) (j >> 16);
            packet[6] = (byte) (j >> 8);
            packet[7] = (byte) j;
            packet[8] = (byte) (this.ssrc >> 24);
            packet[9] = (byte) (this.ssrc >> 16);
            packet[10] = (byte) (this.ssrc >> 8);
            packet[11] = (byte) this.ssrc;
            System.arraycopy(bArr, 0, packet, 12, i2);
            try {
                this.socket.send(new DatagramPacket(packet, packet.length, target));
                this.txPackets++;
            } catch (Exception e) {
                if (this.running) {
                    TelephonyLog.log("RTP send: " + e);
                }
            }
        }
    }

    private void receiveLoop() {
        byte[] buf = new byte[2048];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        short[] pcm = new short[FRAME * 4];
        while (this.running) {
            try {
                this.socket.receive(packet);
            } catch (Exception e) {
                if (this.running) {
                    TelephonyLog.log("RTP recv: " + e);
                }
                return;
            }
            int len = packet.getLength();
            if (len < 12 || (buf[0] & 192) != 128) {
                continue;
            }
            int pt = buf[1] & 127;
            int offset = 12 + 4 * (buf[0] & 15);
            if ((buf[0] & 16) != 0 && len >= offset + 4) {
                offset += 4 + 4 * (((buf[offset + 2] & 255) << 8) | (buf[offset + 3] & 255));
            }
            int payloadLen = len - offset;
            if ((buf[0] & 32) != 0 && payloadLen > 0) {
                payloadLen -= buf[len - 1] & 255;
            }
            if (payloadLen <= 0) {
                continue;
            }
            this.rxPackets++;
            if (!this.latched) {
                InetSocketAddress source = new InetSocketAddress(packet.getAddress(), packet.getPort());
                InetSocketAddress current = this.remote;
                if (current == null || !source.equals(current)) {
                    TelephonyLog.log("RTP: удалённый адрес уточнён " + current + " -> " + source);
                    this.remote = source;
                }
                this.latched = true;
            }
            if (pt == Sdp.PCMA || pt == Sdp.PCMU) {
                byte[] payload = new byte[payloadLen];
                System.arraycopy(buf, offset, payload, 0, payloadLen);
                int samples = Math.min(payloadLen, pcm.length);
                G711.decode(pt == Sdp.PCMA, payload, samples, pcm);
                for (int pos = 0; pos < samples; pos += FRAME) {
                    int n = Math.min(FRAME, samples - pos);
                    short[] frame = new short[FRAME];
                    System.arraycopy(pcm, pos, frame, 0, n);
                    if (!this.playQueue.offer(frame)) {
                        this.playQueue.poll();
                        this.playQueue.offer(frame);
                    }
                }
            }
        }
    }

    private void playLoop() {
        short[] silence = new short[FRAME];
        boolean primed = false;
        while (this.running) {
            AudioTrack t = this.track;
            if (t == null) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException unused) {
                }
                continue;
            }
            try {
                if (!primed) {
                    if (this.playQueue.size() < 3) {
                        t.write(silence, 0, FRAME);
                        continue;
                    }
                    primed = true;
                }
                short[] frame = this.playQueue.poll(25, TimeUnit.MILLISECONDS);
                if (frame == null) {
                    primed = false;
                    frame = silence;
                }
                t.write(frame, 0, FRAME);
            } catch (Throwable th) {
                if (this.running) {
                    TelephonyLog.log("AudioTrack.write: " + th);
                }
                return;
            }
        }
    }

    void setMuted(boolean z) {
        this.muted = z;
    }

    void setSpeaker(boolean z) {
        try {
            if (this.audioManager != null) {
                this.audioManager.setSpeakerphoneOn(z);
            }
        } catch (Exception e) {
            TelephonyLog.log("setSpeakerphoneOn: " + e);
        }
    }

    void sendDtmf(char c) {
        final int event;
        if (c >= '0' && c <= '9') {
            event = c - '0';
        } else if (c == '*') {
            event = 10;
        } else if (c == '#') {
            event = 11;
        } else if (c >= 'A' && c <= 'D') {
            event = (c - 'A') + 12;
        } else {
            return;
        }
        final int pt = this.dtmfPayload;
        if (pt < 0) {
            TelephonyLog.log("DTMF: сервер не согласовал telephone-event");
            return;
        }
        final long ts = this.timestamp;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    for (int step = 1; step <= 11; step++) {
                        boolean end = step > 8;
                        int duration = Math.min(step, 8) * FRAME;
                        byte[] payload = new byte[]{(byte) event, (byte) ((end ? 128 : 0) | 10), (byte) (duration >> 8), (byte) duration};
                        RtpSession.this.sendRtp(pt, step == 1, ts, payload, 4);
                        Thread.sleep(20);
                    }
                } catch (InterruptedException unused) {
                }
            }
        }, "rtp-dtmf");
        thread.start();
    }

    void stop() {
        if (!this.running && this.socket.isClosed()) {
            return;
        }
        this.running = false;
        TelephonyLog.log("RTP стоп: принято=" + this.rxPackets + " отправлено=" + this.txPackets);
        try {
            this.socket.close();
        } catch (Exception unused) {
        }
        try {
            if (this.record != null) {
                this.record.stop();
                this.record.release();
            }
        } catch (Throwable unused2) {
        }
        try {
            if (this.aec != null) {
                this.aec.release();
            }
            if (this.ns != null) {
                this.ns.release();
            }
        } catch (Throwable unused3) {
        }
        try {
            if (this.track != null) {
                this.track.stop();
                this.track.release();
            }
        } catch (Throwable unused4) {
        }
        try {
            if (this.audioManager != null) {
                this.audioManager.setSpeakerphoneOn(false);
                this.audioManager.setMode(this.previousMode);
            }
        } catch (Throwable unused5) {
        }
    }

    /** Закрывает только сокет, если сессия так и не была запущена. */
    void discard() {
        if (!this.running) {
            try {
                this.socket.close();
            } catch (Exception unused) {
            }
        } else {
            stop();
        }
    }
}
