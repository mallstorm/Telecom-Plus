package ua.telecomplus.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** Кнопки уведомления вызова: отклонить входящий / завершить разговор. */
public final class CallActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        final TelecomPlusApplication app = (TelecomPlusApplication) context.getApplicationContext();
        if (app.telephony == null || intent == null) {
            return;
        }
        String action = intent.getAction();
        if (TelephonyService.ACTION_DECLINE.equals(action) || TelephonyService.ACTION_HANGUP.equals(action)) {
            final BroadcastReceiver.PendingResult result = goAsync();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        app.telephony.hangup();
                    } finally {
                        result.finish();
                    }
                }
            }).start();
        }
    }
}
