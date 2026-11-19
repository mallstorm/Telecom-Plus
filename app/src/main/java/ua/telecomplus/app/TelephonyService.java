package ua.telecomplus.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

/**
 * Foreground-служба: держит SIP-регистрацию живой, пока приложение свёрнуто, и показывает
 * уведомление о входящем вызове с кнопками «Ответить» / «Отклонить» (вместо push-уведомлений).
 */
public final class TelephonyService extends Service implements TelephonyEngine.Listener {
    static final String CH_STATUS = "tp_status";
    static final String CH_CALLS = "tp_calls";
    static final String CH_MESSAGES = "tp_messages";
    static final String ACTION_DECLINE = "ua.telecomplus.app.action.DECLINE";
    static final String ACTION_HANGUP = "ua.telecomplus.app.action.HANGUP";
    static final String EXTRA_ANSWER = "answer_call";
    private static final int ID_STATUS = 101;
    private static final int ID_CALL = 102;

    private TelephonyEngine engine;
    private NotificationManager manager;
    private boolean ringingShown;
    private String lastStatus = "";

    static void start(Context context) {
        try {
            Intent intent = new Intent(context, TelephonyService.class);
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception e) {
            TelephonyLog.log("Служба не запущена: " + e);
        }
    }

    static void stop(Context context) {
        context.stopService(new Intent(context, TelephonyService.class));
    }

    static void createChannels(Context context) {
        if (Build.VERSION.SDK_INT < 26) {
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel status = new NotificationChannel(CH_STATUS, "Статус телефонии", NotificationManager.IMPORTANCE_MIN);
        status.setShowBadge(false);
        nm.createNotificationChannel(status);
        NotificationChannel calls = new NotificationChannel(CH_CALLS, "Входящие вызовы", NotificationManager.IMPORTANCE_HIGH);
        calls.enableVibration(true);
        calls.setSound(android.provider.Settings.System.DEFAULT_RINGTONE_URI, new android.media.AudioAttributes.Builder().setUsage(android.media.AudioAttributes.USAGE_NOTIFICATION_RINGTONE).setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
        nm.createNotificationChannel(calls);
        NotificationChannel messages = new NotificationChannel(CH_MESSAGES, "Сообщения", NotificationManager.IMPORTANCE_DEFAULT);
        nm.createNotificationChannel(messages);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        createChannels(this);
        TpEnv.init(this);
        this.engine = TpEnv.telephony;
    }

    @Override
    public int onStartCommand(Intent intent, int i, int i2) {
        startForeground(ID_STATUS, statusNotification(this.engine.snapshot()));
        this.engine.addObserver(this);
        TelephonySnapshot s = this.engine.snapshot();
        if (s.isProvisioned() && s.registrationState != TelephonyRegistrationState.ONLINE && s.registrationState != TelephonyRegistrationState.CONNECTING) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    TelephonyService.this.engine.connect();
                }
            }, "svc-connect").start();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (this.engine != null) {
            this.engine.removeObserver(this);
        }
        if (this.manager != null) {
            this.manager.cancel(ID_CALL);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTelephonyChanged(TelephonySnapshot telephonySnapshot) {
        if (!telephonySnapshot.isProvisioned()) {
            stopForeground(true);
            stopSelf();
            return;
        }
        boolean ringing = this.engine.incomingRinging();
        if (ringing) {
            this.manager.notify(ID_CALL, incomingNotification(telephonySnapshot));
            this.ringingShown = true;
        } else if (telephonySnapshot.hasActiveCall()) {
            this.manager.notify(ID_CALL, ongoingNotification(telephonySnapshot));
            this.ringingShown = true;
        } else if (this.ringingShown) {
            this.manager.cancel(ID_CALL);
            this.ringingShown = false;
        }
        String text = statusText(telephonySnapshot);
        if (!text.equals(this.lastStatus)) {
            this.lastStatus = text;
            this.manager.notify(ID_STATUS, statusNotification(telephonySnapshot));
        }
    }

    private static String statusText(TelephonySnapshot telephonySnapshot) {
        switch (telephonySnapshot.registrationState) {
            case ONLINE:
                return "На связи — можно звонить и принимать вызовы";
            case CONNECTING:
                return "Подключение к серверу телефонии…";
            case OFFLINE:
                return "Нет связи с сервером, переподключаюсь";
            default:
                return "Телефония активирована";
        }
    }

    private PendingIntent openApp(boolean answer) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_ANSWER, answer);
        return PendingIntent.getActivity(this, answer ? 1 : 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private PendingIntent action(String str, int i) {
        Intent intent = new Intent(this, CallActionReceiver.class);
        intent.setAction(str);
        return PendingIntent.getBroadcast(this, i, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private Notification.Builder builder(String str) {
        return Build.VERSION.SDK_INT >= 26 ? new Notification.Builder(this, str) : new Notification.Builder(this);
    }

    private Notification statusNotification(TelephonySnapshot telephonySnapshot) {
        return builder(CH_STATUS).setSmallIcon(Res.d("ic_telecom_plus")).setContentTitle("Telecom+").setContentText(statusText(telephonySnapshot)).setOngoing(true).setContentIntent(openApp(false)).build();
    }

    private String callerTitle(TelephonySnapshot telephonySnapshot) {
        return PhoneData.get(TpEnv.vault).title(telephonySnapshot.remoteNumber);
    }

    private Notification incomingNotification(TelephonySnapshot telephonySnapshot) {
        Notification.Builder b = builder(CH_CALLS).setSmallIcon(Res.d("ic_telecom_plus")).setContentTitle("Входящий вызов").setContentText(callerTitle(telephonySnapshot)).setCategory(Notification.CATEGORY_CALL).setOngoing(true).setAutoCancel(false).setContentIntent(openApp(false)).setFullScreenIntent(openApp(false), true)
                .addAction(new Notification.Action.Builder(null, "Отклонить", action(ACTION_DECLINE, 2)).build())
                .addAction(new Notification.Action.Builder(null, "Ответить", openApp(true)).build());
        if (Build.VERSION.SDK_INT < 26) {
            b.setPriority(Notification.PRIORITY_MAX);
        }
        return b.build();
    }

    private Notification ongoingNotification(TelephonySnapshot telephonySnapshot) {
        Notification.Builder b = builder(CH_CALLS).setSmallIcon(Res.d("ic_telecom_plus")).setContentTitle("Разговор").setContentText(callerTitle(telephonySnapshot)).setCategory(Notification.CATEGORY_CALL).setOngoing(true).setOnlyAlertOnce(true).setContentIntent(openApp(false))
                .addAction(new Notification.Action.Builder(null, "Завершить", action(ACTION_HANGUP, 3)).build());
        if (telephonySnapshot.callStartedAt > 0) {
            b.setWhen(telephonySnapshot.callStartedAt).setUsesChronometer(true);
        }
        return b.build();
    }
}
