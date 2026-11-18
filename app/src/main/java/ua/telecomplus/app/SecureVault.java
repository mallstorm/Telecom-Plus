package ua.telecomplus.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class SecureVault {
    private static final String ALIAS = "telecom_plus_session_key_v3";
    private static final String[] ALL_KEY_ALIASES = {"telecom_plus_session_key_v1", "telecom_plus_session_key_v2", ALIAS};
    private static final String[] LEGACY_PREFS = {"telecom_plus_secure_v1", "telecom_plus_secure_v2"};
    private static final String PREFS_NAME = "telecom_plus_secure_v3";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private final Context context;
    private final SharedPreferences prefs;

    public SecureVault(Context context) {
        Context applicationContext = context.getApplicationContext();
        this.context = applicationContext;
        this.prefs = applicationContext.getSharedPreferences(PREFS_NAME, 0);
    }

    private SecretKey getOrCreateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        Key key = keyStore.getKey(ALIAS, null);
        if (key instanceof SecretKey) {
            return (SecretKey) key;
        }
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES", "AndroidKeyStore");
        keyGenerator.init(new KeyGenParameterSpec.Builder(ALIAS, 3).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setRandomizedEncryptionRequired(true).build());
        return keyGenerator.generateKey();
    }

    public synchronized void clear() {
        this.prefs.edit().clear().commit();
        for (String str : LEGACY_PREFS) {
            try {
                this.context.deleteSharedPreferences(str);
            } catch (Exception unused) {
            }
        }
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            for (String str2 : ALL_KEY_ALIASES) {
                if (keyStore.containsAlias(str2)) {
                    keyStore.deleteEntry(str2);
                }
            }
        } catch (Exception unused2) {
        }
    }

    public synchronized String get(String str) {
        String string = this.prefs.getString(str, null);
        if (string == null) {
            return null;
        }
        try {
            String[] split = string.split("\\.", 2);
            if (split.length != 2) {
                return null;
            }
            byte[] decode = Base64.decode(split[0], 2);
            byte[] decode2 = Base64.decode(split[1], 2);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(2, getOrCreateKey(), new GCMParameterSpec(128, decode));
            return new String(cipher.doFinal(decode2), StandardCharsets.UTF_8);
        } catch (Exception unused) {
            this.prefs.edit().remove(str).apply();
            return null;
        }
    }

    public synchronized void put(String str, String str2) {
        if (str == null || str2 == null) {
            return;
        }
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(1, getOrCreateKey());
            this.prefs.edit().putString(str, Base64.encodeToString(cipher.getIV(), 2) + "." + Base64.encodeToString(cipher.doFinal(str2.getBytes(StandardCharsets.UTF_8)), 2)).apply();
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось зашифровать локальные данные", e);
        }
    }

    public synchronized void remove(String str) {
        this.prefs.edit().remove(str).apply();
    }
}
