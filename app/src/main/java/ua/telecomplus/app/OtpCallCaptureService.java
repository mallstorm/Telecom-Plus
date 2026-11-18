package ua.telecomplus.app;

import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import java.util.Locale;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final class OtpCallCaptureService extends NotificationListenerService {
    static final String ACTION_OTP_CALL_CODE = "ua.telecomplus.app.action.OTP_CALL_CODE";
    static final String EXTRA_CAPTURED_AT = "captured_at";
    static final String EXTRA_CODE = "code";
    static final String IT_PHONE_PACKAGE = "smile.ringotel.it";
    static final String PREFS = "otp_call_capture_v1";
    static final String PREF_CAPTURED_AT = "last_captured_at";
    static final String PREF_CODE = "last_code";
    static final String PREF_CONSUMED_AT = "last_consumed_at";
    private static final String PREF_NOTIFICATION = "last_notification";

    private static void append(StringBuilder sb, CharSequence charSequence) {
        if (charSequence == null || charSequence.length() == 0) {
            return;
        }
        if (sb.length() > 0) {
            sb.append('\n');
        }
        sb.append(charSequence);
    }

    private static String collectText(Notification notification) {
        StringBuilder sb = new StringBuilder();
        append(sb, notification.tickerText);
        Bundle bundle = notification.extras;
        if (bundle != null) {
            append(sb, bundle.getCharSequence("android.title"));
            append(sb, bundle.getCharSequence("android.text"));
            append(sb, bundle.getCharSequence("android.subText"));
            append(sb, bundle.getCharSequence("android.infoText"));
            append(sb, bundle.getCharSequence("android.bigText"));
            CharSequence[] charSequenceArray = bundle.getCharSequenceArray("android.textLines");
            if (charSequenceArray != null) {
                for (CharSequence charSequence : charSequenceArray) {
                    append(sb, charSequence);
                }
            }
        }
        return sb.toString();
    }

    private static boolean looksLikeIncomingCall(Notification notification, String str) {
        if (!"call".equals(notification.category) && notification.fullScreenIntent == null) {
            String lowerCase = str == null ? "" : str.toLowerCase(Locale.ROOT);
            if (!lowerCase.contains("входящ") && !lowerCase.contains("звонок") && !lowerCase.contains("дзвін") && !lowerCase.contains("виклик") && !lowerCase.contains("incoming call")) {
                return false;
            }
        }
        return true;
    }

    @Override // android.service.notification.NotificationListenerService
    public void onNotificationPosted(StatusBarNotification statusBarNotification) {
        Notification notification;
        String extract;
        if (statusBarNotification == null || !IT_PHONE_PACKAGE.equals(statusBarNotification.getPackageName()) || (notification = statusBarNotification.getNotification()) == null) {
            return;
        }
        String str = statusBarNotification.getKey() + ":" + statusBarNotification.getPostTime();
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS, 0);
        if (str.equals(sharedPreferences.getString(PREF_NOTIFICATION, ""))) {
            return;
        }
        String collectText = collectText(notification);
        if (looksLikeIncomingCall(notification, collectText) && (extract = OtpCallerCodeExtractor.extract(collectText)) != null) {
            long currentTimeMillis = System.currentTimeMillis();
            sharedPreferences.edit().putString(PREF_NOTIFICATION, str).putString(PREF_CODE, extract).putLong(PREF_CAPTURED_AT, currentTimeMillis).apply();
            sendBroadcast(new Intent(ACTION_OTP_CALL_CODE).setPackage(getPackageName()).putExtra(EXTRA_CODE, extract).putExtra(EXTRA_CAPTURED_AT, currentTimeMillis));
        }
    }
}
