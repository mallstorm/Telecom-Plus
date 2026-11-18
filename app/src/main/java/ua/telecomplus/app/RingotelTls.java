package ua.telecomplus.app;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/** TLS для серверов Ringotel: системные сертификаты + собственный Ringotel Root CA (как в официальном IT Phone). */
final class RingotelTls {
    private static final String ROOT_PEM =
            "-----BEGIN CERTIFICATE-----\n" +
            "MIIDTTCCAjUCBF5OcmgwDQYJKoZIhvcNAQELBQAwazELMAkGA1UEBhMCSUUxDzAN\n" +
            "BgNVBAgTBkR1YmxpbjEPMA0GA1UEBxMGRHVibGluMREwDwYDVQQKEwhSaW5nb3Rl\n" +
            "bDEMMAoGA1UECxMDUk5EMRkwFwYDVQQDExBSaW5nb3RlbCBSb290IENBMB4XDTIw\n" +
            "MDIyMDExNTAwMFoXDTMwMDIxNzExNTAwMFowazELMAkGA1UEBhMCSUUxDzANBgNV\n" +
            "BAgTBkR1YmxpbjEPMA0GA1UEBxMGRHVibGluMREwDwYDVQQKEwhSaW5nb3RlbDEM\n" +
            "MAoGA1UECxMDUk5EMRkwFwYDVQQDExBSaW5nb3RlbCBSb290IENBMIIBIjANBgkq\n" +
            "hkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoaxkPw8bn+5KY/e9t1wYs43t4zK3yo4h\n" +
            "IG3EWYiWhxPAEbBKXMpFvQz+v1AhAX8guEwNPCFmXtjFe/yFRQcJygvLHAlye2cv\n" +
            "4nmS9Dfxr+DleNXz3Xmo42rIrHpHBprf+ab4h+UOPMZbsUu6d+f/semhlAdtJoHO\n" +
            "kHIPSKsEQ0MRglElwZEVWqgzf3lWhKnyPMyj8/wXRq68OzTx+j9FsbzxvlzTn73s\n" +
            "NwJ4OHsKVGSazUaeSfGKLOZppZD5mdOy5k1fqIJTb5FoVqHfTPHpmDKnqEokJSmU\n" +
            "5L9uDdXe36KRrLr+GWvVv28X36ACYGtJhxchSFAyCFErP+motqlA3QIDAQABMA0G\n" +
            "CSqGSIb3DQEBCwUAA4IBAQCgBcDlWqE7xDWlMyABMOzBwYT4dYrk21+tNWOb+/+E\n" +
            "Pkyx0zGXJ30SS942U3gcdcwdndHg1y+jv9cMOaBX8PYwcqvCBuoqML+5YdF2Scl2\n" +
            "BQiuptMM8cVs4LRW1d1sgzVQU0m9zywVEUvNKyfw5ZbGJeLDPI++AcBThIi0E1yD\n" +
            "iUwRF+rprwPazkNa3ETl3cOI7bOltNQgMaYzoCvaO/3G8/PvSEi9FJc84XtBFTut\n" +
            "5+XjBVq1Siw8RaZjg5CHoOr0aW3sa974DzxB1QQSuiZXyW5RIX0H+z2H8vApY2vW\n" +
            "zE4paIIqXiCoW7iun4I27XeUArephpdN9WXEkzFUwD7P\n" +
            "-----END CERTIFICATE-----\n";

    private static volatile X509Certificate root;

    static X509Certificate root() throws Exception {
        X509Certificate r = root;
        if (r == null) {
            r = (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(ROOT_PEM.getBytes(StandardCharsets.US_ASCII)));
            root = r;
        }
        return r;
    }

    /** Цепочка заканчивается на Ringotel Root CA. */
    static boolean chainsToRoot(Certificate[] chain) {
        try {
            X509Certificate r = root();
            if (chain == null || chain.length == 0) {
                return false;
            }
            for (Certificate c : chain) {
                if (c.equals(r)) {
                    return true;
                }
            }
            X509Certificate last = (X509Certificate) chain[chain.length - 1];
            last.verify(r.getPublicKey());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    static SSLContext context() throws Exception {
        TrustManagerFactory sys = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        sys.init((KeyStore) null);
        final X509TrustManager systemTm = (X509TrustManager) sys.getTrustManagers()[0];
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        ks.setCertificateEntry("ringotel-root", root());
        TrustManagerFactory own = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        own.init(ks);
        final X509TrustManager ownTm = (X509TrustManager) own.getTrustManagers()[0];
        X509TrustManager both = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                systemTm.checkClientTrusted(chain, authType);
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                try {
                    systemTm.checkServerTrusted(chain, authType);
                } catch (CertificateException e) {
                    ownTm.checkServerTrusted(chain, authType);
                }
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return systemTm.getAcceptedIssuers();
            }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, new TrustManager[]{both}, null);
        return ctx;
    }

    private RingotelTls() {
    }
}
