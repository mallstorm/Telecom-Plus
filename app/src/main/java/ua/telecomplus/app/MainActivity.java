package ua.telecomplus.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ua.telecomplus.app.MainActivity;
import ua.telecomplus.app.PortalResult;
import ua.telecomplus.app.TelephonyResult;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
public final class MainActivity extends Activity {
    private static final int REQUEST_SAVE_DIAGNOSTIC = 9203;
    private Button activePortalOtpConfirm;
    private EditText activePortalOtpField;
    private FrameLayout appRoot;
    private Runnable authGuardTick;
    private CallHistorySnapshot callHistory;
    private boolean callsPeriodLoading;
    private int callsPeriodMonth;
    private int callsPeriodYear;
    private DashboardSnapshot dashboard;
    private boolean domCallsAttempted;
    private boolean domFallbackRunning;
    private boolean domFundsAttempted;
    private int fundsFilter;
    private FundsFlowSnapshot fundsFlow;
    private boolean fundsPeriodLoading;
    private int fundsPeriodMonth;
    private int fundsPeriodYear;
    private SubscriberPortalGateway gateway;
    private int historyMode;
    private boolean otpReceiverRegistered;
    private long otpRepeatAllowedAt;
    private int otpSeconds;
    private Runnable otpTick;
    private boolean portalOtpAutoSubmitInFlight;
    private long portalOtpRequestedAt;
    private FrameLayout root;
    private int selectedTab;
    private boolean showingCachedData;
    private boolean startupCompleted;
    private TelephonyEngine telephony;
    private PortalWebAuthBridge webAuth;
    private static final int BLUE = Color.rgb(6, 84, 218);
    private static final int BLUE_DARK = Color.rgb(10, 42, 102);
    private static final int YELLOW = Color.rgb(255, 203, 0);
    private static final int CYAN = Color.rgb(38, 132, 235);
    private static final int PURPLE = Color.rgb(63, 81, 181);
    private static final int ORANGE = Color.rgb(245, 166, 35);
    private static final int BG = Color.rgb(247, 250, 255);
    private static final int TEXT = Color.rgb(9, 28, 69);
    private static final int MUTED = Color.rgb(128, 144, 168);
    private static final int GREEN = Color.rgb(34, 160, 90);
    private static final int RED = Color.rgb(210, 52, 56);
    private static final int BORDER = Color.rgb(228, 237, 249);
    private static final int SOFT_BLUE = Color.rgb(228, 239, 255);
    private static final int SOFT_CYAN = Color.rgb(226, 240, 255);
    private static final int SOFT_PURPLE = Color.rgb(232, 238, 252);
    private static final int SOFT_ORANGE = Color.rgb(255, 245, 210);
    private static final int AMBER_BG = Color.rgb(255, 248, 224);
    private TelephonySnapshot telephonySnapshot = TelephonySnapshot.unprovisioned();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private List<ServiceItem> services = new ArrayList();
    private String phone = "";
    private String refreshWarning = "";
    private String fundsSafeReport = "";
    private String callsSafeReport = "";
    private String pendingDiagnosticReport = "";
    private int fundsRangeMonths = 1;
    private int callsRangeMonths = 1;
    private final BroadcastReceiver otpCallReceiver = new BroadcastReceiver() { // from class: ua.telecomplus.app.MainActivity.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !"ua.telecomplus.app.action.OTP_CALL_CODE".equals(intent.getAction())) {
                return;
            }
            MainActivity.this.acceptCapturedPortalOtp(intent.getStringExtra("code"), intent.getLongExtra("captured_at", 0L));
        }
    };

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    /* renamed from: ua.telecomplus.app.MainActivity$4, reason: invalid class name */
    public static /* synthetic */ class AnonymousClass4 {
        static final /* synthetic */ int[] $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState;

        static {
            int[] iArr = new int[TelephonyRegistrationState.values().length];
            $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState = iArr;
            try {
                iArr[TelephonyRegistrationState.OTP_REQUESTED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            try {
                $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[TelephonyRegistrationState.PROVISIONED.ordinal()] = 2;
            } catch (NoSuchFieldError unused2) {
            }
            try {
                $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[TelephonyRegistrationState.CONNECTING.ordinal()] = 3;
            } catch (NoSuchFieldError unused3) {
            }
            try {
                $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[TelephonyRegistrationState.ONLINE.ordinal()] = 4;
            } catch (NoSuchFieldError unused4) {
            }
            try {
                $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[TelephonyRegistrationState.OFFLINE.ordinal()] = 5;
            } catch (NoSuchFieldError unused5) {
            }
            try {
                $SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[TelephonyRegistrationState.ERROR.ordinal()] = 6;
            } catch (NoSuchFieldError unused6) {
            }
        }
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public interface PortalCallback<T> {
        void accept(PortalResult<T> portalResult);
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public final class TelecomLogoView extends View {
        private final Paint paint;
        private final Path path;

        public TelecomLogoView() {
            super(MainActivity.this);
            this.paint = new Paint(1);
            this.path = new Path();
            setContentDescription("Логотип Telecom+");
        }

        @Override // android.view.View
        public void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float width = getWidth();
            float height = getHeight();
            float min = Math.min(width, height);
            float f = (width - min) / 2.0f;
            float f2 = (height - min) / 2.0f;
            float f3 = f + min;
            float f4 = f2 + min;
            RectF rectF = new RectF(f, f2, f3, f4);
            float f5 = 0.23f * min;
            Paint paint = this.paint;
            Paint.Style style = Paint.Style.FILL;
            paint.setStyle(style);
            this.paint.setShader(new LinearGradient(f, f2, f3, f4, Color.rgb(15, 125, 211), Color.rgb(5, 61, 139), Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rectF, f5, f5, this.paint);
            this.paint.setShader(null);
            this.paint.setStyle(Paint.Style.STROKE);
            this.paint.setStrokeCap(Paint.Cap.ROUND);
            this.paint.setStrokeWidth(0.065f * min);
            this.paint.setColor(Color.rgb(255, 210, 26));
            this.path.reset();
            this.path.moveTo((0.15f * min) + f, (0.65f * min) + f2);
            float f6 = 0.87f * min;
            float f7 = 0.48f * min;
            this.path.cubicTo((0.31f * min) + f, f2 + f6, (0.72f * min) + f, (0.89f * min) + f2, (0.92f * min) + f, f2 + f7);
            canvas.drawPath(this.path, this.paint);
            this.paint.setStrokeWidth(0.043f * min);
            this.paint.setColor(Color.rgb(255, 232, 91));
            this.path.reset();
            this.path.moveTo((0.22f * min) + f, (0.75f * min) + f2);
            float f8 = 0.46f * min;
            this.path.cubicTo(f + f8, (0.91f * min) + f2, (0.79f * min) + f, (0.73f * min) + f2, f + f6, (0.36f * min) + f2);
            canvas.drawPath(this.path, this.paint);
            this.paint.setStyle(style);
            this.paint.setColor(-1);
            this.paint.setTypeface(Typeface.create(Typeface.SANS_SERIF, 3));
            this.paint.setTextSize(f8);
            this.paint.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fontMetrics = this.paint.getFontMetrics();
            canvas.drawText("IT", f + f7, ((min * 0.52f) + f2) - ((fontMetrics.ascent + fontMetrics.descent) / 2.0f), this.paint);
        }
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public interface TelephonyCallback<T> {
        void accept(TelephonyResult<T> telephonyResult);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void acceptCapturedPortalOtp(String str, long j) {
        long currentTimeMillis = System.currentTimeMillis();
        String digits = digits(str);
        if (this.activePortalOtpField == null || this.activePortalOtpConfirm == null || this.portalOtpRequestedAt <= 0 || this.otpSeconds <= 0 || digits.length() != 4 || j < this.portalOtpRequestedAt - 2000 || j > 10000 + currentTimeMillis || currentTimeMillis - j > 600000) {
            return;
        }
        this.activePortalOtpField.setText(digits);
        this.activePortalOtpField.setSelection(digits.length());
        getSharedPreferences("otp_call_capture_v1", 0).edit().putLong("last_consumed_at", j).apply();
        Toast.makeText(this, "Код получен из звонка IT Phone", 0).show();
        if (this.portalOtpAutoSubmitInFlight || !this.activePortalOtpConfirm.isEnabled()) {
            return;
        }
        this.portalOtpAutoSubmitInFlight = true;
        final EditText editText = this.activePortalOtpField;
        final Button button = this.activePortalOtpConfirm;
        this.handler.postDelayed(new Runnable() { // from class: n
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$acceptCapturedPortalOtp$28(editText, button);
            }
        }, 250L);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public long authBlockedSeconds() {
        long j = getSharedPreferences("auth_guard", 0).getLong("blocked_until", 0L);
        long max = Math.max(0L, ((j - System.currentTimeMillis()) + 999) / 1000);
        if (max <= 0 && j != 0) {
            clearAuthGuard();
        }
        return max;
    }

    private void beginStartup() {
        continueStartup();
    }

    private View bottomNav() {
        LinearLayout row = row();
        row.setBackgroundColor(-1);
        row.setElevation(dp(10));
        row.setPadding(dp(6), dp(8), dp(6), dp(8));
        String[] names = {"Кабинет", "Звонки", "Контакты", "Чаты", "Профиль"};
        int[] icons = {R.drawable.ic_home, R.drawable.ic_dialpad, R.drawable.ic_contacts, R.drawable.ic_chat, R.drawable.ic_user};
        final int[] tabs = {0, 2, 5, 6, 4};
        int unread = phoneData().unreadTotal();
        for (int i = 0; i < 5; i++) {
            final int tab = tabs[i];
            boolean active = this.selectedTab == tab || (tab == 0 && (this.selectedTab == 1 || this.selectedTab == 3));
            LinearLayout column = column();
            column.setGravity(1);
            FrameLayout pill = new FrameLayout(this);
            pill.setBackground(active ? rounded(YELLOW, 16, 0, 0) : null);
            pill.addView(new Ico.PlainView(this, Ico.get(this, icons[i], active ? BLUE_DARK : MUTED), 0.78f), new FrameLayout.LayoutParams(dp(60), dp(32)));
            if (tab == 6 && unread > 0) {
                TextView badge = label(unread > 9 ? "9+" : Integer.toString(unread), 10, -1, true);
                badge.setGravity(17);
                badge.setBackground(rounded(RED, 9, 0, 0));
                FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(dp(18), dp(18));
                bp.leftMargin = dp(34);
                bp.topMargin = dp(-2);
                pill.addView(badge, bp);
            }
            column.addView(pill, new LinearLayout.LayoutParams(dp(60), dp(32)));
            TextView text = label(names[i], 11, active ? BLUE_DARK : MUTED, active);
            text.setGravity(17);
            text.setPadding(0, dp(3), 0, 0);
            column.addView(text, matchWrap());
            column.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.chatNumber = null;
                    MainActivity.this.selectedTab = tab;
                    MainActivity.this.showCabinet();
                }
            });
            row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }
        return row;
    }

    private Button button(String str, int i, int i2) {
        Button button = new Button(this);
        button.setText(str);
        button.setTextSize(15.0f);
        button.setTextColor(i);
        button.setAllCaps(false);
        button.setGravity(17);
        button.setBackground(rounded(i2, 16, i2 == 0 ? 0 : BORDER, i2 == 0 ? 0 : 1));
        button.setMinHeight(0);
        button.setMinimumHeight(0);
        return button;
    }

    private View callCard(CallDetail callDetail) {
        LinearLayout colorCard = colorCard(-1, BORDER, 20);
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon("↗", -1, ORANGE, 42), new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout column = column();
        column.setPadding(dp(12), 0, 0, 0);
        column.addView(label(displayValue(callDetail.destination), 17, BLUE_DARK, true), matchWrap());
        column.addView(label(displayValue(callDetail.direction) + " · " + displayValue(callDetail.dateTime), 12, MUTED, false), matchWrap());
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (!"—".equals(callDetail.cost)) {
            row.addView(chip(callDetail.cost, RED, Color.rgb(255, 237, 237)), wrap());
        }
        colorCard.addView(row, matchWrap());
        LinearLayout row2 = row();
        row2.setGravity(16);
        row2.setPadding(0, dp(13), 0, 0);
        row2.addView(chip("◷ " + displayValue(callDetail.duration), BLUE, SOFT_BLUE), wrap());
        colorCard.addView(row2, matchWrap());
        return colorCard;
    }

    private LinearLayout card() {
        LinearLayout column = column();
        column.setPadding(dp(20), dp(18), dp(20), dp(18));
        column.setBackground(rounded(-1, 22, BORDER, 1));
        column.setElevation(dp(1));
        return column;
    }

    private LinearLayout.LayoutParams cardMargin() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10);
        return matchWrap;
    }

    private LinearLayout cardWithMargin() {
        return card();
    }

    private TextView chip(String str, int i, int i2) {
        TextView label = label(str, 12, i, true);
        label.setGravity(17);
        label.setPadding(dp(10), dp(6), dp(10), dp(6));
        label.setBackground(rounded(i2, 16, 0, 0));
        return label;
    }

    private TextView circleIcon(String str, int i, int i2, int i3) {
        int res = Ico.res(str);
        TextView label;
        if (res != 0) {
            label = new Ico.CircleView(this, Ico.get(this, res, i));
        } else {
            label = label(str, Math.max(12, i3 / 3), i, true);
        }
        label.setGravity(17);
        label.setBackground(rounded(i2, i3, 0, 0));
        return label;
    }

    private void clearAuthGuard() {
        getSharedPreferences("auth_guard", 0).edit().remove("blocked_until").apply();
    }

    private void clearPortalOtpUi(boolean z) {
        this.activePortalOtpField = null;
        this.activePortalOtpConfirm = null;
        this.portalOtpAutoSubmitInFlight = false;
        if (z) {
            this.portalOtpRequestedAt = 0L;
        }
    }

    private LinearLayout colorCard(int i, int i2, int i3) {
        LinearLayout column = column();
        column.setPadding(dp(18), dp(16), dp(18), dp(16));
        column.setBackground(rounded(i, i3, i2, i2 == 0 ? 0 : 1));
        return column;
    }

    private LinearLayout column() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(1);
        return linearLayout;
    }

    private static String combineProfileValue(String str, String str2) {
        String displayValue = displayValue(str);
        String displayValue2 = displayValue(str2);
        if ("Не указано".equals(displayValue)) {
            return displayValue2;
        }
        if ("Не указано".equals(displayValue2) || displayValue.equals(displayValue2)) {
            return displayValue;
        }
        return displayValue + " · " + displayValue2;
    }

    private void configureAuthGuard(final Button button, final String str) {
        stopAuthGuardTimer();
        Runnable runnable = new Runnable() { // from class: ua.telecomplus.app.MainActivity.3
            @Override // java.lang.Runnable
            public void run() {
                String str2;
                long authBlockedSeconds = MainActivity.this.authBlockedSeconds();
                button.setEnabled(authBlockedSeconds <= 0);
                Button button2 = button;
                if (authBlockedSeconds > 0) {
                    str2 = "Повторить через " + MainActivity.formatDuration(authBlockedSeconds);
                } else {
                    str2 = str;
                }
                button2.setText(str2);
                if (authBlockedSeconds > 0) {
                    MainActivity.this.handler.postDelayed(this, 1000L);
                }
            }
        };
        this.authGuardTick = runnable;
        this.handler.post(runnable);
    }

    private void confirmLogout(final boolean z) {
        new AlertDialog.Builder(this).setTitle(z ? "Забыть устройство?" : "Выйти из кабинета?").setMessage(z ? "Будут безвозвратно удалены серверные и браузерные cookies, сохранённый номер, локальный кэш и ключи шифрования Telecom+." : "Серверная, нативная и браузерная сессии будут завершены.").setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).setPositiveButton(z ? "Удалить всё" : "Выйти", new DialogInterface.OnClickListener() { // from class: o
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.lambda$confirmLogout$81(z, dialogInterface, i);
            }
        }).show();
    }

    private void consumeRecentPortalOtp() {
        if (this.activePortalOtpField == null || this.activePortalOtpConfirm == null) {
            return;
        }
        SharedPreferences sharedPreferences = getSharedPreferences("otp_call_capture_v1", 0);
        long j = sharedPreferences.getLong("last_captured_at", 0L);
        if (j <= sharedPreferences.getLong("last_consumed_at", 0L)) {
            return;
        }
        acceptCapturedPortalOtp(sharedPreferences.getString("last_code", ""), j);
    }

    private void continueStartup() {
        this.startupCompleted = true;
        showChecking();
        runPortal(new defpackage.b(this, 1), new a(this, 3));
    }

    private Button danger(String str) {
        return button(str, -1, RED);
    }

    private String dateGroup(String str) {
        Matcher matcher = Pattern.compile("\\b\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}\\b").matcher(displayValue(str));
        return matcher.find() ? matcher.group() : "Звонки";
    }

    private static String digits(String str) {
        return str == null ? "" : str.replaceAll("\\D+", "");
    }

    private static String leakedLabel(String str) {
        if (str == null) {
            return null;
        }
        String t = str.trim();
        while (t.startsWith(":")) {
            t = t.substring(1).trim();
        }
        String lc = t.toLowerCase(Locale.ROOT);
        if (lc.contains("данные абонента") || lc.contains("дані абонента") || lc.contains("движение средств") || lc.contains("рух коштів")) {
            return "";
        }
        return t;
    }

    private static String displayValue(String str) {
        str = leakedLabel(str);
        return (str == null || str.trim().isEmpty() || "—".equals(str.trim())) ? "Не указано" : str.trim();
    }

    private int dp(int i) {
        return Math.round(i * getResources().getDisplayMetrics().density);
    }

    private static String durationLabel(long j) {
        long j2 = j / 3600;
        long j3 = (j % 3600) / 60;
        long j4 = j % 60;
        StringBuilder sb = new StringBuilder();
        if (j2 > 0) {
            sb.append(j2);
            sb.append(" ч ");
        }
        if (j3 > 0 || j2 > 0) {
            sb.append(j3);
            sb.append(" мин ");
        }
        sb.append(j4);
        sb.append(" сек");
        return sb.toString().trim();
    }

    private static long durationSeconds(String str) {
        String lowerCase = str == null ? "" : str.toLowerCase(Locale.ROOT);
        Matcher matcher = Pattern.compile("(\\d+)\\s*(?:час|год)").matcher(lowerCase);
        long parseLong = matcher.find() ? Long.parseLong(matcher.group(1)) * 3600 : 0L;
        Matcher matcher2 = Pattern.compile("(\\d+)\\s*(?:мин|хв)").matcher(lowerCase);
        if (matcher2.find()) {
            parseLong += Long.parseLong(matcher2.group(1)) * 60;
        }
        Matcher matcher3 = Pattern.compile("(\\d+)\\s*(?:сек)").matcher(lowerCase);
        if (matcher3.find()) {
            parseLong += Long.parseLong(matcher3.group(1));
        }
        Matcher matcher4 = Pattern.compile("\\b(\\d{1,2}):(\\d{2})(?::(\\d{2}))?\\b").matcher(lowerCase);
        if (parseLong != 0 || !matcher4.find()) {
            return parseLong;
        }
        if (matcher4.group(3) == null) {
            return Long.parseLong(matcher4.group(2)) + (Long.parseLong(matcher4.group(1)) * 60);
        }
        return Long.parseLong(matcher4.group(3)) + (Long.parseLong(matcher4.group(2)) * 60) + (Long.parseLong(matcher4.group(1)) * 3600);
    }

    private void enterCabinet() {
        clearPortalOtpUi(true);
        stopOtpTimer();
        this.domFundsAttempted = false;
        this.domCallsAttempted = false;
        this.fundsSafeReport = "";
        this.callsSafeReport = "";
        stopAuthGuardTimer();
        showLoading("Загружаем данные кабинета…");
        this.executor.execute(new defpackage.i(this, 0));
    }

    private EditText field(String str, int i) {
        EditText editText = new EditText(this);
        editText.setHint(str);
        editText.setInputType(i);
        editText.setSingleLine(true);
        editText.setTextColor(TEXT);
        editText.setHintTextColor(Color.rgb(155, 164, 175));
        editText.setBackground(rounded(-1, 12, BORDER, 1));
        editText.setPadding(dp(16), 0, dp(16), 0);
        return editText;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static String formatDuration(long j) {
        return String.format("%02d:%02d", Long.valueOf(Math.max(0L, j) / 60), Long.valueOf(Math.max(0L, j) % 60));
    }

    private static String formatInternationalPhone(String str) {
        String digits = digits(str);
        return digits.length() == 9 ? "+380".concat(digits) : (digits.length() == 10 && digits.startsWith("0")) ? "+38".concat(digits) : (digits.length() == 12 && digits.startsWith("380")) ? "+".concat(digits) : displayValue(str);
    }

    private Button fundsFilterButton(String str, int i) {
        boolean z = this.fundsFilter == i;
        Button button = button(str, z ? -1 : BLUE, z ? BLUE : Color.rgb(238, 245, 253));
        button.setTextSize(13.0f);
        button.setOnClickListener(new defpackage.f(this, i, 2));
        return button;
    }

    private View fundsSummaryRow(String str, String str2, int i) {
        LinearLayout row = row();
        row.setGravity(16);
        row.setPadding(0, dp(6), 0, dp(6));
        row.addView(label(str, 14, MUTED, false), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView label = label(displayValue(str2), 15, i, true);
        label.setGravity(8388613);
        row.addView(label, wrap());
        return row;
    }

    private View fundsTransactionCard(FundsTransaction fundsTransaction) {
        int i = fundsTransaction.isCredit() ? GREEN : fundsTransaction.isDebit() ? RED : BLUE_DARK;
        LinearLayout colorCard = colorCard(fundsTransaction.isCredit() ? Color.rgb(246, 253, 248) : fundsTransaction.isDebit() ? Color.rgb(255, 248, 248) : -1, BORDER, 18);
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon(fundsTransaction.isCredit() ? "+" : fundsTransaction.isDebit() ? "−" : "₴", i, -1, 38), new LinearLayout.LayoutParams(dp(38), dp(38)));
        LinearLayout column = column();
        column.setPadding(dp(12), 0, dp(8), 0);
        column.addView(label(displayValue(fundsTransaction.description), 16, TEXT, true), matchWrap());
        String displayValue = displayValue(fundsTransaction.dateTime);
        int i2 = MUTED;
        column.addView(label(displayValue, 12, i2, false), matchWrap());
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView label = label(displayValue(fundsTransaction.amount), 17, i, true);
        label.setGravity(8388613);
        row.addView(label, wrap());
        colorCard.addView(row, matchWrap());
        LinearLayout row2 = row();
        row2.setGravity(16);
        row2.setPadding(dp(50), dp(10), 0, 0);
        row2.addView(chip(displayValue(fundsTransaction.category), i, fundsTransaction.isCredit() ? Color.rgb(226, 247, 233) : fundsTransaction.isDebit() ? Color.rgb(255, 229, 229) : SOFT_BLUE), wrap());
        if (!"—".equals(fundsTransaction.balanceAfter)) {
            TextView label2 = label("После: " + fundsTransaction.balanceAfter, 12, i2, false);
            label2.setGravity(8388613);
            row2.addView(label2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }
        colorCard.addView(row2, matchWrap());
        return colorCard;
    }

    private Button glassButton(String str) {
        Button button = button(str, -1, Color.argb(38, 255, 255, 255));
        button.setTypeface(Typeface.DEFAULT, 1);
        return button;
    }

    private GradientDrawable gradient(int i, int i2, int i3) {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{i, i2});
        gradientDrawable.setCornerRadius(dp(i3));
        return gradientDrawable;
    }

    private Button historyModeButton(String str, int i) {
        int i2 = 1;
        int i3 = this.historyMode == i ? 1 : 0;
        Button button = button(str, i3 != 0 ? BLUE_DARK : MUTED, i3 != 0 ? YELLOW : 0);
        button.setTypeface(Typeface.DEFAULT, i3);
        button.setOnClickListener(new defpackage.f(this, i, i2));
        return button;
    }

    private TextView info(String str) {
        TextView label = label(str, 14, TEXT, false);
        label.setPadding(dp(14), dp(12), dp(14), dp(12));
        label.setBackground(rounded(Color.rgb(233, 243, 254), 12, Color.rgb(185, 214, 244), 1));
        return label;
    }

    private void initializePeriodDefaults() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(5, 1);
        calendar.add(2, -1);
        this.fundsPeriodYear = calendar.get(1);
        int i = calendar.get(2) + 1;
        this.fundsPeriodMonth = i;
        this.callsPeriodYear = this.fundsPeriodYear;
        this.callsPeriodMonth = i;
    }

    private boolean isOtpNotificationAccessEnabled() {
        String string = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        if (string != null && !string.trim().isEmpty()) {
            ComponentName componentName = new ComponentName(this, (Class<?>) OtpCallCaptureService.class);
            for (String str : string.split(":")) {
                if (componentName.equals(ComponentName.unflattenFromString(str))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static String joinCodecs(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "не определены";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = list.iterator();
        if (it.hasNext()) {
            while (true) {
                sb.append((CharSequence) it.next());
                if (!it.hasNext()) {
                    break;
                }
                sb.append((CharSequence) " · ");
            }
        }
        return sb.toString();
    }

    private String joinWarnings(String str, String str2) {
        String trim = str == null ? "" : str.trim();
        String trim2 = str2 != null ? str2.trim() : "";
        if (trim.isEmpty()) {
            return trim2;
        }
        if (trim2.isEmpty() || trim.equals(trim2)) {
            return trim;
        }
        return trim + " · " + trim2;
    }

    private TextView label(String str, int i, int i2, boolean z) {
        TextView textView = new TextView(this);
        textView.setText(str);
        textView.setTextSize(i);
        textView.setTextColor(i2);
        textView.setLineSpacing(0.0f, 1.08f);
        if (z) {
            textView.setTypeface(Typeface.DEFAULT, 1);
        }
        return textView;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$acceptCapturedPortalOtp$28(EditText editText, Button button) {
        if (editText == this.activePortalOtpField && button == this.activePortalOtpConfirm) {
            submitPortalOtp(editText, button);
        } else {
            this.portalOtpAutoSubmitInFlight = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$bottomNav$44(int i, View view) {
        this.selectedTab = i;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$confirmLogout$81(boolean z, DialogInterface dialogInterface, int i) {
        performLogout(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ PortalResult lambda$continueStartup$2() {
        return this.gateway.hasSavedSession() ? this.gateway.validateSession() : PortalResult.failure("Нет сессии", true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$continueStartup$3(PortalResult portalResult) {
        if (portalResult instanceof PortalResult.Success) {
            enterCabinet();
            return;
        }
        PortalResult.Failure failure = (PortalResult.Failure) portalResult;
        DashboardSnapshot cachedDashboard = failure.unauthorized ? null : this.gateway.getCachedDashboard();
        if (cachedDashboard == null) {
            showNumberLogin("Нет сессии".equals(failure.message) ? null : failure.message);
            return;
        }
        this.dashboard = cachedDashboard;
        this.services = this.gateway.getCachedServices();
        this.fundsFlow = this.gateway.getCachedFundsFlow();
        this.callHistory = this.gateway.getCachedCallHistory();
        this.showingCachedData = true;
        this.refreshWarning = failure.message;
        this.selectedTab = 0;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$enterCabinet$29(PortalResult portalResult, PortalResult portalResult2, PortalResult portalResult3, PortalResult portalResult4) {
        if (portalResult instanceof PortalResult.Failure) {
            PortalResult.Failure failure = (PortalResult.Failure) portalResult;
            if (failure.unauthorized) {
                showNumberLogin(failure.message);
                return;
            }
            DashboardSnapshot cachedDashboard = this.gateway.getCachedDashboard();
            if (cachedDashboard == null) {
                showLoadFailure(failure.message);
                return;
            }
            this.dashboard = cachedDashboard;
            this.services = this.gateway.getCachedServices();
            this.fundsFlow = this.gateway.getCachedFundsFlow();
            this.callHistory = this.gateway.getCachedCallHistory();
            this.showingCachedData = true;
            this.refreshWarning = failure.message;
            this.selectedTab = 0;
            showCabinet();
            return;
        }
        this.dashboard = (DashboardSnapshot) ((PortalResult.Success) portalResult).value;
        if (portalResult2 instanceof PortalResult.Success) {
            this.services = (List) ((PortalResult.Success) portalResult2).value;
        } else {
            List<ServiceItem> cachedServices = this.gateway.getCachedServices();
            if (cachedServices == null) {
                cachedServices = new ArrayList<>();
            }
            this.services = cachedServices;
        }
        if (portalResult3 instanceof PortalResult.Success) {
            this.fundsFlow = (FundsFlowSnapshot) ((PortalResult.Success) portalResult3).value;
        } else {
            this.fundsFlow = this.gateway.getCachedFundsFlow();
        }
        if (portalResult4 instanceof PortalResult.Success) {
            this.callHistory = (CallHistorySnapshot) ((PortalResult.Success) portalResult4).value;
        } else {
            this.callHistory = this.gateway.getCachedCallHistory();
        }
        this.showingCachedData = false;
        this.refreshWarning = joinWarnings(joinWarnings(portalResult2 instanceof PortalResult.Failure ? ((PortalResult.Failure) portalResult2).message : "", portalResult3 instanceof PortalResult.Failure ? ((PortalResult.Failure) portalResult3).message : ""), portalResult4 instanceof PortalResult.Failure ? ((PortalResult.Failure) portalResult4).message : "");
        this.selectedTab = 0;
        showCabinet();
        startDomFallbackIfNeeded();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$enterCabinet$30() {
        final PortalResult<DashboardSnapshot> dashboard = this.gateway.getDashboard();
        final PortalResult<List<ServiceItem>> services = this.gateway.getServices();
        final PortalResult<FundsFlowSnapshot> fundsFlow = this.gateway.getFundsFlow();
        final PortalResult<CallHistorySnapshot> callHistory = this.gateway.getCallHistory();
        this.handler.post(new Runnable() { // from class: ua.telecomplus.app.h
            @Override // java.lang.Runnable
            public final void run() {
                MainActivity.this.lambda$enterCabinet$29(dashboard, services, fundsFlow, callHistory);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$fundsFilterButton$77(int i, View view) {
        this.fundsFilter = i;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$historyModeButton$61(int i, View view) {
        this.historyMode = i;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$loadCallsDomIfNeeded$37(PortalResult portalResult, Runnable runnable) {
        if (portalResult instanceof PortalResult.Success) {
            this.callHistory = (CallHistorySnapshot) ((PortalResult.Success) portalResult).value;
            if (this.selectedTab == 3 && this.historyMode == 1) {
                showCabinet();
            }
        } else {
            this.refreshWarning = joinWarnings(this.refreshWarning, ((PortalResult.Failure) portalResult).message);
        }
        runnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadCallsDomIfNeeded$38(PortalPageSnapshot portalPageSnapshot, Runnable runnable) {
        this.handler.post(new e(this, this.gateway.acceptCallHistoryHtml(portalPageSnapshot.html), runnable, 1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$loadCallsDomIfNeeded$39(PortalResult portalResult, Runnable runnable) {
        if (!(portalResult instanceof PortalResult.Success)) {
            this.refreshWarning = joinWarnings(this.refreshWarning, ((PortalResult.Failure) portalResult).message);
            runnable.run();
        } else {
            PortalPageSnapshot portalPageSnapshot = (PortalPageSnapshot) ((PortalResult.Success) portalResult).value;
            this.callsSafeReport = portalPageSnapshot.safeReport;
            refreshNativeCookies(portalPageSnapshot.cookieHeader);
            this.executor.execute(new k(this, portalPageSnapshot, runnable, 1));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadCallsDomIfNeeded$40(Runnable runnable, PortalResult portalResult) {
        this.handler.post(new e(this, portalResult, runnable, 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$loadCallsMonth$72(PortalResult portalResult, List list, List list2, int[] iArr, List list3, int i) {
        if (portalResult instanceof PortalResult.Success) {
            list.add((CallHistorySnapshot) ((PortalResult.Success) portalResult).value);
        } else {
            list2.add(monthName(iArr[0], iArr[1]) + ": " + ((PortalResult.Failure) portalResult).message);
        }
        loadCallsMonth(list3, i + 1, list, list2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadCallsMonth$73(PortalPageSnapshot portalPageSnapshot, List list, List list2, int[] iArr, List list3, int i) {
        this.handler.post(new f(this, this.gateway.acceptCallHistoryHtml(portalPageSnapshot.html), list, list2, iArr, list3, i, 3));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$loadCallsMonth$74(PortalResult portalResult, List list, List list2, int[] iArr, List list3, int i) {
        if (portalResult instanceof PortalResult.Success) {
            PortalPageSnapshot portalPageSnapshot = (PortalPageSnapshot) ((PortalResult.Success) portalResult).value;
            if (!portalPageSnapshot.safeReport.trim().isEmpty()) {
                this.callsSafeReport = portalPageSnapshot.safeReport;
            }
            refreshNativeCookies(portalPageSnapshot.cookieHeader);
            this.executor.execute(new j(this, portalPageSnapshot, list, list2, iArr, list3, i));
            return;
        }
        PortalResult.Failure failure = (PortalResult.Failure) portalResult;
        if (failure.unauthorized) {
            this.callsPeriodLoading = false;
            showNumberLogin(failure.message);
            return;
        }
        list2.add(monthName(iArr[0], iArr[1]) + ": " + failure.message);
        loadCallsMonth(list3, i + 1, list, list2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadCallsMonth$75(List list, List list2, int[] iArr, List list3, int i, PortalResult portalResult) {
        this.handler.post(new f(this, portalResult, list, list2, iArr, list3, i, 1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$loadFundsDomIfNeeded$33(PortalResult portalResult, Runnable runnable) {
        if (portalResult instanceof PortalResult.Success) {
            this.fundsFlow = (FundsFlowSnapshot) ((PortalResult.Success) portalResult).value;
            if (this.selectedTab == 3 && this.historyMode == 0) {
                showCabinet();
            }
        } else {
            this.refreshWarning = joinWarnings(this.refreshWarning, ((PortalResult.Failure) portalResult).message);
        }
        runnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadFundsDomIfNeeded$34(PortalPageSnapshot portalPageSnapshot, Runnable runnable) {
        this.handler.post(new e(this, this.gateway.acceptFundsFlowHtml(portalPageSnapshot.html), runnable, 2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$loadFundsDomIfNeeded$35(PortalResult portalResult, Runnable runnable) {
        if (!(portalResult instanceof PortalResult.Success)) {
            this.refreshWarning = joinWarnings(this.refreshWarning, ((PortalResult.Failure) portalResult).message);
            runnable.run();
        } else {
            PortalPageSnapshot portalPageSnapshot = (PortalPageSnapshot) ((PortalResult.Success) portalResult).value;
            this.fundsSafeReport = portalPageSnapshot.safeReport;
            refreshNativeCookies(portalPageSnapshot.cookieHeader);
            this.executor.execute(new k(this, portalPageSnapshot, runnable, 0));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadFundsDomIfNeeded$36(Runnable runnable, PortalResult portalResult) {
        this.handler.post(new e(this, portalResult, runnable, 3));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$loadFundsMonth$68(PortalResult portalResult, int[] iArr, List list, List list2, List list3, int i) {
        if (portalResult instanceof PortalResult.Success) {
            FundsFlowSnapshot fundsFlowSnapshot = (FundsFlowSnapshot) ((PortalResult.Success) portalResult).value;
            if (periodMatches(fundsFlowSnapshot.periodLabel, iArr[0], iArr[1])) {
                list.add(fundsFlowSnapshot);
            } else {
                list2.add(monthName(iArr[0], iArr[1]) + ": портал вернул другой месяц (" + displayValue(fundsFlowSnapshot.periodLabel) + ")");
            }
        } else {
            list2.add(monthName(iArr[0], iArr[1]) + ": " + ((PortalResult.Failure) portalResult).message);
        }
        loadFundsMonth(list3, i + 1, list, list2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadFundsMonth$69(PortalPageSnapshot portalPageSnapshot, int[] iArr, List list, List list2, List list3, int i) {
        this.handler.post(new f(this, this.gateway.acceptFundsFlowHtml(portalPageSnapshot.html), iArr, list, list2, list3, i, 2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$loadFundsMonth$70(PortalResult portalResult, int[] iArr, List list, List list2, List list3, int i) {
        if (portalResult instanceof PortalResult.Success) {
            PortalPageSnapshot portalPageSnapshot = (PortalPageSnapshot) ((PortalResult.Success) portalResult).value;
            if (!portalPageSnapshot.safeReport.trim().isEmpty()) {
                this.fundsSafeReport = portalPageSnapshot.safeReport;
            }
            refreshNativeCookies(portalPageSnapshot.cookieHeader);
            this.executor.execute(new j(this, portalPageSnapshot, iArr, list, list2, list3, i));
            return;
        }
        PortalResult.Failure failure = (PortalResult.Failure) portalResult;
        if (failure.unauthorized) {
            this.fundsPeriodLoading = false;
            showNumberLogin(failure.message);
            return;
        }
        list2.add(monthName(iArr[0], iArr[1]) + ": " + failure.message);
        loadFundsMonth(list3, i + 1, list, list2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loadFundsMonth$71(int[] iArr, List list, List list2, List list3, int i, PortalResult portalResult) {
        this.handler.post(new f(this, portalResult, iArr, list, list2, list3, i, 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$0(TelephonySnapshot telephonySnapshot) {
        this.telephonySnapshot = telephonySnapshot;
        ensureTelephonyService();
        if (this.dashboard != null && this.telephony.incomingRinging()) {
            this.chatNumber = null;
            this.selectedTab = 2;
            showCabinet();
            return;
        }
        if (this.dashboard == null || this.selectedTab != 2) {
            return;
        }
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$onCreate$1(TelephonySnapshot telephonySnapshot) {
        this.handler.post(new d(this, telephonySnapshot, 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ PortalResult lambda$performLogout$82(boolean z) {
        return this.gateway.logout(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$performLogout$83(boolean z, PortalResult portalResult) {
        if (z) {
            getSharedPreferences("telecom_plus_local_security_v1", 0).edit().clear().commit();
            getSharedPreferences("auth_guard", 0).edit().clear().commit();
        }
        if (z) {
            TelephonyService.stop(this);
            this.serviceStarted = false;
            phoneData().clearAll();
        }
        this.dashboard = null;
        this.services = new ArrayList();
        this.fundsFlow = null;
        this.callHistory = null;
        this.phone = "";
        this.showingCachedData = false;
        this.refreshWarning = "";
        this.otpRepeatAllowedAt = 0L;
        showNumberLogin(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$performLogout$84(final boolean z) {
        runPortal(new Callable() { // from class: q
            @Override // java.util.concurrent.Callable
            public final Object call() {
                PortalResult lambda$performLogout$82;
                lambda$performLogout$82 = MainActivity.this.lambda$performLogout$82(z);
                return lambda$performLogout$82;
            }
        }, new PortalCallback() { // from class: ua.telecomplus.app.l
            @Override // ua.telecomplus.app.MainActivity.PortalCallback
            public final void accept(PortalResult portalResult) {
                MainActivity.this.lambda$performLogout$83(z, portalResult);
            }
        });
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$periodSelectorCard$63(boolean z, View view) {
        showMonthDialog(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$periodSelectorCard$64(boolean z, View view) {
        showRangeDialog(z);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$periodSelectorCard$65(boolean z, View view) {
        if (z) {
            loadFundsSelectedPeriod();
        } else {
            loadCallsSelectedPeriod();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderCalls$78(View view) {
        saveDiagnosticReport("telecomplus-calls-structure.txt", this.callsSafeReport);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderFunds$76(View view) {
        saveDiagnosticReport("telecomplus-funds-structure.txt", this.fundsSafeReport);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderHome$45(View view) {
        unavailable("Платёжная интеграция будет подключена после безопасного захвата полного сценария.");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderHome$46(View view) {
        this.selectedTab = 3;
        this.historyMode = 0;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderHome$47(View view) {
        this.selectedTab = 3;
        this.historyMode = 1;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderHome$48(View view) {
        this.selectedTab = 1;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderHome$49(View view) {
        this.selectedTab = 2;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ TelephonyResult lambda$renderPhone$50(String str) {
        return this.telephony.confirmProvisioningOtp(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderPhone$51(EditText editText, Button button, View view) {
        String digits = digits(editText.getText().toString());
        if (digits.length() != 4) {
            Toast.makeText(this, "Введите ровно четыре цифры", 0).show();
            return;
        }
        button.setEnabled(false);
        button.setText("Проверяем…");
        runTelephony(new defpackage.m(this, digits, 2), new a(this, 5));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderPhone$52(TelephonySnapshot telephonySnapshot, Button button, View view) {
        requestTelephonyActivation(telephonySnapshot.phoneNumber, button);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderPhone$53(EditText editText, Button button, View view) {
        requestTelephonyActivation(editText.getText().toString(), button);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$renderPhone$55(EditText editText, View view) {
        int length = editText.length();
        if (length > 0) {
            editText.getText().delete(length - 1, length);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ TelephonyResult lambda$renderPhone$56(EditText editText) {
        return this.telephony.placeCall(editText.getText().toString(), false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderPhone$57(EditText editText, View view) {
        final String number = editText.getText().toString();
        withMicPermission(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.runTelephony(new Callable<TelephonyResult<Void>>() {
                    @Override
                    public TelephonyResult<Void> call() {
                        return MainActivity.this.telephony.placeCall(number, false);
                    }
                }, new TelephonyCallback<Void>() {
                    @Override
                    public void accept(TelephonyResult<Void> telephonyResult) {
                        MainActivity.this.showTelephonyResult(telephonyResult);
                    }
                });
            }
        });
    }

    private Runnable pendingMicAction;

    private void withMicPermission(Runnable runnable) {
        if (checkSelfPermission("android.permission.RECORD_AUDIO") == 0) {
            runnable.run();
            return;
        }
        this.pendingMicAction = runnable;
        requestPermissions(new String[]{"android.permission.RECORD_AUDIO"}, 4711);
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (i == 4713) {
            if (iArr.length > 0 && iArr[0] == 0) {
                importDeviceContacts();
            } else {
                Toast.makeText(this, "Нет доступа к контактам", 0).show();
            }
        }
        if (i == 4715) {
            String pending = this.pendingCallNumber;
            this.pendingCallNumber = null;
            if (pending != null) {
                launchItPhoneCall(pending);
            }
        }
        if (i == 4711) {
            Runnable runnable = this.pendingMicAction;
            this.pendingMicAction = null;
            if (iArr.length == 0 || iArr[0] != 0) {
                Toast.makeText(this, "Нет доступа к микрофону — собеседник вас не услышит", 1).show();
            }
            if (runnable != null) {
                runnable.run();
            }
        }
    }

    private void telephonyAction(final Callable<TelephonyResult<Void>> callable) {
        runTelephony(callable, new TelephonyCallback<Void>() {
            @Override
            public void accept(TelephonyResult<Void> telephonyResult) {
                MainActivity.this.showTelephonyResult(telephonyResult);
            }
        });
    }

    private void showTelephonyLog() {
        TextView textView = new TextView(this);
        textView.setTextIsSelectable(true);
        textView.setTextSize(11.0f);
        textView.setTypeface(Typeface.MONOSPACE);
        textView.setPadding(dp(14), dp(10), dp(14), dp(10));
        final String dump = TelephonyLog.dump();
        textView.setText(dump.isEmpty() ? "Журнал пуст" : dump);
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(textView);
        new AlertDialog.Builder(this).setTitle("Журнал телефонии").setView(scrollView).setPositiveButton("Поделиться", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                android.content.Intent intent = new android.content.Intent("android.intent.action.SEND");
                intent.setType("text/plain");
                intent.putExtra("android.intent.extra.TEXT", dump);
                MainActivity.this.startActivity(android.content.Intent.createChooser(intent, "Журнал телефонии"));
            }
        }).setNeutralButton("Очистить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                TelephonyLog.clear();
            }
        }).setNegativeButton("Закрыть", (DialogInterface.OnClickListener) null).show();
    }

    private final StringBuilder dialBuf = new StringBuilder();
    private TextView dialDisplay;
    private boolean callPadShown;
    private android.media.ToneGenerator toneGen;

    private static final String[] KEY_LABELS = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "*", "0", "#"};
    private static final String[] KEY_LETTERS = {"", "ABC", "DEF", "GHI", "JKL", "MNO", "PQRS", "TUV", "WXYZ", "", "+", ""};

    private void keyTone(char c) {
        try {
            if (this.toneGen == null) {
                this.toneGen = new android.media.ToneGenerator(android.media.AudioManager.STREAM_DTMF, 60);
            }
            int t = c == '*' ? android.media.ToneGenerator.TONE_DTMF_S : c == '#' ? android.media.ToneGenerator.TONE_DTMF_P : android.media.ToneGenerator.TONE_DTMF_0 + (c - '0');
            this.toneGen.startTone(t, 90);
        } catch (Exception unused) {
        }
    }

    private View keypadKey(final int index, int size, boolean dark, final View.OnClickListener extra) {
        final String digit = KEY_LABELS[index];
        LinearLayout key = column();
        key.setGravity(17);
        key.setBackground(rounded(dark ? Color.argb(36, 255, 255, 255) : Color.rgb(238, 244, 254), size / 2, 0, 0));
        TextView d = label(digit, 28, dark ? -1 : BLUE_DARK, false);
        d.setGravity(17);
        key.addView(d, wrap());
        String letters = KEY_LETTERS[index];
        if (!letters.isEmpty()) {
            TextView l = label(letters, 9, dark ? Color.rgb(190, 212, 250) : MUTED, true);
            l.setGravity(17);
            l.setLetterSpacing(0.12f);
            key.addView(l, wrap());
        }
        key.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.performHapticFeedback(3);
                keyTone(digit.charAt(0));
                extra.onClick(view);
            }
        });
        if ("0".equals(digit)) {
            key.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    view.performHapticFeedback(0);
                    if (extra instanceof PlusAware) {
                        ((PlusAware) extra).plus();
                    }
                    return true;
                }
            });
        }
        return key;
    }

    private abstract static class PlusAware implements View.OnClickListener {
        abstract void plus();
    }

    private View keypadGrid(int size, boolean dark, java.util.function.Consumer<String> onKey, Runnable onPlus) {
        LinearLayout grid = column();
        grid.setGravity(1);
        for (int r = 0; r < 4; r++) {
            LinearLayout kr = row();
            kr.setGravity(17);
            for (int c = 0; c < 3; c++) {
                final int idx = r * 3 + c;
                final String digit = KEY_LABELS[idx];
                final Runnable plus = onPlus;
                final java.util.function.Consumer<String> cb = onKey;
                View key = keypadKey(idx, size, dark, new PlusAware() {
                    @Override
                    public void onClick(View view) {
                        cb.accept(digit);
                    }

                    @Override
                    void plus() {
                        if (plus != null) {
                            plus.run();
                        }
                    }
                });
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(size), dp(size));
                lp.setMargins(dp(10), dp(6), dp(10), dp(6));
                kr.addView(key, lp);
            }
            grid.addView(kr, matchWrap());
        }
        return grid;
    }

    private void refreshDial() {
        if (this.dialDisplay == null) {
            return;
        }
        String t = this.dialBuf.toString();
        this.dialDisplay.setText(t.isEmpty() ? "Введите номер" : t);
        this.dialDisplay.setTextColor(t.isEmpty() ? Color.rgb(160, 172, 192) : BLUE_DARK);
        this.dialDisplay.setTextSize(t.length() <= 11 ? 34.0f : t.length() <= 15 ? 28.0f : 22.0f);
    }

    private TextView roundIconButton(int iconRes, int iconColor, int bg, int size) {
        TextView v = new Ico.CircleView(this, Ico.get(this, iconRes, iconColor));
        v.setBackground(rounded(bg, size / 2, 0, 0));
        return v;
    }

    private void renderDialer(LinearLayout parent, final TelephonySnapshot snap) {
        this.dialBuf.setLength(0);
        LinearLayout card = column();
        card.setGravity(1);
        card.setPadding(dp(10), dp(16), dp(10), dp(18));
        card.setBackground(rounded(-1, 32, BORDER, 1));
        String st = telephonyStatusTitle(snap);
        TextView status = chip("●  " + st, GREEN, Color.rgb(226, 246, 235));
        card.addView(status, wrap());
        this.dialDisplay = label("Введите номер", 34, Color.rgb(160, 172, 192), true);
        this.dialDisplay.setGravity(17);
        this.dialDisplay.setSingleLine(true);
        this.dialDisplay.setPadding(dp(8), dp(14), dp(8), dp(10));
        this.dialDisplay.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    android.content.ClipboardManager cm = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (cm != null && cm.hasPrimaryClip() && cm.getPrimaryClip().getItemCount() > 0) {
                        CharSequence cs = cm.getPrimaryClip().getItemAt(0).getText();
                        String pasted = cs == null ? "" : cs.toString().replaceAll("[^0-9+*#]", "");
                        MainActivity.this.dialBuf.setLength(0);
                        MainActivity.this.dialBuf.append(pasted);
                        MainActivity.this.refreshDial();
                    }
                } catch (Exception unused) {
                }
                return true;
            }
        });
        card.addView(this.dialDisplay, matchWrap());
        card.addView(keypadGrid(74, false, new java.util.function.Consumer<String>() {
            @Override
            public void accept(String d) {
                if (MainActivity.this.dialBuf.length() < 20) {
                    MainActivity.this.dialBuf.append(d);
                    MainActivity.this.refreshDial();
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                if (MainActivity.this.dialBuf.length() == 0) {
                    MainActivity.this.dialBuf.append('+');
                    MainActivity.this.refreshDial();
                } else {
                    MainActivity.this.dialBuf.setLength(MainActivity.this.dialBuf.length() - 1);
                    MainActivity.this.dialBuf.append('+');
                    MainActivity.this.refreshDial();
                }
            }
        }), matchWrap());
        LinearLayout bottom = row();
        bottom.setGravity(17);
        View spacer = new View(this);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(74), dp(74));
        sp.setMargins(dp(10), dp(8), dp(10), dp(4));
        bottom.addView(spacer, sp);
        TextView callBtn = roundIconButton(R.drawable.ic_phone, -1, GREEN, 74);
        callBtn.setElevation(dp(4));
        callBtn.setContentDescription("Позвонить");
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = MainActivity.this.dialBuf.toString();
                if (number.isEmpty()) {
                    java.util.List<PhoneData.CallEntry> calls = MainActivity.this.phoneData().calls();
                    if (!calls.isEmpty()) {
                        MainActivity.this.dialBuf.append(calls.get(0).number);
                        MainActivity.this.refreshDial();
                    } else {
                        Toast.makeText(MainActivity.this, "Введите номер", 0).show();
                    }
                    return;
                }
                MainActivity.this.dialNumber(number);
            }
        });
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(dp(74), dp(74));
        cp.setMargins(dp(10), dp(8), dp(10), dp(4));
        bottom.addView(callBtn, cp);
        TextView back = roundIconButton(R.drawable.ic_backspace, MUTED, Color.TRANSPARENT, 74);
        back.setContentDescription("Стереть");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.this.dialBuf.length() > 0) {
                    MainActivity.this.dialBuf.setLength(MainActivity.this.dialBuf.length() - 1);
                    MainActivity.this.refreshDial();
                }
            }
        });
        back.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                MainActivity.this.dialBuf.setLength(0);
                MainActivity.this.refreshDial();
                return true;
            }
        });
        LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(dp(74), dp(74));
        bp.setMargins(dp(10), dp(8), dp(10), dp(4));
        bottom.addView(back, bp);
        card.addView(bottom, matchWrap());
        parent.addView(card, matchWrap());
        LinearLayout.LayoutParams rp = cardMargin();
        renderRecents(parent);
        LinearLayout tools = row();
        Button check = secondary("Открыть IT Phone");
        check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent launch = MainActivity.this.getPackageManager().getLaunchIntentForPackage(IT_PHONE_PACKAGE);
                if (launch != null) {
                    MainActivity.this.startActivity(launch);
                } else {
                    MainActivity.this.callViaItPhone("0");
                }
            }
        });
        Button log = secondary("Журнал");
        log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.showTelephonyLog();
            }
        });
        tools.addView(check, new LinearLayout.LayoutParams(0, dp(46), 1.0f));
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(0, dp(46), 1.0f);
        lp2.leftMargin = dp(10);
        tools.addView(log, lp2);
        parent.addView(tools, cardMargin());
    }

    private View callAction(int iconRes, String caption, boolean on, View.OnClickListener l) {
        LinearLayout col = column();
        col.setGravity(1);
        TextView c = roundIconButton(iconRes, on ? BLUE_DARK : -1, on ? -1 : Color.argb(40, 255, 255, 255), 68);
        col.addView(c, new LinearLayout.LayoutParams(dp(68), dp(68)));
        TextView t = label(caption, 12, Color.rgb(205, 224, 255), false);
        t.setGravity(17);
        t.setPadding(0, dp(6), 0, 0);
        col.addView(t, matchWrap());
        col.setOnClickListener(l);
        return col;
    }

    private void doTelephony(final Callable<TelephonyResult<Void>> action) {
        telephonyAction(action);
    }

    private void renderCallPanel(LinearLayout linearLayout, final TelephonySnapshot telephonySnapshot) {
        final boolean ringingIn = this.telephony.incomingRinging();
        LinearLayout screen = column();
        screen.setGravity(1);
        screen.setMinimumHeight(getResources().getDisplayMetrics().heightPixels - dp(40));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{BLUE_DARK, Color.rgb(14, 70, 160)});
        screen.setBackground(bg);
        screen.setPadding(dp(20), dp(48), dp(20), dp(36));
        String number = telephonySnapshot.remoteNumber == null ? "" : telephonySnapshot.remoteNumber;
        TextView head = label(ringingIn ? "Входящий вызов" : "Telecom+", 14, Color.rgb(200, 222, 255), false);
        head.setGravity(17);
        screen.addView(head, matchWrap());
        View av = avatar(number, 112);
        LinearLayout.LayoutParams avp = new LinearLayout.LayoutParams(dp(112), dp(112));
        avp.topMargin = dp(28);
        screen.addView(av, avp);
        TextView who = label(number.isEmpty() ? "Вызов" : formatInternationalPhone(number), 30, -1, true);
        who.setGravity(17);
        who.setPadding(0, dp(20), 0, dp(4));
        screen.addView(who, matchWrap());
        TextView state = label(ringingIn ? "Звонит…" : telephonySnapshot.statusMessage, 15, Color.rgb(200, 222, 255), false);
        state.setGravity(17);
        screen.addView(state, matchWrap());
        if (telephonySnapshot.callState == TelecomCallState.ACTIVE || telephonySnapshot.callState == TelecomCallState.HELD) {
            android.widget.Chronometer chronometer = new android.widget.Chronometer(this);
            chronometer.setTextSize(24.0f);
            chronometer.setTextColor(YELLOW);
            chronometer.setGravity(17);
            chronometer.setBase(android.os.SystemClock.elapsedRealtime() - Math.max(0L, System.currentTimeMillis() - telephonySnapshot.callStartedAt));
            chronometer.start();
            LinearLayout.LayoutParams chp = matchWrap();
            chp.topMargin = dp(10);
            screen.addView(chronometer, chp);
        }
        View filler = new View(this);
        screen.addView(filler, new LinearLayout.LayoutParams(1, 0, 1.0f));
        if (ringingIn) {
            LinearLayout row = row();
            row.setGravity(17);
            View decline = callAction(R.drawable.ic_call_end, "Отклонить", false, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.doTelephony(new Callable<TelephonyResult<Void>>() {
                        @Override
                        public TelephonyResult<Void> call() {
                            return MainActivity.this.telephony.hangup();
                        }
                    });
                }
            });
            ((TextView) ((LinearLayout) decline).getChildAt(0)).setBackground(rounded(RED, 34, 0, 0));
            View accept = callAction(R.drawable.ic_phone, "Ответить", false, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.withMicPermission(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.doTelephony(new Callable<TelephonyResult<Void>>() {
                                @Override
                                public TelephonyResult<Void> call() {
                                    return MainActivity.this.telephony.answer();
                                }
                            });
                        }
                    });
                }
            });
            ((TextView) ((LinearLayout) accept).getChildAt(0)).setBackground(rounded(GREEN, 34, 0, 0));
            LinearLayout.LayoutParams l1 = new LinearLayout.LayoutParams(0, -2, 1.0f);
            row.addView(decline, l1);
            row.addView(accept, new LinearLayout.LayoutParams(0, -2, 1.0f));
            screen.addView(row, matchWrap());
            linearLayout.addView(screen, new LinearLayout.LayoutParams(-1, -2));
            return;
        }
        if (this.callPadShown) {
            screen.addView(keypadGrid(66, true, new java.util.function.Consumer<String>() {
                @Override
                public void accept(final String key) {
                    MainActivity.this.doTelephony(new Callable<TelephonyResult<Void>>() {
                        @Override
                        public TelephonyResult<Void> call() {
                            return MainActivity.this.telephony.sendDtmf(key.charAt(0));
                        }
                    });
                }
            }, null), matchWrap());
        } else {
            LinearLayout r1 = row();
            r1.setGravity(17);
            final boolean speaker = telephonySnapshot.audioRoute == AudioRoute.SPEAKER;
            View mute = callAction(telephonySnapshot.muted ? R.drawable.ic_mic_off : R.drawable.ic_mic, "Микрофон", telephonySnapshot.muted, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final boolean target = !telephonySnapshot.muted;
                    MainActivity.this.doTelephony(new Callable<TelephonyResult<Void>>() {
                        @Override
                        public TelephonyResult<Void> call() {
                            return MainActivity.this.telephony.setMuted(target);
                        }
                    });
                }
            });
            View pad = callAction(R.drawable.ic_dialpad, "Клавиатура", false, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.callPadShown = true;
                    MainActivity.this.showCabinet();
                }
            });
            View spk = callAction(R.drawable.ic_speaker, "Динамик", speaker, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.doTelephony(new Callable<TelephonyResult<Void>>() {
                        @Override
                        public TelephonyResult<Void> call() {
                            return MainActivity.this.telephony.setAudioRoute(speaker ? AudioRoute.EARPIECE : AudioRoute.SPEAKER);
                        }
                    });
                }
            });
            r1.addView(mute, new LinearLayout.LayoutParams(0, -2, 1.0f));
            r1.addView(pad, new LinearLayout.LayoutParams(0, -2, 1.0f));
            r1.addView(spk, new LinearLayout.LayoutParams(0, -2, 1.0f));
            screen.addView(r1, matchWrap());
            LinearLayout r2 = row();
            r2.setGravity(17);
            r2.setPadding(0, dp(18), 0, 0);
            View hold = callAction(telephonySnapshot.held ? R.drawable.ic_play : R.drawable.ic_pause, telephonySnapshot.held ? "Снять" : "Удержание", telephonySnapshot.held, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final boolean target = !telephonySnapshot.held;
                    MainActivity.this.doTelephony(new Callable<TelephonyResult<Void>>() {
                        @Override
                        public TelephonyResult<Void> call() {
                            return MainActivity.this.telephony.setHeld(target);
                        }
                    });
                }
            });
            hold.setEnabled(telephonySnapshot.callState == TelecomCallState.ACTIVE || telephonySnapshot.callState == TelecomCallState.HELD);
            r2.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1.0f));
            r2.addView(hold, new LinearLayout.LayoutParams(0, -2, 1.0f));
            r2.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1.0f));
            screen.addView(r2, matchWrap());
        }
        LinearLayout endRow = row();
        endRow.setGravity(17);
        endRow.setPadding(0, dp(26), 0, 0);
        TextView end = roundIconButton(R.drawable.ic_call_end, -1, RED, 72);
        end.setElevation(dp(4));
        end.setContentDescription("Завершить");
        end.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.callPadShown = false;
                MainActivity.this.doTelephony(new Callable<TelephonyResult<Void>>() {
                    @Override
                    public TelephonyResult<Void> call() {
                        return MainActivity.this.telephony.hangup();
                    }
                });
            }
        });
        endRow.addView(end, new LinearLayout.LayoutParams(dp(72), dp(72)));
        if (this.callPadShown) {
            TextView hide = label("Скрыть", 15, -1, true);
            hide.setGravity(17);
            hide.setPadding(dp(24), 0, 0, 0);
            hide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.callPadShown = false;
                    MainActivity.this.showCabinet();
                }
            });
            endRow.addView(hide, wrap());
        }
        screen.addView(endRow, matchWrap());
        linearLayout.addView(screen, new LinearLayout.LayoutParams(-1, -2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderPhone$58(View view) {
        final TelephonyEngine telephonyEngine = this.telephony;
        Objects.requireNonNull(telephonyEngine);
        runTelephony(new Callable() { // from class: ua.telecomplus.app.o
            @Override // java.util.concurrent.Callable
            public final Object call() {
                return telephonyEngine.connect();
            }
        }, new a(this, 5));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderPhone$59(View view) {
        this.selectedTab = 3;
        this.historyMode = 1;
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderProfile$79(View view) {
        confirmLogout(false);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$renderProfile$80(View view) {
        confirmLogout(true);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ TelephonyResult lambda$requestTelephonyActivation$60(String str) {
        return this.telephony.requestProvisioningOtp(str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$runPortal$86(Callable callable, PortalCallback portalCallback) {
        Object failure;
        try {
            failure = (PortalResult) callable.call();
        } catch (Exception e) {
            StringBuilder sb = new StringBuilder("Ошибка: ");
            sb.append(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
            failure = PortalResult.failure(sb.toString());
        }
        this.handler.post(new d(portalCallback, failure, 1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$runTelephony$88(Callable callable, TelephonyCallback telephonyCallback) {
        Object failure;
        try {
            failure = (TelephonyResult) callable.call();
        } catch (Exception e) {
            failure = TelephonyResult.failure("TELEPHONY_OPERATION_FAILED", "Ошибка: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
        this.handler.post(new d(telephonyCallback, failure, 2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$serviceCard$62(ServiceItem serviceItem, View view) {
        new AlertDialog.Builder(this).setTitle(serviceItem.title).setMessage(serviceItem.details.isEmpty() ? "Описание не предоставлено порталом." : serviceItem.details).setPositiveButton("Закрыть", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showLoadFailure$41(View view) {
        enterCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showLoadFailure$42(View view) {
        showNumberLogin(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showMonthDialog$66(boolean z, int[] iArr, int[] iArr2, DialogInterface dialogInterface, int i) {
        if (z) {
            this.fundsPeriodYear = iArr[i];
            this.fundsPeriodMonth = iArr2[i];
        } else {
            this.callsPeriodYear = iArr[i];
            this.callsPeriodMonth = iArr2[i];
        }
        dialogInterface.dismiss();
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$showNumberLogin$4(PortalResult portalResult) {
        if (!(portalResult instanceof PortalResult.Success)) {
            String str = ((PortalResult.Failure) portalResult).message;
            registerAuthGuard(str);
            showNumberLogin(str);
        } else {
            clearAuthGuard();
            Integer num = (Integer) ((PortalResult.Success) portalResult).value;
            this.otpSeconds = num == null ? 600 : num.intValue();
            this.otpRepeatAllowedAt = System.currentTimeMillis() + 30000;
            showOtpLogin(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showNumberLogin$5(PortalResult portalResult) {
        this.handler.post(new c(this, portalResult, 1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ PortalResult lambda$showNumberLogin$6() {
        return this.gateway.requestOtp(this.phone);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$showNumberLogin$7(PortalResult portalResult) {
        if (!(portalResult instanceof PortalResult.Success)) {
            String str = ((PortalResult.Failure) portalResult).message;
            registerAuthGuard(str);
            showNumberLogin(str);
        } else {
            clearAuthGuard();
            Integer num = (Integer) ((PortalResult.Success) portalResult).value;
            this.otpSeconds = num == null ? 600 : num.intValue();
            this.otpRepeatAllowedAt = System.currentTimeMillis() + 30000;
            showOtpLogin(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showNumberLogin$8(EditText editText, Button button, View view) {
        this.phone = digits(editText.getText().toString());
        markPortalOtpRequested();
        int i = 0;
        button.setEnabled(false);
        button.setText("Запрашиваем…");
        PortalWebAuthBridge portalWebAuthBridge = this.webAuth;
        if (portalWebAuthBridge != null) {
            portalWebAuthBridge.requestOtp(this.phone, new a(this, 8));
        } else {
            runPortal(new defpackage.b(this, 2), new a(this, i));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showNumberLogin$9(EditText editText, View view) {
        this.phone = digits(editText.getText().toString());
        showPasswordLogin(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showOtpLogin$16(EditText editText, Button button, View view) {
        submitPortalOtp(editText, button);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showOtpLogin$17(View view) {
        openOtpNotificationAccess();
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$showOtpLogin$18(PortalResult portalResult) {
        if (!(portalResult instanceof PortalResult.Success)) {
            String str = ((PortalResult.Failure) portalResult).message;
            registerAuthGuard(str);
            showOtpLogin(str);
        } else {
            clearAuthGuard();
            Integer num = (Integer) ((PortalResult.Success) portalResult).value;
            this.otpSeconds = num == null ? 600 : num.intValue();
            this.otpRepeatAllowedAt = System.currentTimeMillis() + 30000;
            Toast.makeText(this, "Повторный звонок запрошен", 0).show();
            showOtpLogin(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showOtpLogin$19(PortalResult portalResult) {
        this.handler.post(new c(this, portalResult, 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ PortalResult lambda$showOtpLogin$20() {
        return this.gateway.requestOtp(this.phone);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$showOtpLogin$21(PortalResult portalResult) {
        if (!(portalResult instanceof PortalResult.Success)) {
            String str = ((PortalResult.Failure) portalResult).message;
            registerAuthGuard(str);
            showOtpLogin(str);
        } else {
            clearAuthGuard();
            Integer num = (Integer) ((PortalResult.Success) portalResult).value;
            this.otpSeconds = num == null ? 600 : num.intValue();
            this.otpRepeatAllowedAt = System.currentTimeMillis() + 30000;
            Toast.makeText(this, "Повторный звонок запрошен", 0).show();
            showOtpLogin(null);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showOtpLogin$22(final Button button, View view) {
        final int[] types = {1, 2, 3};
        new AlertDialog.Builder(this).setTitle("Получить одноразовый пароль").setItems(new String[]{"Звонок на номер", "Звонок на контактный номер", "SMS на номер"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, final int i) {
                MainActivity.this.markPortalOtpRequested();
                button.setEnabled(false);
                button.setText("Запрашиваем…");
                MainActivity.this.runPortal(new Callable<PortalResult<PortalLoginFlow.Challenge>>() {
                    @Override
                    public PortalResult<PortalLoginFlow.Challenge> call() {
                        return MainActivity.this.gateway.loginFlow().resend(types[i]);
                    }
                }, new PortalCallback<PortalLoginFlow.Challenge>() {
                    @Override
                    public void accept(PortalResult<PortalLoginFlow.Challenge> portalResult) {
                        if (!(portalResult instanceof PortalResult.Success)) {
                            String str = ((PortalResult.Failure) portalResult).message;
                            MainActivity.this.registerAuthGuard(str);
                            MainActivity.this.showOtpLogin(str);
                            return;
                        }
                        MainActivity.this.clearAuthGuard();
                        PortalLoginFlow.Challenge challenge = (PortalLoginFlow.Challenge) ((PortalResult.Success) portalResult).value;
                        MainActivity.this.otpHint = challenge == null ? "" : challenge.hint;
                        MainActivity.this.otpSeconds = 600;
                        MainActivity.this.otpRepeatAllowedAt = System.currentTimeMillis() + 30000;
                        Toast.makeText(MainActivity.this, "Одноразовый пароль отправлен", 0).show();
                        MainActivity.this.showOtpLogin(null);
                    }
                });
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showOtpLogin$23(View view) {
        showPasswordLogin(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$showPasswordLogin$10(PortalResult portalResult, String str) {
        if (!(portalResult instanceof PortalResult.Success)) {
            String str2 = ((PortalResult.Failure) portalResult).message;
            registerAuthGuard(str2);
            showPasswordLogin(str2);
            return;
        }
        clearAuthGuard();
        PortalResult<Void> importBrowserSession = this.gateway.importBrowserSession(str, (String) ((PortalResult.Success) portalResult).value);
        if (importBrowserSession instanceof PortalResult.Success) {
            enterCabinet();
        } else {
            showPasswordLogin(((PortalResult.Failure) importBrowserSession).message);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPasswordLogin$11(String str, PortalResult portalResult) {
        this.handler.post(new i(this, portalResult, str, 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ PortalResult lambda$showPasswordLogin$12(String str, String str2) {
        return this.gateway.loginWithPassword(str, str2);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPasswordLogin$13(PortalResult portalResult) {
        if (portalResult instanceof PortalResult.Success) {
            enterCabinet();
        } else {
            showPasswordLogin(((PortalResult.Failure) portalResult).message);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPasswordLogin$14(EditText editText, EditText editText2, Button button, View view) {
        startPortalLogin(editText.getText().toString(), editText2.getText().toString(), button);
    }

    private String otpHint = "";

    private void startPortalLogin(String str, final String str2, final Button button) {
        final String digits = digits(str);
        this.phone = digits;
        button.setEnabled(false);
        button.setText("Подготовка…");
        runPortal(new Callable<PortalResult<String>>() {
            @Override
            public PortalResult<String> call() {
                return MainActivity.this.gateway.loginFlow().start();
            }
        }, new PortalCallback<String>() {
            @Override
            public void accept(PortalResult<String> portalResult) {
                if (!(portalResult instanceof PortalResult.Success)) {
                    String str3 = ((PortalResult.Failure) portalResult).message;
                    MainActivity.this.registerAuthGuard(str3);
                    MainActivity.this.showPasswordLogin(str3);
                    return;
                }
                MainActivity.this.turnstileKey = (String) ((PortalResult.Success) portalResult).value;
                MainActivity.this.showAuthCheck(digits, str2);
            }
        });
    }

    private void onPortalStepResult(PortalResult<PortalLoginFlow.Challenge> portalResult) {
        if (!(portalResult instanceof PortalResult.Success)) {
            String str = ((PortalResult.Failure) portalResult).message;
            registerAuthGuard(str);
            showPasswordLogin(str);
            return;
        }
        clearAuthGuard();
        PortalLoginFlow.Challenge challenge = (PortalLoginFlow.Challenge) ((PortalResult.Success) portalResult).value;
        if (challenge == null) {
            enterCabinet();
            return;
        }
        this.otpHint = challenge.hint;
        this.otpSeconds = 600;
        this.otpRepeatAllowedAt = System.currentTimeMillis() + 30000;
        markPortalOtpRequested();
        showOtpLogin(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showPasswordLogin$15(EditText editText, View view) {
        this.phone = digits(editText.getText().toString());
        showNumberLogin(null);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showRangeDialog$67(boolean z, int[] iArr, DialogInterface dialogInterface, int i) {
        if (z) {
            this.fundsRangeMonths = iArr[i];
        } else {
            this.callsRangeMonths = iArr[i];
        }
        dialogInterface.dismiss();
        showCabinet();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startDomFallbackIfNeeded$31() {
        this.domFallbackRunning = false;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$startDomFallbackIfNeeded$32(boolean z) {
        loadCallsDomIfNeeded(z, this.gateway.getSessionCookieHeader(), new defpackage.i(this, 1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    /* JADX WARN: Multi-variable type inference failed */
    public /* synthetic */ void lambda$submitPortalOtp$24(PortalResult portalResult) {
        if (!(portalResult instanceof PortalResult.Success)) {
            String str = ((PortalResult.Failure) portalResult).message;
            registerAuthGuard(str);
            showOtpLogin(str);
            return;
        }
        clearAuthGuard();
        PortalResult<Void> importBrowserSession = this.gateway.importBrowserSession(this.phone, (String) ((PortalResult.Success) portalResult).value);
        if (importBrowserSession instanceof PortalResult.Success) {
            enterCabinet();
        } else {
            showOtpLogin(((PortalResult.Failure) importBrowserSession).message);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$submitPortalOtp$25(PortalResult portalResult) {
        this.handler.post(new c(this, portalResult, 2));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ PortalResult lambda$submitPortalOtp$26(String str) {
        return this.gateway.confirmOtp(this.phone, str);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$submitPortalOtp$27(PortalResult portalResult) {
        if (portalResult instanceof PortalResult.Success) {
            enterCabinet();
        } else {
            showOtpLogin(((PortalResult.Failure) portalResult).message);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$topBar$43(View view) {
        refreshCabinet();
    }

    private void loadCallsDomIfNeeded(boolean z, String str, Runnable runnable) {
        if (!z || this.domCallsAttempted) {
            runnable.run();
        } else {
            this.domCallsAttempted = true;
            this.webAuth.readAuthenticatedPageForMonth("/pages/call_details/?lang=ru", str, this.callsPeriodYear, this.callsPeriodMonth, new b(this, runnable, 1));
        }
    }

    private void loadCallsMonth(List<int[]> list, int i, List<CallHistorySnapshot> list2, List<String> list3) {
        if (i < list.size()) {
            int[] iArr = list.get(i);
            this.webAuth.readAuthenticatedPageForMonth("/pages/call_details/?lang=ru", this.gateway.getSessionCookieHeader(), iArr[0], iArr[1], new m(this, list2, list3, iArr, list, i));
            return;
        }
        this.callsPeriodLoading = false;
        if (list2.isEmpty()) {
            Toast.makeText(this, list3.isEmpty() ? "Портал не вернул звонки за выбранный период" : list3.get(0), 1).show();
        } else {
            this.callHistory = mergeCallSnapshots(list2, list.size());
            String str = this.refreshWarning;
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = list3.iterator();
            if (it.hasNext()) {
                while (true) {
                    sb.append((CharSequence) it.next());
                    if (!it.hasNext()) {
                        break;
                    } else {
                        sb.append((CharSequence) " · ");
                    }
                }
            }
            this.refreshWarning = joinWarnings(str, sb.toString());
        }
        showCabinet();
    }

    private void loadCallsSelectedPeriod() {
        if (this.webAuth == null || !this.gateway.hasSavedSession()) {
            Toast.makeText(this, "Для загрузки истории требуется действующая сессия кабинета", 1).show();
            return;
        }
        this.callsPeriodLoading = true;
        this.callsSafeReport = "";
        showCabinet();
        loadCallsMonth(monthSequence(this.callsPeriodYear, this.callsPeriodMonth, this.callsRangeMonths), 0, new ArrayList(), new ArrayList());
    }

    private void loadFundsDomIfNeeded(boolean z, String str, Runnable runnable) {
        if (!z || this.domFundsAttempted) {
            runnable.run();
        } else {
            this.domFundsAttempted = true;
            this.webAuth.readAuthenticatedPageForMonth("/pages/funds_flow/?lang=ru", str, this.fundsPeriodYear, this.fundsPeriodMonth, new b(this, runnable, 0));
        }
    }

    private void loadFundsMonth(List<int[]> list, int i, List<FundsFlowSnapshot> list2, List<String> list3) {
        if (i < list.size()) {
            int[] iArr = list.get(i);
            this.webAuth.readAuthenticatedPageForMonth("/pages/funds_flow/?lang=ru", this.gateway.getSessionCookieHeader(), iArr[0], iArr[1], new m(this, iArr, list2, list3, list, i));
            return;
        }
        this.fundsPeriodLoading = false;
        if (list2.isEmpty()) {
            Toast.makeText(this, list3.isEmpty() ? "Портал не вернул данные за выбранный период" : list3.get(0), 1).show();
        } else {
            this.fundsFlow = mergeFundsSnapshots(list2, list.size());
            String str = this.refreshWarning;
            StringBuilder sb = new StringBuilder();
            Iterator<String> it = list3.iterator();
            if (it.hasNext()) {
                while (true) {
                    sb.append((CharSequence) it.next());
                    if (!it.hasNext()) {
                        break;
                    } else {
                        sb.append((CharSequence) " · ");
                    }
                }
            }
            this.refreshWarning = joinWarnings(str, sb.toString());
        }
        showCabinet();
    }

    private void loadFundsSelectedPeriod() {
        if (this.webAuth == null || !this.gateway.hasSavedSession()) {
            Toast.makeText(this, "Для загрузки истории требуется действующая сессия кабинета", 1).show();
            return;
        }
        this.fundsPeriodLoading = true;
        this.fundsSafeReport = "";
        showCabinet();
        loadFundsMonth(monthSequence(this.fundsPeriodYear, this.fundsPeriodMonth, this.fundsRangeMonths), 0, new ArrayList(), new ArrayList());
    }

    private View logo() {
        return new TelecomLogoView();
    }

    private void markPortalOtpRequested() {
        this.portalOtpRequestedAt = System.currentTimeMillis();
        this.portalOtpAutoSubmitInFlight = false;
    }

    private static String maskPhone(String str) {
        String digits = digits(str);
        if (digits.length() < 5) {
            return "***";
        }
        return digits.substring(0, Math.min(3, digits.length())) + "***" + digits.substring(Math.max(3, digits.length() - 3));
    }

    private static String maskRegistrationIdentity(String str) {
        String trim = str == null ? "" : str.trim();
        int indexOf = trim.indexOf(64);
        if (indexOf > 0) {
            return trim.substring(0, 1) + "***" + trim.substring(indexOf);
        }
        String digits = digits(trim);
        if (digits.length() < 3) {
            return "***";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(digits.startsWith("380") ? "+380 " : "+");
        sb.append("***-***-");
        sb.append(digits.substring(digits.length() - 3));
        return sb.toString();
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(-1, -2);
    }

    private boolean matchesFundsFilter(FundsTransaction fundsTransaction) {
        int i = this.fundsFilter;
        return i == 0 || (i == 1 && fundsTransaction.isDebit()) || (this.fundsFilter == 2 && fundsTransaction.isCredit());
    }

    private CallHistorySnapshot mergeCallSnapshots(List<CallHistorySnapshot> list, int i) {
        ArrayList arrayList = new ArrayList();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        boolean z = false;
        long j = 0;
        for (CallHistorySnapshot callHistorySnapshot : list) {
            long durationSeconds = durationSeconds(callHistorySnapshot.totalDuration);
            if (durationSeconds > 0) {
                j += durationSeconds;
                z = true;
            }
            for (CallDetail callDetail : callHistorySnapshot.calls) {
                if (linkedHashSet.add((callDetail.dateTime + "|" + callDetail.destination + "|" + callDetail.duration).toLowerCase(Locale.ROOT))) {
                    arrayList.add(callDetail);
                }
            }
        }
        return new CallHistorySnapshot(rangePeriodLabel(this.callsPeriodYear, this.callsPeriodMonth, i), z ? durationLabel(j) : "—", arrayList);
    }

    private FundsFlowSnapshot mergeFundsSnapshots(List<FundsFlowSnapshot> list, int i) {
        ArrayList arrayList = new ArrayList();
        LinkedHashSet linkedHashSet = new LinkedHashSet();
        Iterator<FundsFlowSnapshot> it = list.iterator();
        double d = 0.0d;
        double d2 = 0.0d;
        boolean z = false;
        boolean z2 = false;
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            FundsFlowSnapshot next = it.next();
            double moneyValue = moneyValue(next.creditsTotal);
            double moneyValue2 = moneyValue(next.debitsTotal);
            if (!"—".equals(next.creditsTotal)) {
                d = Math.abs(moneyValue) + d;
                z = true;
            }
            if (!"—".equals(next.debitsTotal)) {
                d2 = Math.abs(moneyValue2) + d2;
                z2 = true;
            }
            for (FundsTransaction fundsTransaction : next.transactions) {
                if (linkedHashSet.add((fundsTransaction.dateTime + "|" + fundsTransaction.description + "|" + fundsTransaction.amount).toLowerCase(Locale.ROOT))) {
                    arrayList.add(fundsTransaction);
                }
            }
        }
        FundsFlowSnapshot fundsFlowSnapshot = list.get(0);
        return new FundsFlowSnapshot(rangePeriodLabel(this.fundsPeriodYear, this.fundsPeriodMonth, i), list.get(list.size() - 1).openingBalance, z ? String.format(Locale.US, "+%.2f грн", Double.valueOf(d)) : "—", z2 ? String.format(Locale.US, "-%.2f грн", Double.valueOf(d2)) : "—", fundsFlowSnapshot.closingBalance, arrayList);
    }

    private Button miniButton(String str) {
        Button button = button(str, BLUE, 0);
        button.setTextSize(25.0f);
        button.setPadding(0, 0, 0, 0);
        return button;
    }

    private static double moneyValue(String str) {
        if (str == null) {
            str = "";
        }
        try {
            return Double.parseDouble(str.replace((char) 8722, '-').replace(',', '.').replaceAll("[^0-9+\\-.]", ""));
        } catch (Exception unused) {
            return 0.0d;
        }
    }

    private static String monthName(int i, int i2) {
        return new String[]{"Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"}[Math.max(1, Math.min(12, i2)) - 1] + " " + i;
    }

    private List<int[]> monthSequence(int i, int i2, int i3) {
        ArrayList arrayList = new ArrayList();
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(i, Math.max(1, Math.min(12, i2)) - 1, 1);
        for (int i4 = 0; i4 < Math.max(1, i3); i4++) {
            arrayList.add(new int[]{calendar.get(1), calendar.get(2) + 1});
            calendar.add(2, -1);
        }
        return arrayList;
    }

    private void openOtpNotificationAccess() {
        try {
            startActivity(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
        } catch (Exception unused) {
            Toast.makeText(this, "Откройте Настройки → Уведомления → Специальный доступ", 1).show();
        }
    }

    private View packageCard(PackageBalance packageBalance, int i) {
        int[] iArr = {BLUE, BLUE_DARK, CYAN, PURPLE, GREEN};
        int i2 = iArr[Math.abs(i) % 5];
        LinearLayout colorCard = colorCard(-1, BORDER, 22);
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon(Math.abs(i) % 2 == 0 ? "P" : "☎", i2, SOFT_BLUE, 44), new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout column = column();
        column.setPadding(dp(12), 0, 0, 0);
        column.addView(label(packageBalance.title, 17, BLUE_DARK, true), matchWrap());
        column.addView(label(packageBalance.subtitle, 13, MUTED, false), matchWrap());
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView pc = chip(packageBalance.progressPercent + "%", BLUE_DARK, YELLOW);
        row.addView(pc, wrap());
        colorCard.addView(row, matchWrap());
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(packageBalance.progressPercent);
        progressBar.setProgressTintList(ColorStateList.valueOf(i2));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.rgb(228, 236, 248)));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(9));
        layoutParams.topMargin = dp(14);
        colorCard.addView(progressBar, layoutParams);
        return colorCard;
    }


    private void performLogout(boolean z) {
        TelephonyEngine telephonyEngine;
        showLoading(z ? "Удаляем данные устройства…" : "Завершаем сессию…");
        if (z && (telephonyEngine = this.telephony) != null) {
            telephonyEngine.forgetProvisioning();
        }
        defpackage.l lVar = new defpackage.l(this, z, 1);
        PortalWebAuthBridge portalWebAuthBridge = this.webAuth;
        if (portalWebAuthBridge != null) {
            portalWebAuthBridge.clearBrowserSession(lVar);
        } else {
            lVar.run();
        }
    }

    private static boolean periodMatches(String str, int i, int i2) {
        String lowerCase = str == null ? "" : str.toLowerCase(Locale.ROOT);
        return lowerCase.contains(Integer.toString(i)) && lowerCase.contains(new String[]{"январ", "феврал", "март", "апрел", "май", "июн", "июл", "август", "сентябр", "октябр", "ноябр", "декабр"}[Math.max(1, Math.min(12, i2)) - 1]);
    }

    private View periodSelectorCard(final boolean z) {
        int i = z ? this.fundsPeriodYear : this.callsPeriodYear;
        int i2 = z ? this.fundsPeriodMonth : this.callsPeriodMonth;
        int i3 = z ? this.fundsRangeMonths : this.callsRangeMonths;
        boolean z2 = z ? this.fundsPeriodLoading : this.callsPeriodLoading;
        LinearLayout card = card();
        final int i4 = 1;
        card.addView(label("Период", 18, BLUE_DARK, true), matchWrap());
        final int i5 = 0;
        TextView label = label("Выберите конечный месяц и глубину истории. Для 5, 6 месяцев или года Telecom+ последовательно загрузит каждый месяц и объединит результаты.", 13, MUTED, false);
        label.setPadding(0, dp(7), 0, dp(12));
        card.addView(label, matchWrap());
        LinearLayout row = row();
        Button secondary = secondary(monthName(i, i2));
        boolean z3 = !z2;
        secondary.setEnabled(z3);
        secondary.setOnClickListener(new View.OnClickListener() { // from class: r
            public final /* synthetic */ MainActivity b;

            {
                this.b = MainActivity.this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                int i6 = i5;
                boolean z4 = z;
                MainActivity mainActivity = this.b;
                switch (i6) {
                    case 0:
                        mainActivity.lambda$periodSelectorCard$63(z4, view);
                        break;
                    case 1:
                        mainActivity.lambda$periodSelectorCard$64(z4, view);
                        break;
                    default:
                        mainActivity.lambda$periodSelectorCard$65(z4, view);
                        break;
                }
            }
        });
        row.addView(secondary, new LinearLayout.LayoutParams(0, dp(46), 1.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(46), 1.0f);
        layoutParams.leftMargin = dp(8);
        Button secondary2 = secondary(rangeLabel(i3));
        secondary2.setEnabled(z3);
        secondary2.setOnClickListener(new View.OnClickListener() { // from class: r
            public final /* synthetic */ MainActivity b;

            {
                this.b = MainActivity.this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                int i6 = i4;
                boolean z4 = z;
                MainActivity mainActivity = this.b;
                switch (i6) {
                    case 0:
                        mainActivity.lambda$periodSelectorCard$63(z4, view);
                        break;
                    case 1:
                        mainActivity.lambda$periodSelectorCard$64(z4, view);
                        break;
                    default:
                        mainActivity.lambda$periodSelectorCard$65(z4, view);
                        break;
                }
            }
        });
        row.addView(secondary2, layoutParams);
        card.addView(row, matchWrap());
        View primary = primary(z2 ? "Загружаем период…" : "Показать");
        primary.setEnabled(z3);
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10);
        card.addView(primary, matchWrap);
        final int i6 = 2;
        primary.setOnClickListener(new View.OnClickListener() { // from class: r
            public final /* synthetic */ MainActivity b;

            {
                this.b = MainActivity.this;
            }

            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                int i62 = i6;
                boolean z4 = z;
                MainActivity mainActivity = this.b;
                switch (i62) {
                    case 0:
                        mainActivity.lambda$periodSelectorCard$63(z4, view);
                        break;
                    case 1:
                        mainActivity.lambda$periodSelectorCard$64(z4, view);
                        break;
                    default:
                        mainActivity.lambda$periodSelectorCard$65(z4, view);
                        break;
                }
            }
        });
        return card;
    }

    private static String presentValue(String str) {
        str = leakedLabel(str);
        if (str == null) {
            return "";
        }
        String trim = str.trim();
        if (!trim.isEmpty() && !"—".equals(trim)) {
            String lowerCase = trim.toLowerCase(Locale.ROOT);
            if (!"не указан".equals(lowerCase) && !"не указано".equals(lowerCase) && !"не вказано".equals(lowerCase) && !"не задан".equals(lowerCase)) {
                return trim;
            }
        }
        return "";
    }

    private Button accent(String str) {
        Button button = button(str, BLUE_DARK, YELLOW);
        button.setTypeface(Typeface.DEFAULT, 1);
        return button;
    }

    private View quickActions() {
        LinearLayout row = row();
        String[] names = {"Позвонить", "Услуги", "История", "Чаты"};
        String[] icons = {"☎", "☷", "◷", "✉"};
        final int[] tabs = {2, 1, 3, 6};
        for (int i = 0; i < 4; i++) {
            final int tab = tabs[i];
            LinearLayout col = column();
            col.setGravity(1);
            TextView ic = circleIcon(icons[i], BLUE, SOFT_BLUE, 54);
            col.addView(ic, new LinearLayout.LayoutParams(dp(54), dp(54)));
            TextView cap = label(names[i], 12, BLUE_DARK, true);
            cap.setGravity(17);
            cap.setPadding(0, dp(6), 0, 0);
            col.addView(cap, matchWrap());
            col.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.chatNumber = null;
                    MainActivity.this.selectedTab = tab;
                    MainActivity.this.showCabinet();
                }
            });
            row.addView(col, new LinearLayout.LayoutParams(0, -2, 1.0f));
        }
        LinearLayout card = card();
        card.setPadding(dp(8), dp(16), dp(8), dp(14));
        card.addView(row, matchWrap());
        return card;
    }

    private static String cleanServiceDetails(ServiceItem serviceItem) {
        String d = serviceItem.details;
        if (d == null) {
            return "";
        }
        String title = serviceItem.title == null ? "" : serviceItem.title.trim();
        StringBuilder sb = new StringBuilder();
        for (String line : d.split("\\n")) {
            String t = line.trim();
            String lc = t.toLowerCase(Locale.ROOT);
            if (t.isEmpty() || t.equals(title) || lc.equals("подробнее") || lc.equals("настроить") || lc.equals("абонентская плата") || lc.equals("абонентная плата") || lc.equals("подключена") || lc.equals("отключена") || lc.equals("детальніше")) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(t);
        }
        return sb.toString();
    }

    private Button primary(String str) {
        Button button = button(str, -1, BLUE);
        button.setTypeface(Typeface.DEFAULT, 1);
        return button;
    }

    private View profileDivider() {
        View view = new View(this);
        view.setBackgroundColor(Color.rgb(235, 239, 246));
        view.setMinimumHeight(dp(1));
        return view;
    }

    private View profileInfoRow(String str, String str2, String str3, int i, int i2) {
        LinearLayout row = row();
        row.setGravity(16);
        row.setPadding(0, dp(9), 0, dp(9));
        row.addView(circleIcon(str, i, i2, 42), new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout column = column();
        column.setPadding(dp(12), 0, 0, 0);
        column.addView(label(str2, 12, MUTED, false), matchWrap());
        column.addView(label(displayValue(str3), 16, TEXT, true), matchWrap());
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        return row;
    }

    private static String rangeLabel(int i) {
        if (i == 1) {
            return "1 месяц";
        }
        if (i < 5 || i > 20) {
            return i + " мес.";
        }
        return i + " месяцев";
    }

    private String rangePeriodLabel(int i, int i2, int i3) {
        List<int[]> monthSequence = monthSequence(i, i2, i3);
        int[] iArr = monthSequence.get(monthSequence.size() - 1);
        if (monthSequence.size() == 1) {
            return monthName(i, i2);
        }
        return monthName(iArr[0], iArr[1]) + " — " + monthName(i, i2);
    }

    private void refreshCabinet() {
        enterCabinet();
    }

    private void refreshNativeCookies(String str) {
        if (str == null || str.trim().isEmpty()) {
            return;
        }
        DashboardSnapshot dashboardSnapshot = this.dashboard;
        this.gateway.importBrowserSession(dashboardSnapshot == null ? this.phone : dashboardSnapshot.phoneNumber, str);
    }

    private void registerAuthGuard(String str) {
        int extractRetryAfterSeconds = PortalHtmlParser.extractRetryAfterSeconds(str);
        if (extractRetryAfterSeconds <= 0) {
            return;
        }
        getSharedPreferences("auth_guard", 0).edit().putLong("blocked_until", (extractRetryAfterSeconds * 1000) + System.currentTimeMillis()).apply();
    }

    private void registerOtpCallReceiver() {
        IntentFilter intentFilter = new IntentFilter("ua.telecomplus.app.action.OTP_CALL_CODE");
        int i = Build.VERSION.SDK_INT;
        BroadcastReceiver broadcastReceiver = this.otpCallReceiver;
        if (i >= 33) {
            registerReceiver(broadcastReceiver, intentFilter, 4);
        } else {
            registerReceiver(broadcastReceiver, intentFilter);
        }
        this.otpReceiverRegistered = true;
    }

    private void renderCalls(LinearLayout linearLayout) {
        CallHistorySnapshot callHistorySnapshot = this.callHistory;
        String str = "—";
        if (callHistorySnapshot == null || "—".equals(callHistorySnapshot.totalDuration)) {
            DashboardSnapshot dashboardSnapshot = this.dashboard;
            if (dashboardSnapshot != null) {
                str = dashboardSnapshot.outgoingSummary;
            }
        } else {
            str = callHistorySnapshot.totalDuration;
        }
        linearLayout.addView(periodSelectorCard(false), matchWrap());
        LinearLayout column = column();
        column.setPadding(dp(20), dp(20), dp(20), dp(20));
        int i = ORANGE;
        column.setBackground(gradient(i, Color.rgb(229, 82, 88), 24));
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon("☎", i, -1, 48), new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout column2 = column();
        column2.setPadding(dp(13), 0, 0, 0);
        column2.addView(label("Исходящие звонки", 22, -1, true), matchWrap());
        column2.addView(label(callHistorySnapshot == null ? "Текущий период" : displayValue(callHistorySnapshot.periodLabel), 12, Color.rgb(255, 234, 226), false), matchWrap());
        row.addView(column2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        column.addView(row, matchWrap());
        TextView label = label("Общая продолжительность", 12, Color.rgb(255, 234, 226), false);
        label.setPadding(0, dp(18), 0, dp(2));
        column.addView(label, matchWrap());
        column.addView(label(displayValue(str), 31, -1, true), matchWrap());
        StringBuilder sb = new StringBuilder("Записей в детализации: ");
        sb.append(callHistorySnapshot == null ? 0 : callHistorySnapshot.calls.size());
        column.addView(label(sb.toString(), 12, Color.rgb(255, 234, 226), false), matchWrap());
        linearLayout.addView(column, cardMargin());
        int i2 = 15;
        if (callHistorySnapshot == null || callHistorySnapshot.calls.isEmpty()) {
            LinearLayout colorCard = colorCard(SOFT_ORANGE, Color.rgb(242, 215, 177), 20);
            colorCard.addView(label("Детализация пока пуста", 18, TEXT, true), matchWrap());
            TextView label2 = label("Портал передал общую продолжительность, но не вернул подтверждённые строки отдельных звонков за выбранный месяц.", 14, MUTED, false);
            label2.setPadding(0, dp(9), 0, 0);
            colorCard.addView(label2, matchWrap());
            if (!this.callsSafeReport.trim().isEmpty()) {
                Button secondary = secondary("Сохранить безопасный отчёт");
                LinearLayout.LayoutParams matchWrap = matchWrap();
                matchWrap.topMargin = dp(12);
                colorCard.addView(secondary, matchWrap);
                secondary.setOnClickListener(new defpackage.d(this, i2));
            }
            linearLayout.addView(colorCard, cardMargin());
            return;
        }
        linearLayout.addView(sectionTitle("История звонков", callHistorySnapshot.calls.size() + " записей"), sectionMargin());
        String str2 = "";
        for (CallDetail callDetail : callHistorySnapshot.calls) {
            String dateGroup = dateGroup(callDetail.dateTime);
            if (!dateGroup.equals(str2)) {
                TextView label3 = label(dateGroup, 16, BLUE_DARK, true);
                label3.setPadding(dp(4), dp(15), 0, dp(7));
                linearLayout.addView(label3, matchWrap());
                str2 = dateGroup;
            }
            linearLayout.addView(callCard(callDetail), cardMargin());
        }
    }

    private void renderFunds(LinearLayout linearLayout) {
        DashboardSnapshot dashboardSnapshot;
        FundsFlowSnapshot fundsFlowSnapshot = this.fundsFlow;
        if (fundsFlowSnapshot == null) {
            linearLayout.addView(info("Не удалось получить страницу движения средств. Нажмите ↻ для повторной загрузки."), matchWrap());
            return;
        }
        if (this.showingCachedData) {
            linearLayout.addView(warning("Показана сохранённая детализация от " + DateFormat.getDateTimeInstance(3, 3).format(new Date(fundsFlowSnapshot.updatedAt)) + "."), matchWrap());
        }
        linearLayout.addView(periodSelectorCard(true), matchWrap());
        linearLayout.addView(sectionTitle("Финансовая сводка", displayValue(fundsFlowSnapshot.periodLabel)), sectionMargin());
        String str = (!"—".equals(fundsFlowSnapshot.closingBalance) || (dashboardSnapshot = this.dashboard) == null) ? fundsFlowSnapshot.closingBalance : dashboardSnapshot.balance;
        LinearLayout row = row();
        row.addView(statTile("На начало", displayValue(fundsFlowSnapshot.openingBalance), "↙", BLUE, SOFT_BLUE), new LinearLayout.LayoutParams(0, dp(118), 1.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(118), 1.0f);
        layoutParams.leftMargin = dp(10);
        row.addView(statTile("Поступления", displayValue(fundsFlowSnapshot.creditsTotal), "+", GREEN, Color.rgb(232, 249, 238)), layoutParams);
        linearLayout.addView(row, matchWrap());
        LinearLayout row2 = row();
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10);
        row2.addView(statTile("Списания", displayValue(fundsFlowSnapshot.debitsTotal), "−", RED, Color.rgb(255, 237, 237)), new LinearLayout.LayoutParams(0, dp(118), 1.0f));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(118), 1.0f);
        layoutParams2.leftMargin = dp(10);
        row2.addView(statTile("На конец", displayValue(str), "₴", PURPLE, SOFT_PURPLE), layoutParams2);
        linearLayout.addView(row2, matchWrap);
        linearLayout.addView(sectionTitle("Операции", fundsFlowSnapshot.transactions.size() + " записей · " + fundsFlowSnapshot.creditCount() + " поступлений · " + fundsFlowSnapshot.debitCount() + " списаний"), sectionMargin());
        LinearLayout row3 = row();
        row3.addView(fundsFilterButton("Все", 0), new LinearLayout.LayoutParams(0, dp(42), 1.0f));
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(0, dp(42), 1.0f);
        layoutParams3.leftMargin = dp(7);
        row3.addView(fundsFilterButton("Списания", 1), layoutParams3);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(0, dp(42), 1.0f);
        layoutParams4.leftMargin = dp(7);
        row3.addView(fundsFilterButton("Пополнения", 2), layoutParams4);
        linearLayout.addView(row3, matchWrap());
        String str2 = "";
        int i = 0;
        for (FundsTransaction fundsTransaction : fundsFlowSnapshot.transactions) {
            if (matchesFundsFilter(fundsTransaction)) {
                String dateGroup = dateGroup(fundsTransaction.dateTime);
                if (!dateGroup.equals(str2)) {
                    TextView label = label(dateGroup, 16, BLUE_DARK, true);
                    label.setPadding(dp(4), dp(16), 0, dp(7));
                    linearLayout.addView(label, matchWrap());
                    str2 = dateGroup;
                }
                linearLayout.addView(fundsTransactionCard(fundsTransaction), matchWrap());
                i++;
            }
        }
        if (i == 0) {
            String str3 = fundsFlowSnapshot.transactions.isEmpty() ? "За выбранный период портал не вернул подтверждённых операций. Начальный и конечный остатки показаны отдельно и не считаются пополнениями." : "В выбранной категории операций нет.";
            LinearLayout colorCard = colorCard(SOFT_ORANGE, Color.rgb(242, 215, 177), 20);
            colorCard.addView(label("Операции не найдены", 18, TEXT, true), matchWrap());
            TextView label2 = label(str3, 14, MUTED, false);
            label2.setPadding(0, dp(9), 0, 0);
            colorCard.addView(label2, matchWrap());
            if (!this.fundsSafeReport.trim().isEmpty()) {
                Button secondary = secondary("Сохранить безопасный отчёт");
                LinearLayout.LayoutParams matchWrap2 = matchWrap();
                matchWrap2.topMargin = dp(12);
                colorCard.addView(secondary, matchWrap2);
                secondary.setOnClickListener(new defpackage.d(this, 13));
            }
            linearLayout.addView(colorCard, cardMargin());
        }
    }

    private void renderHistory(LinearLayout parent) {
        parent.addView(subHeader(this.historyMode == 0 ? "Платежи и движение средств" : "Расходы и детализация", 4), matchWrap());
        LinearLayout seg = row();
        seg.setPadding(dp(4), dp(4), dp(4), dp(4));
        seg.setBackground(rounded(-1, 24, BORDER, 1));
        seg.addView(historyModeButton("Деньги", 0), lp(0, dp(44), 1.0f));
        LinearLayout.LayoutParams l2 = lp(0, dp(44), 1.0f);
        l2.leftMargin = dp(4);
        seg.addView(historyModeButton("Звонки", 1), l2);
        LinearLayout.LayoutParams sp = matchWrap();
        sp.bottomMargin = dp(14);
        parent.addView(seg, sp);
        if (this.historyMode == 0) {
            renderFundsNew(parent);
        } else {
            renderCalls(parent);
        }
    }

    private void renderHome(LinearLayout parent) {
        DashboardSnapshot d = this.dashboard;
        if (d == null) {
            return;
        }
        LinearLayout bar = row();
        bar.setGravity(16);
        bar.setPadding(dp(2), dp(4), dp(2), dp(12));
        LinearLayout marks = row();
        marks.setGravity(80);
        int[] hs = {9, 16, 24};
        for (int h : hs) {
            View b = new View(this);
            b.setBackground(rounded(YELLOW, 2, 0, 0));
            LinearLayout.LayoutParams bp = lp(dp(6), dp(h), 0);
            bp.rightMargin = dp(3);
            marks.addView(b, bp);
        }
        bar.addView(marks, lp(dp(28), dp(27), 0));
        TextView brand = label("Telecom", 24, TEXT, true);
        brand.setPadding(dp(10), 0, 0, 0);
        bar.addView(brand, wrap());
        bar.addView(label("+", 24, YELLOW, true), wrap());
        bar.addView(new View(this), lp(0, 1, 1.0f));
        bar.addView(roundRefresh(), lp(dp(44), dp(44), 0));
        parent.addView(bar, matchWrap());
        if (this.showingCachedData) {
            parent.addView(warning("Показаны сохранённые данные от " + DateFormat.getDateTimeInstance(3, 3).format(new Date(d.updatedAt)) + ". Обновление не выполнено" + (this.refreshWarning.isEmpty() ? "." : ": " + this.refreshWarning)), cardMargin());
        }
        FrameLayout bal = new FrameLayout(this);
        bal.setBackground(gradient(Color.rgb(11, 100, 244), Color.rgb(0, 74, 203), 22));
        bal.setElevation(dp(4));
        View ring = new View(this);
        ring.setBackground(rounded(Color.argb(24, 107, 169, 255), 110, 0, 0));
        FrameLayout.LayoutParams rp = new FrameLayout.LayoutParams(dp(210), dp(210));
        rp.gravity = 85;
        rp.rightMargin = dp(-100);
        rp.bottomMargin = dp(-120);
        bal.addView(ring, rp);
        LinearLayout bc = column();
        bc.setPadding(dp(22), dp(20), dp(22), dp(18));
        bc.addView(label("Баланс", 13, Color.rgb(212, 232, 255), false), matchWrap());
        bc.addView(label(d.balance, 36, -1, true), matchWrap());
        View line = new View(this);
        line.setBackgroundColor(Color.argb(77, 255, 255, 255));
        LinearLayout.LayoutParams ln = lp(-1, 1, 0);
        ln.topMargin = dp(14);
        ln.bottomMargin = dp(12);
        bc.addView(line, ln);
        LinearLayout nr = row();
        nr.setGravity(16);
        nr.addView(label("Мой номер", 12, Color.rgb(212, 232, 255), false), lp(0, -2, 1.0f));
        nr.addView(label(formatInternationalPhone(d.phoneNumber), 14, -1, true), wrap());
        bc.addView(nr, matchWrap());
        bal.addView(bc, new FrameLayout.LayoutParams(-1, -2));
        TextView plus = circleIcon("+", BLUE_DARK, YELLOW, 46);
        FrameLayout.LayoutParams pp = new FrameLayout.LayoutParams(dp(46), dp(46));
        pp.gravity = 53;
        pp.topMargin = dp(26);
        pp.rightMargin = dp(20);
        plus.setOnClickListener(new defpackage.d(this, 2));
        bal.addView(plus, pp);
        parent.addView(bal, matchWrap());
        FrameLayout tar = new FrameLayout(this);
        tar.setBackground(rounded(-1, 22, BORDER, 1));
        View corner = new View(this);
        corner.setBackground(rounded(Color.rgb(255, 226, 98), 40, 0, 0));
        FrameLayout.LayoutParams cnp = new FrameLayout.LayoutParams(dp(110), dp(60));
        cnp.gravity = 85;
        cnp.rightMargin = dp(-30);
        cnp.bottomMargin = dp(-34);
        tar.addView(corner, cnp);
        LinearLayout tr = row();
        tr.setGravity(16);
        tr.setPadding(dp(16), dp(16), dp(16), dp(16));
        tr.addView(logo(), new LinearLayout.LayoutParams(dp(54), dp(54)));
        LinearLayout tc = column();
        tc.setPadding(dp(14), 0, dp(8), 0);
        tc.addView(label("Мой тариф", 12, MUTED, false), matchWrap());
        tc.addView(label(displayValue(d.tariffName), 18, TEXT, true), matchWrap());
        String stl = displayValue(d.status);
        boolean active = stl.toLowerCase(Locale.ROOT).startsWith("актив");
        TextView stc = chip(active ? "●  Активен" : stl, active ? GREEN : MUTED, active ? Color.rgb(226, 246, 235) : Color.rgb(238, 242, 248));
        LinearLayout.LayoutParams scp = wrap();
        scp.topMargin = dp(6);
        tc.addView(stc, scp);
        tr.addView(tc, lp(0, -2, 1.0f));
        tr.addView(label("›", 28, TEXT, false), wrap());
        tar.addView(tr, new FrameLayout.LayoutParams(-1, -2));
        tar.setOnClickListener(tabClick(7, 0));
        LinearLayout.LayoutParams tm = matchWrap();
        tm.topMargin = dp(12);
        parent.addView(tar, tm);
        if (!d.packages.isEmpty()) {
            LinearLayout st = row();
            int n = Math.min(2, d.packages.size());
            for (int i = 0; i < n; i++) {
                LinearLayout.LayoutParams sp2 = lp(0, -1, 1.0f);
                if (i > 0) {
                    sp2.leftMargin = dp(10);
                }
                st.addView(pkgTile(d.packages.get(i)), sp2);
            }
            LinearLayout.LayoutParams sm = matchWrap();
            sm.topMargin = dp(12);
            parent.addView(st, sm);
        }
        LinearLayout qa = row();
        String[] qn = {"Пополнить", "Услуги", "Детализация", "Поддержка"};
        String[] qg = {"₴", "☷", "▣", "?"};
        for (int i = 0; i < 4; i++) {
            LinearLayout q = column();
            q.setGravity(1);
            q.setPadding(dp(4), dp(14), dp(4), dp(12));
            q.setBackground(rounded(-1, 18, BORDER, 1));
            q.addView(circleIcon(qg[i], BLUE, SOFT_BLUE, 38), lp(dp(38), dp(38), 0));
            TextView ql = label(qn[i], 11, TEXT, true);
            ql.setGravity(17);
            ql.setSingleLine(true);
            ql.setPadding(0, dp(8), 0, 0);
            q.addView(ql, matchWrap());
            if (i == 0) {
                q.setOnClickListener(new defpackage.d(this, 2));
            } else if (i == 1) {
                q.setOnClickListener(tabClick(1, 0));
            } else if (i == 2) {
                q.setOnClickListener(tabClick(3, 1));
            } else {
                q.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://intertelecom.ua")));
                        } catch (Exception e) {
                        }
                    }
                });
            }
            LinearLayout.LayoutParams ql2 = lp(0, -2, 1.0f);
            if (i > 0) {
                ql2.leftMargin = dp(8);
            }
            qa.addView(q, ql2);
        }
        LinearLayout.LayoutParams qm = matchWrap();
        qm.topMargin = dp(10);
        parent.addView(qa, qm);
        FrameLayout promo = new FrameLayout(this);
        promo.setBackground(gradient(Color.rgb(221, 236, 255), Color.rgb(176, 208, 252), 20));
        View ph = new View(this);
        ph.setBackground(rounded(YELLOW, 12, BLUE_DARK, 4));
        ph.setRotation(-14);
        FrameLayout.LayoutParams php = new FrameLayout.LayoutParams(dp(58), dp(94));
        php.gravity = 19;
        php.leftMargin = dp(18);
        promo.addView(ph, php);
        LinearLayout pt = column();
        pt.setPadding(dp(100), dp(18), dp(44), dp(18));
        pt.addView(label("Управляйте связью проще", 17, BLUE_DARK, true), matchWrap());
        TextView ps = label("Меняйте тариф, подключайте услуги и контролируйте расходы.", 12, TEXT, false);
        ps.setPadding(0, dp(4), 0, 0);
        pt.addView(ps, matchWrap());
        promo.addView(pt, new FrameLayout.LayoutParams(-1, -2));
        TextView pc = label("›", 28, BLUE, false);
        FrameLayout.LayoutParams pcp = new FrameLayout.LayoutParams(-2, -2);
        pcp.gravity = 21;
        pcp.rightMargin = dp(16);
        promo.addView(pc, pcp);
        promo.setOnClickListener(tabClick(1, 0));
        LinearLayout.LayoutParams pm = matchWrap();
        pm.topMargin = dp(12);
        parent.addView(promo, pm);
        LinearLayout h2 = row();
        h2.setGravity(16);
        h2.setPadding(dp(2), dp(18), dp(2), dp(10));
        h2.addView(label("Полезно", 17, TEXT, true), lp(0, -2, 1.0f));
        TextView all = label("Весь кабинет", 12, BLUE, true);
        all.setOnClickListener(tabClick(4, 0));
        h2.addView(all, wrap());
        parent.addView(h2, matchWrap());
        LinearLayout us = row();
        String[] un = {"Платежи", "Контакты", "Журнал"};
        String[] ug = {"₴", "☺", "◷"};
        final int[] ut = {3, 5, 8};
        for (int i = 0; i < 3; i++) {
            LinearLayout u = row();
            u.setGravity(16);
            u.setPadding(dp(12), dp(16), dp(10), dp(16));
            u.setBackground(rounded(-1, 18, BORDER, 1));
            u.addView(circleIcon(ug[i], BLUE, SOFT_BLUE, 28), lp(dp(28), dp(28), 0));
            TextView ul = label(un[i], 13, TEXT, true);
            ul.setPadding(dp(8), 0, 0, 0);
            ul.setSingleLine(true);
            u.addView(ul, wrap());
            u.setOnClickListener(tabClick(ut[i], 0));
            LinearLayout.LayoutParams ulp = lp(0, -2, 1.0f);
            if (i > 0) {
                ulp.leftMargin = dp(8);
            }
            us.addView(u, ulp);
        }
        parent.addView(us, matchWrap());
    }

    private void renderPhone(LinearLayout linearLayout) {
        String str;
        TelephonySnapshot telephonySnapshot0 = this.telephonySnapshot;
        if (telephonySnapshot0 == null) {
            telephonySnapshot0 = TelephonySnapshot.unprovisioned();
        }
        if (telephonySnapshot0 != null) {
            renderDialer(linearLayout, telephonySnapshot0);
            return;
        }
        final TelephonySnapshot telephonySnapshot = telephonySnapshot0;
        LinearLayout column = column();
        column.setPadding(dp(20), dp(20), dp(20), dp(20));
        int i = GREEN;
        column.setBackground(gradient(i, CYAN, 24));
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon("☎", i, -1, 52), new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout column2 = column();
        int i2 = 0;
        column2.setPadding(dp(14), 0, 0, 0);
        column2.addView(label("Телефон Telecom+", 23, -1, true), matchWrap());
        column2.addView(label(telephonyStatusTitle(telephonySnapshot), 13, Color.rgb(222, 255, 244), false), matchWrap());
        row.addView(column2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        column.addView(row, matchWrap());
        if (!telephonySnapshot.statusMessage.isEmpty()) {
            TextView label = label(telephonySnapshot.statusMessage, 13, -1, false);
            label.setPadding(0, dp(16), 0, 0);
            column.addView(label, matchWrap());
        }
        if (!telephonySnapshot.isProvisioned()) {
            linearLayout.addView(column, matchWrap());
        }
        boolean isProvisioned = telephonySnapshot.isProvisioned();
        DashboardSnapshot dashboardSnapshot = this.dashboard;
        int i3 = 8;
        if (!isProvisioned) {
            SubscriberProfile subscriberProfile = dashboardSnapshot == null ? new SubscriberProfile() : dashboardSnapshot.profile;
            DashboardSnapshot dashboardSnapshot2 = this.dashboard;
            String presentValue = dashboardSnapshot2 == null ? "" : presentValue(dashboardSnapshot2.phoneNumber);
            if (presentValue.isEmpty()) {
                presentValue = presentValue(subscriberProfile.phoneNumber);
            }
            String presentValue2 = presentValue(subscriberProfile.itPhoneId);
            LinearLayout card = card();
            card.addView(label("Вход в существующий профиль IT Phone", 20, BLUE_DARK, true), matchWrap());
            int i4 = MUTED;
            TextView label2 = label("Номер Интертелекома уже подключён оператором. Telecom+ должен войти в ваш существующий профиль IT Phone как новый терминал — по зарегистрированному e-mail или контактному номеру.", 14, i4, false);
            label2.setPadding(0, dp(8), 0, dp(14));
            card.addView(label2, matchWrap());
            if (!presentValue.isEmpty()) {
                StringBuilder sb = new StringBuilder("Привязанный номер: ");
                sb.append(formatInternationalPhone(presentValue));
                sb.append(presentValue2.isEmpty() ? "" : "\nIT Phone ID: ".concat(presentValue2));
                card.addView(info(sb.toString()), matchWrap());
            }
            if (telephonySnapshot.registrationState == TelephonyRegistrationState.OTP_REQUESTED) {
                boolean isEmailIdentity = RingotelProvisioningClient.isEmailIdentity(telephonySnapshot.phoneNumber);
                String str2 = telephonySnapshot.phoneNumber;
                if (isEmailIdentity) {
                    str = "Код отправлен на " + maskRegistrationIdentity(str2) + ". Введите четыре цифры из письма.";
                } else {
                    str = "Короткий звонок запрошен на контактный номер " + maskRegistrationIdentity(str2) + ". Введите последние четыре цифры вызывающего номера.";
                }
                card.addView(info(str), matchWrap());
                EditText field = field("••••", 18);
                field.setGravity(17);
                field.setTextSize(25.0f);
                field.setLetterSpacing(0.28f);
                field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(60));
                layoutParams.topMargin = dp(12);
                card.addView(field, layoutParams);
                Button primary = primary("Войти в IT Phone");
                LinearLayout.LayoutParams matchWrap = matchWrap();
                matchWrap.topMargin = dp(12);
                card.addView(primary, matchWrap);
                primary.setOnClickListener(new defpackage.c(this, field, primary, 2));
                final Button secondary = secondary("Отправить код заново");
                LinearLayout.LayoutParams matchWrap2 = matchWrap();
                matchWrap2.topMargin = dp(9);
                card.addView(secondary, matchWrap2);
                secondary.setOnClickListener(new View.OnClickListener() { // from class: ua.telecomplus.app.n
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view) {
                        MainActivity.this.lambda$renderPhone$52(telephonySnapshot, secondary, view);
                    }
                });
            } else {
                String presentValue3 = presentValue(subscriberProfile.email);
                if (presentValue3.isEmpty()) {
                    presentValue3 = presentValue(subscriberProfile.contactPhone);
                }
                EditText field2 = field("E-mail профиля IT Phone", 33);
                field2.setText(presentValue3);
                field2.setTextSize(18.0f);
                card.addView(field2, new LinearLayout.LayoutParams(-1, dp(58)));
                TextView label3 = label("Используйте e-mail из поля «Номер или e-mail регистрации» в оригинальном iP Phone. Абонентский номер 944… сюда вводить не нужно.", 13, i4, false);
                label3.setPadding(0, dp(8), 0, 0);
                card.addView(label3, matchWrap());
                Button primary2 = primary("Получить код IT Phone");
                LinearLayout.LayoutParams matchWrap3 = matchWrap();
                matchWrap3.topMargin = dp(12);
                card.addView(primary2, matchWrap3);
                primary2.setOnClickListener(new defpackage.c(this, field2, primary2, 3));
                Button google = secondary("Войти через Google");
                LinearLayout.LayoutParams gp = matchWrap();
                gp.topMargin = dp(10);
                card.addView(google, gp);
                google.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.this.startGoogleLogin();
                    }
                });
            }
            linearLayout.addView(card, cardMargin());
            linearLayout.addView(info("Телефонный пароль после активации хранится только в Android Keystore в зашифрованном виде."), cardMargin());
            Button diag = secondary("Журнал телефонии (диагностика)");
            diag.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.showTelephonyLog();
                }
            });
            linearLayout.addView(diag, lp(-1, dp(48), 0));
            return;
        }
        if (telephonySnapshot.hasActiveCall()) {
            renderCallPanel(linearLayout, telephonySnapshot);
            return;
        }
        renderDialer(linearLayout, telephonySnapshot);
    }

    private void renderProfile(LinearLayout parent) {
        DashboardSnapshot ds = this.dashboard;
        SubscriberProfile sp = ds == null ? new SubscriberProfile() : ds.profile;
        parent.addView(pageHeader("Профиль", null), matchWrap());
        LinearLayout hero = column();
        hero.setGravity(1);
        hero.setPadding(dp(20), dp(28), dp(20), dp(24));
        hero.setBackground(gradient(BLUE_DARK, BLUE, 32));
        TextView av = circleIcon("T+", BLUE_DARK, YELLOW, 88);
        av.setBackground(rounded(YELLOW, 44, Color.argb(90, 255, 255, 255), 6));
        hero.addView(av, lp(dp(88), dp(88), 0));
        TextView num = label(formatInternationalPhone(sp.phoneNumber), 25, -1, true);
        num.setGravity(17);
        num.setPadding(0, dp(14), 0, dp(8));
        hero.addView(num, matchWrap());
        LinearLayout chipRow = row();
        chipRow.setGravity(17);
        chipRow.addView(chip(displayValue(sp.registrationStatus), BLUE_DARK, YELLOW), wrap());
        hero.addView(chipRow, matchWrap());
        TextView tar = label(displayValue(sp.tariffName), 14, Color.rgb(190, 212, 250), false);
        tar.setGravity(17);
        tar.setPadding(0, dp(10), 0, 0);
        hero.addView(tar, matchWrap());
        parent.addView(hero, matchWrap());
        LinearLayout st1 = row();
        st1.addView(miniStat("Подключён", sp.connectionDate), lp(0, -2, 1.0f));
        LinearLayout.LayoutParams a = lp(0, -2, 1.0f);
        a.leftMargin = dp(10);
        st1.addView(miniStat("Действителен до", sp.validUntil), a);
        parent.addView(st1, cardMargin());
        LinearLayout st2 = row();
        st2.addView(miniStat("Абонентский стаж", sp.subscriberTenure), lp(0, -2, 1.0f));
        LinearLayout.LayoutParams b = lp(0, -2, 1.0f);
        b.leftMargin = dp(10);
        st2.addView(miniStat("Лояльный стаж", sp.loyaltyTenure), b);
        parent.addView(st2, cardMargin());
        parent.addView(sectionTitle("Аккаунт", "Данные лицевого счёта"), sectionMargin());
        LinearLayout card = card();
        card.setPadding(dp(16), dp(6), dp(16), dp(6));
        card.addView(profileInfoRow("№", "Лицевой счёт", combineProfileValue(sp.accountId, sp.contractStatus), BLUE, SOFT_BLUE), matchWrap());
        card.addView(profileDivider(), lp(-1, dp(1), 0));
        card.addView(profileInfoRow("SIM", "Номер и регистрация", combineProfileValue(sp.phoneNumber, sp.registrationStatus), BLUE, SOFT_BLUE), matchWrap());
        card.addView(profileDivider(), lp(-1, dp(1), 0));
        card.addView(profileInfoRow("IT", "ITPhone ID", sp.itPhoneId, BLUE, SOFT_BLUE), matchWrap());
        card.addView(profileDivider(), lp(-1, dp(1), 0));
        card.addView(profileInfoRow("☎", "Контактный номер", sp.contactPhone, BLUE, SOFT_BLUE), matchWrap());
        card.addView(profileDivider(), lp(-1, dp(1), 0));
        card.addView(profileInfoRow("@", "Электронная почта", sp.email, BLUE, SOFT_BLUE), matchWrap());
        parent.addView(card, matchWrap());
        parent.addView(sectionTitle("Сессия", "Управление на этом устройстве"), sectionMargin());
        LinearLayout card3 = card();
        TextView note = label("Telecom+ хранит только серверную сессию. Личный пароль и одноразовый код не сохраняются.", 13, MUTED, false);
        note.setPadding(0, 0, 0, dp(14));
        card3.addView(note, matchWrap());
        Button out = secondary("Выйти из кабинета");
        card3.addView(out, lp(-1, dp(50), 0));
        out.setOnClickListener(new defpackage.d(this, 7));
        Button danger = danger("Забыть это устройство");
        LinearLayout.LayoutParams dl = lp(-1, dp(50), 0);
        dl.topMargin = dp(10);
        card3.addView(danger, dl);
        danger.setOnClickListener(new defpackage.d(this, 8));
        parent.addView(card3, matchWrap());
        TextView ver = label("Telecom+ 1.0", 12, MUTED, false);
        ver.setGravity(17);
        ver.setPadding(0, dp(20), 0, 0);
        parent.addView(ver, matchWrap());
    }

    private void renderServiceSection(LinearLayout linearLayout, String str, String str2, List<ServiceItem> list, int i, int i2) {
        if (list == null || list.isEmpty()) {
            return;
        }
        linearLayout.addView(sectionTitle(str + " · " + list.size(), str2), sectionMargin());
        Iterator<ServiceItem> it = list.iterator();
        int i3 = 0;
        while (it.hasNext()) {
            linearLayout.addView(serviceCard(it.next(), i, i2, i3), cardMargin());
            i3++;
        }
    }

    private void renderServices(LinearLayout parent) {
        parent.addView(subHeader("Услуги", 4), matchWrap());
        final List<ServiceItem> all = this.services == null ? new ArrayList<ServiceItem>() : this.services;
        final List<ServiceItem> connected = new ArrayList<>();
        final List<ServiceItem> available = new ArrayList<>();
        for (ServiceItem it : all) {
            if (it.isConnected()) {
                connected.add(it);
            } else {
                available.add(it);
            }
        }
        if (this.showingCachedData && !all.isEmpty()) {
            parent.addView(warning("Показан сохранённый каталог услуг."), matchWrap());
        }
        LinearLayout tabs = row();
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        tabs.setBackground(rounded(-1, 24, BORDER, 1));
        String[] names = {"Включено " + connected.size(), "Доступно " + available.size(), "Все " + all.size()};
        for (int i = 0; i < 3; i++) {
            final int f = i;
            View seg = segment(names[i], this.serviceFilter == i, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.serviceFilter = f;
                    MainActivity.this.showCabinet();
                }
            });
            LinearLayout.LayoutParams sl = lp(0, dp(42), 1.0f);
            if (i > 0) {
                sl.leftMargin = dp(4);
            }
            tabs.addView(seg, sl);
        }
        LinearLayout.LayoutParams tp = matchWrap();
        tp.bottomMargin = dp(12);
        parent.addView(tabs, tp);
        if (all.isEmpty()) {
            parent.addView(info("Каталог пока пуст. Нажмите обновить, чтобы повторно получить список услуг из личного кабинета."), matchWrap());
            return;
        }
        final List<ServiceItem> src = this.serviceFilter == 0 ? connected : this.serviceFilter == 1 ? available : all;
        EditText search = field("Поиск услуги", 1);
        search.setText(this.serviceQuery);
        LinearLayout.LayoutParams ep = lp(-1, dp(50), 0);
        ep.bottomMargin = dp(12);
        parent.addView(search, ep);
        final LinearLayout box = column();
        final Runnable fill = new Runnable() {
            @Override
            public void run() {
                box.removeAllViews();
                String q = MainActivity.this.serviceQuery.toLowerCase(Locale.ROOT).trim();
                LinearLayout card = null;
                int n = 0;
                for (ServiceItem it : src) {
                    if (!q.isEmpty() && !displayValue(it.title).toLowerCase(Locale.ROOT).contains(q)) {
                        continue;
                    }
                    if (card == null) {
                        card = MainActivity.this.card();
                        card.setPadding(dp(16), dp(4), dp(16), dp(4));
                        box.addView(card, matchWrap());
                    } else {
                        card.addView(MainActivity.this.profileDivider(), MainActivity.this.lp(-1, MainActivity.this.dp(1), 0));
                    }
                    card.addView(MainActivity.this.serviceRow(it), matchWrap());
                    n++;
                }
                if (n == 0) {
                    box.addView(MainActivity.this.info("Ничего не найдено"), matchWrap());
                }
            }
        };
        fill.run();
        search.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence cs, int a, int b, int c) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int a, int b, int c) {
                MainActivity.this.serviceQuery = cs.toString();
                fill.run();
            }

            @Override
            public void afterTextChanged(android.text.Editable e) {
            }
        });
        parent.addView(box, matchWrap());
    }

    private void requestTelephonyActivation(String str, Button button) {
        button.setEnabled(false);
        button.setText("Запрашиваем…");
        runTelephony(new defpackage.m(this, str, 1), new a(this, 5));
    }

    private GradientDrawable rounded(int i, int i2, int i3, int i4) {
        GradientDrawable gradientDrawable = new GradientDrawable();
        gradientDrawable.setColor(i);
        gradientDrawable.setCornerRadius(dp(i2));
        if (i4 > 0) {
            gradientDrawable.setStroke(dp(i4), i3);
        }
        return gradientDrawable;
    }

    private LinearLayout row() {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(0);
        return linearLayout;
    }

    private <T> void runPortal(Callable<PortalResult<T>> callable, PortalCallback<T> portalCallback) {
        this.executor.execute(new i(this, callable, portalCallback, 2));
    }

    private <T> void runTelephony(Callable<TelephonyResult<T>> callable, TelephonyCallback<T> telephonyCallback) {
        this.executor.execute(new i(this, callable, telephonyCallback, 1));
    }

    private void saveDiagnosticReport(String str, String str2) {
        if (str2 == null || str2.trim().isEmpty()) {
            Toast.makeText(this, "Отчёт ещё не сформирован", 0).show();
            return;
        }
        this.pendingDiagnosticReport = str2;
        Intent intent = new Intent("android.intent.action.CREATE_DOCUMENT");
        intent.addCategory("android.intent.category.OPENABLE");
        intent.setType("text/plain");
        intent.putExtra("android.intent.extra.TITLE", str);
        startActivityForResult(intent, REQUEST_SAVE_DIAGNOSTIC);
    }

    private Button secondary(String str) {
        return button(str, BLUE, Color.rgb(238, 245, 253));
    }

    private LinearLayout.LayoutParams sectionMargin() {
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(22);
        matchWrap.bottomMargin = dp(9);
        return matchWrap;
    }

    private TextView sectionTitle(String str, String str2) {
        if (str == null) {
            str = "";
        }
        if (str2 == null) {
            str2 = "";
        }
        SpannableString spannableString = new SpannableString(str.concat(str2.isEmpty() ? "" : "\n".concat(str2)));
        spannableString.setSpan(new StyleSpan(1), 0, str.length(), 33);
        int i = BLUE_DARK;
        spannableString.setSpan(new ForegroundColorSpan(i), 0, str.length(), 33);
        if (!str2.isEmpty()) {
            int length = str.length() + 1;
            spannableString.setSpan(new RelativeSizeSpan(0.68f), length, spannableString.length(), 33);
            spannableString.setSpan(new ForegroundColorSpan(MUTED), length, spannableString.length(), 33);
        }
        TextView label = label("", 20, i, false);
        label.setText(spannableString);
        label.setLineSpacing(dp(2), 1.0f);
        return label;
    }

    private View serviceCard(final ServiceItem serviceItem, int i, int i2, int i3) {
        LinearLayout colorCard = colorCard(i2, 0, 20);
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon(serviceItem.isConnected() ? "✓" : serviceItem.isAvailable() ? "+" : "?", -1, i, 42), new LinearLayout.LayoutParams(dp(42), dp(42)));
        LinearLayout column = column();
        column.setPadding(dp(12), 0, 0, 0);
        column.addView(label(displayValue(serviceItem.title), 17, BLUE_DARK, true), matchWrap());
        column.addView(label("Абонплата: " + displayValue(serviceItem.fee), 12, MUTED, false), matchWrap());
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        row.addView(chip(displayValue(serviceItem.status), i, -1), wrap());
        colorCard.addView(row, matchWrap());
        String str = cleanServiceDetails(serviceItem);
        if (!str.isEmpty()) {
            TextView label = label(str, 13, TEXT, false);
            label.setMaxLines(3);
            label.setPadding(0, dp(12), 0, 0);
            colorCard.addView(label, matchWrap());
        }
        Button secondary = secondary("Подробнее");
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(12);
        colorCard.addView(secondary, matchWrap);
        secondary.setOnClickListener(new View.OnClickListener() { // from class: ua.telecomplus.app.g
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$serviceCard$62(serviceItem, view);
            }
        });
        return colorCard;
    }

    private View serviceStat(String str, int i, String str2) {
        LinearLayout colorCard = colorCard(Color.argb(38, 255, 255, 255), 0, 16);
        colorCard.setPadding(dp(10), dp(10), dp(10), dp(10));
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(label(str2, 15, -1, true), wrap());
        TextView label = label(Integer.toString(i), 20, -1, true);
        label.setGravity(8388613);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1.0f));
        colorCard.addView(row, matchWrap());
        TextView label2 = label(str, 11, Color.rgb(226, 247, 255), false);
        label2.setPadding(0, dp(4), 0, 0);
        colorCard.addView(label2, matchWrap());
        return colorCard;
    }

    // ======================= Звонки / Контакты / Чаты =======================

    private String chatNumber;
    private String contactFilter = "";
    private LinearLayout chatMessages;
    private ScrollView chatScroll;
    private boolean serviceStarted;
    private final PhoneData.Listener dataListener = new PhoneData.Listener() {
        @Override
        public void onDataChanged() {
            MainActivity.this.handler.post(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.onPhoneDataChanged();
                }
            });
        }
    };

    private PhoneData phoneData() {
        return PhoneData.get(((TelecomPlusApplication) getApplication()).vault);
    }

    private void onPhoneDataChanged() {
        if (this.dashboard == null) {
            return;
        }
        if (this.chatNumber != null) {
            refreshChatMessages();
        } else if (this.selectedTab == 6 || (this.selectedTab == 2 && !this.telephonySnapshot.hasActiveCall())) {
            showCabinet();
        }
    }

    private static final int[] AVATAR_COLORS = {Color.rgb(22, 101, 216), Color.rgb(14, 177, 202), Color.rgb(124, 77, 255), Color.rgb(245, 139, 49), Color.rgb(39, 155, 83), Color.rgb(214, 62, 98)};

    private View avatar(String str, int i) {
        String t = str == null ? "" : str.trim();
        String initials = "•";
        if (!t.isEmpty() && Character.isLetter(t.charAt(0))) {
            String[] parts = t.split("\\s+");
            initials = parts[0].substring(0, 1).toUpperCase();
            if (parts.length > 1 && !parts[1].isEmpty()) {
                initials += parts[1].substring(0, 1).toUpperCase();
            }
        } else if (!t.isEmpty()) {
            initials = "#";
        }
        int color = AVATAR_COLORS[Math.abs(t.hashCode()) % AVATAR_COLORS.length];
        TextView icon = circleIcon(initials, -1, color, i);
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(i), dp(i)));
        return icon;
    }

    private String timeLabel(long j) {
        long now = System.currentTimeMillis();
        java.util.Calendar a = java.util.Calendar.getInstance();
        a.setTimeInMillis(j);
        java.util.Calendar b = java.util.Calendar.getInstance();
        b.setTimeInMillis(now);
        if (a.get(java.util.Calendar.YEAR) == b.get(java.util.Calendar.YEAR) && a.get(java.util.Calendar.DAY_OF_YEAR) == b.get(java.util.Calendar.DAY_OF_YEAR)) {
            return new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(new Date(j));
        }
        return new java.text.SimpleDateFormat("dd.MM HH:mm", java.util.Locale.getDefault()).format(new Date(j));
    }

    private static final String IT_PHONE_PACKAGE = "smile.ringotel.it";
    private String pendingCallNumber;

    private boolean itPhoneInstalled() {
        try {
            getPackageManager().getPackageInfo(IT_PHONE_PACKAGE, 0);
            return true;
        } catch (Exception unused) {
            return false;
        }
    }

    private void dialNumber(final String str) {
        this.chatNumber = null;
        this.selectedTab = 2;
        callViaItPhone(str);
        showCabinet();
    }

    private void callViaItPhone(String raw) {
        String num = raw == null ? "" : raw.trim().replaceAll("[^0-9+*#]", "");
        if (num.isEmpty()) {
            return;
        }
        if (!itPhoneInstalled()) {
            new AlertDialog.Builder(this).setTitle("Нужен IT Phone").setMessage("Звонки выполняются через официальное приложение IT Phone. Установите его и войдите в свой профиль один раз.").setPositiveButton("Установить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    try {
                        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + IT_PHONE_PACKAGE)));
                    } catch (Exception e) {
                        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://play.google.com/store/apps/details?id=" + IT_PHONE_PACKAGE)));
                    }
                }
            }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
            return;
        }
        boolean asked = getSharedPreferences("telecom_plus_net", 0).getBoolean("call_perm_asked", false);
        if (checkSelfPermission("android.permission.CALL_PHONE") != 0 && !asked) {
            getSharedPreferences("telecom_plus_net", 0).edit().putBoolean("call_perm_asked", true).apply();
            this.pendingCallNumber = num;
            requestPermissions(new String[]{"android.permission.CALL_PHONE"}, 4715);
            return;
        }
        launchItPhoneCall(num);
    }

    private void launchItPhoneCall(String num) {
        boolean canCall = checkSelfPermission("android.permission.CALL_PHONE") == 0;
        try {
            Intent intent = new Intent(canCall ? "android.intent.action.CALL" : "android.intent.action.DIAL", Uri.fromParts("tel", num, null));
            intent.setPackage(IT_PHONE_PACKAGE);
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent2 = new Intent("android.intent.action.DIAL", Uri.fromParts("tel", num, null));
                intent2.setPackage(IT_PHONE_PACKAGE);
                startActivity(intent2);
            } catch (Exception e2) {
                Intent launch = getPackageManager().getLaunchIntentForPackage(IT_PHONE_PACKAGE);
                if (launch != null) {
                    startActivity(launch);
                    Toast.makeText(this, "Откройте набор в IT Phone и введите номер: " + num, 1).show();
                }
            }
        }
    }

    private void openChat(String str) {
        this.chatNumber = str;
        this.selectedTab = 6;
        phoneData().openThread(str);
        showCabinet();
    }

    private void cabinetSegments(LinearLayout linearLayout) {
        String[] names = {"Главная", "Услуги", "История"};
        final int[] tabs = {0, 1, 3};
        LinearLayout row = row();
        row.setPadding(dp(4), dp(4), dp(4), dp(4));
        row.setBackground(rounded(-1, 22, BORDER, 1));
        for (int i = 0; i < 3; i++) {
            final int tab = tabs[i];
            boolean active = this.selectedTab == tab;
            TextView t = label(names[i], 14, active ? BLUE_DARK : MUTED, true);
            t.setGravity(17);
            t.setBackground(active ? rounded(YELLOW, 18, 0, 0) : null);
            t.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.selectedTab = tab;
                    MainActivity.this.showCabinet();
                }
            });
            row.addView(t, new LinearLayout.LayoutParams(0, dp(40), 1.0f));
        }
        LinearLayout.LayoutParams rp = matchWrap();
        rp.bottomMargin = dp(14);
        linearLayout.addView(row, rp);
    }

    private void renderRecents(LinearLayout linearLayout) {
        List<PhoneData.CallEntry> calls = phoneData().calls();
        LinearLayout card = card();
        LinearLayout head = row();
        head.setGravity(16);
        head.addView(label("Недавние вызовы", 18, BLUE_DARK, true), new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (!calls.isEmpty()) {
            TextView clear = label("Очистить", 13, MUTED, false);
            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.phoneData().clearCalls();
                }
            });
            head.addView(clear, wrap());
        }
        card.addView(head, matchWrap());
        if (calls.isEmpty()) {
            TextView empty = label("Вызовов пока нет", 14, MUTED, false);
            empty.setPadding(0, dp(10), 0, 0);
            card.addView(empty, matchWrap());
        }
        for (int i = 0; i < Math.min(calls.size(), 25); i++) {
            final PhoneData.CallEntry e = calls.get(i);
            LinearLayout row = row();
            row.setGravity(16);
            row.setPadding(0, dp(10), 0, dp(10));
            String title = phoneData().title(e.number);
            row.addView(avatar(title, 42));
            LinearLayout col = column();
            col.setPadding(dp(12), 0, dp(8), 0);
            int color = e.dir == PhoneData.CALL_MISSED ? RED : TEXT;
            col.addView(label(title, 16, color, true), matchWrap());
            String arrow = e.dir == PhoneData.CALL_IN ? "↙ Входящий" : e.dir == PhoneData.CALL_OUT ? "↗ Исходящий" : "✕ Пропущенный";
            String detail = arrow + " · " + timeLabel(e.time) + (e.duration > 0 ? " · " + formatDuration(e.duration) : "");
            col.addView(label(detail, 12, MUTED, false), matchWrap());
            row.addView(col, new LinearLayout.LayoutParams(0, -2, 1.0f));
            TextView call = circleIcon("☎", -1, GREEN, 40);
            call.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.dialNumber(e.number);
                }
            });
            row.addView(call);
            TextView msg = circleIcon("✉", -1, BLUE, 40);
            LinearLayout.LayoutParams ml = new LinearLayout.LayoutParams(dp(40), dp(40));
            ml.leftMargin = dp(8);
            msg.setLayoutParams(ml);
            msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.openChat(e.number);
                }
            });
            row.addView(msg);
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    MainActivity.this.blockNumberDialog(e.number);
                    return true;
                }
            });
            card.addView(row, matchWrap());
        }
        linearLayout.addView(card, cardMargin());
    }

    private void editContactDialog(final PhoneData.Contact contact, String presetNumber) {
        LinearLayout form = column();
        form.setPadding(dp(20), dp(8), dp(20), 0);
        final EditText name = field("Имя", 8193);
        final EditText number = field("Номер", 3);
        if (contact != null) {
            name.setText(contact.name);
            number.setText(contact.number);
        } else if (presetNumber != null) {
            number.setText(presetNumber);
        }
        form.addView(name, new LinearLayout.LayoutParams(-1, dp(52)));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(52));
        lp.topMargin = dp(10);
        form.addView(number, lp);
        final boolean[] fav = {contact != null && contact.favorite};
        final TextView star = label(fav[0] ? "★ В избранном" : "☆ В избранное", 15, ORANGE, true);
        star.setPadding(0, dp(12), 0, 0);
        star.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fav[0] = !fav[0];
                star.setText(fav[0] ? "★ В избранном" : "☆ В избранное");
            }
        });
        form.addView(star, matchWrap());
        AlertDialog.Builder b = new AlertDialog.Builder(this).setTitle(contact == null ? "Новый контакт" : "Контакт").setView(form).setPositiveButton("Сохранить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String n = name.getText().toString().trim();
                String num = number.getText().toString().trim();
                if (num.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Введите номер", 0).show();
                    return;
                }
                if (contact == null) {
                    PhoneData.Contact c = MainActivity.this.phoneData().addContact(n, num);
                    if (fav[0]) {
                        MainActivity.this.phoneData().updateContact(c, c.name, c.number, true);
                    }
                } else {
                    MainActivity.this.phoneData().updateContact(contact, n.isEmpty() ? contact.name : n, num, fav[0]);
                }
                MainActivity.this.showCabinet();
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null);
        if (contact != null) {
            b.setNeutralButton("Удалить", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    MainActivity.this.phoneData().removeContact(contact);
                    MainActivity.this.showCabinet();
                }
            });
        }
        b.show();
    }

    private void renderContacts(LinearLayout linearLayout) {
        LinearLayout actions = row();
        Button add = primary("＋ Контакт");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.editContactDialog(null, null);
            }
        });
        Button imp = secondary("Импорт");
        imp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.this.checkSelfPermission("android.permission.READ_CONTACTS") == 0) {
                    MainActivity.this.importDeviceContacts();
                } else {
                    MainActivity.this.requestPermissions(new String[]{"android.permission.READ_CONTACTS"}, 4713);
                }
            }
        });
        actions.addView(add, new LinearLayout.LayoutParams(0, dp(50), 1.0f));
        LinearLayout.LayoutParams il = new LinearLayout.LayoutParams(0, dp(50), 1.0f);
        il.leftMargin = dp(10);
        actions.addView(imp, il);
        LinearLayout.LayoutParams ap = cardMargin();
        linearLayout.addView(actions, ap);
        final EditText search = field("Поиск по имени или номеру", 1);
        search.setText(this.contactFilter);
        search.setOnEditorActionListener(new android.widget.TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, android.view.KeyEvent keyEvent) {
                MainActivity.this.contactFilter = textView.getText().toString();
                MainActivity.this.showCabinet();
                return true;
            }
        });
        search.setImeOptions(3);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, dp(50));
        sp.topMargin = dp(10);
        linearLayout.addView(search, sp);
        String q = this.contactFilter.trim().toLowerCase(java.util.Locale.ROOT);
        String qd = q.replaceAll("[^0-9]", "");
        List<PhoneData.Contact> all = phoneData().contacts();
        LinearLayout card = card();
        int shown = 0;
        for (final PhoneData.Contact c : all) {
            if (!q.isEmpty() && !c.name.toLowerCase(java.util.Locale.ROOT).contains(q) && (qd.isEmpty() || !c.number.contains(qd))) {
                continue;
            }
            shown++;
            LinearLayout row = row();
            row.setGravity(16);
            row.setPadding(0, dp(9), 0, dp(9));
            row.addView(avatar(c.name, 44));
            LinearLayout col = column();
            col.setPadding(dp(12), 0, dp(8), 0);
            col.addView(label((c.favorite ? "★ " : "") + c.name, 16, TEXT, true), matchWrap());
            col.addView(label(PhoneData.pretty(c.number), 13, MUTED, false), matchWrap());
            row.addView(col, new LinearLayout.LayoutParams(0, -2, 1.0f));
            TextView call = circleIcon("☎", -1, GREEN, 40);
            call.setLayoutParams(new LinearLayout.LayoutParams(dp(40), dp(40)));
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.dialNumber(c.number);
                }
            });
            row.addView(call);
            TextView msg = circleIcon("✉", -1, BLUE, 40);
            LinearLayout.LayoutParams ml = new LinearLayout.LayoutParams(dp(40), dp(40));
            ml.leftMargin = dp(8);
            msg.setLayoutParams(ml);
            msg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.openChat(c.number);
                }
            });
            row.addView(msg);
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.editContactDialog(c, null);
                }
            });
            card.addView(row, matchWrap());
        }
        if (shown == 0) {
            TextView empty = label(all.isEmpty() ? "Контактов пока нет. Добавьте вручную или импортируйте из телефона." : "Ничего не найдено", 14, MUTED, false);
            empty.setGravity(17);
            empty.setPadding(0, dp(16), 0, dp(16));
            card.addView(empty, matchWrap());
        }
        linearLayout.addView(card, cardMargin());
    }

    private void importDeviceContacts() {
        final android.content.Context ctx = this;
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                final int n = MainActivity.this.phoneData().importDeviceContacts(ctx);
                MainActivity.this.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, n == 0 ? "Новых контактов нет" : "Импортировано контактов: " + n, 0).show();
                        MainActivity.this.showCabinet();
                    }
                });
            }
        });
    }

    private void newMessageDialog() {
        LinearLayout form = column();
        form.setPadding(dp(20), dp(8), dp(20), 0);
        final EditText number = field("Номер получателя", 3);
        form.addView(number, new LinearLayout.LayoutParams(-1, dp(52)));
        new AlertDialog.Builder(this).setTitle("Новое сообщение").setView(form).setPositiveButton("Написать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String num = number.getText().toString().trim();
                if (num.isEmpty()) {
                    return;
                }
                MainActivity.this.openChat(num);
            }
        }).setNeutralButton("Из контактов", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.pickContactForChat();
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    private void pickContactForChat() {
        final List<PhoneData.Contact> all = phoneData().contacts();
        if (all.isEmpty()) {
            Toast.makeText(this, "В книге нет контактов", 0).show();
            return;
        }
        String[] names = new String[all.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = all.get(i).name + " · " + PhoneData.pretty(all.get(i).number);
        }
        new AlertDialog.Builder(this).setTitle("Выберите контакт").setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                MainActivity.this.openChat(all.get(i).number);
            }
        }).show();
    }

    private void renderChats(LinearLayout linearLayout) {
        Button add = primary("＋ Новое сообщение");
        add.setBackground(rounded(YELLOW, 26, 0, 0));
        add.setTextColor(BLUE_DARK);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.newMessageDialog();
            }
        });
        LinearLayout.LayoutParams ap = cardMargin();
        ap.height = dp(50);
        linearLayout.addView(add, ap);
        List<PhoneData.Thread> threads = phoneData().threads();
        LinearLayout card = card();
        if (threads.isEmpty()) {
            TextView empty = label("Переписок пока нет", 14, MUTED, false);
            empty.setGravity(17);
            empty.setPadding(0, dp(16), 0, dp(16));
            card.addView(empty, matchWrap());
        }
        for (final PhoneData.Thread t : threads) {
            String title = phoneData().title(t.number);
            LinearLayout row = row();
            row.setGravity(16);
            row.setPadding(0, dp(10), 0, dp(10));
            row.addView(avatar(title, 46));
            LinearLayout col = column();
            col.setPadding(dp(12), 0, dp(8), 0);
            col.addView(label(title, 16, TEXT, true), matchWrap());
            String last = t.messages.isEmpty() ? "Нет сообщений" : (t.messages.get(t.messages.size() - 1).outgoing ? "Вы: " : "") + t.messages.get(t.messages.size() - 1).text;
            TextView preview = label(last, 13, t.unread > 0 ? TEXT : MUTED, t.unread > 0);
            preview.setSingleLine(true);
            preview.setEllipsize(android.text.TextUtils.TruncateAt.END);
            col.addView(preview, matchWrap());
            row.addView(col, new LinearLayout.LayoutParams(0, -2, 1.0f));
            LinearLayout right = column();
            right.setGravity(5);
            right.addView(label(timeLabel(t.updated), 11, MUTED, false), wrap());
            if (t.unread > 0) {
                TextView badge = label(String.valueOf(t.unread), 11, -1, true);
                badge.setGravity(17);
                badge.setBackground(rounded(BLUE, 10, 0, 0));
                LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(dp(22), dp(22));
                bl.topMargin = dp(4);
                right.addView(badge, bl);
            }
            row.addView(right, wrap());
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.openChat(t.number);
                }
            });
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    new AlertDialog.Builder(MainActivity.this).setTitle("Удалить переписку?").setPositiveButton("Удалить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            MainActivity.this.phoneData().deleteThread(t.number);
                        }
                    }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
                    return true;
                }
            });
            card.addView(row, matchWrap());
        }
        linearLayout.addView(card, cardMargin());
    }

    private void showChat() {
        final String number = this.chatNumber;
        this.root.removeAllViews();
        LinearLayout column = column();
        LinearLayout bar = row();
        bar.setGravity(16);
        bar.setPadding(dp(8), dp(8), dp(12), dp(8));
        bar.setBackground(gradient(BLUE_DARK, BLUE, 0));
        Button back = button("‹", -1, 0);
        back.setTextSize(30.0f);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.chatNumber = null;
                MainActivity.this.showCabinet();
            }
        });
        bar.addView(back, new LinearLayout.LayoutParams(dp(46), dp(46)));
        bar.addView(avatar(phoneData().title(number), 40));
        LinearLayout titles = column();
        titles.setPadding(dp(12), 0, 0, 0);
        titles.addView(label(phoneData().title(number), 18, -1, true), matchWrap());
        titles.addView(label(PhoneData.pretty(number), 12, Color.rgb(214, 231, 255), false), matchWrap());
        bar.addView(titles, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView call = circleIcon("☎", -1, GREEN, 42);
        call.setLayoutParams(new LinearLayout.LayoutParams(dp(42), dp(42)));
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.dialNumber(number);
            }
        });
        bar.addView(call);
        column.addView(bar, new LinearLayout.LayoutParams(-1, dp(66)));
        this.chatScroll = new ScrollView(this);
        this.chatScroll.setBackgroundColor(BG);
        this.chatMessages = column();
        this.chatMessages.setPadding(dp(12), dp(12), dp(12), dp(12));
        this.chatScroll.addView(this.chatMessages);
        column.addView(this.chatScroll, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        LinearLayout input = row();
        input.setGravity(16);
        input.setPadding(dp(10), dp(8), dp(10), dp(8));
        input.setBackgroundColor(-1);
        final EditText text = field("Сообщение", 16385);
        text.setSingleLine(false);
        text.setMaxLines(4);
        text.setMinHeight(dp(46));
        text.setPadding(dp(16), dp(8), dp(16), dp(8));
        input.addView(text, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView send = circleIcon("➤", -1, BLUE, 46);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(dp(46), dp(46));
        sl.leftMargin = dp(8);
        send.setLayoutParams(sl);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String body = text.getText().toString().trim();
                if (body.isEmpty()) {
                    return;
                }
                text.setText("");
                MainActivity.this.sendChatMessage(number, body);
            }
        });
        input.addView(send);
        column.addView(input, new LinearLayout.LayoutParams(-1, -2));
        this.root.addView(column, new FrameLayout.LayoutParams(-1, -1));
        refreshChatMessages();
    }

    private void sendChatMessage(String str, String str2) {
        RingotelControl control = this.telephony.control();
        if (control == null) {
            phoneData().addMessage(str, null, true, str2, System.currentTimeMillis(), -1);
            Toast.makeText(this, "Телефония не подключена — сообщение не отправлено", 1).show();
            return;
        }
        control.sendText(str, str2);
    }

    private void refreshChatMessages() {
        if (this.chatMessages == null || this.chatNumber == null) {
            return;
        }
        PhoneData.Thread thread = phoneData().thread(this.chatNumber);
        this.chatMessages.removeAllViews();
        if (thread == null || thread.messages.isEmpty()) {
            TextView empty = label("Напишите первое сообщение", 14, MUTED, false);
            empty.setGravity(17);
            empty.setPadding(0, dp(40), 0, 0);
            this.chatMessages.addView(empty, matchWrap());
        } else {
            for (final PhoneData.Msg m : thread.messages) {
                LinearLayout line = row();
                line.setGravity(m.outgoing ? 5 : 3);
                LinearLayout bubble = column();
                bubble.setPadding(dp(14), dp(9), dp(14), dp(7));
                bubble.setBackground(m.outgoing ? gradient(BLUE, CYAN, 18) : rounded(-1, 18, BORDER, 1));
                TextView body = label(m.text, 15, m.outgoing ? -1 : TEXT, false);
                body.setTextIsSelectable(true);
                bubble.addView(body, wrap());
                String meta = timeLabel(m.time) + (m.outgoing ? (m.status == -1 ? "  ! не отправлено" : m.status == 0 ? "  …" : m.status == 2 ? "  ✓✓" : "  ✓") : "");
                TextView time = label(meta, 10, m.outgoing ? Color.rgb(214, 231, 255) : MUTED, false);
                time.setGravity(5);
                bubble.addView(time, matchWrap());
                if (m.outgoing && m.status == -1) {
                    bubble.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            PhoneData.Thread tt = MainActivity.this.phoneData().thread(MainActivity.this.chatNumber);
                            if (tt != null) {
                                tt.messages.remove(m);
                            }
                            MainActivity.this.sendChatMessage(MainActivity.this.chatNumber, m.text);
                        }
                    });
                }
                LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(-2, -2);
                bp.leftMargin = m.outgoing ? dp(60) : 0;
                bp.rightMargin = m.outgoing ? 0 : dp(60);
                line.addView(bubble, bp);
                LinearLayout.LayoutParams lp = matchWrap();
                lp.topMargin = dp(6);
                this.chatMessages.addView(line, lp);
            }
        }
        phoneData().markRead(this.chatNumber);
        RingotelControl control = this.telephony.control();
        if (control != null) {
            control.markRead(this.chatNumber);
        }
        this.chatScroll.post(new Runnable() {
            @Override
            public void run() {
                MainActivity.this.chatScroll.fullScroll(130);
            }
        });
    }

    private void handleLaunchIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        if (intent.getBooleanExtra(TelephonyService.EXTRA_ANSWER, false)) {
            intent.removeExtra(TelephonyService.EXTRA_ANSWER);
            withMicPermission(new Runnable() {
                @Override
                public void run() {
                    MainActivity.this.telephonyAction(new Callable<TelephonyResult<Void>>() {
                        @Override
                        public TelephonyResult<Void> call() {
                            return MainActivity.this.telephony.answer();
                        }
                    });
                }
            });
            this.selectedTab = 2;
            this.chatNumber = null;
        }
        String chat = intent.getStringExtra("open_chat");
        if (chat != null) {
            intent.removeExtra("open_chat");
            this.chatNumber = chat;
            this.selectedTab = 6;
        }
        if (this.dashboard != null) {
            showCabinet();
        }
    }

    private void ensureTelephonyService() {
        if (this.serviceStarted || !this.telephonySnapshot.isProvisioned()) {
            return;
        }
        this.serviceStarted = true;
        if (android.os.Build.VERSION.SDK_INT >= 33 && checkSelfPermission("android.permission.POST_NOTIFICATIONS") != 0) {
            requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 4712);
        }
        TelephonyService.start(this);
    }

    private boolean batteryRestricted() {
        try {
            android.os.PowerManager pm = (android.os.PowerManager) getSystemService(POWER_SERVICE);
            return pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName());
        } catch (Exception unused) {
            return false;
        }
    }

    private void requestBatteryWhitelist() {
        try {
            Intent intent = new Intent("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception e) {
            try {
                startActivity(new Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS"));
            } catch (Exception unused) {
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleLaunchIntent(intent);
    }

    // ======================= Google-вход, номера, баланс =======================

    private org.json.JSONArray numbersCache = new org.json.JSONArray();
    private final java.util.Map<String, org.json.JSONObject> balanceCache = new java.util.HashMap<>();
    private long numbersLoadedAt;
    private boolean numbersLoading;

    private void toastUi(final String str) {
        this.handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, str, 1).show();
            }
        });
    }

    private void startGoogleLogin() {
        showLoading("Открываем вход через Google…");
        runTelephony(new Callable<TelephonyResult<String>>() {
            @Override
            public TelephonyResult<String> call() {
                return MainActivity.this.telephony.beginGoogleLogin();
            }
        }, new TelephonyCallback<String>() {
            @Override
            public void accept(TelephonyResult<String> telephonyResult) {
                if (telephonyResult instanceof TelephonyResult.Success) {
                    MainActivity.this.showGoogleWebView((String) ((TelephonyResult.Success) telephonyResult).value);
                    return;
                }
                MainActivity.this.showCabinet();
                Toast.makeText(MainActivity.this, ((TelephonyResult.Failure) telephonyResult).message, 1).show();
            }
        });
    }

    private void showGoogleWebView(String str) {
        final android.app.Dialog dialog = new android.app.Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        LinearLayout box = column();
        LinearLayout bar = row();
        bar.setGravity(16);
        bar.setBackgroundColor(BLUE_DARK);
        bar.setPadding(dp(14), dp(8), dp(8), dp(8));
        bar.addView(label("Вход через Google", 18, -1, true), new LinearLayout.LayoutParams(0, -2, 1.0f));
        Button close = button("✕", -1, 0);
        close.setTextSize(20.0f);
        bar.addView(close, new LinearLayout.LayoutParams(dp(48), dp(48)));
        box.addView(bar, new LinearLayout.LayoutParams(-1, dp(58)));
        final android.webkit.WebView web = new android.webkit.WebView(this);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setUserAgentString(getPackageName());
        final boolean[] done = {false};
        web.setWebViewClient(new android.webkit.WebViewClient() {
            private void check(String url) {
                if (!done[0] && url != null && url.contains("ringotel.net/services/Google") && url.contains(".profile")) {
                    done[0] = true;
                    dialog.dismiss();
                    MainActivity.this.completeGoogleLogin();
                }
            }

            @Override
            public void onPageStarted(android.webkit.WebView view, String url, android.graphics.Bitmap favicon) {
                check(url);
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                check(url);
            }
        });
        box.addView(web, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        dialog.setContentView(box);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                web.destroy();
                if (!done[0]) {
                    MainActivity.this.showCabinet();
                }
            }
        });
        dialog.show();
        web.loadUrl(str);
    }

    private void completeGoogleLogin() {
        showLoading("Завершаем вход…");
        runTelephony(new Callable<TelephonyResult<Void>>() {
            @Override
            public TelephonyResult<Void> call() {
                return MainActivity.this.telephony.finishGoogleLogin();
            }
        }, new TelephonyCallback<Void>() {
            @Override
            public void accept(TelephonyResult<Void> telephonyResult) {
                MainActivity.this.serviceStarted = false;
                MainActivity.this.showCabinet();
                if (telephonyResult instanceof TelephonyResult.Failure) {
                    Toast.makeText(MainActivity.this, ((TelephonyResult.Failure) telephonyResult).message, 1).show();
                } else {
                    MainActivity.this.ensureTelephonyService();
                }
            }
        });
    }

    private static String pickString(org.json.JSONObject o, String... keys) {
        for (String k : keys) {
            String v = o.optString(k, "");
            if (!v.isEmpty() && !v.equals("null")) {
                return v;
            }
        }
        return "";
    }

    private String balanceText(org.json.JSONObject b) {
        if (b == null) {
            return "";
        }
        String amount = pickString(b, "balance", "amount", "sum", "money", "value");
        if (amount.isEmpty()) {
            org.json.JSONObject inner = b.optJSONObject("balance");
            if (inner != null) {
                amount = pickString(inner, "amount", "sum", "value", "balance");
                String cur = pickString(inner, "currency", "cur");
                return amount.isEmpty() ? "" : amount + (cur.isEmpty() ? "" : " " + cur);
            }
        }
        String cur = pickString(b, "currency", "cur");
        return amount.isEmpty() ? "" : amount + (cur.isEmpty() ? " UAH" : " " + cur);
    }

    private void refreshNumbers(final boolean force) {
        final RingotelControl control = this.telephony == null ? null : this.telephony.control();
        if (control == null || !control.isOnline() || this.numbersLoading) {
            return;
        }
        if (!force && System.currentTimeMillis() - this.numbersLoadedAt < 60000) {
            return;
        }
        this.numbersLoading = true;
        this.numbersLoadedAt = System.currentTimeMillis();
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    org.json.JSONArray trunks = control.refreshTrunks();
                    java.util.Map<String, org.json.JSONObject> balances = new java.util.HashMap<>();
                    for (int i = 0; i < trunks.length(); i++) {
                        org.json.JSONObject t = trunks.optJSONObject(i);
                        if (t == null) {
                            continue;
                        }
                        String provider = t.optString("provider", control.providerId());
                        String phone = pickString(t, "phone", "user");
                        if (provider != null && !provider.isEmpty() && !phone.isEmpty()) {
                            try {
                                balances.put(phone, control.phoneBalance(provider, phone));
                            } catch (Exception unused) {
                            }
                        }
                    }
                    MainActivity.this.numbersCache = trunks;
                    MainActivity.this.balanceCache.putAll(balances);
                } catch (Exception unused2) {
                }
                MainActivity.this.numbersLoading = false;
                MainActivity.this.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (MainActivity.this.dashboard != null && MainActivity.this.chatNumber == null && MainActivity.this.selectedTab == 4) {
                            MainActivity.this.showCabinet();
                        }
                    }
                });
            }
        });
    }

    private void renderNumbers(LinearLayout linearLayout) {
        final RingotelControl control = this.telephony == null ? null : this.telephony.control();
        if (!this.telephonySnapshot.isProvisioned()) {
            return;
        }
        refreshNumbers(false);
        LinearLayout card = card();
        LinearLayout head = row();
        head.setGravity(16);
        head.addView(label("Мои номера", 18, BLUE_DARK, true), new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView refresh = label("↻ Обновить", 13, BLUE, true);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.refreshNumbers(true);
                Toast.makeText(MainActivity.this, "Обновляем…", 0).show();
            }
        });
        head.addView(refresh, wrap());
        card.addView(head, matchWrap());
        if (control == null || !control.isOnline()) {
            TextView off = label("Нет подключения к серверу телефонии", 14, MUTED, false);
            off.setPadding(0, dp(10), 0, 0);
            card.addView(off, matchWrap());
        } else if (this.numbersCache.length() == 0) {
            TextView none = label(this.numbersLoading ? "Загружаем…" : "К аккаунту пока не привязано номеров", 14, MUTED, false);
            none.setPadding(0, dp(10), 0, 0);
            card.addView(none, matchWrap());
        }
        for (int i = 0; i < this.numbersCache.length(); i++) {
            final org.json.JSONObject t = this.numbersCache.optJSONObject(i);
            if (t == null) {
                continue;
            }
            final String phone = pickString(t, "phone", "user");
            String balance = balanceText(this.balanceCache.get(phone));
            LinearLayout row = column();
            row.setPadding(dp(12), dp(10), dp(12), dp(10));
            row.setBackground(rounded(SOFT_BLUE, 14, 0, 0));
            row.addView(label(PhoneData.pretty(phone), 17, TEXT, true), matchWrap());
            if (!balance.isEmpty()) {
                row.addView(label("Баланс: " + balance, 14, GREEN, true), matchWrap());
            }
            int state = t.optInt("state", 1);
            row.addView(label("Звонки и SMS: " + (state > 0 ? "включено" : "отключено"), 13, MUTED, false), matchWrap());
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.numberDialog(t, phone);
                }
            });
            LinearLayout.LayoutParams lp = matchWrap();
            lp.topMargin = dp(10);
            card.addView(row, lp);
        }
        LinearLayout actions = row();
        Button add = secondary("＋ Добавить номер");
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.addNumberDialog();
            }
        });
        Button buy = secondary("Купить номер");
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.buyNumberFlow();
            }
        });
        actions.addView(add, new LinearLayout.LayoutParams(0, dp(48), 1.0f));
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(0, dp(48), 1.0f);
        bl.leftMargin = dp(10);
        actions.addView(buy, bl);
        LinearLayout.LayoutParams ap = matchWrap();
        ap.topMargin = dp(12);
        card.addView(actions, ap);
        Button reload = secondary("Перезагрузить чаты, контакты и историю с сервера");
        LinearLayout.LayoutParams rp = matchWrap();
        rp.topMargin = dp(10);
        card.addView(reload, rp);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final RingotelControl c = MainActivity.this.telephony.control();
                if (c == null || !c.isOnline()) {
                    Toast.makeText(MainActivity.this, "Нет подключения к серверу телефонии", 0).show();
                    return;
                }
                Toast.makeText(MainActivity.this, "Загружаем с сервера…", 0).show();
                MainActivity.this.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            c.reloadObjects();
                            MainActivity.this.toastUi("Данные обновлены");
                        } catch (Exception e) {
                            MainActivity.this.toastUi("Не удалось: " + e.getMessage());
                        }
                    }
                });
            }
        });
        linearLayout.addView(card, cardMargin());
    }

    private void numberDialog(final org.json.JSONObject trunk, final String phone) {
        final String oid = trunk.optString("oid", "");
        new AlertDialog.Builder(this).setTitle(PhoneData.pretty(phone)).setMessage("Звонки и SMS с этого номера работают через ваш аккаунт IT Phone.").setPositiveButton("Закрыть", (DialogInterface.OnClickListener) null).setNegativeButton("Отвязать номер", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                new AlertDialog.Builder(MainActivity.this).setTitle("Отвязать номер?").setMessage("Номер " + PhoneData.pretty(phone) + " будет удалён из аккаунта IT Phone.").setPositiveButton("Отвязать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface2, int i2) {
                        final RingotelControl c = MainActivity.this.telephony.control();
                        if (c == null || oid.isEmpty()) {
                            return;
                        }
                        MainActivity.this.executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    c.deleteTrunk(oid);
                                    MainActivity.this.numbersCache = c.trunks();
                                    MainActivity.this.toastUi("Номер отвязан");
                                } catch (Exception e) {
                                    MainActivity.this.toastUi("Не удалось: " + e.getMessage());
                                }
                                MainActivity.this.handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        MainActivity.this.showCabinet();
                                    }
                                });
                            }
                        });
                    }
                }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
            }
        }).show();
    }

    private String serverError(org.json.JSONObject r) {
        String code = r.optString("code", r.optString("rcode", "0"));
        if (code.isEmpty() || code.equals("0")) {
            return null;
        }
        String text = r.optString("text", "Ошибка " + code);
        return text.equals("InvalidCode") ? "Неверный код" : text;
    }

    private void addNumberDialog() {
        final RingotelControl control = this.telephony.control();
        if (control == null || !control.isOnline() || control.providerId() == null) {
            Toast.makeText(this, "Нет подключения к серверу телефонии", 1).show();
            return;
        }
        LinearLayout form = column();
        form.setPadding(dp(20), dp(8), dp(20), 0);
        final EditText number = field("380XXXXXXXXX", 3);
        form.addView(number, new LinearLayout.LayoutParams(-1, dp(52)));
        form.addView(info("На номер придёт короткий звонок. Введите последние четыре цифры номера, который позвонил."), cardMargin());
        new AlertDialog.Builder(this).setTitle("Добавить номер Интертелеком").setView(form).setPositiveButton("Продолжить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String phone = PhoneData.dialable(number.getText().toString());
                if (phone.length() < 12) {
                    Toast.makeText(MainActivity.this, "Введите номер в формате 380XXXXXXXXX", 1).show();
                    return;
                }
                MainActivity.this.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            org.json.JSONObject r = control.connectNumber(control.providerId(), phone, null);
                            final String err = MainActivity.this.serverError(r);
                            MainActivity.this.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (err != null) {
                                        Toast.makeText(MainActivity.this, err, 1).show();
                                    } else {
                                        MainActivity.this.addNumberCodeDialog(control, phone);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            MainActivity.this.toastUi("Не удалось: " + e.getMessage());
                        }
                    }
                });
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    private void addNumberCodeDialog(final RingotelControl control, final String phone) {
        LinearLayout form = column();
        form.setPadding(dp(20), dp(8), dp(20), 0);
        final EditText code = field("••••", 18);
        code.setGravity(17);
        code.setTextSize(24.0f);
        code.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        form.addView(code, new LinearLayout.LayoutParams(-1, dp(58)));
        new AlertDialog.Builder(this).setTitle("Код подтверждения").setView(form).setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final String c = code.getText().toString().trim();
                MainActivity.this.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            org.json.JSONObject r = control.connectNumber(control.providerId(), phone, c);
                            String err = MainActivity.this.serverError(r);
                            if (err == null) {
                                MainActivity.this.numbersCache = control.trunks();
                                MainActivity.this.numbersLoadedAt = 0;
                            }
                            MainActivity.this.toastUi(err == null ? "Номер добавлен" : err);
                        } catch (Exception e) {
                            MainActivity.this.toastUi("Не удалось: " + e.getMessage());
                        }
                        MainActivity.this.handler.post(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.this.showCabinet();
                            }
                        });
                    }
                });
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    private void buyNumberFlow() {
        final RingotelControl control = this.telephony.control();
        final String provider = control == null ? null : control.providerId();
        if (control == null || !control.isOnline() || provider == null) {
            Toast.makeText(this, "Нет подключения к серверу телефонии", 1).show();
            return;
        }
        Toast.makeText(this, "Загружаем предложение…", 0).show();
        this.executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    final org.json.JSONObject offer = control.offer(provider);
                    MainActivity.this.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.pickNumberDialog(control, provider, offer);
                        }
                    });
                } catch (Exception e) {
                    MainActivity.this.toastUi("Не удалось: " + e.getMessage());
                }
            }
        });
    }

    private void pickNumberDialog(final RingotelControl control, final String provider, final org.json.JSONObject offer) {
        org.json.JSONArray nums = offer.optJSONArray("numbers");
        final org.json.JSONArray packs = offer.optJSONArray("packages");
        if (nums == null || nums.length() == 0 || packs == null || packs.length() == 0) {
            Toast.makeText(this, "Свободных номеров или тарифов сейчас нет", 1).show();
            return;
        }
        final String[] numbers = new String[nums.length()];
        for (int i = 0; i < numbers.length; i++) {
            Object o = nums.opt(i);
            numbers[i] = o instanceof org.json.JSONObject ? pickString((org.json.JSONObject) o, "phone_number", "number", "phone") : String.valueOf(o);
        }
        new AlertDialog.Builder(this).setTitle("Выберите номер").setItems(numbers, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainActivity.this.pickPackageDialog(control, provider, offer, numbers[i2]);
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    private void pickPackageDialog(final RingotelControl control, final String provider, org.json.JSONObject offer, final String phone) {
        final org.json.JSONArray packs = offer.optJSONArray("packages");
        final String[] titles = new String[packs.length()];
        for (int i = 0; i < titles.length; i++) {
            org.json.JSONObject p = packs.optJSONObject(i);
            int fee = p == null ? 0 : p.optInt("subscription_fee", 0) + p.optInt("install_fee", 0);
            titles[i] = (p == null ? "Тариф" : p.optString("package_name", "Тариф")) + (fee > 0 ? " — " + fee + " UAH" : "");
        }
        new AlertDialog.Builder(this).setTitle("Тариф для " + PhoneData.pretty(phone)).setItems(titles, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i2) {
                final org.json.JSONObject p = packs.optJSONObject(i2);
                if (p == null) {
                    return;
                }
                final String packageId = p.optString("package_id", "");
                int fee = p.optInt("subscription_fee", 0) + p.optInt("install_fee", 0);
                String bonus = p.optString("bonus", "");
                String msg = "Номер: " + PhoneData.pretty(phone) + "\nТариф: " + p.optString("package_name", "") + "\nСтоимость подключения: " + fee + " UAH" + (bonus.isEmpty() ? "" : "\nБонусы на абонплату: " + bonus + " UAH") + "\n\nДальше откроется страница оплаты. Деньги списываются только после оплаты на ней.";
                new AlertDialog.Builder(MainActivity.this).setTitle("Купить номер").setMessage(msg).setPositiveButton("Перейти к оплате", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface2, int i3) {
                        MainActivity.this.executor.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    org.json.JSONObject r = control.requestNumberPurchase(provider, phone, packageId);
                                    final String err = MainActivity.this.serverError(r);
                                    final String url = r.optString("payment_url", "");
                                    MainActivity.this.handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (err != null || url.isEmpty()) {
                                                Toast.makeText(MainActivity.this, err != null ? err : "Сервер не вернул ссылку на оплату", 1).show();
                                                return;
                                            }
                                            try {
                                                MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse(url)));
                                            } catch (Exception unused) {
                                                Toast.makeText(MainActivity.this, "Не удалось открыть страницу оплаты", 1).show();
                                            }
                                        }
                                    });
                                } catch (Exception e) {
                                    MainActivity.this.toastUi("Не удалось: " + e.getMessage());
                                }
                            }
                        });
                    }
                }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
            }
        }).setNegativeButton("Назад", (DialogInterface.OnClickListener) null).show();
    }

    // ======================= Настройки уведомлений и заблокированные =======================

    private void openChannelSettings(String channel) {
        try {
            Intent intent = new Intent("android.settings.CHANNEL_NOTIFICATION_SETTINGS");
            intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
            intent.putExtra("android.provider.extra.CHANNEL_ID", channel);
            startActivity(intent);
        } catch (Exception e) {
            try {
                Intent intent2 = new Intent("android.settings.APP_NOTIFICATION_SETTINGS");
                intent2.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
                startActivity(intent2);
            } catch (Exception unused) {
            }
        }
    }

    private LinearLayout settingRow(String title, String hint, View.OnClickListener listener) {
        LinearLayout row = column();
        row.setPadding(0, dp(10), 0, dp(10));
        row.addView(label(title, 16, TEXT, true), matchWrap());
        row.addView(label(hint, 12, MUTED, false), matchWrap());
        row.setOnClickListener(listener);
        return row;
    }

    private void renderPhoneSettings(LinearLayout linearLayout) {
        if (!this.telephonySnapshot.isProvisioned()) {
            return;
        }
        LinearLayout card = card();
        card.addView(label("Телефония: настройки", 18, BLUE_DARK, true), matchWrap());
        card.addView(settingRow("Звук и вид уведомлений о вызовах", "Мелодия, вибрация, показ на экране блокировки", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.openChannelSettings(TelephonyService.CH_CALLS);
            }
        }), matchWrap());
        card.addView(settingRow("Уведомления о сообщениях", "Звук и приоритет новых сообщений", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.openChannelSettings(TelephonyService.CH_MESSAGES);
            }
        }), matchWrap());
        card.addView(settingRow("Режим «Не беспокоить»", "Разрешить вызовам пробиваться через режим DnD", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    MainActivity.this.startActivity(new Intent("android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"));
                } catch (Exception unused) {
                }
            }
        }), matchWrap());
        card.addView(settingRow("Работа в фоне", batteryRestricted() ? "Сейчас система может останавливать приложение — отключите ограничение батареи" : "Ограничение батареи отключено", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.requestBatteryWhitelist();
            }
        }), matchWrap());
        int blockedCount = phoneData().blocked().size();
        card.addView(settingRow("Заблокированные номера (" + blockedCount + ")", "Вызовы и сообщения с этих номеров игнорируются", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.blockedDialog();
            }
        }), matchWrap());
        linearLayout.addView(card, cardMargin());
    }

    private void blockedDialog() {
        final List<String> blocked = phoneData().blocked();
        final String[] items = new String[blocked.size()];
        for (int i = 0; i < items.length; i++) {
            items[i] = PhoneData.pretty(blocked.get(i));
        }
        AlertDialog.Builder b = new AlertDialog.Builder(this).setTitle("Заблокированные номера");
        if (items.length == 0) {
            b.setMessage("Список пуст. Заблокировать номер можно кнопкой ниже или долгим нажатием на вызов в «Недавних».");
        } else {
            b.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i2) {
                    MainActivity.this.phoneData().setBlocked(blocked.get(i2), false);
                    Toast.makeText(MainActivity.this, "Номер разблокирован", 0).show();
                    MainActivity.this.showCabinet();
                }
            });
        }
        b.setPositiveButton("Заблокировать номер", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i2) {
                MainActivity.this.blockNumberDialog(null);
            }
        }).setNegativeButton("Закрыть", (DialogInterface.OnClickListener) null).show();
    }

    private void blockNumberDialog(String preset) {
        LinearLayout form = column();
        form.setPadding(dp(20), dp(8), dp(20), 0);
        final EditText number = field("Номер", 3);
        if (preset != null) {
            number.setText(preset);
        }
        form.addView(number, new LinearLayout.LayoutParams(-1, dp(52)));
        new AlertDialog.Builder(this).setTitle("Заблокировать номер").setView(form).setPositiveButton("Заблокировать", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String n = number.getText().toString().trim();
                if (n.isEmpty()) {
                    return;
                }
                MainActivity.this.phoneData().setBlocked(n, true);
                Toast.makeText(MainActivity.this, "Номер заблокирован", 0).show();
                MainActivity.this.showCabinet();
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    private void showCabinet() {
        if (this.chatNumber != null && this.selectedTab == 6) {
            showChat();
            return;
        }
        this.chatMessages = null;
        this.root.removeAllViews();
        final boolean inCall = this.selectedTab == 2 && this.telephonySnapshot != null && this.telephonySnapshot.isProvisioned() && this.telephonySnapshot.hasActiveCall();
        getWindow().setNavigationBarColor(inCall ? BLUE_DARK : BG);
        LinearLayout column = column();
        column.setFitsSystemWindows(true);
        getWindow().setStatusBarColor(inCall ? BLUE_DARK : BG);
        getWindow().getDecorView().setSystemUiVisibility(inCall ? 0 : 8192);
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(inCall ? BLUE_DARK : BG);
        column.addView(frameLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout column2 = column();
        if (inCall) {
            column2.setPadding(0, 0, 0, 0);
        } else {
            column2.setPadding(dp(16), dp(8), dp(16), dp(96));
        }
        int i = this.selectedTab;
        if ((i == 2 && !inCall) || i == 5 || i == 8) {
            commsTabs(column2);
        }
        if (i == 6) {
            LinearLayout ch = row();
            ch.setPadding(dp(2), dp(4), dp(2), dp(12));
            ch.addView(label("Сообщения", 26, TEXT, true), lp(0, -2, 1.0f));
            column2.addView(ch, matchWrap());
        }
        if (i == 0) {
            renderHome(column2);
        } else if (i == 1) {
            renderServices(column2);
        } else if (i == 2) {
            if (this.telephonySnapshot.isProvisioned() && batteryRestricted()) {
                TextView battery = warning("Чтобы входящие вызовы приходили при закрытом приложении, отключите ограничение батареи для Telecom+. Нажмите здесь.");
                battery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity.this.requestBatteryWhitelist();
                    }
                });
                LinearLayout.LayoutParams bw = matchWrap();
                bw.bottomMargin = dp(12);
                column2.addView(battery, bw);
            }
            renderPhone(column2);
        } else if (i == 3) {
            renderHistory(column2);
        } else if (i == 5) {
            renderContacts(column2);
        } else if (i == 6) {
            renderChats(column2);
        } else if (i == 7) {
            renderTariff(column2);
        } else if (i == 8) {
            renderJournal(column2);
        } else if (i == 9) {
            renderProfileAll(column2);
        } else {
            renderCabinet(column2);
        }
        scrollView.addView(column2);
        frameLayout.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));
        if (!inCall) {
            FrameLayout.LayoutParams np = new FrameLayout.LayoutParams(-1, -2);
            np.gravity = 80;
            np.setMargins(0, 0, 0, 0);
            frameLayout.addView(floatNav(), np);
        }
        this.root.addView(column, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showChecking() {
        this.root.removeAllViews();
        LinearLayout column = column();
        column.setGravity(17);
        column.setPadding(dp(24), dp(24), dp(24), dp(24));
        column.addView(new ProgressBar(this), wrap());
        TextView label = label("Проверяем сохранённую сессию…", 16, TEXT, false);
        label.setGravity(17);
        label.setPadding(0, dp(16), 0, 0);
        column.addView(label, matchWrap());
        this.root.addView(column, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showLoadFailure(String str) {
        this.root.removeAllViews();
        LinearLayout column = column();
        column.setGravity(17);
        column.setPadding(dp(28), dp(28), dp(28), dp(28));
        TextView label = label("!", 34, RED, true);
        label.setGravity(17);
        column.addView(label, new LinearLayout.LayoutParams(-1, dp(54)));
        TextView label2 = label(str, 16, TEXT, false);
        label2.setGravity(17);
        label2.setPadding(0, dp(12), 0, dp(18));
        column.addView(label2, matchWrap());
        Button primary = primary("Повторить");
        primary.setOnClickListener(new defpackage.d(this, 9));
        column.addView(primary, matchWrap());
        Button secondary = secondary("Вернуться ко входу");
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10);
        column.addView(secondary, matchWrap);
        secondary.setOnClickListener(new defpackage.d(this, 10));
        this.root.addView(column, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showLoading(String str) {
        this.root.removeAllViews();
        LinearLayout column = column();
        column.setGravity(17);
        column.setPadding(dp(24), dp(24), dp(24), dp(24));
        column.addView(new ProgressBar(this), wrap());
        TextView label = label(str, 16, TEXT, false);
        label.setGravity(17);
        label.setPadding(0, dp(16), 0, 0);
        column.addView(label, matchWrap());
        this.root.addView(column, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showMonthDialog(final boolean z) {
        String[] strArr = new String[36];
        final int[] iArr = new int[36];
        final int[] iArr2 = new int[36];
        Calendar calendar = Calendar.getInstance();
        calendar.set(5, 1);
        calendar.add(2, -1);
        int i = z ? this.fundsPeriodYear : this.callsPeriodYear;
        int i2 = z ? this.fundsPeriodMonth : this.callsPeriodMonth;
        int i3 = 0;
        for (int i4 = 0; i4 < 36; i4++) {
            iArr[i4] = calendar.get(1);
            int i5 = calendar.get(2) + 1;
            iArr2[i4] = i5;
            strArr[i4] = monthName(iArr[i4], i5);
            if (iArr[i4] == i && iArr2[i4] == i2) {
                i3 = i4;
            }
            calendar.add(2, -1);
        }
        new AlertDialog.Builder(this).setTitle("Конечный месяц").setSingleChoiceItems(strArr, i3, new DialogInterface.OnClickListener() { // from class: p
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i6) {
                MainActivity.this.lambda$showMonthDialog$66(z, iArr, iArr2, dialogInterface, i6);
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    private void showNumberLogin(String str) {
        showPasswordLogin(str);
    }

    private void legacyShowNumberLogin(String str) {
        int i = 1;
        clearPortalOtpUi(true);
        stopOtpTimer();
        this.root.removeAllViews();
        ScrollView scrollView = new ScrollView(this);
        LinearLayout column = column();
        column.setGravity(1);
        column.setPadding(dp(22), dp(38), dp(22), dp(32));
        column.addView(logo(), new LinearLayout.LayoutParams(dp(78), dp(78)));
        TextView label = label("Telecom+", 34, BLUE, true);
        label.setGravity(17);
        label.setPadding(0, dp(14), 0, 0);
        column.addView(label, matchWrap());
        int i2 = MUTED;
        TextView label2 = label("Нативный личный кабинет", 16, i2, false);
        label2.setGravity(17);
        label2.setPadding(0, dp(4), 0, dp(24));
        column.addView(label2, matchWrap());
        LinearLayout card = card();
        card.addView(label("Вход по номеру", 24, BLUE_DARK, true), matchWrap());
        TextView label3 = label("Введите 9 цифр номера без начального нуля. Портал выполнит короткий входящий звонок.", 15, i2, false);
        label3.setPadding(0, dp(10), 0, dp(16));
        card.addView(label3, matchWrap());
        EditText field = field("944000000", 3);
        field.setText(this.phone);
        field.setTextSize(20.0f);
        card.addView(field, new LinearLayout.LayoutParams(-1, dp(58)));
        if (str != null && !str.trim().isEmpty()) {
            TextView label4 = label(str, 14, RED, false);
            label4.setPadding(0, dp(12), 0, 0);
            card.addView(label4, matchWrap());
        }
        Button primary = primary("Получить одноразовый пароль");
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(18);
        card.addView(primary, matchWrap);
        configureAuthGuard(primary, "Получить одноразовый пароль");
        primary.setOnClickListener(new defpackage.c(this, field, primary, i));
        Button secondary = secondary("Войти по логину и паролю");
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(10);
        card.addView(secondary, matchWrap2);
        secondary.setOnClickListener(new defpackage.h(this, field, i));
        column.addView(card, matchWrap());
        TextView label5 = label("Личный пароль и одноразовый код не сохраняются.", 13, i2, false);
        label5.setGravity(17);
        label5.setPadding(dp(8), dp(20), dp(8), 0);
        column.addView(label5, matchWrap());
        scrollView.addView(column, new FrameLayout.LayoutParams(-1, -2));
        this.root.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));
    }

    private void showOtpLogin(String str) {
        clearPortalOtpUi(false);
        LinearLayout card = authScreen(3);
        card.addView(label("Введите код", 24, TEXT, true), matchWrap());
        String hint = this.otpHint.isEmpty() ? "На номер +380" + maskPhone(this.phone) + " поступит короткий звонок. Введите последние четыре цифры номера, с которого звонят." : this.otpHint;
        TextView sub = label(hint, 14, MUTED, false);
        sub.setPadding(0, dp(8), 0, dp(18));
        card.addView(sub, matchWrap());
        EditText field = field("••••", 18);
        field.setTextSize(30.0f);
        field.setGravity(17);
        field.setLetterSpacing(0.4f);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        card.addView(field, new LinearLayout.LayoutParams(-1, dp(66)));
        TextView timer = label("", 13, MUTED, false);
        timer.setGravity(17);
        timer.setPadding(0, dp(12), 0, 0);
        card.addView(timer, matchWrap());
        if (str != null && !str.trim().isEmpty()) {
            TextView err = label(str, 14, RED, false);
            err.setGravity(17);
            err.setPadding(0, dp(8), 0, 0);
            card.addView(err, matchWrap());
        }
        Button primary = primary("Войти");
        LinearLayout.LayoutParams pp = lp(-1, dp(54), 0);
        pp.topMargin = dp(16);
        card.addView(primary, pp);
        configureAuthGuard(primary, "Войти");
        this.activePortalOtpField = field;
        this.activePortalOtpConfirm = primary;
        primary.setOnClickListener(new defpackage.c(this, field, primary, 0));
        Button again = secondary("Получить код ещё раз");
        LinearLayout.LayoutParams ap = lp(-1, dp(50), 0);
        ap.topMargin = dp(10);
        card.addView(again, ap);
        again.setOnClickListener(new defpackage.e(this, again, 0));
        TextView back = label("Изменить номер или пароль", 14, BLUE, true);
        back.setGravity(17);
        back.setPadding(0, dp(16), 0, dp(4));
        card.addView(back, matchWrap());
        back.setOnClickListener(new defpackage.d(this, 1));
        startOtpTimer(timer, again);
        consumeRecentPortalOtp();
    }

    private LinearLayout authScreen(int step) {
        this.root.removeAllViews();
        getWindow().setStatusBarColor(BLUE_DARK);
        getWindow().getDecorView().setSystemUiVisibility(0);
        getWindow().setNavigationBarColor(BG);
        FrameLayout fl = new FrameLayout(this);
        fl.setBackgroundColor(BG);
        View hero = new View(this);
        hero.setBackground(gradient(BLUE_DARK, BLUE, 40));
        FrameLayout.LayoutParams hp = new FrameLayout.LayoutParams(-1, dp(330));
        hp.topMargin = dp(-70);
        fl.addView(hero, hp);
        View c1 = new View(this);
        c1.setBackground(rounded(Color.argb(30, 255, 203, 0), 90, 0, 0));
        FrameLayout.LayoutParams c1p = new FrameLayout.LayoutParams(dp(180), dp(180));
        c1p.gravity = 53;
        c1p.rightMargin = dp(-60);
        c1p.topMargin = dp(-30);
        fl.addView(c1, c1p);
        ScrollView sv = new ScrollView(this);
        sv.setFillViewport(true);
        LinearLayout col = column();
        col.setGravity(1);
        col.setPadding(dp(20), dp(48), dp(20), dp(28));
        col.addView(logo(), new LinearLayout.LayoutParams(dp(68), dp(68)));
        LinearLayout nm = row();
        nm.setGravity(17);
        nm.setPadding(0, dp(12), 0, 0);
        nm.addView(label("Telecom", 30, -1, true), wrap());
        nm.addView(label("+", 30, YELLOW, true), wrap());
        col.addView(nm, matchWrap());
        TextView tg = label("Личный кабинет Intertelecom", 13, Color.rgb(190, 212, 250), false);
        tg.setGravity(17);
        col.addView(tg, matchWrap());
        LinearLayout card = column();
        card.setPadding(dp(22), dp(20), dp(22), dp(22));
        card.setBackground(rounded(-1, 28, BORDER, 1));
        card.setElevation(dp(8));
        LinearLayout steps = row();
        steps.setPadding(0, 0, 0, dp(18));
        for (int i = 1; i <= 3; i++) {
            View b = new View(this);
            b.setBackground(rounded(i <= step ? YELLOW : Color.rgb(228, 237, 249), 3, 0, 0));
            LinearLayout.LayoutParams bp = lp(0, dp(5), 1.0f);
            if (i > 1) {
                bp.leftMargin = dp(6);
            }
            steps.addView(b, bp);
        }
        card.addView(steps, matchWrap());
        LinearLayout.LayoutParams cp = matchWrap();
        cp.topMargin = dp(26);
        col.addView(card, cp);
        TextView note = label("Личный пароль не сохраняется. Приложение хранит только зашифрованную серверную сессию.", 12, MUTED, false);
        note.setGravity(17);
        note.setPadding(dp(8), dp(18), dp(8), 0);
        col.addView(note, matchWrap());
        sv.addView(col, new FrameLayout.LayoutParams(-1, -2));
        fl.addView(sv, new FrameLayout.LayoutParams(-1, -1));
        this.root.addView(fl, new FrameLayout.LayoutParams(-1, -1));
        card.setAlpha(0.0f);
        card.animate().alpha(1.0f).setDuration(220).start();
        return card;
    }

    private Runnable authCancel;

    private void showAuthCheck(final String digits, final String password) {
        LinearLayout card = authScreen(2);
        card.addView(label("Проверка безопасности", 24, TEXT, true), matchWrap());
        TextView sub = label("Отметьте галочку Cloudflare, если она появилась. Проверка может пройти автоматически.", 14, MUTED, false);
        sub.setPadding(0, dp(8), 0, dp(12));
        card.addView(sub, matchWrap());
        FrameLayout box = new FrameLayout(this);
        card.addView(box, new LinearLayout.LayoutParams(-1, dp(90)));
        final TextView status = label("", 14, MUTED, false);
        status.setGravity(17);
        status.setPadding(0, dp(8), 0, 0);
        card.addView(status, matchWrap());
        TextView back = label("Назад", 14, BLUE, true);
        back.setGravity(17);
        back.setPadding(0, dp(14), 0, dp(4));
        card.addView(back, matchWrap());
        final String[] sk = {this.turnstileKey};
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.this.authCancel != null) {
                    MainActivity.this.authCancel.run();
                    MainActivity.this.authCancel = null;
                }
                MainActivity.this.showPasswordLogin(null);
            }
        });
        this.authCancel = TurnstileSolver.solveEmbedded(this, box, this.turnstileKey, PortalLoginFlow.ACTION_PASSWORD, new TurnstileSolver.Callback() {
            @Override
            public void onToken(final String token) {
                MainActivity.this.authCancel = null;
                status.setText("Входим…");
                MainActivity.this.runPortal(new Callable<PortalResult<PortalLoginFlow.Challenge>>() {
                    @Override
                    public PortalResult<PortalLoginFlow.Challenge> call() {
                        return MainActivity.this.gateway.loginFlow().submitCredentials(digits, password, token);
                    }
                }, new PortalCallback<PortalLoginFlow.Challenge>() {
                    @Override
                    public void accept(PortalResult<PortalLoginFlow.Challenge> portalResult2) {
                        MainActivity.this.onPortalStepResult(portalResult2);
                    }
                });
            }

            @Override
            public void onFailure(String message) {
                MainActivity.this.authCancel = null;
                MainActivity.this.showPasswordLogin(message);
            }
        });
    }

    private String turnstileKey = "";

    private void showPasswordLogin(String str) {
        clearPortalOtpUi(true);
        stopOtpTimer();
        LinearLayout card = authScreen(1);
        card.addView(label("Вход в кабинет", 24, TEXT, true), matchWrap());
        TextView sub = label("Номер без нуля (9 цифр) или идентификатор и пароль от личного кабинета.", 14, MUTED, false);
        sub.setPadding(0, dp(8), 0, dp(18));
        card.addView(sub, matchWrap());
        final EditText field = field("Номер или идентификатор", 3);
        field.setText(this.phone);
        field.setTextSize(18.0f);
        card.addView(field, new LinearLayout.LayoutParams(-1, dp(56)));
        markAutofill(field, "username");
        final EditText field2 = field("Пароль", 129);
        markAutofill(field2, "password");
        field2.setTextSize(18.0f);
        LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(-1, dp(56));
        fp.topMargin = dp(12);
        card.addView(field2, fp);
        if (str != null && !str.trim().isEmpty()) {
            TextView err = label(str, 14, RED, false);
            err.setPadding(0, dp(12), 0, 0);
            card.addView(err, matchWrap());
        }
        final Button primary = primary("Далее");
        LinearLayout.LayoutParams pp = lp(-1, dp(54), 0);
        pp.topMargin = dp(18);
        card.addView(primary, pp);
        configureAuthGuard(primary, "Далее");
        primary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.lambda$showPasswordLogin$14(field, field2, primary, view);
            }
        });
    }

    private void showRangeDialog(final boolean z) {
        final int[] iArr = {1, 5, 6, 12};
        String[] strArr = {"1 месяц", "5 месяцев", "6 месяцев", "1 год"};
        int i = z ? this.fundsRangeMonths : this.callsRangeMonths;
        int i2 = 0;
        for (int i3 = 0; i3 < 4; i3++) {
            if (iArr[i3] == i) {
                i2 = i3;
            }
        }
        new AlertDialog.Builder(this).setTitle("Глубина истории").setSingleChoiceItems(strArr, i2, new DialogInterface.OnClickListener() { // from class: j
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i4) {
                MainActivity.this.lambda$showRangeDialog$67(z, iArr, dialogInterface, i4);
            }
        }).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showTelephonyResult(TelephonyResult<?> telephonyResult) {
        if (telephonyResult instanceof TelephonyResult.Failure) {
            new AlertDialog.Builder(this).setTitle("Телефония").setMessage(((TelephonyResult.Failure) telephonyResult).message).setPositiveButton("Понятно", (DialogInterface.OnClickListener) null).show();
            return;
        }
        Toast.makeText(this, "Операция телефонии выполнена", 0).show();
        if (this.dashboard == null || this.selectedTab != 2) {
            return;
        }
        showCabinet();
    }

    private void startDomFallbackIfNeeded() {
        String sessionCookieHeader;
        if (this.webAuth == null || this.domFallbackRunning || !this.gateway.hasSavedSession()) {
            return;
        }
        FundsFlowSnapshot fundsFlowSnapshot = this.fundsFlow;
        int i = 0;
        boolean z = fundsFlowSnapshot == null || fundsFlowSnapshot.transactions.isEmpty();
        CallHistorySnapshot callHistorySnapshot = this.callHistory;
        boolean z2 = callHistorySnapshot == null || callHistorySnapshot.calls.isEmpty();
        if ((!z && !z2) || (sessionCookieHeader = this.gateway.getSessionCookieHeader()) == null || sessionCookieHeader.trim().isEmpty()) {
            return;
        }
        this.domFallbackRunning = true;
        loadFundsDomIfNeeded(z, sessionCookieHeader, new defpackage.l(this, z2, i));
    }

    private void startOtpTimer(final TextView textView, final Button button) {
        stopOtpTimer();
        Runnable runnable = new Runnable() { // from class: ua.telecomplus.app.MainActivity.2
            @Override // java.lang.Runnable
            public void run() {
                String str;
                textView.setText(MainActivity.this.otpSeconds > 0 ? String.format("Код действителен ещё %02d:%02d", Integer.valueOf(Math.max(0, MainActivity.this.otpSeconds) / 60), Integer.valueOf(Math.max(0, MainActivity.this.otpSeconds) % 60)) : "Срок действия кода истёк");
                long max = Math.max(MainActivity.this.authBlockedSeconds(), Math.max(0L, ((MainActivity.this.otpRepeatAllowedAt - System.currentTimeMillis()) + 999) / 1000));
                button.setEnabled(max <= 0);
                Button button2 = button;
                if (max > 0) {
                    str = "Повторить звонок через " + MainActivity.formatDuration(max);
                } else {
                    str = "Повторить короткий звонок";
                }
                button2.setText(str);
                if (MainActivity.this.otpSeconds > 0) {
                    MainActivity.this.otpSeconds--;
                }
                MainActivity.this.handler.postDelayed(this, 1000L);
            }
        };
        this.otpTick = runnable;
        this.handler.post(runnable);
    }

    private View statTile(String str, String str2, String str3, int i, int i2) {
        LinearLayout colorCard = colorCard(i2, 0, 20);
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon(str3, -1, i, 34), new LinearLayout.LayoutParams(dp(34), dp(34)));
        TextView label = label(str, 12, MUTED, false);
        label.setPadding(dp(9), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1.0f));
        colorCard.addView(row, matchWrap());
        TextView label2 = label(str2, 17, BLUE_DARK, true);
        label2.setMaxLines(2);
        label2.setPadding(0, dp(12), 0, 0);
        colorCard.addView(label2, matchWrap());
        return colorCard;
    }

    private void stopAuthGuardTimer() {
        Runnable runnable = this.authGuardTick;
        if (runnable != null) {
            this.handler.removeCallbacks(runnable);
        }
        this.authGuardTick = null;
    }

    private void stopOtpTimer() {
        Runnable runnable = this.otpTick;
        if (runnable != null) {
            this.handler.removeCallbacks(runnable);
        }
        this.otpTick = null;
    }

    private void submitPortalOtp(EditText editText, Button button) {
        int i = 0;
        if (this.otpSeconds <= 0) {
            this.portalOtpAutoSubmitInFlight = false;
            showOtpLogin("Срок действия одноразового пароля истёк. Запросите новый короткий звонок.");
            return;
        }
        String digits = digits(editText.getText().toString());
        if (digits.length() != 4) {
            this.portalOtpAutoSubmitInFlight = false;
            Toast.makeText(this, "Введите ровно четыре цифры", 0).show();
            return;
        }
        button.setEnabled(false);
        button.setText("Проверяем…");
        final String code = digits;
        runPortal(new Callable<PortalResult<Void>>() {
            @Override
            public PortalResult<Void> call() {
                return MainActivity.this.gateway.loginFlow().confirm(code);
            }
        }, new PortalCallback<Void>() {
            @Override
            public void accept(PortalResult<Void> portalResult) {
                MainActivity.this.portalOtpAutoSubmitInFlight = false;
                if (portalResult instanceof PortalResult.Success) {
                    MainActivity.this.clearAuthGuard();
                    MainActivity.this.enterCabinet();
                } else {
                    String str = ((PortalResult.Failure) portalResult).message;
                    MainActivity.this.registerAuthGuard(str);
                    MainActivity.this.showOtpLogin(str);
                }
            }
        });
    }

    private String tabTitle() {
        int i = this.selectedTab;
        return i == 0 ? "Главная" : i == 1 ? "Услуги" : i == 2 ? "Телефон" : i == 3 ? this.historyMode == 0 ? "История средств" : "История звонков" : i == 5 ? "Контакты" : i == 6 ? "Чаты" : "Профиль";
    }

    private static String telephonyStatusTitle(TelephonySnapshot telephonySnapshot) {
        if (telephonySnapshot == null || telephonySnapshot != null) {
            return "Звонки через IT Phone";
        }
        if (telephonySnapshot == null) {
            return "Не активирован";
        }
        switch (telephonySnapshot.registrationState) {
            case OTP_REQUESTED:
                return "Ожидает код входа";
            case PROVISIONED:
                return "Профиль активирован";
            case CONNECTING:
                return "Подключение…";
            case ONLINE:
                return telephonySnapshot.hasActiveCall() ? "Идёт вызов" : "На связи";
            case OFFLINE:
                return "Нет связи с сервером";
            case ERROR:
                return "Ошибка";
            default:
                return "Не активирован";
        }
    }

    private View topBar() {
        String str;
        LinearLayout row = row();
        row.setGravity(16);
        row.setPadding(dp(16), dp(14), dp(14), dp(18));
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{BLUE_DARK, BLUE});
        bg.setCornerRadii(new float[]{0, 0, 0, 0, dp(26), dp(26), dp(26), dp(26)});
        row.setBackground(bg);
        row.setElevation(dp(4));
        View logo = logo();
        row.addView(logo, new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout column = column();
        column.setPadding(dp(12), 0, dp(8), 0);
        column.addView(label(tabTitle(), 21, -1, true), matchWrap());
        if (this.dashboard == null) {
            str = "Telecom+";
        } else {
            str = formatInternationalPhone(this.dashboard.phoneNumber) + " · " + this.dashboard.status;
        }
        column.addView(label(str, 13, Color.rgb(200, 222, 255), false), matchWrap());
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView refresh = new Ico.CircleView(this, Ico.get(this, R.drawable.ic_refresh, -1));
        refresh.setBackground(rounded(Color.argb(40, 255, 255, 255), 24, 0, 0));
        refresh.setContentDescription("Обновить");
        refresh.setOnClickListener(new defpackage.d(this, 14));
        row.addView(refresh, new LinearLayout.LayoutParams(dp(46), dp(46)));
        return row;
    }

    private void unavailable(String str) {
        new AlertDialog.Builder(this).setTitle("Функция временно защищена").setMessage(str).setPositiveButton("Понятно", (DialogInterface.OnClickListener) null).show();
    }

    private TextView warning(String str) {
        TextView label = label(str, 14, TEXT, false);
        label.setPadding(dp(14), dp(12), dp(14), dp(12));
        label.setBackground(rounded(AMBER_BG, 12, Color.rgb(238, 207, 104), 1));
        return label;
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(-2, -2);
    }

    @Override // android.app.Activity
    public void onActivityResult(int i, int i2, Intent intent) {
        Uri data;
        super.onActivityResult(i, i2, intent);
        if (i != REQUEST_SAVE_DIAGNOSTIC || i2 != -1 || intent == null || (data = intent.getData()) == null) {
            return;
        }
        try {
            if (this.pendingDiagnosticReport.trim().isEmpty()) {
                return;
            }
            OutputStream openOutputStream = getContentResolver().openOutputStream(data);
            try {
                if (openOutputStream == null) {
                    throw new IllegalStateException("Не удалось открыть выбранный файл");
                }
                openOutputStream.write(this.pendingDiagnosticReport.getBytes(StandardCharsets.UTF_8));
                openOutputStream.flush();
                Toast.makeText(this, "Диагностический отчёт сохранён", 0).show();
                openOutputStream.close();
            } catch (Throwable th) {
                if (openOutputStream != null) {
                    try {
                        openOutputStream.close();
                    } catch (Throwable th2) {
                        th.addSuppressed(th2);
                    }
                }
                throw th;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось сохранить отчёт: " + e.getMessage(), 1).show();
        } finally {
            this.pendingDiagnosticReport = "";
        }
    }

    @Override // android.app.Activity
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setStatusBarColor(BLUE_DARK);
        getWindow().setNavigationBarColor(-1);
        TelecomPlusApplication telecomPlusApplication = (TelecomPlusApplication) getApplication();
        this.gateway = telecomPlusApplication.gateway;
        this.telephony = telecomPlusApplication.telephony;
        this.appRoot = new FrameLayout(this);
        FrameLayout frameLayout = new FrameLayout(this);
        this.root = frameLayout;
        frameLayout.setBackgroundColor(BG);
        this.appRoot.addView(this.root, new FrameLayout.LayoutParams(-1, -1));
        this.webAuth = new PortalWebAuthBridge(this, this.appRoot);
        initializePeriodDefaults();
        setContentView(this.appRoot);
        registerOtpCallReceiver();
        this.telephonySnapshot = this.telephony.snapshot();
        this.telephony.setListener(new a(this, 9));
        if (this.telephonySnapshot.isProvisioned()) {
            runTelephony(new Callable<TelephonyResult<Void>>() {
                @Override
                public TelephonyResult<Void> call() {
                    return MainActivity.this.telephony.connect();
                }
            }, new TelephonyCallback<Void>() {
                @Override
                public void accept(TelephonyResult<Void> telephonyResult) {
                }
            });
        }
        phoneData().addListener(this.dataListener);
        ensureTelephonyService();
        beginStartup();
        handleLaunchIntent(getIntent());
    }

    @Override // android.app.Activity
    public void onDestroy() {
        phoneData().removeListener(this.dataListener);
        clearPortalOtpUi(true);
        stopOtpTimer();
        stopAuthGuardTimer();
        TelephonyEngine telephonyEngine = this.telephony;
        if (telephonyEngine != null) {
            telephonyEngine.setListener(null);
        }
        if (this.otpReceiverRegistered) {
            unregisterReceiver(this.otpCallReceiver);
            this.otpReceiverRegistered = false;
        }
        this.executor.shutdownNow();
        PortalWebAuthBridge portalWebAuthBridge = this.webAuth;
        if (portalWebAuthBridge != null) {
            portalWebAuthBridge.destroy();
        }
        super.onDestroy();
    }

    @Override // android.app.Activity
    public void onPause() {
        TelecomPlusApplication.foreground = false;
        super.onPause();
    }

    @Override // android.app.Activity
    public void onResume() {
        super.onResume();
        TelecomPlusApplication.foreground = true;
        consumeRecentPortalOtp();
    }

    private int serviceFilter;
    private String serviceQuery = "";

    private LinearLayout.LayoutParams lp(int w, int h, float weight) {
        return weight > 0 ? new LinearLayout.LayoutParams(w, h, weight) : new LinearLayout.LayoutParams(w, h);
    }

    private String greeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return h < 6 ? "Доброй ночи" : h < 12 ? "Доброе утро" : h < 18 ? "Добрый день" : "Добрый вечер";
    }

    private View roundRefresh() {
        TextView refresh = new Ico.CircleView(this, Ico.get(this, R.drawable.ic_refresh, BLUE_DARK));
        refresh.setBackground(rounded(-1, 23, BORDER, 1));
        refresh.setContentDescription("Обновить");
        refresh.setOnClickListener(new defpackage.d(this, 14));
        return refresh;
    }

    private View pageHeader(String title, String sub) {
        LinearLayout row = row();
        row.setGravity(16);
        row.setPadding(dp(4), dp(6), dp(2), dp(14));
        LinearLayout col = column();
        col.addView(label(title, 30, BLUE_DARK, true), matchWrap());
        if (sub != null && !sub.isEmpty()) {
            col.addView(label(sub, 13, MUTED, false), matchWrap());
        }
        row.addView(col, lp(0, -2, 1.0f));
        row.addView(roundRefresh(), lp(dp(46), dp(46), 0));
        return row;
    }

    private View.OnClickListener tabClick(final int tab, final int mode) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.chatNumber = null;
                MainActivity.this.selectedTab = tab;
                if (tab == 3) {
                    MainActivity.this.historyMode = mode;
                }
                MainActivity.this.showCabinet();
            }
        };
    }

    private View segment(String name, boolean active, View.OnClickListener l) {
        TextView t = label(name, 14, active ? BLUE_DARK : MUTED, true);
        t.setGravity(17);
        t.setBackground(active ? rounded(YELLOW, 20, 0, 0) : null);
        t.setOnClickListener(l);
        return t;
    }

    private void commsTabs(LinearLayout parent) {
        LinearLayout hd = row();
        hd.setGravity(16);
        hd.setPadding(dp(2), dp(4), dp(2), dp(12));
        LinearLayout hc = column();
        hc.addView(label("Телефон", 26, TEXT, true), matchWrap());
        hc.addView(label(telephonyStatusTitle(this.telephonySnapshot), 12, MUTED, false), matchWrap());
        hd.addView(hc, lp(0, -2, 1.0f));
        parent.addView(hd, matchWrap());
        LinearLayout tabs = row();
        tabs.setPadding(dp(4), dp(4), dp(4), dp(4));
        tabs.setBackground(rounded(Color.rgb(228, 237, 249), 20, 0, 0));
        String[] names = {"Набор", "Журнал", "Контакты"};
        final int[] ids = {2, 8, 5};
        for (int i = 0; i < 3; i++) {
            boolean act = this.selectedTab == ids[i];
            TextView sg = (TextView) segment(names[i], act, tabClick(ids[i], 0));
            sg.setBackground(act ? rounded(BLUE, 16, 0, 0) : null);
            sg.setTextColor(act ? -1 : TEXT);
            tabs.addView(sg, lp(0, dp(38), 1.0f));
        }
        LinearLayout.LayoutParams tp = matchWrap();
        tp.bottomMargin = dp(12);
        parent.addView(tabs, tp);
    }

    private View floatNav() {
        LinearLayout row = row();
        row.setGravity(16);
        row.setBackgroundColor(-1);
        row.setElevation(dp(10));
        row.setPadding(dp(4), dp(8), dp(4), dp(8));
        String[] names = {"Главная", "Телефон", "Сообщения", "Кабинет"};
        int[] icons = {R.drawable.ic_home, R.drawable.ic_phone, R.drawable.ic_chat, R.drawable.ic_user};
        int[] tabs = {0, 2, 6, 4};
        int unread = phoneData().unreadTotal();
        for (int i = 0; i < 4; i++) {
            int tab = tabs[i];
            int st = this.selectedTab;
            boolean active = tab == 0 ? st == 0 : tab == 2 ? (st == 2 || st == 5 || st == 8) : tab == 6 ? st == 6 : (st == 4 || st == 1 || st == 3 || st == 7 || st == 9);
            LinearLayout item = column();
            item.setGravity(1);
            FrameLayout iw = new FrameLayout(this);
            if (active) {
                iw.setBackground(rounded(Color.rgb(228, 239, 255), 14, 0, 0));
            }
            View ic = new Ico.PlainView(this, Ico.get(this, icons[i], active ? Color.rgb(0, 79, 215) : Color.rgb(52, 73, 109)), 0.56f);
            iw.addView(ic, new FrameLayout.LayoutParams(dp(54), dp(34), 17));
            if (tab == 6 && unread > 0) {
                TextView b = label(String.valueOf(unread), 10, -1, true);
                b.setGravity(17);
                b.setBackground(rounded(RED, 8, 0, 0));
                FrameLayout.LayoutParams bp = new FrameLayout.LayoutParams(dp(18), dp(16));
                bp.leftMargin = dp(32);
                iw.addView(b, bp);
            }
            item.addView(iw, lp(dp(54), dp(34), 0));
            TextView nl = label(names[i], 11, active ? Color.rgb(0, 79, 215) : Color.rgb(52, 73, 109), active);
            nl.setGravity(17);
            nl.setPadding(0, dp(3), 0, 0);
            item.addView(nl, matchWrap());
            item.setOnClickListener(tabClick(tab, 0));
            row.addView(item, lp(0, -2, 1.0f));
        }
        return row;
    }

    private View miniTile(String name, String value, String glyph, View.OnClickListener l) {
        LinearLayout c = column();
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackground(rounded(-1, 24, BORDER, 1));
        LinearLayout h = row();
        h.setGravity(16);
        h.addView(circleIcon(glyph, BLUE, SOFT_BLUE, 30), lp(dp(30), dp(30), 0));
        TextView t = label(name, 12, MUTED, false);
        t.setPadding(dp(8), 0, 0, 0);
        h.addView(t, lp(0, -2, 1.0f));
        c.addView(h, matchWrap());
        c.addView(new View(this), lp(1, 0, 1.0f));
        TextView v = label(value, 16, BLUE_DARK, true);
        v.setMaxLines(2);
        c.addView(v, matchWrap());
        c.setOnClickListener(l);
        return c;
    }

    private View miniStat(String name, String value) {
        LinearLayout c = column();
        c.setPadding(dp(14), dp(12), dp(14), dp(12));
        c.setBackground(rounded(-1, 22, BORDER, 1));
        c.addView(label(name, 11, MUTED, false), matchWrap());
        TextView v = label(displayValue(value), 15, BLUE_DARK, true);
        v.setMaxLines(2);
        v.setPadding(0, dp(4), 0, 0);
        c.addView(v, matchWrap());
        return c;
    }

    private View txRow(FundsTransaction tx) {
        int color = tx.isCredit() ? GREEN : tx.isDebit() ? RED : BLUE_DARK;
        int soft = tx.isCredit() ? Color.rgb(226, 246, 235) : tx.isDebit() ? Color.rgb(255, 232, 232) : SOFT_BLUE;
        LinearLayout row = row();
        row.setGravity(16);
        row.setPadding(0, dp(11), 0, dp(11));
        row.addView(circleIcon(tx.isCredit() ? "+" : tx.isDebit() ? "−" : "₴", color, soft, 38), lp(dp(38), dp(38), 0));
        LinearLayout col = column();
        col.setPadding(dp(12), 0, dp(8), 0);
        TextView d = label(displayValue(tx.description), 15, TEXT, true);
        d.setMaxLines(2);
        col.addView(d, matchWrap());
        col.addView(label(displayValue(tx.dateTime), 12, MUTED, false), matchWrap());
        row.addView(col, lp(0, -2, 1.0f));
        TextView a = label(displayValue(tx.amount), 15, color, true);
        a.setGravity(8388613);
        row.addView(a, wrap());
        return row;
    }

    private View serviceRow(final ServiceItem it) {
        boolean on = it.isConnected();
        LinearLayout row = row();
        row.setGravity(16);
        row.setPadding(0, dp(12), 0, dp(12));
        row.addView(circleIcon(on ? "✓" : "+", on ? -1 : BLUE, on ? GREEN : SOFT_BLUE, 40), lp(dp(40), dp(40), 0));
        LinearLayout col = column();
        col.setPadding(dp(12), 0, dp(8), 0);
        TextView t = label(displayValue(it.title), 15, BLUE_DARK, true);
        t.setMaxLines(2);
        col.addView(t, matchWrap());
        col.addView(label("Абонплата: " + displayValue(it.fee), 12, MUTED, false), matchWrap());
        row.addView(col, lp(0, -2, 1.0f));
        FrameLayout tg = new FrameLayout(this);
        tg.setBackground(rounded(on ? YELLOW : Color.rgb(214, 224, 240), 14, 0, 0));
        View kn = new View(this);
        kn.setBackground(rounded(-1, 11, 0, 0));
        FrameLayout.LayoutParams knp = new FrameLayout.LayoutParams(dp(22), dp(22));
        knp.gravity = on ? 21 : 19;
        knp.leftMargin = dp(3);
        knp.rightMargin = dp(3);
        tg.addView(kn, knp);
        row.addView(tg, lp(dp(48), dp(28), 0));
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.lambda$serviceCard$62(it, view);
            }
        });
        return row;
    }

    private static double num(String s) {
        if (s == null) {
            return 0;
        }
        Matcher m = Pattern.compile("(\\d[\\d\\s]*[.,]?\\d*)").matcher(s);
        if (!m.find()) {
            return 0;
        }
        try {
            return Double.parseDouble(m.group(1).replace(" ", "").replace(',', '.'));
        } catch (Exception e) {
            return 0;
        }
    }

    private View barLine(String name, String value, double frac, int color) {
        LinearLayout col = column();
        col.setPadding(0, dp(14), 0, 0);
        LinearLayout head = row();
        head.addView(label(name, 13, Color.rgb(190, 212, 250), false), lp(0, -2, 1.0f));
        head.addView(label(value, 14, -1, true), wrap());
        col.addView(head, matchWrap());
        LinearLayout track = row();
        track.setBackground(rounded(Color.argb(45, 255, 255, 255), 5, 0, 0));
        View fill = new View(this);
        fill.setBackground(rounded(color, 5, 0, 0));
        int w = (int) Math.max(3, Math.min(100, Math.round(frac * 100)));
        track.addView(fill, new LinearLayout.LayoutParams(0, dp(8), (float) w));
        track.addView(new View(this), new LinearLayout.LayoutParams(0, dp(8), (float) (100 - w)));
        LinearLayout.LayoutParams tp = lp(-1, dp(8), 0);
        tp.topMargin = dp(6);
        col.addView(track, tp);
        return col;
    }

    private void renderFundsNew(LinearLayout parent) {
        FundsFlowSnapshot f = this.fundsFlow;
        if (f == null) {
            parent.addView(info("Не удалось получить движение средств. Нажмите обновить для повторной загрузки."), matchWrap());
            return;
        }
        if (this.showingCachedData) {
            parent.addView(warning("Показана сохранённая детализация от " + DateFormat.getDateTimeInstance(3, 3).format(new Date(f.updatedAt)) + "."), matchWrap());
        }
        LinearLayout sum = column();
        sum.setPadding(dp(22), dp(20), dp(22), dp(22));
        sum.setBackground(gradient(BLUE_DARK, BLUE, 30));
        sum.addView(label(displayValue(f.periodLabel), 13, Color.rgb(190, 212, 250), false), matchWrap());
        String close = (!"—".equals(f.closingBalance) || this.dashboard == null) ? f.closingBalance : this.dashboard.balance;
        TextView cl = label(displayValue(close), 38, -1, true);
        cl.setPadding(0, dp(2), 0, 0);
        sum.addView(cl, matchWrap());
        sum.addView(label("баланс на конец периода", 12, Color.rgb(190, 212, 250), false), matchWrap());
        double cr = num(f.creditsTotal);
        double db = num(f.debitsTotal);
        double mx = Math.max(1, Math.max(cr, db));
        sum.addView(barLine("Поступления", displayValue(f.creditsTotal), cr / mx, Color.rgb(90, 220, 150)), matchWrap());
        sum.addView(barLine("Списания", displayValue(f.debitsTotal), db / mx, YELLOW), matchWrap());
        parent.addView(sum, matchWrap());
        parent.addView(periodSelectorCard(true), cardMargin());
        LinearLayout chips = row();
        chips.setPadding(0, dp(16), 0, dp(2));
        chips.addView(fundsFilterButton("Все", 0), lp(0, dp(42), 1.0f));
        LinearLayout.LayoutParams l1 = lp(0, dp(42), 1.0f);
        l1.leftMargin = dp(7);
        chips.addView(fundsFilterButton("Списания", 1), l1);
        LinearLayout.LayoutParams l2 = lp(0, dp(42), 1.0f);
        l2.leftMargin = dp(7);
        chips.addView(fundsFilterButton("Пополнения", 2), l2);
        parent.addView(chips, matchWrap());
        String last = "";
        LinearLayout cur = null;
        int count = 0;
        for (FundsTransaction tx : f.transactions) {
            if (!matchesFundsFilter(tx)) {
                continue;
            }
            String g = dateGroup(tx.dateTime);
            if (cur == null || !g.equals(last)) {
                TextView h = label(g, 13, MUTED, true);
                h.setPadding(dp(6), dp(18), 0, dp(8));
                parent.addView(h, matchWrap());
                cur = card();
                cur.setPadding(dp(16), dp(4), dp(16), dp(4));
                parent.addView(cur, matchWrap());
                last = g;
            } else {
                cur.addView(profileDivider(), lp(-1, dp(1), 0));
            }
            cur.addView(txRow(tx), matchWrap());
            count++;
        }
        if (count == 0) {
            parent.addView(info(f.transactions.isEmpty() ? "За выбранный период портал не вернул операций." : "В выбранной категории операций нет."), cardMargin());
        }
    }

    private View subHeader(String title, final int backTab) {
        LinearLayout row = row();
        row.setGravity(16);
        row.setPadding(0, dp(4), 0, dp(12));
        TextView back = label("‹", 34, TEXT, false);
        back.setPadding(dp(4), 0, dp(14), dp(4));
        back.setOnClickListener(tabClick(backTab, 0));
        row.addView(back, wrap());
        row.addView(label(title, 21, TEXT, true), lp(0, -2, 1.0f));
        row.addView(roundRefresh(), lp(dp(44), dp(44), 0));
        return row;
    }

    private View menuRow(String glyph, String title, String sub, View.OnClickListener l) {
        LinearLayout r = row();
        r.setGravity(16);
        r.setPadding(0, dp(12), 0, dp(12));
        r.addView(circleIcon(glyph, Color.rgb(228, 169, 0), Color.rgb(255, 245, 191), 40), lp(dp(40), dp(40), 0));
        LinearLayout c = column();
        c.setPadding(dp(12), 0, dp(8), 0);
        c.addView(label(title, 15, TEXT, true), matchWrap());
        if (sub != null) {
            c.addView(label(sub, 12, MUTED, false), matchWrap());
        }
        r.addView(c, lp(0, -2, 1.0f));
        r.addView(label("›", 24, MUTED, false), wrap());
        r.setOnClickListener(l);
        return r;
    }

    private void renderCabinet(LinearLayout parent) {
        DashboardSnapshot d = this.dashboard;
        LinearLayout hd = row();
        hd.setGravity(16);
        hd.setPadding(dp(2), dp(4), dp(2), dp(12));
        hd.addView(label("Кабинет", 26, TEXT, true), lp(0, -2, 1.0f));
        hd.addView(roundRefresh(), lp(dp(44), dp(44), 0));
        parent.addView(hd, matchWrap());
        LinearLayout me = row();
        me.setGravity(16);
        me.setPadding(dp(14), dp(14), dp(14), dp(14));
        me.setBackground(rounded(-1, 20, BORDER, 1));
        me.addView(circleIcon("☺", Color.rgb(228, 169, 0), Color.rgb(255, 245, 191), 46), lp(dp(46), dp(46), 0));
        LinearLayout mc = column();
        mc.setPadding(dp(12), 0, 0, 0);
        mc.addView(label("Мой номер", 12, MUTED, false), matchWrap());
        mc.addView(label(d == null ? "—" : formatInternationalPhone(d.phoneNumber), 17, TEXT, true), matchWrap());
        me.addView(mc, lp(0, -2, 1.0f));
        me.addView(label("›", 26, TEXT, false), wrap());
        me.setOnClickListener(tabClick(9, 0));
        parent.addView(me, matchWrap());
        LinearLayout c1 = card();
        c1.setPadding(dp(14), dp(2), dp(14), dp(2));
        c1.addView(menuRow("₴", "Платежи и пополнения", "История платежей и движение средств", tabClick(3, 0)), matchWrap());
        c1.addView(profileDivider(), lp(-1, dp(1), 0));
        c1.addView(menuRow("▣", "Расходы и детализация", "Звонки, интернет, SMS", tabClick(3, 1)), matchWrap());
        c1.addView(profileDivider(), lp(-1, dp(1), 0));
        c1.addView(menuRow("★", "Мой тариф", "Пакет услуг и условия", tabClick(7, 0)), matchWrap());
        c1.addView(profileDivider(), lp(-1, dp(1), 0));
        c1.addView(menuRow("☷", "Услуги", "Подключение и управление", tabClick(1, 0)), matchWrap());
        c1.addView(profileDivider(), lp(-1, dp(1), 0));
        c1.addView(menuRow("☺", "Профиль", "Личные данные, номера, телефония", tabClick(9, 0)), matchWrap());
        LinearLayout.LayoutParams m1 = matchWrap();
        m1.topMargin = dp(14);
        parent.addView(c1, m1);
        TextView dh = label("Дополнительно", 17, TEXT, true);
        dh.setPadding(dp(2), dp(18), 0, dp(10));
        parent.addView(dh, matchWrap());
        LinearLayout c2 = card();
        c2.setPadding(dp(14), dp(2), dp(14), dp(2));
        c2.addView(menuRow("☎", "Телефон Telecom+", telephonyStatusTitle(this.telephonySnapshot), tabClick(2, 0)), matchWrap());
        c2.addView(profileDivider(), lp(-1, dp(1), 0));
        c2.addView(menuRow("?", "Поддержка", "Сайт оператора", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    MainActivity.this.startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://intertelecom.ua")));
                } catch (Exception e) {
                }
            }
        }), matchWrap());
        parent.addView(c2, matchWrap());
    }

    private void renderTariff(LinearLayout parent) {
        DashboardSnapshot d = this.dashboard;
        parent.addView(subHeader("Мой тариф", 4), matchWrap());
        if (d == null) {
            return;
        }
        FrameLayout hero = new FrameLayout(this);
        hero.setBackground(gradient(Color.rgb(221, 236, 255), Color.rgb(246, 250, 255), 22));
        View sw = new View(this);
        sw.setBackground(rounded(YELLOW, 40, 0, 0));
        FrameLayout.LayoutParams swp = new FrameLayout.LayoutParams(dp(150), dp(80));
        swp.gravity = 85;
        swp.rightMargin = dp(-40);
        swp.bottomMargin = dp(-40);
        hero.addView(sw, swp);
        LinearLayout hr = row();
        hr.setGravity(16);
        hr.setPadding(dp(16), dp(18), dp(16), dp(18));
        hr.addView(circleIcon("★", Color.rgb(228, 169, 0), Color.rgb(255, 245, 191), 54), lp(dp(54), dp(54), 0));
        LinearLayout hc = column();
        hc.setPadding(dp(14), 0, 0, 0);
        hc.addView(label(displayValue(d.tariffName), 22, TEXT, true), matchWrap());
        hc.addView(label(displayValue(d.status), 13, MUTED, false), matchWrap());
        hr.addView(hc, lp(0, -2, 1.0f));
        hero.addView(hr, new FrameLayout.LayoutParams(-1, -2));
        parent.addView(hero, matchWrap());
        TextView ph = label("Ваш пакет услуг", 17, TEXT, true);
        ph.setPadding(dp(2), dp(18), 0, dp(10));
        parent.addView(ph, matchWrap());
        LinearLayout pk = card();
        pk.setPadding(dp(16), dp(4), dp(16), dp(4));
        if (d.packages.isEmpty()) {
            pk.addView(label("Портал не вернул остатков пакетов", 14, MUTED, false), matchWrap());
        }
        int i = 0;
        for (PackageBalance pb : d.packages) {
            if (i > 0) {
                pk.addView(profileDivider(), lp(-1, dp(1), 0));
            }
            LinearLayout r = column();
            r.setPadding(0, dp(12), 0, dp(12));
            LinearLayout rr = row();
            rr.setGravity(16);
            TextView t = label(pb.title, 15, TEXT, true);
            rr.addView(t, lp(0, -2, 1.0f));
            rr.addView(label(pb.progressPercent + "%", 13, BLUE, true), wrap());
            r.addView(rr, matchWrap());
            r.addView(label(displayValue(pb.subtitle), 12, MUTED, false), matchWrap());
            LinearLayout track = row();
            track.setBackground(rounded(Color.rgb(228, 237, 249), 4, 0, 0));
            View f = new View(this);
            f.setBackground(rounded(YELLOW, 4, 0, 0));
            int w = Math.max(4, Math.min(100, pb.progressPercent));
            track.addView(f, new LinearLayout.LayoutParams(0, dp(6), (float) w));
            track.addView(new View(this), new LinearLayout.LayoutParams(0, dp(6), (float) (100 - w)));
            LinearLayout.LayoutParams tl = lp(-1, dp(6), 0);
            tl.topMargin = dp(8);
            r.addView(track, tl);
            pk.addView(r, matchWrap());
            i++;
        }
        parent.addView(pk, matchWrap());
        LinearLayout info = card();
        info.setPadding(dp(16), dp(4), dp(16), dp(4));
        info.addView(kv("Номер", formatInternationalPhone(d.phoneNumber)), matchWrap());
        info.addView(profileDivider(), lp(-1, dp(1), 0));
        info.addView(kv("Исходящие за период", displayValue(d.outgoingSummary)), matchWrap());
        if (d.profile != null) {
            info.addView(profileDivider(), lp(-1, dp(1), 0));
            info.addView(kv("Действителен до", displayValue(d.profile.validUntil)), matchWrap());
            info.addView(profileDivider(), lp(-1, dp(1), 0));
            info.addView(kv("Подключён", displayValue(d.profile.connectionDate)), matchWrap());
        }
        TextView ih = label("Управление тарифом", 17, TEXT, true);
        ih.setPadding(dp(2), dp(18), 0, dp(10));
        parent.addView(ih, matchWrap());
        parent.addView(info, matchWrap());
        Button svc = primary("Услуги и подключения");
        svc.setOnClickListener(tabClick(1, 0));
        LinearLayout.LayoutParams sp = lp(-1, dp(52), 0);
        sp.topMargin = dp(14);
        parent.addView(svc, sp);
    }

    private View kv(String k, String v) {
        LinearLayout r = row();
        r.setGravity(16);
        r.setPadding(0, dp(14), 0, dp(14));
        r.addView(label(k, 14, MUTED, false), lp(0, -2, 1.0f));
        TextView t = label(v, 14, TEXT, true);
        t.setGravity(8388613);
        r.addView(t, wrap());
        return r;
    }

    private void renderProfileAll(LinearLayout parent) {
        parent.addView(subHeader("Профиль", 4), matchWrap());
        renderProfile(parent);
        renderNumbers(parent);
        renderPhoneSettings(parent);
    }

    private void renderJournal(LinearLayout parent) {
        List<PhoneData.CallEntry> calls = phoneData().calls();
        LinearLayout seg = row();
        seg.setPadding(dp(4), dp(4), dp(4), dp(4));
        seg.setBackground(rounded(Color.rgb(228, 237, 249), 20, 0, 0));
        String[] names = {"Все", "Входящие", "Исходящие", "Пропущ."};
        for (int i = 0; i < 4; i++) {
            final int f = i;
            View sg = segment(names[i], this.callFilter == i, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.callFilter = f;
                    MainActivity.this.showCabinet();
                }
            });
            sg.setBackground(this.callFilter == i ? rounded(BLUE, 16, 0, 0) : null);
            ((TextView) sg).setTextColor(this.callFilter == i ? -1 : TEXT);
            seg.addView(sg, lp(0, dp(36), 1.0f));
        }
        LinearLayout.LayoutParams sgp = matchWrap();
        sgp.bottomMargin = dp(8);
        parent.addView(seg, sgp);
        String last = "";
        LinearLayout cur = null;
        int shown = 0;
        for (int idx = 0; idx < Math.min(calls.size(), 120); idx++) {
            final PhoneData.CallEntry e = calls.get(idx);
            if (this.callFilter == 1 && e.dir != PhoneData.CALL_IN) {
                continue;
            }
            if (this.callFilter == 2 && e.dir != PhoneData.CALL_OUT) {
                continue;
            }
            if (this.callFilter == 3 && e.dir != PhoneData.CALL_MISSED) {
                continue;
            }
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(e.time);
            Calendar now = Calendar.getInstance();
            String g;
            if (c.get(Calendar.YEAR) == now.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)) {
                g = "Сегодня";
            } else if (c.get(Calendar.YEAR) == now.get(Calendar.YEAR) && c.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR) - 1) {
                g = "Вчера";
            } else {
                g = new java.text.SimpleDateFormat("d MMMM", new Locale("ru")).format(new Date(e.time));
            }
            if (cur == null || !g.equals(last)) {
                TextView h = label(g, 12, MUTED, true);
                h.setPadding(dp(4), dp(14), 0, dp(6));
                parent.addView(h, matchWrap());
                cur = card();
                cur.setPadding(dp(14), dp(2), dp(14), dp(2));
                parent.addView(cur, matchWrap());
                last = g;
            } else {
                cur.addView(profileDivider(), lp(-1, dp(1), 0));
            }
            LinearLayout row = row();
            row.setGravity(16);
            row.setPadding(0, dp(12), 0, dp(12));
            boolean miss = e.dir == PhoneData.CALL_MISSED;
            String gl = e.dir == PhoneData.CALL_IN ? "↙" : e.dir == PhoneData.CALL_OUT ? "↗" : "✕";
            int col = miss ? RED : GREEN;
            row.addView(label(gl, 18, col, true), lp(dp(26), -2, 0));
            LinearLayout tc = column();
            String title = phoneData().title(e.number);
            tc.addView(label(title, 15, miss ? RED : TEXT, true), matchWrap());
            tc.addView(label(displayValue(e.number) + (e.duration > 0 ? " · " + formatDuration(e.duration) : ""), 12, MUTED, false), matchWrap());
            row.addView(tc, lp(0, -2, 1.0f));
            row.addView(label(timeLabel(e.time), 11, MUTED, false), wrap());
            TextView call = new Ico.CircleView(this, Ico.get(this, R.drawable.ic_phone, BLUE));
            LinearLayout.LayoutParams cl = lp(dp(36), dp(36), 0);
            cl.leftMargin = dp(8);
            call.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    MainActivity.this.dialNumber(e.number);
                }
            });
            row.addView(call, cl);
            row.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    MainActivity.this.blockNumberDialog(e.number);
                    return true;
                }
            });
            cur.addView(row, matchWrap());
            shown++;
        }
        if (shown == 0) {
            parent.addView(info("Вызовов пока нет"), matchWrap());
        }
    }
    private int callFilter;

    private void markAutofill(EditText e, String hint) {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            e.setImportantForAutofill(1);
            e.setAutofillHints(new String[]{hint});
        }
        e.setContentDescription(hint.equals("password") ? "Пароль" : "Логин");
        e.setTag(hint);
    }

    private static int usageColor(int remaining) {
        float h = 120.0f * Math.max(0, Math.min(100, remaining)) / 100.0f;
        return Color.HSVToColor(new float[]{h, 0.78f, 0.86f});
    }

    private static String shortValue(String raw) {
        String t = raw == null ? "" : raw;
        String low = t.toLowerCase(Locale.ROOT);
        if (low.contains("неогранич") || low.contains("необмеж")) {
            return "Безлимит";
        }
        t = t.replaceAll("(\\d+)\\s*час\\S*", "$1 ч").replaceAll("(\\d+)\\s*минут\\S*", "$1 мин").replaceAll("(\\d+)\\s*секунд\\S*", "$1 с");
        if (t.contains(" ч ") || t.contains(" мин")) {
            t = t.replaceAll("\\s*\\d+\\s*с\\b", "");
        }
        return t.trim();
    }

    private View pkgTile(PackageBalance pb) {
        String sub = displayValue(pb.subtitle);
        String date = "";
        String value = sub;
        Matcher m = Pattern.compile("^(.*?)\\s+(?:по|до)\\s+(\\d{2}\\.\\d{2}\\.\\d{4})\\s*$").matcher(sub);
        if (m.find()) {
            value = m.group(1);
            date = "до " + m.group(2);
        }
        boolean unlimited = sub.toLowerCase(Locale.ROOT).contains("неогранич") || sub.toLowerCase(Locale.ROOT).contains("необмеж");
        int pct = unlimited ? 100 : pb.progressPercent;
        int color = usageColor(pct);
        String tl = pb.title.toLowerCase(Locale.ROOT);
        int icon = (tl.contains("интернет") || tl.contains("трафик") || tl.contains("гб")) ? R.drawable.ic_globe : (tl.contains("sms") || tl.contains("смс")) ? R.drawable.ic_mail : R.drawable.ic_phone;
        LinearLayout t = column();
        t.setPadding(dp(14), dp(14), dp(14), dp(16));
        t.setBackground(rounded(-1, 20, BORDER, 1));
        FrameLayout ib = new FrameLayout(this);
        ib.setBackground(rounded(Color.argb(40, Color.red(color), Color.green(color), Color.blue(color)), 18, 0, 0));
        ib.addView(new Ico.PlainView(this, Ico.get(this, icon, color), 0.6f), new FrameLayout.LayoutParams(dp(36), dp(36)));
        t.addView(ib, lp(dp(36), dp(36), 0));
        TextView title = label(pb.title, 13, MUTED, true);
        title.setMinLines(2);
        title.setMaxLines(2);
        title.setPadding(0, dp(10), 0, dp(4));
        t.addView(title, matchWrap());
        TextView val = label(shortValue(value), 22, TEXT, true);
        val.setSingleLine(true);
        t.addView(val, matchWrap());
        TextView dt = label(date.isEmpty() ? " " : date, 12, MUTED, false);
        dt.setSingleLine(true);
        t.addView(dt, matchWrap());
        t.addView(new View(this), lp(1, 0, 1.0f));
        LinearLayout track = row();
        track.setBackground(rounded(Color.rgb(228, 237, 249), 5, 0, 0));
        int w = pct <= 0 ? 0 : Math.max(4, Math.min(100, pct));
        if (w > 0) {
            View f = new View(this);
            f.setBackground(rounded(color, 5, 0, 0));
            track.addView(f, new LinearLayout.LayoutParams(0, dp(8), (float) w));
        }
        if (w < 100) {
            track.addView(new View(this), new LinearLayout.LayoutParams(0, dp(8), (float) (100 - w)));
        }
        LinearLayout.LayoutParams tp = lp(-1, dp(8), 0);
        tp.topMargin = dp(12);
        t.addView(track, tp);
        TextView pl = label(unlimited ? "без ограничений" : "осталось " + pct + "%", 11, color, true);
        pl.setPadding(0, dp(6), 0, 0);
        t.addView(pl, matchWrap());
        return t;
    }
}
