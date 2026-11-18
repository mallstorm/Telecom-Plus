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
    private static final int BLUE = Color.rgb(22, 101, 216);
    private static final int BLUE_DARK = Color.rgb(8, 47, 106);
    private static final int CYAN = Color.rgb(14, 177, 202);
    private static final int PURPLE = Color.rgb(105, 82, 218);
    private static final int ORANGE = Color.rgb(245, 139, 49);
    private static final int BG = Color.rgb(243, 246, 252);
    private static final int TEXT = Color.rgb(35, 43, 54);
    private static final int MUTED = Color.rgb(105, 116, 130);
    private static final int GREEN = Color.rgb(39, 155, 83);
    private static final int RED = Color.rgb(190, 45, 45);
    private static final int BORDER = Color.rgb(219, 226, 237);
    private static final int SOFT_BLUE = Color.rgb(232, 242, 255);
    private static final int SOFT_CYAN = Color.rgb(228, 248, 250);
    private static final int SOFT_PURPLE = Color.rgb(239, 235, 255);
    private static final int SOFT_ORANGE = Color.rgb(255, 243, 229);
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
        row.setPadding(dp(5), dp(5), dp(5), dp(5));
        String[] strArr = {"Главная", "Услуги", "Телефон", "История", "Профиль"};
        String[] strArr2 = {"⌂", "☷", "☎", "◷", "●"};
        int i = 0;
        int i2 = 0;
        while (i2 < 5) {
            LinearLayout column = column();
            column.setGravity(17);
            column.setPadding(dp(2), dp(3), dp(2), dp(3));
            TextView label = label(strArr2[i2], 22, this.selectedTab == i2 ? -1 : MUTED, true);
            label.setGravity(17);
            column.addView(label, matchWrap());
            String str = strArr[i2];
            int i3 = this.selectedTab;
            TextView label2 = label(str, 10, i3 == i2 ? -1 : MUTED, i3 == i2);
            label2.setGravity(17);
            column.addView(label2, matchWrap());
            column.setBackground(this.selectedTab == i2 ? gradient(BLUE, CYAN, 16) : null);
            column.setOnClickListener(new defpackage.f(this, i2, i));
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, -1, 1.0f);
            layoutParams.setMargins(dp(2), dp(2), dp(2), dp(2));
            row.addView(column, layoutParams);
            i2++;
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
        button.setBackground(rounded(i2, 12, i2 == 0 ? 0 : BORDER, i2 == 0 ? 0 : 1));
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
        column.setBackground(rounded(-1, 18, BORDER, 1));
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
        TextView label = label(str, Math.max(12, i3 / 3), i, true);
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

    private static String displayValue(String str) {
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
        Button button = button(str, i3 != 0 ? -1 : BLUE_DARK, i3 != 0 ? i == 0 ? BLUE : ORANGE : 0);
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
        runTelephony(new defpackage.m(this, editText, 3), new a(this, 5));
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
    public /* synthetic */ void lambda$showOtpLogin$22(Button button, View view) {
        markPortalOtpRequested();
        int i = 0;
        button.setEnabled(false);
        button.setText("Запрашиваем звонок…");
        PortalWebAuthBridge portalWebAuthBridge = this.webAuth;
        if (portalWebAuthBridge != null) {
            portalWebAuthBridge.requestOtp(this.phone, new a(this, 1));
        } else {
            runPortal(new defpackage.b(this, i), new a(this, 2));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$showOtpLogin$23(View view) {
        PortalWebAuthBridge portalWebAuthBridge = this.webAuth;
        if (portalWebAuthBridge != null) {
            portalWebAuthBridge.resetAuth();
        }
        showNumberLogin(null);
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
        final String digits = digits(editText.getText().toString());
        final String obj = editText2.getText().toString();
        this.phone = digits;
        button.setEnabled(false);
        button.setText("Входим…");
        PortalWebAuthBridge portalWebAuthBridge = this.webAuth;
        if (portalWebAuthBridge != null) {
            portalWebAuthBridge.loginWithPassword(digits, obj, new b(this, digits, 2));
        } else {
            runPortal(new Callable() { // from class: k
                @Override // java.util.concurrent.Callable
                public final Object call() {
                    PortalResult lambda$showPasswordLogin$12;
                    lambda$showPasswordLogin$12 = MainActivity.this.lambda$showPasswordLogin$12(digits, obj);
                    return lambda$showPasswordLogin$12;
                }
            }, new a(this, 4));
        }
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
        int[] iArr = {BLUE, CYAN, PURPLE, ORANGE, GREEN};
        int[] iArr2 = {SOFT_BLUE, SOFT_CYAN, SOFT_PURPLE, SOFT_ORANGE, Color.rgb(232, 249, 238)};
        int i2 = iArr[Math.abs(i) % 5];
        LinearLayout colorCard = colorCard(iArr2[Math.abs(i) % 5], 0, 20);
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon("P", i2, -1, 40), new LinearLayout.LayoutParams(dp(40), dp(40)));
        LinearLayout column = column();
        column.setPadding(dp(12), 0, 0, 0);
        column.addView(label(packageBalance.title, 17, BLUE_DARK, true), matchWrap());
        column.addView(label(packageBalance.subtitle, 13, MUTED, false), matchWrap());
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        row.addView(chip(packageBalance.progressPercent + "%", i2, -1), wrap());
        colorCard.addView(row, matchWrap());
        ProgressBar progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setProgress(packageBalance.progressPercent);
        progressBar.setProgressTintList(ColorStateList.valueOf(i2));
        progressBar.setProgressBackgroundTintList(ColorStateList.valueOf(Color.argb(45, 0, 0, 0)));
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

    private void renderHistory(LinearLayout linearLayout) {
        LinearLayout row = row();
        row.setPadding(dp(4), dp(4), dp(4), dp(4));
        row.setBackground(rounded(-1, 16, BORDER, 1));
        row.addView(historyModeButton("Средства", 0), new LinearLayout.LayoutParams(0, dp(46), 1.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(46), 1.0f);
        layoutParams.leftMargin = dp(6);
        row.addView(historyModeButton("Звонки", 1), layoutParams);
        linearLayout.addView(row, matchWrap());
        if (this.historyMode == 0) {
            renderFunds(linearLayout);
        } else {
            renderCalls(linearLayout);
        }
    }

    private void renderHome(LinearLayout linearLayout) {
        String str;
        DashboardSnapshot dashboardSnapshot = this.dashboard;
        if (dashboardSnapshot == null) {
            return;
        }
        if (this.showingCachedData) {
            StringBuilder sb = new StringBuilder("Показаны сохранённые данные от ");
            sb.append(DateFormat.getDateTimeInstance(3, 3).format(new Date(dashboardSnapshot.updatedAt)));
            sb.append(". Обновление не выполнено");
            if (this.refreshWarning.isEmpty()) {
                str = ".";
            } else {
                str = ": " + this.refreshWarning;
            }
            sb.append(str);
            linearLayout.addView(warning(sb.toString()), matchWrap());
        }
        LinearLayout column = column();
        column.setPadding(dp(20), dp(20), dp(20), dp(20));
        int i = BLUE_DARK;
        column.setBackground(gradient(i, Color.rgb(28, 133, 222), 24));
        LinearLayout row = row();
        row.setGravity(16);
        LinearLayout column2 = column();
        int i2 = 0;
        column2.addView(label("Ваш номер", 12, Color.rgb(205, 228, 255), false), matchWrap());
        column2.addView(label(dashboardSnapshot.phoneNumber, 21, -1, true), matchWrap());
        row.addView(column2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        row.addView(chip(displayValue(dashboardSnapshot.status), -1, Color.argb(42, 255, 255, 255)), wrap());
        column.addView(row, matchWrap());
        TextView label = label("Текущий баланс", 13, Color.rgb(205, 228, 255), false);
        label.setPadding(0, dp(22), 0, dp(2));
        column.addView(label, matchWrap());
        column.addView(label(dashboardSnapshot.balance, 38, -1, true), matchWrap());
        TextView label2 = label("Доступно для перевода: " + displayValue(dashboardSnapshot.transferableBalance), 13, Color.rgb(220, 238, 255), false);
        label2.setPadding(0, dp(5), 0, dp(16));
        column.addView(label2, matchWrap());
        LinearLayout row2 = row();
        Button glassButton = glassButton("Пополнить");
        Button glassButton2 = glassButton("История средств");
        glassButton.setOnClickListener(new defpackage.d(this, 2));
        glassButton2.setOnClickListener(new defpackage.d(this, 3));
        row2.addView(glassButton, new LinearLayout.LayoutParams(0, dp(46), 1.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(46), 1.0f);
        layoutParams.leftMargin = dp(9);
        row2.addView(glassButton2, layoutParams);
        column.addView(row2, matchWrap());
        linearLayout.addView(column, matchWrap());
        linearLayout.addView(sectionTitle("Краткая сводка", "Основные данные вашего номера"), sectionMargin());
        LinearLayout row3 = row();
        row3.addView(statTile("Тариф", displayValue(dashboardSnapshot.tariffName), "▣", PURPLE, SOFT_PURPLE), new LinearLayout.LayoutParams(0, dp(126), 1.0f));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(126), 1.0f);
        layoutParams2.leftMargin = dp(10);
        View statTile = statTile("Исходящие", displayValue(dashboardSnapshot.outgoingSummary), "◷", ORANGE, SOFT_ORANGE);
        statTile.setOnClickListener(new defpackage.d(this, 4));
        row3.addView(statTile, layoutParams2);
        linearLayout.addView(row3, matchWrap());
        LinearLayout colorCard = colorCard(SOFT_CYAN, Color.rgb(190, 232, 236), 20);
        LinearLayout row4 = row();
        row4.setGravity(16);
        int i3 = CYAN;
        row4.addView(circleIcon("☷", i3, -1, 44), new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout column3 = column();
        column3.setPadding(dp(12), 0, 0, 0);
        column3.addView(label("Услуги и опции", 17, i, true), matchWrap());
        StringBuilder sb2 = new StringBuilder("Подключено и доступно: ");
        List<ServiceItem> list = this.services;
        sb2.append(list == null ? 0 : list.size());
        String sb3 = sb2.toString();
        int i4 = MUTED;
        column3.addView(label(sb3, 12, i4, false), matchWrap());
        row4.addView(column3, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView label3 = label("›", 30, i3, true);
        label3.setGravity(17);
        row4.addView(label3, wrap());
        colorCard.addView(row4, matchWrap());
        colorCard.setOnClickListener(new defpackage.d(this, 5));
        linearLayout.addView(colorCard, cardMargin());
        LinearLayout colorCard2 = colorCard(Color.rgb(232, 249, 238), Color.rgb(190, 231, 203), 20);
        LinearLayout row5 = row();
        row5.setGravity(16);
        int i5 = GREEN;
        row5.addView(circleIcon("☎", -1, i5, 44), new LinearLayout.LayoutParams(dp(44), dp(44)));
        LinearLayout column4 = column();
        column4.setPadding(dp(12), 0, 0, 0);
        column4.addView(label("Телефон Telecom+", 17, i, true), matchWrap());
        column4.addView(label(telephonyStatusTitle(this.telephonySnapshot), 12, i4, false), matchWrap());
        row5.addView(column4, new LinearLayout.LayoutParams(0, -2, 1.0f));
        TextView label4 = label("›", 30, i5, true);
        label4.setGravity(17);
        row5.addView(label4, wrap());
        colorCard2.addView(row5, matchWrap());
        colorCard2.setOnClickListener(new defpackage.d(this, 6));
        linearLayout.addView(colorCard2, cardMargin());
        linearLayout.addView(sectionTitle("Пакеты", "Остатки включённых минут и услуг"), sectionMargin());
        if (dashboardSnapshot.packages.isEmpty()) {
            linearLayout.addView(info("Пакетные остатки не найдены в ответе портала."), matchWrap());
        }
        Iterator<PackageBalance> it = dashboardSnapshot.packages.iterator();
        while (it.hasNext()) {
            linearLayout.addView(packageCard(it.next(), i2), cardMargin());
            i2++;
        }
    }

    private void renderPhone(LinearLayout linearLayout) {
        String str;
        TelephonySnapshot telephonySnapshot0 = this.telephonySnapshot;
        if (telephonySnapshot0 == null) {
            telephonySnapshot0 = TelephonySnapshot.unprovisioned();
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
        linearLayout.addView(column, matchWrap());
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
            }
            linearLayout.addView(card, cardMargin());
            linearLayout.addView(info("Телефонный пароль после активации хранится только в Android Keystore в зашифрованном виде."), cardMargin());
            return;
        }
        SubscriberProfile subscriberProfile2 = dashboardSnapshot == null ? new SubscriberProfile() : dashboardSnapshot.profile;
        DashboardSnapshot dashboardSnapshot3 = this.dashboard;
        String presentValue4 = dashboardSnapshot3 != null ? presentValue(dashboardSnapshot3.phoneNumber) : "";
        if (presentValue4.isEmpty()) {
            presentValue4 = presentValue(subscriberProfile2.phoneNumber);
        }
        String presentValue5 = presentValue(subscriberProfile2.itPhoneId);
        if (presentValue5.isEmpty()) {
            presentValue5 = presentValue(telephonySnapshot.displayName);
        }
        LinearLayout colorCard = colorCard(SOFT_CYAN, Color.rgb(190, 232, 236), 20);
        int i5 = BLUE_DARK;
        colorCard.addView(label("Профиль IT Phone", 18, i5, true), matchWrap());
        if (!presentValue5.isEmpty()) {
            colorCard.addView(label("ID пользователя: ".concat(presentValue5), 14, TEXT, true), matchWrap());
        }
        if (!presentValue4.isEmpty()) {
            colorCard.addView(label("Привязанный номер: " + formatInternationalPhone(presentValue4), 14, TEXT, false), matchWrap());
        }
        colorCard.addView(label("Кодеки профиля: " + joinCodecs(telephonySnapshot.supportedCodecs), 12, MUTED, false), matchWrap());
        linearLayout.addView(colorCard, cardMargin());
        LinearLayout card2 = card();
        card2.addView(label("Набор номера", 20, i5, true), matchWrap());
        EditText field3 = field("Номер абонента", 3);
        field3.setTextSize(23.0f);
        field3.setGravity(17);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-1, dp(62));
        layoutParams2.topMargin = dp(12);
        card2.addView(field3, layoutParams2);
        String[][] strArr = {new String[]{"1", "2", "3"}, new String[]{"4", "5", "6"}, new String[]{"7", "8", "9"}, new String[]{"*", "0", "#"}};
        int i6 = 0;
        while (i6 < 4) {
            String[] strArr2 = strArr[i6];
            LinearLayout row2 = row();
            LinearLayout.LayoutParams matchWrap4 = matchWrap();
            matchWrap4.topMargin = dp(i3);
            int i7 = i2;
            while (i7 < strArr2.length) {
                String str3 = strArr2[i7];
                Button button = button(str3, BLUE_DARK, Color.rgb(242, 247, 253));
                button.setTextSize(23.0f);
                button.setOnClickListener(new defpackage.e(field3, str3, 1));
                LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(0, dp(54), 1.0f);
                if (i7 > 0) {
                    layoutParams3.leftMargin = dp(8);
                }
                row2.addView(button, layoutParams3);
                i7++;
                i3 = 8;
            }
            card2.addView(row2, matchWrap4);
            i6++;
            i2 = 0;
        }
        LinearLayout row3 = row();
        Button secondary2 = secondary("⌫");
        secondary2.setOnClickListener(new defpackage.d(field3, 16));
        Button button2 = button("Позвонить", -1, GREEN);
        button2.setTypeface(Typeface.DEFAULT, 1);
        button2.setOnClickListener(new defpackage.h(this, field3, 2));
        LinearLayout.LayoutParams matchWrap5 = matchWrap();
        matchWrap5.topMargin = dp(12);
        row3.addView(secondary2, new LinearLayout.LayoutParams(0, dp(52), 1.0f));
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(0, dp(52), 2.0f);
        layoutParams4.leftMargin = dp(9);
        row3.addView(button2, layoutParams4);
        card2.addView(row3, matchWrap5);
        linearLayout.addView(card2, cardMargin());
        linearLayout.addView(warning("Профиль и серверная активация работают в Live. Голосовой медиамодуль ещё закрыт feature-gate: до добавления проверенных ABI-библиотек DSP/Opus/G.722/G.729A приложение явно отклоняет звонок, а не показывает ложное соединение."), cardMargin());
        View secondary3 = secondary("Проверить подключение телефонии");
        secondary3.setOnClickListener(new defpackage.d(this, 11));
        linearLayout.addView(secondary3, cardMargin());
        View secondary4 = secondary("Открыть историю звонков");
        secondary4.setOnClickListener(new defpackage.d(this, 12));
        linearLayout.addView(secondary4, cardMargin());
    }

    private void renderProfile(LinearLayout linearLayout) {
        DashboardSnapshot dashboardSnapshot = this.dashboard;
        SubscriberProfile subscriberProfile = dashboardSnapshot == null ? new SubscriberProfile() : dashboardSnapshot.profile;
        LinearLayout column = column();
        column.setPadding(dp(20), dp(20), dp(20), dp(20));
        int i = PURPLE;
        int i2 = BLUE;
        column.setBackground(gradient(i, i2, 24));
        LinearLayout row = row();
        row.setGravity(16);
        row.addView(circleIcon("T+", -1, Color.argb(40, 255, 255, 255), 58), new LinearLayout.LayoutParams(dp(58), dp(58)));
        LinearLayout column2 = column();
        column2.setPadding(dp(14), 0, 0, 0);
        column2.addView(label(displayValue(subscriberProfile.phoneNumber), 22, -1, true), matchWrap());
        column2.addView(label(displayValue(subscriberProfile.registrationStatus), 13, Color.rgb(226, 224, 255), false), matchWrap());
        row.addView(column2, new LinearLayout.LayoutParams(0, -2, 1.0f));
        column.addView(row, matchWrap());
        TextView label = label(displayValue(subscriberProfile.tariffName), 16, -1, true);
        label.setPadding(0, dp(18), 0, 0);
        column.addView(label, matchWrap());
        column.addView(label("Личный кабинет абонента", 12, Color.rgb(226, 224, 255), false), matchWrap());
        linearLayout.addView(column, matchWrap());
        linearLayout.addView(sectionTitle("Основные данные", "Информация лицевого счёта и номера"), sectionMargin());
        LinearLayout card = card();
        String combineProfileValue = combineProfileValue(subscriberProfile.accountId, subscriberProfile.contractStatus);
        int i3 = SOFT_BLUE;
        card.addView(profileInfoRow("№", "Лицевой счёт", combineProfileValue, i2, i3), matchWrap());
        card.addView(profileDivider(), matchWrap());
        card.addView(profileInfoRow("SIM", "Номер и регистрация", combineProfileValue(subscriberProfile.phoneNumber, subscriberProfile.registrationStatus), CYAN, SOFT_CYAN), matchWrap());
        card.addView(profileDivider(), matchWrap());
        String str = subscriberProfile.tariffName;
        int i4 = SOFT_PURPLE;
        card.addView(profileInfoRow("TP", "Тарифный план", str, i, i4), matchWrap());
        card.addView(profileDivider(), matchWrap());
        String str2 = subscriberProfile.itPhoneId;
        int i5 = ORANGE;
        card.addView(profileInfoRow("IT", "ITPhone ID", str2, i5, SOFT_ORANGE), matchWrap());
        linearLayout.addView(card, matchWrap());
        linearLayout.addView(sectionTitle("Контакты", "Данные для связи и уведомлений"), sectionMargin());
        LinearLayout card2 = card();
        String str3 = subscriberProfile.contactPhone;
        int i6 = GREEN;
        card2.addView(profileInfoRow("☎", "Контактный номер", str3, i6, Color.rgb(232, 249, 238)), matchWrap());
        card2.addView(profileDivider(), matchWrap());
        card2.addView(profileInfoRow("@", "Электронная почта", subscriberProfile.email, i2, i3), matchWrap());
        linearLayout.addView(card2, matchWrap());
        linearLayout.addView(sectionTitle("История номера", "Срок обслуживания и лояльность"), sectionMargin());
        LinearLayout row2 = row();
        row2.addView(statTile("Подключён", displayValue(subscriberProfile.connectionDate), "●", i6, Color.rgb(232, 249, 238)), new LinearLayout.LayoutParams(0, dp(118), 1.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(118), 1.0f);
        layoutParams.leftMargin = dp(10);
        row2.addView(statTile("Действителен до", displayValue(subscriberProfile.validUntil), "✓", i2, i3), layoutParams);
        linearLayout.addView(row2, matchWrap());
        LinearLayout colorCard = colorCard(i4, Color.rgb(218, 207, 250), 20);
        colorCard.addView(profileInfoRow("★", "Абонентский стаж", subscriberProfile.subscriberTenure, i, -1), matchWrap());
        colorCard.addView(profileDivider(), matchWrap());
        colorCard.addView(profileInfoRow("◆", "Лояльный стаж", subscriberProfile.loyaltyTenure, i5, -1), matchWrap());
        linearLayout.addView(colorCard, cardMargin());
        linearLayout.addView(sectionTitle("Управление аккаунтом", "Сессия и данные на этом устройстве"), sectionMargin());
        LinearLayout card3 = card();
        int i7 = MUTED;
        TextView label2 = label("Telecom+ хранит только серверную сессию. Личный пароль и одноразовый код не сохраняются.", 13, i7, false);
        label2.setPadding(0, 0, 0, dp(14));
        card3.addView(label2, matchWrap());
        Button secondary = secondary("Выйти из кабинета");
        card3.addView(secondary, matchWrap());
        secondary.setOnClickListener(new defpackage.d(this, 7));
        Button danger = danger("Забыть это устройство");
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(10);
        card3.addView(danger, matchWrap);
        danger.setOnClickListener(new defpackage.d(this, 8));
        linearLayout.addView(card3, matchWrap());
        TextView label3 = label("Telecom+ 0.2.24-alpha26.5 · LIVE", 12, i7, false);
        label3.setGravity(17);
        label3.setPadding(0, dp(22), 0, 0);
        linearLayout.addView(label3, matchWrap());
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

    private void renderServices(LinearLayout linearLayout) {
        List<ServiceItem> list;
        ArrayList arrayList = new ArrayList();
        ArrayList arrayList2 = new ArrayList();
        ArrayList arrayList3 = new ArrayList();
        List<ServiceItem> list2 = this.services;
        if (list2 != null) {
            for (ServiceItem serviceItem : list2) {
                if (serviceItem.isConnected()) {
                    arrayList.add(serviceItem);
                } else if (serviceItem.isAvailable()) {
                    arrayList2.add(serviceItem);
                } else {
                    arrayList3.add(serviceItem);
                }
            }
        }
        LinearLayout column = column();
        column.setPadding(dp(20), dp(20), dp(20), dp(20));
        int i = CYAN;
        int i2 = BLUE;
        column.setBackground(gradient(i, i2, 24));
        column.addView(label("Услуги и опции", 24, -1, true), matchWrap());
        TextView label = label("Всё, что подключено к вашему номеру", 13, Color.rgb(221, 248, 255), false);
        label.setPadding(0, dp(4), 0, dp(16));
        column.addView(label, matchWrap());
        LinearLayout row = row();
        row.addView(serviceStat("Подключено", arrayList.size(), "✓"), new LinearLayout.LayoutParams(0, dp(78), 1.0f));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, dp(78), 1.0f);
        layoutParams.leftMargin = dp(9);
        row.addView(serviceStat("Доступно", arrayList2.size(), "+"), layoutParams);
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(0, dp(78), 1.0f);
        layoutParams2.leftMargin = dp(9);
        List<ServiceItem> list3 = this.services;
        row.addView(serviceStat("Всего", list3 == null ? 0 : list3.size(), "☷"), layoutParams2);
        column.addView(row, matchWrap());
        linearLayout.addView(column, matchWrap());
        if (this.showingCachedData && (list = this.services) != null && !list.isEmpty()) {
            LinearLayout.LayoutParams matchWrap = matchWrap();
            matchWrap.topMargin = dp(10);
            linearLayout.addView(warning("Показан сохранённый каталог услуг."), matchWrap);
        }
        List<ServiceItem> list4 = this.services;
        if (list4 == null || list4.isEmpty()) {
            LinearLayout colorCard = colorCard(SOFT_ORANGE, Color.rgb(242, 215, 177), 20);
            colorCard.addView(label("Каталог пока пуст", 18, TEXT, true), matchWrap());
            TextView label2 = label("Нажмите ↻, чтобы повторно получить список услуг из личного кабинета.", 14, MUTED, false);
            label2.setPadding(0, dp(8), 0, 0);
            colorCard.addView(label2, matchWrap());
            linearLayout.addView(colorCard, cardMargin());
        } else {
            renderServiceSection(linearLayout, "Подключённые", "Активны на вашем номере", arrayList, GREEN, Color.rgb(232, 249, 238));
            renderServiceSection(linearLayout, "Доступные", "Можно подключить после проверки протокола", arrayList2, i2, SOFT_BLUE);
            renderServiceSection(linearLayout, "Другие", "Статус уточняется порталом", arrayList3, PURPLE, SOFT_PURPLE);
        }
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
        String str = serviceItem.details;
        if (str != null && !str.trim().isEmpty()) {
            TextView label = label(serviceItem.details, 13, TEXT, false);
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

    private void showCabinet() {
        this.root.removeAllViews();
        LinearLayout column = column();
        column.addView(topBar(), new LinearLayout.LayoutParams(-1, dp(82)));
        FrameLayout frameLayout = new FrameLayout(this);
        frameLayout.setBackgroundColor(BG);
        column.addView(frameLayout, new LinearLayout.LayoutParams(-1, 0, 1.0f));
        column.addView(bottomNav(), new LinearLayout.LayoutParams(-1, dp(74)));
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        LinearLayout column2 = column();
        column2.setPadding(dp(14), dp(16), dp(14), dp(30));
        int i = this.selectedTab;
        if (i == 0) {
            renderHome(column2);
        } else if (i == 1) {
            renderServices(column2);
        } else if (i == 2) {
            renderPhone(column2);
        } else if (i == 3) {
            renderHistory(column2);
        } else {
            renderProfile(column2);
        }
        scrollView.addView(column2);
        frameLayout.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));
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
        int i = 0;
        clearPortalOtpUi(false);
        this.root.removeAllViews();
        ScrollView scrollView = new ScrollView(this);
        LinearLayout column = column();
        int i2 = 1;
        column.setGravity(1);
        column.setPadding(dp(22), dp(38), dp(22), dp(32));
        column.addView(logo(), new LinearLayout.LayoutParams(dp(72), dp(72)));
        LinearLayout card = card();
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(20);
        card.addView(label("Подтверждение входа", 24, BLUE_DARK, true), matchWrap());
        String str2 = "На номер +380" + maskPhone(this.phone) + " выполнен короткий звонок. Введите последние четыре цифры номера вызывающего абонента.";
        int i3 = MUTED;
        TextView label = label(str2, 15, i3, false);
        label.setPadding(0, dp(10), 0, dp(16));
        card.addView(label, matchWrap());
        EditText field = field("••••", 18);
        field.setTextSize(27.0f);
        field.setGravity(17);
        field.setLetterSpacing(0.32f);
        field.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        card.addView(field, new LinearLayout.LayoutParams(-1, dp(62)));
        TextView label2 = label("", 14, i3, false);
        label2.setGravity(17);
        label2.setPadding(0, dp(12), 0, 0);
        card.addView(label2, matchWrap());
        if (str != null && !str.trim().isEmpty()) {
            TextView label3 = label(str, 14, RED, false);
            label3.setPadding(0, dp(12), 0, 0);
            card.addView(label3, matchWrap());
        }
        Button primary = primary("Войти");
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(18);
        card.addView(primary, matchWrap2);
        configureAuthGuard(primary, "Войти");
        this.activePortalOtpField = field;
        this.activePortalOtpConfirm = primary;
        primary.setOnClickListener(new defpackage.c(this, field, primary, i));
        if (isOtpNotificationAccessEnabled()) {
            TextView info = info("Автоподстановка включена. Telecom+ возьмёт последние четыре цифры из уведомления входящего звонка IT Phone и сразу проверит код.");
            LinearLayout.LayoutParams matchWrap3 = matchWrap();
            matchWrap3.topMargin = dp(12);
            card.addView(info, matchWrap3);
        } else {
            TextView warning = warning("Чтобы подставлять код автоматически, один раз разрешите Telecom+ доступ к уведомлениям. Обрабатываются только уведомления IT Phone; полный номер не сохраняется.");
            LinearLayout.LayoutParams matchWrap4 = matchWrap();
            matchWrap4.topMargin = dp(12);
            card.addView(warning, matchWrap4);
            Button secondary = secondary("Разрешить автоподстановку");
            LinearLayout.LayoutParams matchWrap5 = matchWrap();
            matchWrap5.topMargin = dp(9);
            card.addView(secondary, matchWrap5);
            secondary.setOnClickListener(new defpackage.d(this, i));
        }
        Button secondary2 = secondary("Повторить короткий звонок");
        LinearLayout.LayoutParams matchWrap6 = matchWrap();
        matchWrap6.topMargin = dp(10);
        card.addView(secondary2, matchWrap6);
        secondary2.setOnClickListener(new defpackage.e(this, secondary2, i));
        Button secondary3 = secondary("Изменить номер");
        LinearLayout.LayoutParams matchWrap7 = matchWrap();
        matchWrap7.topMargin = dp(10);
        card.addView(secondary3, matchWrap7);
        secondary3.setOnClickListener(new defpackage.d(this, i2));
        TextView label4 = label("Если звонок не поступил за 20–30 секунд, нажмите «Повторить короткий звонок» один раз.", 13, i3, false);
        label4.setGravity(17);
        label4.setPadding(dp(4), dp(12), dp(4), 0);
        card.addView(label4, matchWrap());
        startOtpTimer(label2, secondary2);
        column.addView(card, matchWrap);
        scrollView.addView(column);
        this.root.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));
        consumeRecentPortalOtp();
    }

    private void showPasswordLogin(String str) {
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
        int i = 0;
        label.setPadding(0, dp(14), 0, dp(24));
        column.addView(label, matchWrap());
        LinearLayout card = card();
        card.addView(label("Вход по личному паролю", 24, BLUE_DARK, true), matchWrap());
        int i2 = MUTED;
        TextView label2 = label("Введите 9-значный номер или цифровой идентификатор и постоянный пароль личного кабинета.", 15, i2, false);
        label2.setPadding(0, dp(10), 0, dp(16));
        card.addView(label2, matchWrap());
        final EditText field = field("Номер или цифровой идентификатор", 3);
        field.setText(this.phone);
        field.setTextSize(20.0f);
        card.addView(field, new LinearLayout.LayoutParams(-1, dp(58)));
        final EditText field2 = field("Личный пароль", 129);
        field2.setTextSize(18.0f);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, dp(58));
        layoutParams.topMargin = dp(12);
        card.addView(field2, layoutParams);
        if (str != null && !str.trim().isEmpty()) {
            TextView label3 = label(str, 14, RED, false);
            label3.setPadding(0, dp(12), 0, 0);
            card.addView(label3, matchWrap());
        }
        final Button primary = primary("Войти");
        LinearLayout.LayoutParams matchWrap = matchWrap();
        matchWrap.topMargin = dp(18);
        card.addView(primary, matchWrap);
        configureAuthGuard(primary, "Войти");
        primary.setOnClickListener(new View.OnClickListener() { // from class: g
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                MainActivity.this.lambda$showPasswordLogin$14(field, field2, primary, view);
            }
        });
        Button secondary = secondary("Войти по одноразовому паролю");
        LinearLayout.LayoutParams matchWrap2 = matchWrap();
        matchWrap2.topMargin = dp(10);
        card.addView(secondary, matchWrap2);
        secondary.setOnClickListener(new defpackage.h(this, field, i));
        column.addView(card, matchWrap());
        TextView label4 = label("Личный пароль не сохраняется. После успешного входа приложение хранит только зашифрованную серверную сессию.", 13, i2, false);
        label4.setGravity(17);
        label4.setPadding(dp(8), dp(20), dp(8), 0);
        column.addView(label4, matchWrap());
        scrollView.addView(column, new FrameLayout.LayoutParams(-1, -2));
        this.root.addView(scrollView, new FrameLayout.LayoutParams(-1, -1));
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
        PortalWebAuthBridge portalWebAuthBridge = this.webAuth;
        if (portalWebAuthBridge != null) {
            portalWebAuthBridge.confirmOtp(digits, new a(this, 6));
        } else {
            runPortal(new defpackage.m(this, digits, i), new a(this, 7));
        }
    }

    private String tabTitle() {
        int i = this.selectedTab;
        return i == 0 ? "Главная" : i == 1 ? "Услуги" : i == 2 ? "Телефон" : i == 3 ? this.historyMode == 0 ? "История средств" : "История звонков" : "Профиль";
    }

    private static String telephonyStatusTitle(TelephonySnapshot telephonySnapshot) {
        if (telephonySnapshot == null) {
            return "Не активирован";
        }
        switch (AnonymousClass4.$SwitchMap$ua$telecomplus$app$TelephonyRegistrationState[telephonySnapshot.registrationState.ordinal()]) {
        }
        return "Не активирован";
    }

    private View topBar() {
        String str;
        LinearLayout row = row();
        row.setGravity(16);
        row.setPadding(dp(18), dp(10), dp(10), dp(10));
        row.setBackground(gradient(BLUE_DARK, BLUE, 0));
        LinearLayout column = column();
        column.addView(label(tabTitle(), 23, -1, true), matchWrap());
        if (this.dashboard == null) {
            str = "Telecom+";
        } else {
            str = this.dashboard.phoneNumber + " · " + this.dashboard.status;
        }
        column.addView(label(str, 13, Color.rgb(214, 231, 255), false), matchWrap());
        row.addView(column, new LinearLayout.LayoutParams(0, -2, 1.0f));
        Button button = button("↻", -1, Color.argb(36, 255, 255, 255));
        button.setTextSize(24.0f);
        button.setContentDescription("Обновить");
        button.setOnClickListener(new defpackage.d(this, 14));
        row.addView(button, new LinearLayout.LayoutParams(dp(48), dp(48)));
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
        beginStartup();
    }

    @Override // android.app.Activity
    public void onDestroy() {
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
    public void onResume() {
        super.onResume();
        consumeRecentPortalOtp();
    }
}
