package ua.telecomplus.app;

/** G.711 A-law / μ-law (PCMA = 8, PCMU = 0). */
final class G711 {
    private static final short[] ALAW_DECODE = new short[256];
    private static final short[] ULAW_DECODE = new short[256];

    static {
        for (int i = 0; i < 256; i++) {
            ALAW_DECODE[i] = alawToLinear((byte) i);
            ULAW_DECODE[i] = ulawToLinear((byte) i);
        }
    }

    private G711() {
    }

    private static short alawToLinear(byte b) {
        int a = (b & 255) ^ 85;
        int t = (a & 15) << 4;
        int seg = (a & 112) >> 4;
        switch (seg) {
            case 0:
                t += 8;
                break;
            case 1:
                t += 264;
                break;
            default:
                t = (t + 264) << (seg - 1);
                break;
        }
        return (short) ((a & 128) != 0 ? t : -t);
    }

    private static short ulawToLinear(byte b) {
        int u = ~b & 255;
        int t = (((u & 15) << 3) + 132) << ((u & 112) >> 4);
        return (short) ((u & 128) != 0 ? 132 - t : t - 132);
    }

    static byte encodeAlaw(short s) {
        int pcm = s;
        int mask;
        if (pcm >= 0) {
            mask = 213;
        } else {
            mask = 85;
            pcm = -pcm - 8;
        }
        if (pcm < 0) {
            pcm = 0;
        }
        if (pcm > 32635) {
            pcm = 32635;
        }
        int seg = 0;
        for (int v = pcm >> 8; v != 0 && seg < 7; v >>= 1) {
            seg++;
        }
        int aval = seg << 4;
        if (seg < 2) {
            aval |= (pcm >> 4) & 15;
        } else {
            aval |= (pcm >> (seg + 3)) & 15;
        }
        return (byte) (aval ^ mask);
    }

    static byte encodeUlaw(short s) {
        int pcm = s;
        int sign = 0;
        if (pcm < 0) {
            pcm = -pcm;
            sign = 128;
        }
        if (pcm > 32635) {
            pcm = 32635;
        }
        pcm += 132;
        int exponent = 7;
        for (int mask = 16384; (pcm & mask) == 0 && exponent > 0; mask >>= 1) {
            exponent--;
        }
        int mantissa = (pcm >> (exponent + 3)) & 15;
        return (byte) ~(sign | (exponent << 4) | mantissa);
    }

    static void encode(boolean alaw, short[] pcm, int count, byte[] out) {
        for (int i = 0; i < count; i++) {
            out[i] = alaw ? encodeAlaw(pcm[i]) : encodeUlaw(pcm[i]);
        }
    }

    static void decode(boolean alaw, byte[] in, int count, short[] out) {
        short[] table = alaw ? ALAW_DECODE : ULAW_DECODE;
        for (int i = 0; i < count; i++) {
            out[i] = table[in[i] & 255];
        }
    }
}
