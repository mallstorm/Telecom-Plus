package ua.telecomplus.app;

import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.security.auth.x500.X500Principal;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class RingotelHostnameVerifier implements HostnameVerifier {
    static final String HOST = "intertelecom.ringotel.net";
    static final RingotelHostnameVerifier INSTANCE = new RingotelHostnameVerifier();
    private final HostnameVerifier platformVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

    private RingotelHostnameVerifier() {
    }

    public static boolean matchesLegacyIdentity(String str, X500Principal x500Principal, Collection<List<?>> collection) {
        if (str == null || x500Principal == null || !HOST.equalsIgnoreCase(str) || !(collection == null || collection.isEmpty())) {
            return false;
        }
        String name = x500Principal.getName("RFC2253");
        if (name.indexOf(92) < 0 && name.indexOf(34) < 0 && name.indexOf(43) < 0) {
            String str2 = null;
            int i = 0;
            for (String str3 : name.split(",", -1)) {
                int indexOf = str3.indexOf(61);
                if (indexOf > 0 && "CN".equalsIgnoreCase(str3.substring(0, indexOf).trim())) {
                    i++;
                    str2 = str3.substring(indexOf + 1).trim();
                }
            }
            if (i == 1 && HOST.equalsIgnoreCase(str2)) {
                return true;
            }
        }
        return false;
    }

    @Override // javax.net.ssl.HostnameVerifier
    public boolean verify(String str, SSLSession sSLSession) {
        if (this.platformVerifier.verify(str, sSLSession)) {
            return true;
        }
        if (!HOST.equalsIgnoreCase(str)) {
            return false;
        }
        try {
            if (RingotelTls.chainsToRoot(sSLSession.getPeerCertificates())) {
                return true;
            }
            Certificate[] peerCertificates = sSLSession.getPeerCertificates();
            if (peerCertificates != null && peerCertificates.length != 0) {
                Certificate certificate = peerCertificates[0];
                if (certificate instanceof X509Certificate) {
                    X509Certificate x509Certificate = (X509Certificate) certificate;
                    x509Certificate.checkValidity();
                    return matchesLegacyIdentity(str, x509Certificate.getSubjectX500Principal(), x509Certificate.getSubjectAlternativeNames());
                }
            }
        } catch (RuntimeException | CertificateException | SSLPeerUnverifiedException unused) {
        }
        return false;
    }
}
