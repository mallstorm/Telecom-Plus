package ua.telecomplus.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Минимальный разбор и построение SDP для одного аудио-потока (RTP/AVP, G.711 + RFC 2833). */
final class Sdp {
    static final int PCMU = 0;
    static final int PCMA = 8;

    String address = "";
    int port;
    boolean savp;
    String direction = "sendrecv";
    final List<Integer> payloads = new ArrayList<>();
    int dtmfPayload = -1;

    static Sdp parse(String str) {
        Sdp sdp = new Sdp();
        boolean audio = false;
        for (String raw : str.split("\r?\n")) {
            String line = raw.trim();
            if (line.startsWith("c=") && (!audio || sdp.address.isEmpty())) {
                String[] parts = line.substring(2).split("\\s+");
                if (parts.length >= 3) {
                    sdp.address = parts[2];
                }
            } else if (line.startsWith("m=")) {
                audio = line.startsWith("m=audio");
                if (audio) {
                    String[] parts = line.substring(2).split("\\s+");
                    if (parts.length >= 4) {
                        try {
                            sdp.port = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException unused) {
                        }
                        sdp.savp = parts[2].contains("SAVP");
                        for (int i = 3; i < parts.length; i++) {
                            try {
                                sdp.payloads.add(Integer.valueOf(Integer.parseInt(parts[i])));
                            } catch (NumberFormatException unused2) {
                            }
                        }
                    }
                }
            } else if (audio && line.startsWith("a=rtpmap:")) {
                String lower = line.toLowerCase(Locale.ROOT);
                if (lower.contains("telephone-event/8000")) {
                    try {
                        sdp.dtmfPayload = Integer.parseInt(line.substring(9, line.indexOf(' ')));
                    } catch (Exception unused3) {
                    }
                }
            } else if (audio && (line.equals("a=sendrecv") || line.equals("a=sendonly") || line.equals("a=recvonly") || line.equals("a=inactive"))) {
                sdp.direction = line.substring(2);
            }
        }
        return sdp;
    }

    /** Выбирает кодек из предложения удалённой стороны: PCMA/PCMU в порядке её предпочтения. */
    int chooseCodec() {
        for (Integer pt : this.payloads) {
            if (pt.intValue() == PCMA || pt.intValue() == PCMU) {
                return pt.intValue();
            }
        }
        return -1;
    }

    boolean isHold() {
        return this.direction.equals("sendonly") || this.direction.equals("inactive") || this.address.equals("0.0.0.0");
    }

    /** Предложение (offer) для исходящего вызова или re-INVITE. */
    static String offer(String user, int port, long sessionId, int version, String direction) {
        return build(user, port, sessionId, version, direction, new int[]{PCMA, PCMU}, 101);
    }

    static String answer(String user, int port, long sessionId, int version, String direction, int codec, int dtmf) {
        return build(user, port, sessionId, version, direction, new int[]{codec}, dtmf);
    }

    private static String build(String user, int port, long sessionId, int version, String direction, int[] codecs, int dtmf) {
        StringBuilder sb = new StringBuilder();
        sb.append("v=0\r\n");
        sb.append("o=").append(user == null || user.isEmpty() ? "-" : user).append(' ').append(sessionId).append(' ').append(version).append(" IN IP4 127.0.0.1\r\n");
        sb.append("s=SIP Call\r\n");
        sb.append("c=IN IP4 127.0.0.1\r\n");
        sb.append("t=0 0\r\n");
        sb.append("m=audio ").append(port).append(" RTP/AVP");
        for (int c : codecs) {
            sb.append(' ').append(c);
        }
        if (dtmf >= 0) {
            sb.append(' ').append(dtmf);
        }
        sb.append("\r\n");
        for (int c : codecs) {
            sb.append("a=rtpmap:").append(c).append(c == PCMA ? " PCMA/8000" : " PCMU/8000").append("\r\n");
        }
        if (dtmf >= 0) {
            sb.append("a=rtpmap:").append(dtmf).append(" telephone-event/8000\r\n");
            sb.append("a=fmtp:").append(dtmf).append(" 0-15\r\n");
        }
        sb.append("a=ptime:20\r\n");
        sb.append("a=").append(direction).append("\r\n");
        return sb.toString();
    }
}
