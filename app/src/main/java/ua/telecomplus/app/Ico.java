package ua.telecomplus.app;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

/** Векторные иконки (SVG-path → VectorDrawable) и круглая иконка-плашка. */
final class Ico {
    private Ico() {
    }

    /** Сопоставление старых текстовых глифов с векторными иконками. */
    static int res(String g) {
        if (g == null) {
            return 0;
        }
        switch (g) {
            case "⌂": return R.drawable.ic_home;
            case "☎": return R.drawable.ic_phone;
            case "☺": return R.drawable.ic_contacts;
            case "✉": return R.drawable.ic_chat;
            case "☰": return R.drawable.ic_user;
            case "↗": return R.drawable.ic_call_made;
            case "▣": return R.drawable.ic_tariff;
            case "◷": return R.drawable.ic_clock;
            case "☷": return R.drawable.ic_services;
            case "✓": return R.drawable.ic_check;
            case "+": return R.drawable.ic_plus;
            case "−": return R.drawable.ic_minus;
            case "₴": return R.drawable.ic_wallet;
            case "?": return R.drawable.ic_info;
            case "➤": return R.drawable.ic_send;
            case "P": return R.drawable.ic_globe;
            case "T+": return R.drawable.ic_user;
            case "№": return R.drawable.ic_card;
            case "SIM": return R.drawable.ic_sim;
            case "TP": return R.drawable.ic_tariff;
            case "IT": return R.drawable.ic_phone;
            case "@": return R.drawable.ic_mail;
            default: return 0;
        }
    }

    static Drawable get(Context c, int res, int color) {
        Drawable d = c.getResources().getDrawable(res, c.getTheme()).mutate();
        d.setColorFilter(color, PorterDuff.Mode.SRC_IN);
        return d;
    }

    /** Круг с векторной иконкой по центру. */
    static final class CircleView extends TextView {
        private final Drawable icon;

        CircleView(Context c, Drawable icon) {
            super(c);
            this.icon = icon;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int w = getWidth();
            int h = getHeight();
            int s = Math.round(Math.min(w, h) * 0.52f);
            int l = (w - s) / 2;
            int t = (h - s) / 2;
            icon.setBounds(l, t, l + s, t + s);
            icon.draw(canvas);
        }
    }

    /** Просто иконка заданного размера, без фона. */
    static final class PlainView extends android.view.View {
        private final Drawable icon;
        private final float scale;

        PlainView(Context c, Drawable icon, float scale) {
            super(c);
            this.icon = icon;
            this.scale = scale;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int w = getWidth();
            int h = getHeight();
            int s = Math.round(Math.min(w, h) * scale);
            int l = (w - s) / 2;
            int t = (h - s) / 2;
            icon.setBounds(l, t, l + s, t + s);
            icon.draw(canvas);
        }
    }
}
