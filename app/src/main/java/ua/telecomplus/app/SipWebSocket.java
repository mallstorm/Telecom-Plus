package ua.telecomplus.app;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import android.util.Base64;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

/** Минимальный клиент WebSocket (RFC 6455) поверх TLS для SIP-over-WSS. Без внешних зависимостей. */
final class SipWebSocket {
    interface Listener {
        void onClosed(String str);

        void onText(String str);
    }

    private static final String GUID = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
    private final String host;
    private final int port;
    private final String subProtocol;
    private final String userAgent;
    private final Listener listener;
    private final SecureRandom random = new SecureRandom();
    private final Object writeLock = new Object();
    private volatile boolean closed;
    private SSLSocket socket;
    private InputStream in;
    private OutputStream out;

    SipWebSocket(String str, int i, String str2, String str3, Listener listener) {
        this.host = str;
        this.port = i;
        this.subProtocol = str2;
        this.userAgent = str3;
        this.listener = listener;
    }

    void connect() throws Exception {
        SSLContext sSLContext = SSLContext.getInstance("TLS");
        sSLContext.init(null, null, null);
        SSLSocket sSLSocket = (SSLSocket) sSLContext.getSocketFactory().createSocket();
        sSLSocket.connect(new java.net.InetSocketAddress(this.host, this.port), 15000);
        sSLSocket.setSoTimeout(15000);
        sSLSocket.startHandshake();
        if (!RingotelHostnameVerifier.INSTANCE.verify(this.host, sSLSocket.getSession())) {
            sSLSocket.close();
            throw new IOException("Сертификат сервера телефонии не подходит для " + this.host);
        }
        this.socket = sSLSocket;
        this.in = sSLSocket.getInputStream();
        this.out = sSLSocket.getOutputStream();
        byte[] keyBytes = new byte[16];
        this.random.nextBytes(keyBytes);
        String key = Base64.encodeToString(keyBytes, Base64.NO_WRAP);
        String request = "GET / HTTP/1.1\r\nHost: " + this.host + (this.port == 443 ? "" : ":" + this.port) + "\r\n"
                + "Upgrade: websocket\r\nConnection: Upgrade\r\n"
                + "Sec-WebSocket-Key: " + key + "\r\n"
                + "Sec-WebSocket-Protocol: " + this.subProtocol + "\r\n"
                + "Sec-WebSocket-Version: 13\r\n"
                + "User-Agent: " + this.userAgent + "\r\n\r\n";
        this.out.write(request.getBytes(StandardCharsets.UTF_8));
        this.out.flush();
        String head = readHttpHead();
        String statusLine = head.split("\r\n", 2)[0];
        if (!statusLine.contains(" 101")) {
            close();
            throw new IOException("Сервер телефонии отклонил WebSocket: " + statusLine);
        }
        String expected = Base64.encodeToString(MessageDigest.getInstance("SHA-1").digest((key + GUID).getBytes(StandardCharsets.UTF_8)), Base64.NO_WRAP);
        String accept = headerValue(head, "Sec-WebSocket-Accept");
        if (accept == null || !accept.equals(expected)) {
            close();
            throw new IOException("Неверный Sec-WebSocket-Accept");
        }
        this.socket.setSoTimeout(0);
        Thread thread = new Thread(this::readLoop, "sip-ws-reader");
        thread.setDaemon(true);
        thread.start();
    }

    private String readHttpHead() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int state = 0;
        while (state < 4) {
            int b = this.in.read();
            if (b < 0) {
                throw new IOException("Соединение закрыто сервером");
            }
            bos.write(b);
            if ((b == 13 && (state == 0 || state == 2)) || (b == 10 && (state == 1 || state == 3))) {
                state++;
            } else {
                state = b == 13 ? 1 : 0;
            }
            if (bos.size() > 16384) {
                throw new IOException("Слишком длинный ответ сервера");
            }
        }
        return new String(bos.toByteArray(), StandardCharsets.ISO_8859_1);
    }

    private static String headerValue(String str, String str2) {
        for (String line : str.split("\r\n")) {
            int i = line.indexOf(':');
            if (i > 0 && line.substring(0, i).trim().equalsIgnoreCase(str2)) {
                return line.substring(i + 1).trim();
            }
        }
        return null;
    }

    private void readLoop() {
        ByteArrayOutputStream fragments = new ByteArrayOutputStream();
        boolean fragmentedText = false;
        String reason = "закрыто";
        try {
            while (!this.closed) {
                int b0 = this.in.read();
                int b1 = this.in.read();
                if (b0 < 0 || b1 < 0) {
                    reason = "соединение разорвано";
                    break;
                }
                boolean fin = (b0 & 128) != 0;
                int opcode = b0 & 15;
                boolean masked = (b1 & 128) != 0;
                long length = b1 & 127;
                if (length == 126) {
                    length = ((long) readByte() << 8) | readByte();
                } else if (length == 127) {
                    length = 0;
                    for (int i = 0; i < 8; i++) {
                        length = (length << 8) | readByte();
                    }
                }
                byte[] mask = null;
                if (masked) {
                    mask = new byte[4];
                    readFully(mask);
                }
                if (length > 4_000_000L) {
                    reason = "слишком большой кадр";
                    break;
                }
                byte[] payload = new byte[(int) length];
                readFully(payload);
                if (mask != null) {
                    for (int i = 0; i < payload.length; i++) {
                        payload[i] ^= mask[i & 3];
                    }
                }
                if (opcode == 9) {
                    sendFrame(10, payload);
                } else if (opcode == 8) {
                    reason = "сервер закрыл соединение";
                    break;
                } else if (opcode == 1 || opcode == 2 || opcode == 0) {
                    if (opcode != 0) {
                        fragments.reset();
                        fragmentedText = opcode == 1;
                    }
                    fragments.write(payload);
                    if (fin) {
                        String text = new String(fragments.toByteArray(), StandardCharsets.UTF_8);
                        fragments.reset();
                        if (fragmentedText || opcode == 1 || opcode == 2) {
                            this.listener.onText(text);
                        }
                    }
                }
            }
        } catch (Exception e) {
            reason = this.closed ? "закрыто" : String.valueOf(e);
        }
        boolean wasClosed = this.closed;
        this.closed = true;
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException unused) {
        }
        if (!wasClosed) {
            this.listener.onClosed(reason);
        }
    }

    private int readByte() throws IOException {
        int b = this.in.read();
        if (b < 0) {
            throw new IOException("EOF");
        }
        return b;
    }

    private void readFully(byte[] bArr) throws IOException {
        int off = 0;
        while (off < bArr.length) {
            int n = this.in.read(bArr, off, bArr.length - off);
            if (n < 0) {
                throw new IOException("EOF");
            }
            off += n;
        }
    }

    void sendText(String str) throws IOException {
        sendFrame(1, str.getBytes(StandardCharsets.UTF_8));
    }

    void sendPing() throws IOException {
        sendFrame(9, new byte[0]);
    }

    private void sendFrame(int i, byte[] bArr) throws IOException {
        if (this.closed || this.out == null) {
            throw new IOException("WebSocket закрыт");
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream(bArr.length + 14);
        bos.write(128 | i);
        int len = bArr.length;
        if (len < 126) {
            bos.write(128 | len);
        } else if (len < 65536) {
            bos.write(128 | 126);
            bos.write(len >> 8);
            bos.write(len & 255);
        } else {
            bos.write(128 | 127);
            for (int s = 56; s >= 0; s -= 8) {
                bos.write((int) (((long) len >> s) & 255));
            }
        }
        byte[] mask = new byte[4];
        this.random.nextBytes(mask);
        bos.write(mask);
        for (int k = 0; k < len; k++) {
            bos.write(bArr[k] ^ mask[k & 3]);
        }
        synchronized (this.writeLock) {
            this.out.write(bos.toByteArray());
            this.out.flush();
        }
    }

    boolean isOpen() {
        return !this.closed;
    }

    void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        try {
            sendFrameQuiet();
        } catch (Exception unused) {
        }
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException unused) {
        }
    }

    private void sendFrameQuiet() throws IOException {
        // закрывающий кадр отправляется без проверки closed
        byte[] mask = new byte[4];
        this.random.nextBytes(mask);
        byte[] frame = new byte[]{(byte) 136, (byte) 128, mask[0], mask[1], mask[2], mask[3]};
        synchronized (this.writeLock) {
            if (this.out != null) {
                this.out.write(frame);
                this.out.flush();
            }
        }
    }
}
