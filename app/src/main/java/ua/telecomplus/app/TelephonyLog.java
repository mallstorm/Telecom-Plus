package ua.telecomplus.app;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.Date;
import java.util.Locale;

/** Кольцевой журнал телефонии (пароли в него не попадают — см. SipMessage.redacted). */
final class TelephonyLog {
    private static final int MAX_LINES = 600;
    private static final ArrayDeque<String> LINES = new ArrayDeque<>();
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("HH:mm:ss.SSS", Locale.US);

    private TelephonyLog() {
    }

    static synchronized void log(String str) {
        if (LINES.size() >= MAX_LINES) {
            LINES.removeFirst();
        }
        LINES.addLast(FORMAT.format(new Date()) + "  " + str);
        android.util.Log.d("TelecomPlus", str);
    }

    private static final java.util.ArrayList<String> PINNED = new java.util.ArrayList<>();

    /** Строка, которая не стирается кнопкой «Очистить» и показывается в начале журнала. */
    static synchronized void pin(String str) {
        if (PINNED.size() >= 60) {
            PINNED.remove(0);
        }
        PINNED.add(FORMAT.format(new Date()) + "  " + str);
        log(str);
    }

    static synchronized String dump() {
        StringBuilder sb = new StringBuilder();
        if (!PINNED.isEmpty()) {
            sb.append("== проверка входа ==\n");
            for (String line : PINNED) {
                sb.append(line).append('\n');
            }
            sb.append("== журнал ==\n");
        }
        for (String line : LINES) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    static synchronized void clear() {
        LINES.clear();
    }
}
