package ua.telecomplus.app;

import android.content.Context;

/** Ресурсы ищутся по имени (с префиксом tp_), чтобы код работал и в своём APK, и внутри чужого. */
final class Res {
    private static Context ctx;

    private Res() {
    }

    static void init(Context context) {
        if (context != null) {
            ctx = context.getApplicationContext() != null ? context.getApplicationContext() : context;
        }
    }

    static int d(String name) {
        if (ctx == null) {
            return 0;
        }
        return ctx.getResources().getIdentifier("tp_" + name, "drawable", ctx.getPackageName());
    }
}
