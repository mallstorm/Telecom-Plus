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

    /** Кольцевой индикатор с процентом в центре. */
    static final class RingView extends android.view.View {
        private final android.graphics.Paint paint = new android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG);
        private final android.graphics.RectF rect = new android.graphics.RectF();
        private final int percent;
        private final int color;
        private final int track;
        private final String text;

        RingView(Context c, int percent, int color, int track, String text) {
            super(c);
            this.percent = percent;
            this.color = color;
            this.track = track;
            this.text = text;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float stroke = Math.min(w, h) * 0.13f;
            float r = Math.min(w, h) / 2f - stroke / 2f;
            rect.set(w / 2f - r, h / 2f - r, w / 2f + r, h / 2f + r);
            paint.setStyle(android.graphics.Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setStrokeCap(android.graphics.Paint.Cap.ROUND);
            paint.setColor(track);
            canvas.drawArc(rect, 0, 360, false, paint);
            paint.setColor(color);
            canvas.drawArc(rect, -90, 360f * Math.max(2, percent) / 100f, false, paint);
            paint.setStyle(android.graphics.Paint.Style.FILL);
            paint.setColor(0xFF0A2A66);
            paint.setTextAlign(android.graphics.Paint.Align.CENTER);
            paint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            paint.setTextSize(Math.min(w, h) * 0.26f);
            float y = h / 2f - (paint.ascent() + paint.descent()) / 2f;
            canvas.drawText(text, w / 2f, y, paint);
        }
    }
}
