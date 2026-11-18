package ua.telecomplus.app;

import android.app.Activity;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONObject;

/* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
/* loaded from: classes.dex */
final class PortalWebAuthBridge {
    private static final long DATA_PAGE_HARD_TIMEOUT_MS = 18000;
    private static final long LOGIN_HARD_TIMEOUT_MS = 30000;
    private static final int MAX_LOGIN_INSPECT_ATTEMPTS = 40;
    private static final int MAX_PASSWORD_TAB_ATTEMPTS = 1;
    private static final long OTP_REQUEST_HARD_TIMEOUT_MS = 30000;
    private final Activity activity;
    private final CookieManager cookieManager;
    private int dataInspectAttempts;
    private DataPageCallback dataPageCallback;
    private final Runnable dataPageHardTimeout;
    private int dataPeriodMonth;
    private int dataPeriodYear;
    private boolean dataReadOnlyFormTriggered;
    private int dataStablePasses;
    private LoginCallback loginCallback;
    private final Runnable loginHardTimeout;
    private int loginInspectAttempts;
    private OtpCallback otpCallback;
    private int otpInspectAttempts;
    private final Runnable otpRequestHardTimeout;
    private boolean otpScriptInjected;
    private long pageReadGeneration;
    private boolean passwordFallbackTried;
    private boolean passwordLoginFlow;
    private boolean passwordScriptInjected;
    private int passwordTabAttempts;
    private boolean requestScriptInjected;
    private boolean terminalCallbackSent;
    private final WebView webView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private State state = State.IDLE;
    private String phone = "";
    private String pendingPassword = "";
    private String dataPagePath = "";
    private int dataLastLength = -1;

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public interface DataPageCallback {
        void onResult(PortalResult<PortalPageSnapshot> portalResult);
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public interface LoginCallback {
        void onResult(PortalResult<String> portalResult);
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public interface OtpCallback {
        void onResult(PortalResult<Integer> portalResult);
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public interface PageCallback {
        void onPage(String str, String str2);
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public enum State {
        IDLE,
        LOADING_NUMBER,
        SUBMITTING_NUMBER,
        WAITING_OTP_PAGE,
        OTP_READY,
        SUBMITTING_OTP,
        LOADING_PASSWORD,
        SUBMITTING_PASSWORD,
        WAITING_LOGIN_RESULT,
        READING_DATA
    }

    public PortalWebAuthBridge(Activity activity, ViewGroup viewGroup) {
        this.activity = activity;
        CookieManager cookieManager = CookieManager.getInstance();
        this.cookieManager = cookieManager;
        WebView webView = new WebView(activity);
        this.webView = webView;
        this.loginHardTimeout = new s(this, 2);
        this.otpRequestHardTimeout = new s(this, 3);
        this.dataPageHardTimeout = new s(this, 4);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(1);
        settings.setCacheMode(2);
        settings.setSaveFormData(false);
        settings.setMediaPlaybackRequiresUserGesture(true);
        WebView.setWebContentsDebuggingEnabled(false);
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, false);
        webView.setBackgroundColor(0);
        webView.setAlpha(0.01f);
        webView.setFocusable(false);
        webView.setClickable(false);
        webView.setImportantForAccessibility(4);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new AuthClient(this, 0));
        viewGroup.addView(webView, new ViewGroup.LayoutParams(1, 1));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void armLoginHardTimeout() {
        this.handler.removeCallbacks(this.loginHardTimeout);
        this.handler.postDelayed(this.loginHardTimeout, 30000L);
    }

    private void armOtpRequestHardTimeout() {
        this.handler.removeCallbacks(this.otpRequestHardTimeout);
        this.handler.postDelayed(this.otpRequestHardTimeout, 30000L);
    }

    private static String buildNumberSubmitScript(String str) {
        return "(function(){try{var arr=function(x){return Array.prototype.slice.call(x||[]);};var norm=function(v){return String(v||'').toLowerCase();};var visible=function(e){if(!e)return false;var st=window.getComputedStyle?getComputedStyle(e):null;return (!st||((st.display!=='none')&&(st.visibility!=='hidden'))) && (e.getClientRects?e.getClientRects().length>0:true);};var text=function(f){var es=arr(f.elements);return norm((f.innerText||'')+' '+(f.action||'')+' '+es.map(function(e){return (e.name||'')+' '+(e.id||'')+' '+(e.value||'')+' '+(e.placeholder||'');}).join(' '));};var userScore=function(e){var h=norm((e.name||'')+' '+(e.id||'')+' '+(e.placeholder||'')+' '+(e.autocomplete||''));var type=norm(e.type);var sc=0;if(/subscriber_number/.test(h))sc+=220;if(/subscriber|msisdn|phone|number|номер/.test(h))sc+=90;if(type==='tel')sc+=80;if(/password|token|csrf|otp/.test(h)||type==='hidden')sc-=260;return sc;};var formScore=function(f){var t=text(f);var es=arr(f.elements);var us=es.map(userScore).sort(function(a,b){return b-a;})[0]||-999;var sc=us>0?us:0;if(/number_check/.test(t))sc+=360;if(/получить однораз|отримати однораз|коротк.*(?:звон|вызов|виклик)/.test(t))sc+=220;if(/password_check|one_time_password_check|личн.*парол|особист.*парол/.test(t))sc-=420;if(visible(f))sc+=30;return sc;};var forms=arr(document.forms).sort(function(a,b){return formScore(b)-formScore(a);});var form=forms.length&&formScore(forms[0])>180?forms[0]:null;if(!form)return 'ERROR:Форма запроса короткого звонка не найдена';var els=arr(form.elements);var input=els.slice().sort(function(a,b){return userScore(b)-userScore(a);})[0];if(!input||userScore(input)<=0)return 'ERROR:Поле номера не найдено';var setv=function(el,v){try{var proto=el instanceof HTMLInputElement?HTMLInputElement.prototype:Object.getPrototypeOf(el);var d=Object.getOwnPropertyDescriptor(proto,'value');if(d&&d.set)d.set.call(el,v);else el.value=v;}catch(x){el.value=v;}el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));};input.focus();setv(input," + JSONObject.quote(str) + ");var controls=arr(form.querySelectorAll('button,input[type=submit],input[type=image],input[type=button],a,[role=button]'));var submit=controls.find(function(e){var t=norm((e.innerText||e.textContent||e.value||'')+' '+(e.name||'')+' '+(e.id||''));return /получить однораз|отримати однораз|number_check|коротк.*(?:звон|вызов|виклик)/.test(t);})||controls.find(visible)||controls[0];var isSubmitter=function(e){if(!e)return false;var tag=norm(e.tagName);var type=norm(e.type);return (tag==='button'&&(type===''||type==='submit'))||(tag==='input'&&(type==='submit'||type==='image'));};if(submit){try{submit.click();return 'OK:CLICK';}catch(clickError){if(isSubmitter(submit)&&form.requestSubmit){form.requestSubmit(submit);return 'OK:REQUEST_SUBMIT_VALID';}}}if(form.requestSubmit){form.requestSubmit();return 'OK:REQUEST_SUBMIT_NO_CONTROL';}HTMLFormElement.prototype.submit.call(form);return 'OK:NATIVE_SUBMIT';}catch(e){return 'ERROR:'+String(e&&e.message?e.message:e);}})()";
    }

    private static String buildOtpSubmitScript(String str) {
        return "(function(){try{var arr=function(x){return Array.prototype.slice.call(x||[]);};var norm=function(v){return String(v||'').toLowerCase();};var visible=function(e){if(!e)return false;var st=window.getComputedStyle?getComputedStyle(e):null;return (!st||((st.display!=='none')&&(st.visibility!=='hidden'))) && (e.getClientRects?e.getClientRects().length>0:true);};var formText=function(f){var es=arr(f.elements);return norm((f.innerText||'')+' '+(f.action||'')+' '+es.map(function(e){return (e.name||'')+' '+(e.id||'')+' '+(e.value||'')+' '+(e.placeholder||'')+' '+(e.autocomplete||'');}).join(' '));};var otpScore=function(e,ctx){var h=norm((e.name||'')+' '+(e.id||'')+' '+(e.placeholder||'')+' '+(e.autocomplete||''));var type=norm(e.type);var sc=0;if(/one_time_password|one-time-code|(^|[^a-z])otp([^a-z]|$)/.test(h))sc+=260;if(h.indexOf('ongoing_password')>=0)sc+=ctx?220:30;if(type==='password'||type==='tel'||type==='number')sc+=30;if(/token|csrf/.test(h)||type==='hidden')sc-=300;return sc;};var formScore=function(f){var t=formText(f);var oneTime=/one_time_password_check|одноразов.*парол|одноразовим парол|останні чотири|последние четыре/.test(t);var personal=/password_check/.test(t)&&!/one_time_password_check/.test(t);var ctx=oneTime&&!personal;var es=arr(f.elements);var best=es.map(function(e){return otpScore(e,ctx);}).sort(function(a,b){return b-a;})[0]||-999;var sc=best>0?best:0;if(/one_time_password_check/.test(t))sc+=420;if(/останні чотири|последние четыре/.test(t))sc+=180;if(personal)sc-=420;if(visible(f))sc+=30;return sc;};var forms=arr(document.forms).sort(function(a,b){return formScore(b)-formScore(a);});var form=forms.length&&formScore(forms[0])>220?forms[0]:null;if(!form)return 'ERROR:Форма OTP не найдена';var ft=formText(form);var ctx=/one_time_password_check|одноразов.*парол|одноразовим парол|останні чотири|последние четыре/.test(ft)&&!/password_check/.test(ft.replace(/one_time_password_check/g,''));var els=arr(form.elements);var input=els.slice().sort(function(a,b){return otpScore(b,ctx)-otpScore(a,ctx);})[0];if(!input||otpScore(input,ctx)<=0)return 'ERROR:Поле OTP не найдено';var setv=function(el,v){try{var proto=el instanceof HTMLInputElement?HTMLInputElement.prototype:Object.getPrototypeOf(el);var d=Object.getOwnPropertyDescriptor(proto,'value');if(d&&d.set)d.set.call(el,v);else el.value=v;}catch(x){el.value=v;}el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));};input.focus();setv(input," + JSONObject.quote(str) + ");var controls=arr(form.querySelectorAll('button,input[type=submit],input[type=image],input[type=button],a,[role=button]'));var submit=controls.find(function(e){var t=norm((e.innerText||e.textContent||e.value||'')+' '+(e.name||'')+' '+(e.id||''));return (t.trim()==='войти'||t.trim()==='увійти'||/one_time_password_check|подтверд|підтверд/.test(t))&&!/личн.*парол|особист.*парол/.test(t);})||controls.find(visible)||controls[0];var isSubmitter=function(e){if(!e)return false;var tag=norm(e.tagName);var type=norm(e.type);return (tag==='button'&&(type===''||type==='submit'))||(tag==='input'&&(type==='submit'||type==='image'));};if(submit){try{submit.click();return 'OK:CLICK';}catch(clickError){if(isSubmitter(submit)&&form.requestSubmit){form.requestSubmit(submit);return 'OK:REQUEST_SUBMIT_VALID';}}}if(form.requestSubmit){form.requestSubmit();return 'OK:REQUEST_SUBMIT_NO_CONTROL';}HTMLFormElement.prototype.submit.call(form);return 'OK:NATIVE_SUBMIT';}catch(e){return 'ERROR:'+String(e&&e.message?e.message:e);}})()";
    }

    private static String buildPasswordSubmitScript(String str, String str2) {
        return "(function(){try{var arr=function(x){return Array.prototype.slice.call(x||[]);};var norm=function(v){return String(v||'').toLowerCase();};var visible=function(e){if(!e)return false;var st=window.getComputedStyle?getComputedStyle(e):null;return (!st||((st.display!=='none')&&(st.visibility!=='hidden'))) && (e.getClientRects?e.getClientRects().length>0:true);};var formText=function(f){var es=arr(f.elements);return norm((f.innerText||'')+' '+(f.action||'')+' '+es.map(function(e){return (e.name||'')+' '+(e.id||'')+' '+(e.value||'')+' '+(e.placeholder||'')+' '+(e.autocomplete||'');}).join(' '));};var personalContext=function(f){var t=formText(f);return /password_check|вход по личному паролю|личный пароль|особист.*парол|personal.*password/.test(t)&&!/one_time_password_check/.test(t);};var userScore=function(e){var h=norm((e.name||'')+' '+(e.id||'')+' '+(e.placeholder||'')+' '+(e.autocomplete||''));var type=norm(e.type);var sc=0;if(/subscriber_number/.test(h))sc+=180;if(/subscriber|msisdn|login|number|номер|ідентифікатор/.test(h))sc+=80;if(type==='tel'||h.indexOf('username')>=0)sc+=60;if(/password|token|csrf|otp/.test(h)||type==='hidden')sc-=240;return sc;};var passwordScore=function(e,personal){var h=norm((e.name||'')+' '+(e.id||'')+' '+(e.placeholder||'')+' '+(e.autocomplete||''));var type=norm(e.type);var ac=norm(e.autocomplete);var sc=0;if(/(^|[_-])passwordform\\d*/.test(h)||h.indexOf('passwordform')===0)sc+=220;if(/personal_password|current_password/.test(h))sc+=180;if(type==='password')sc+=personal?170:90;if(ac==='current-password')sc+=160;if(/личн.*парол|особист.*парол|введите пароль/.test(h))sc+=100;if(h.indexOf('ongoing_password')>=0)sc+=personal?130:-220;if(/one_time|one-time-code|(^|[^a-z])otp([^a-z]|$)|temporary/.test(h))sc-=260;if(/token|csrf/.test(h)||type==='hidden')sc-=260;return sc;};var scoreForm=function(f){var es=arr(f.elements);var t=formText(f);var personal=personalContext(f);var us=es.map(userScore).sort(function(a,b){return b-a;})[0]||-999;var ps=es.map(function(e){return passwordScore(e,personal);}).sort(function(a,b){return b-a;})[0]||-999;var sc=0;if(us>0)sc+=us;if(ps>0)sc+=ps;if(personal)sc+=260;if(/password_check/.test(t))sc+=260;if(/one_time_password_check|получить однораз|отримати однораз/.test(t))sc-=320;if(visible(f))sc+=20;return sc;};var forms=arr(document.forms).sort(function(a,b){return scoreForm(b)-scoreForm(a);});var form=forms.length&&scoreForm(forms[0])>180?forms[0]:null;if(!form){var switches=arr(document.querySelectorAll('a,button,[role=button]'));var sw=switches.find(function(e){var t=norm((e.innerText||e.textContent||'')+' '+(e.id||'')+' '+(e.className||''));return /вход по личному паролю|особист.*парол|personal.*password/.test(t);});if(sw){sw.click();return 'WAIT:PERSONAL_PASSWORD_TAB';}var diag=forms.map(function(f){return scoreForm(f)+':'+formText(f).replace(/\\s+/g,' ').slice(0,120);}).join('|');return 'ERROR:Форма входа по личному паролю не найдена. forms='+forms.length+' '+diag;}var els=arr(form.elements);var personal=personalContext(form);var user=els.slice().sort(function(a,b){return userScore(b)-userScore(a);})[0];var pass=els.slice().sort(function(a,b){return passwordScore(b,personal)-passwordScore(a,personal);})[0];if(!user||userScore(user)<=0)return 'ERROR:Поле логина не найдено';if(!pass||passwordScore(pass,personal)<=0)return 'ERROR:Поле личного пароля не найдено';var setv=function(el,v){try{var proto=el instanceof HTMLInputElement?HTMLInputElement.prototype:Object.getPrototypeOf(el);var d=Object.getOwnPropertyDescriptor(proto,'value');if(d&&d.set)d.set.call(el,v);else el.value=v;}catch(x){el.value=v;}el.dispatchEvent(new Event('input',{bubbles:true}));el.dispatchEvent(new Event('change',{bubbles:true}));};user.focus();setv(user," + JSONObject.quote(str) + ");pass.focus();setv(pass," + JSONObject.quote(str2) + ");var controls=arr(form.querySelectorAll('button,input[type=submit],input[type=image],input[type=button],a,[role=button]'));var submit=controls.find(function(e){var t=norm((e.innerText||e.textContent||e.value||'')+' '+(e.name||'')+' '+(e.id||''));return (t.trim()==='войти'||t.trim()==='увійти'||t.indexOf('password_check')>=0)&&t.indexOf('one_time')<0&&t.indexOf('однораз')<0;})||controls.find(visible)||controls[0];var isSubmitter=function(e){if(!e)return false;var tag=norm(e.tagName);var type=norm(e.type);return (tag==='button'&&(type===''||type==='submit'))||(tag==='input'&&(type==='submit'||type==='image'));};if(submit){try{submit.click();return 'OK:CLICK';}catch(clickError){if(isSubmitter(submit)&&form.requestSubmit){form.requestSubmit(submit);return 'OK:REQUEST_SUBMIT_VALID';}}}if(form.requestSubmit){form.requestSubmit();return 'OK:REQUEST_SUBMIT_NO_CONTROL';}HTMLFormElement.prototype.submit.call(form);return 'OK:NATIVE_SUBMIT';}catch(e){return 'ERROR:'+String(e&&e.message?e.message:e);}})()";
    }

    private static String buildReadOnlyDataTriggerScript(int i, int i2) {
        return "(function(){try{var norm=function(v){return String(v||'').toLowerCase().replace(/\\s+/g,' ').trim();};var allowed=/показать|сформировать|получить|применить|найти|детализац|выписк|показати|сформувати|отримати|застосувати/;var forbidden=/оплат|пополнить|поповнити|перевод|переказ|подключ|підключ|отключ|відключ|изменить|змінити|заказать|замовити/;var periodYear=" + i + ";var periodMonth=" + i2 + ";var monthRe=new RegExp(" + JSONObject.quote(monthPattern(i2)) + ",'i');var periodLabel=" + JSONObject.quote(monthLabel(i, i2)) + ";var arr=function(x){return Array.prototype.slice.call(x||[]);};var targetSelect=null,targetOption=null;if(periodYear>0&&periodMonth>0){var selects=arr(document.querySelectorAll('select'));var best=-1;selects.forEach(function(sel){arr(sel.options).forEach(function(opt){var t=norm((opt.text||opt.textContent||'')+' '+(opt.value||''));var score=0;if(String(t).indexOf(String(periodYear))>=0)score+=100;if(monthRe.test(t))score+=180;var mm=(periodMonth<10?'0':'')+periodMonth;if(t.indexOf(periodYear+'-'+mm)>=0||t.indexOf(mm+'.'+periodYear)>=0||t.indexOf(mm+'/'+periodYear)>=0)score+=220;if(score>best){best=score;targetSelect=sel;targetOption=opt;}});});if(!targetSelect||!targetOption||best<180)return 'NO_PERIOD:'+periodLabel;try{var d=Object.getOwnPropertyDescriptor(HTMLSelectElement.prototype,'value');if(d&&d.set)d.set.call(targetSelect,targetOption.value);else targetSelect.value=targetOption.value;}catch(x){targetSelect.selectedIndex=targetOption.index;}targetOption.selected=true;targetSelect.dispatchEvent(new Event('input',{bubbles:true}));targetSelect.dispatchEvent(new Event('change',{bubbles:true}));}var scope=(targetSelect&&targetSelect.form)?targetSelect.form:document;var controls=arr(scope.querySelectorAll('button,input[type=submit],input[type=button],input[type=image],a[role=button]'));var c=controls.find(function(e){var t=norm((e.innerText||e.textContent||e.value||'')+' '+(e.name||'')+' '+(e.id||''));return allowed.test(t)&&!forbidden.test(t);});if(!c){controls=arr(document.querySelectorAll('button,input[type=submit],input[type=button],input[type=image],a[role=button]'));c=controls.find(function(e){var t=norm((e.innerText||e.textContent||e.value||'')+' '+(e.name||'')+' '+(e.id||''));return allowed.test(t)&&!forbidden.test(t);});}if(!c)return 'ERROR:Кнопка Показать не найдена';try{c.click();return 'CLICKED:'+periodLabel;}catch(clickError){var f=c.form||targetSelect&&targetSelect.form;if(f&&f.requestSubmit){f.requestSubmit(c);return 'SUBMITTED:'+periodLabel;}if(f){HTMLFormElement.prototype.submit.call(f);return 'SUBMITTED:'+periodLabel;}return 'ERROR:'+String(clickError);}}catch(e){return 'ERROR:'+String(e&&e.message?e.message:e);}})()";
    }

    private void cancelPending() {
        this.handler.removeCallbacksAndMessages(null);
        this.handler.removeCallbacks(this.loginHardTimeout);
        this.handler.removeCallbacks(this.otpRequestHardTimeout);
        this.handler.removeCallbacks(this.dataPageHardTimeout);
        this.pageReadGeneration++;
        this.otpCallback = null;
        this.loginCallback = null;
        this.dataPageCallback = null;
        this.dataPagePath = "";
        this.dataPeriodYear = 0;
        this.dataPeriodMonth = 0;
        this.state = State.IDLE;
        this.pendingPassword = "";
        this.passwordLoginFlow = false;
        this.passwordFallbackTried = false;
        this.terminalCallbackSent = false;
    }

    private static String decodeJsString(String str) {
        if (str == null || "null".equals(str)) {
            return "";
        }
        try {
            return new JSONArray("[" + str + "]").getString(0);
        } catch (Exception unused) {
            return str.replace("\\\"", "\"").replace("\\n", "\n");
        }
    }

    private static String digits(String str) {
        return str == null ? "" : str.replaceAll("\\D+", "");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void failDataPage(String str, boolean z) {
        if (this.state != State.READING_DATA) {
            return;
        }
        this.handler.removeCallbacks(this.dataPageHardTimeout);
        DataPageCallback dataPageCallback = this.dataPageCallback;
        this.dataPageCallback = null;
        this.dataPagePath = "";
        this.dataPeriodYear = 0;
        this.dataPeriodMonth = 0;
        this.state = State.IDLE;
        if (dataPageCallback != null) {
            dataPageCallback.onResult(PortalResult.failure(str, z));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void failLogin(String str) {
        if (this.terminalCallbackSent) {
            return;
        }
        this.handler.removeCallbacks(this.loginHardTimeout);
        this.terminalCallbackSent = true;
        boolean z = this.passwordLoginFlow;
        this.pendingPassword = "";
        this.passwordLoginFlow = false;
        this.state = z ? State.IDLE : State.OTP_READY;
        LoginCallback loginCallback = this.loginCallback;
        this.loginCallback = null;
        if (loginCallback != null) {
            loginCallback.onResult(PortalResult.failure(str));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void failOtp(String str) {
        if (this.terminalCallbackSent) {
            return;
        }
        this.handler.removeCallbacks(this.otpRequestHardTimeout);
        this.terminalCallbackSent = true;
        this.state = State.IDLE;
        OtpCallback otpCallback = this.otpCallback;
        this.otpCallback = null;
        if (otpCallback != null) {
            otpCallback.onResult(PortalResult.failure(str));
        }
    }

    private void finishDataPage(String str, String str2) {
        if (this.state != State.READING_DATA) {
            return;
        }
        this.handler.removeCallbacks(this.dataPageHardTimeout);
        String mergeCookies = mergeCookies(this.cookieManager.getCookie("https://services.intertelecom.ua/pages/overview/?lang=ru"), this.cookieManager.getCookie(str), this.cookieManager.getCookie("https://services.intertelecom.ua/"));
        String buildSafeStructureReport = PortalHtmlParser.buildSafeStructureReport(str, str2);
        DataPageCallback dataPageCallback = this.dataPageCallback;
        this.dataPageCallback = null;
        this.dataPagePath = "";
        this.dataPeriodYear = 0;
        this.dataPeriodMonth = 0;
        this.state = State.IDLE;
        this.cookieManager.flush();
        if (dataPageCallback != null) {
            dataPageCallback.onResult(PortalResult.success(new PortalPageSnapshot(str, str2, buildSafeStructureReport, mergeCookies)));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void inspectDataPage() {
        if (this.state != State.READING_DATA) {
            return;
        }
        readPage(new q(this, 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void inspectLoginResult() {
        State state = this.state;
        if (state == State.WAITING_LOGIN_RESULT || state == State.SUBMITTING_OTP || state == State.SUBMITTING_PASSWORD) {
            readPage(new q(this, 2));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void inspectOtpPage() {
        State state = this.state;
        if (state == State.WAITING_OTP_PAGE || state == State.SUBMITTING_NUMBER) {
            readPage(new q(this, 1));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static boolean isAllowed(Uri uri) {
        return uri != null && "https".equalsIgnoreCase(uri.getScheme()) && "services.intertelecom.ua".equalsIgnoreCase(uri.getHost());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public static /* synthetic */ void lambda$clearBrowserSession$6(AtomicBoolean atomicBoolean, Runnable runnable) {
        if (!atomicBoolean.compareAndSet(false, true) || runnable == null) {
            return;
        }
        runnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$clearBrowserSession$7(Runnable runnable, Boolean bool) {
        this.cookieManager.flush();
        runnable.run();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$confirmOtp$5(String str) {
        if (this.terminalCallbackSent || this.state == State.IDLE) {
            return;
        }
        String decodeJsString = decodeJsString(str);
        if (decodeJsString.startsWith("ERROR:")) {
            failLogin(decodeJsString.substring(6));
        } else {
            this.state = State.WAITING_LOGIN_RESULT;
            this.handler.postDelayed(new s(this, 5), 450L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inspectDataPage$12(String str) {
        String decodeJsString = decodeJsString(str);
        if (decodeJsString.startsWith("NO_PERIOD:")) {
            failDataPage("На портале отсутствует период ".concat(decodeJsString.substring(10)), false);
        } else {
            if (decodeJsString.startsWith("ERROR:")) {
                failDataPage("Не удалось применить период: ".concat(decodeJsString.substring(6)), false);
                return;
            }
            this.dataLastLength = -1;
            this.dataStablePasses = 0;
            this.handler.postDelayed(new s(this, 1), 1100L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inspectDataPage$13(String str, String str2) {
        int i;
        if (this.state != State.READING_DATA) {
            return;
        }
        boolean looksUnauthorized = PortalHtmlParser.looksUnauthorized(str, str2);
        int i2 = 1;
        if (looksUnauthorized) {
            failDataPage("Серверная сессия истекла. Выполните повторный вход.", true);
            return;
        }
        int length = str2 == null ? 0 : str2.length();
        int i3 = this.dataLastLength;
        if (i3 < 0 || Math.abs(length - i3) >= 48) {
            this.dataStablePasses = 0;
        } else {
            this.dataStablePasses += 1;
        }
        this.dataLastLength = length;
        this.dataInspectAttempts += 1;
        boolean hasDynamicDetailRows = PortalHtmlParser.hasDynamicDetailRows(str2);
        int i4 = this.dataPeriodYear;
        boolean z = (i4 <= 0 || (i = this.dataPeriodMonth) < 1 || i > 12) ? false : true;
        if (!this.dataReadOnlyFormTriggered && ((z && this.dataInspectAttempts >= 1) || (!z && !hasDynamicDetailRows && this.dataInspectAttempts >= 3))) {
            this.dataReadOnlyFormTriggered = true;
            this.webView.evaluateJavascript(z ? buildReadOnlyDataTriggerScript(i4, this.dataPeriodMonth) : buildReadOnlyDataTriggerScript(0, 0), new t(this, 2));
        } else if ((!hasDynamicDetailRows || this.dataStablePasses < 1) && this.dataInspectAttempts < 11) {
            this.handler.postDelayed(new s(this, i2), 650L);
        } else {
            finishDataPage(str, str2);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inspectLoginResult$11(String str, String str2) {
        State state = this.state;
        if (state == State.WAITING_LOGIN_RESULT || state == State.SUBMITTING_OTP || state == State.SUBMITTING_PASSWORD) {
            boolean z = (!str.contains("/pages/") || PortalHtmlParser.looksUnauthorized(str, str2)) ? false : true;
            if (str.contains("/pages/overview") || z) {
                this.handler.removeCallbacks(this.loginHardTimeout);
                this.state = State.IDLE;
                this.pendingPassword = "";
                this.passwordLoginFlow = false;
                String mergeCookies = mergeCookies(this.cookieManager.getCookie("https://services.intertelecom.ua/pages/overview/?lang=ru"), this.cookieManager.getCookie(str), this.cookieManager.getCookie("https://services.intertelecom.ua/"));
                LoginCallback loginCallback = this.loginCallback;
                this.loginCallback = null;
                if (loginCallback != null) {
                    loginCallback.onResult(PortalResult.success(mergeCookies));
                    return;
                }
                return;
            }
            String extractError = PortalHtmlParser.extractError(str2);
            if (extractError == null) {
                int i = this.loginInspectAttempts + 1;
                this.loginInspectAttempts = i;
                if (i < MAX_LOGIN_INSPECT_ATTEMPTS) {
                    this.handler.postDelayed(new s(this, 5), 500L);
                    return;
                }
            }
            if (extractError == null) {
                extractError = this.passwordLoginFlow ? "Логин или личный пароль не принят порталом" : "Одноразовый пароль не принят порталом";
            }
            failLogin(extractError);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$inspectOtpPage$10(String str, String str2) {
        State state = this.state;
        if (state == State.WAITING_OTP_PAGE || state == State.SUBMITTING_NUMBER) {
            String extractError = PortalHtmlParser.extractError(str2);
            AuthForm parseAuthForm = PortalHtmlParser.parseAuthForm(str2, true, str);
            if (extractError != null && (parseAuthForm == null || PortalHtmlParser.isRateLimitError(extractError))) {
                failOtp(extractError);
                return;
            }
            if (parseAuthForm != null && PortalHtmlParser.hasOtpCallConfirmation(str2)) {
                this.handler.removeCallbacks(this.otpRequestHardTimeout);
                this.cookieManager.flush();
                this.state = State.OTP_READY;
                OtpCallback otpCallback = this.otpCallback;
                this.otpCallback = null;
                if (otpCallback != null) {
                    otpCallback.onResult(PortalResult.success(Integer.valueOf(PortalHtmlParser.extractOtpLifetimeSeconds(str2))));
                    return;
                }
                return;
            }
            int i = this.otpInspectAttempts + 1;
            this.otpInspectAttempts = i;
            if (i < 10) {
                this.handler.postDelayed(new s(this, 0), 500L);
                return;
            }
            if (parseAuthForm != null) {
                failOtp("Портал открыл поле OTP, но браузерный движок не получил подтверждение запуска короткого звонка. URL: " + safePath(str));
            } else {
                failOtp("Портал не вернул форму подтверждения. " + PortalHtmlParser.diagnoseAuthPage(str, str2));
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$loginWithPassword$4(Boolean bool) {
        this.cookieManager.flush();
        this.webView.clearCache(true);
        this.webView.clearHistory();
        this.webView.loadUrl("https://services.intertelecom.ua/login/number?lang=ru");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$0() {
        State state = this.state;
        if (state == State.LOADING_PASSWORD || state == State.SUBMITTING_PASSWORD || state == State.SUBMITTING_OTP || state == State.WAITING_LOGIN_RESULT) {
            failLogin("Портал не завершил вход за 30 секунд. Проверьте VPN и повторите одну попытку. Страница: " + safePath(this.webView.getUrl()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$1() {
        State state = this.state;
        if (state == State.LOADING_NUMBER || state == State.SUBMITTING_NUMBER || state == State.WAITING_OTP_PAGE) {
            failOtp("Портал не подтвердил запуск короткого звонка за 30 секунд. Не нажимайте кнопку многократно: проверьте VPN и повторите позже. Страница: " + safePath(this.webView.getUrl()));
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$new$2() {
        if (this.state == State.READING_DATA) {
            failDataPage("Портал не завершил загрузку детализации за 18 секунд. Проверьте VPN и повторите обновление.", false);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$readPage$14(boolean[] zArr, long j, PageCallback pageCallback) {
        if (zArr[0] || j != this.pageReadGeneration) {
            return;
        }
        zArr[0] = true;
        pageCallback.onPage(this.webView.getUrl() == null ? "" : this.webView.getUrl(), "");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$readPage$15(boolean[] zArr, long j, Runnable runnable, PageCallback pageCallback, String str) {
        if (zArr[0] || j != this.pageReadGeneration) {
            return;
        }
        zArr[0] = true;
        this.handler.removeCallbacks(runnable);
        pageCallback.onPage(this.webView.getUrl() == null ? "" : this.webView.getUrl(), decodeJsString(str));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$requestOtp$3(Boolean bool) {
        this.cookieManager.flush();
        this.webView.clearCache(true);
        this.webView.clearHistory();
        armOtpRequestHardTimeout();
        this.webView.loadUrl("https://services.intertelecom.ua/login/number?lang=ru");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$submitNumber$8(String str) {
        State state;
        if (this.terminalCallbackSent || (state = this.state) == State.OTP_READY || state == State.IDLE) {
            return;
        }
        String decodeJsString = decodeJsString(str);
        if (decodeJsString.startsWith("ERROR:")) {
            failOtp(decodeJsString.substring(6));
        } else {
            this.state = State.WAITING_OTP_PAGE;
            this.handler.postDelayed(new s(this, 0), 450L);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public /* synthetic */ void lambda$submitPassword$9(String str) {
        if (this.terminalCallbackSent) {
            return;
        }
        String decodeJsString = decodeJsString(str);
        int i = 6;
        if (decodeJsString.startsWith("WAIT:")) {
            int i2 = this.passwordTabAttempts + 1;
            this.passwordTabAttempts = i2;
            if (i2 < 1) {
                this.state = State.LOADING_PASSWORD;
                this.passwordScriptInjected = false;
                this.handler.postDelayed(new s(this, i), 650L);
                return;
            } else if (this.passwordFallbackTried) {
                this.pendingPassword = "";
                failLogin("Портал не открыл форму личного пароля после переключения вкладки");
                return;
            } else {
                this.passwordFallbackTried = true;
                this.state = State.LOADING_PASSWORD;
                this.passwordScriptInjected = false;
                this.webView.loadUrl("https://services.intertelecom.ua/login/password/?lang=ru");
                return;
            }
        }
        if (!decodeJsString.startsWith("ERROR:")) {
            this.pendingPassword = "";
            this.state = State.WAITING_LOGIN_RESULT;
            armLoginHardTimeout();
            this.handler.postDelayed(new s(this, 5), 450L);
            return;
        }
        String substring = decodeJsString.substring(6);
        if (this.passwordFallbackTried || !substring.contains("Форма входа по личному паролю")) {
            this.pendingPassword = "";
            failLogin(substring);
        } else {
            this.passwordFallbackTried = true;
            this.state = State.LOADING_PASSWORD;
            this.passwordScriptInjected = false;
            this.webView.loadUrl("https://services.intertelecom.ua/login/password/?lang=ru");
        }
    }

    private static String mergeCookies(String... strArr) {
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        int length = strArr.length;
        for (int i = 0; i < length; i += 1) {
            String str = strArr[i];
            if (str != null) {
                String[] split = str.split(";");
                int length2 = split.length;
                for (int i2 = 0; i2 < length2; i2 += 1) {
                    String trim = split[i2].trim();
                    int indexOf = trim.indexOf(61);
                    if (indexOf > 0) {
                        linkedHashMap.put(trim.substring(0, indexOf).trim(), trim.substring(indexOf + 1).trim());
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : linkedHashMap.entrySet()) {
            if (sb.length() > 0) {
                sb.append("; ");
            }
            sb.append((String) entry.getKey());
            sb.append('=');
            sb.append((String) entry.getValue());
        }
        return sb.toString();
    }

    private static String monthLabel(int i, int i2) {
        String[] strArr = {"", "Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь"};
        if (i2 < 1 || i2 > 12) {
            return "текущий период";
        }
        return strArr[i2] + " " + i;
    }

    private static String monthPattern(int i) {
        switch (i) {
            case 1:
                return "январ|січ";
            case 2:
                return "феврал|лют";
            case 3:
                return "март|берез";
            case 4:
                return "апрел|квіт";
            case 5:
                return "май|мая|трав";
            case 6:
                return "июн|черв";
            case 7:
                return "июл|лип";
            case 8:
                return "август|серп";
            case 9:
                return "сентябр|верес";
            case 10:
                return "октябр|жовт";
            case 11:
                return "ноябр|листоп";
            case 12:
                return "декабр|груд";
            default:
                return ".*";
        }
    }

    private static String normalizeDataPath(String str) {
        String trim = str == null ? "" : str.trim();
        return (!trim.startsWith("/pages/") || trim.contains("\r") || trim.contains("\n") || trim.contains("//")) ? "" : trim;
    }

    private static String normalizeNumber(String str) {
        String digits = digits(str);
        if (digits.startsWith("380")) {
            digits = digits.substring(3);
        }
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        if (digits.length() == 9) {
            return digits;
        }
        return null;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v0, types: [java.lang.Runnable, ua.telecomplus.app.u] */
    private void readPage(final PageCallback pageCallback) {
        final long j = this.pageReadGeneration + 1;
        this.pageReadGeneration = j;
        final boolean[] zArr = {false};
        final Runnable r4 = new Runnable() { // from class: ua.telecomplus.app.u
            @Override // java.lang.Runnable
            public final void run() {
                PortalWebAuthBridge.this.lambda$readPage$14(zArr, j, pageCallback);
            }
        };
        this.handler.postDelayed(r4, 1800L);
        this.webView.evaluateJavascript("(function(){return document.documentElement ? document.documentElement.outerHTML : '';})()", new ValueCallback() { // from class: ua.telecomplus.app.v
            @Override // android.webkit.ValueCallback
            public final void onReceiveValue(Object obj) {
                PortalWebAuthBridge.this.lambda$readPage$15(zArr, j, r4, pageCallback, (String) obj);
            }
        });
    }

    private static String safePath(String str) {
        try {
            String path = Uri.parse(str).getPath();
            if (path != null) {
                if (!path.isEmpty()) {
                    return path;
                }
            }
        } catch (Exception unused) {
        }
        return "/";
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void submitNumber() {
        if (this.state != State.LOADING_NUMBER) {
            return;
        }
        this.state = State.SUBMITTING_NUMBER;
        armOtpRequestHardTimeout();
        this.handler.postDelayed(new s(this, 0), 700L);
        this.webView.evaluateJavascript(buildNumberSubmitScript(this.phone), new t(this, 0));
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void submitPassword() {
        if (this.state != State.LOADING_PASSWORD) {
            return;
        }
        this.passwordScriptInjected = true;
        this.state = State.SUBMITTING_PASSWORD;
        String str = this.pendingPassword;
        armLoginHardTimeout();
        this.handler.postDelayed(new s(this, 5), 700L);
        this.webView.evaluateJavascript(buildPasswordSubmitScript(this.phone, str), new t(this, 5));
    }

    private void syncSessionCookies(String str) {
        if (str == null) {
            str = "";
        }
        String[] split = str.split(";");
        int length = split.length;
        for (int i = 0; i < length; i += 1) {
            String trim = split[i].trim();
            if (trim.indexOf(61) > 0) {
                this.cookieManager.setCookie("https://services.intertelecom.ua", trim.concat("; Path=/; Secure"));
            }
        }
        this.cookieManager.flush();
    }

    public void clearBrowserSession(Runnable runnable) {
        cancelPending();
        try {
            this.webView.stopLoading();
            this.webView.loadUrl("about:blank");
            this.webView.clearCache(true);
            this.webView.clearFormData();
            this.webView.clearHistory();
            WebStorage.getInstance().deleteAllData();
        } catch (Exception unused) {
        }
        final AtomicBoolean clearGuard = new AtomicBoolean(false);
        final Runnable dVar = new Runnable() {
            @Override
            public void run() {
                PortalWebAuthBridge.lambda$clearBrowserSession$6(clearGuard, runnable);
            }
        };
        this.cookieManager.removeAllCookies(new ValueCallback() { // from class: ua.telecomplus.app.w
            @Override // android.webkit.ValueCallback
            public final void onReceiveValue(Object obj) {
                PortalWebAuthBridge.this.lambda$clearBrowserSession$7(dVar, (Boolean) obj);
            }
        });
        this.handler.postDelayed(dVar, 1500L);
    }

    public void confirmOtp(String str, LoginCallback loginCallback) {
        String digits = digits(str);
        if (digits.length() != 4) {
            loginCallback.onResult(PortalResult.failure("Введите последние четыре цифры входящего номера"));
            return;
        }
        if (this.state != State.OTP_READY) {
            loginCallback.onResult(PortalResult.failure("Сначала запросите короткий звонок"));
            return;
        }
        this.passwordLoginFlow = false;
        this.loginCallback = loginCallback;
        this.terminalCallbackSent = false;
        int i = 1;
        this.otpScriptInjected = true;
        this.loginInspectAttempts = 0;
        this.state = State.SUBMITTING_OTP;
        armLoginHardTimeout();
        this.handler.postDelayed(new s(this, 5), 700L);
        this.webView.evaluateJavascript(buildOtpSubmitScript(digits), new t(this, i));
    }

    public void destroy() {
        cancelPending();
        try {
            this.webView.stopLoading();
            this.webView.loadUrl("about:blank");
            this.webView.clearCache(true);
            this.webView.clearFormData();
            this.webView.clearHistory();
            this.webView.removeAllViews();
            this.webView.destroy();
        } catch (Exception unused) {
        }
    }

    public void loginWithPassword(String str, String str2, LoginCallback loginCallback) {
        String normalizeNumber = normalizeNumber(str);
        if (str2 == null) {
            str2 = "";
        }
        if (normalizeNumber == null) {
            loginCallback.onResult(PortalResult.failure("Введите номер или цифровой идентификатор: 9 цифр без начального нуля"));
            return;
        }
        if (str2.isEmpty()) {
            loginCallback.onResult(PortalResult.failure("Введите личный пароль"));
            return;
        }
        if (str2.length() > 256) {
            loginCallback.onResult(PortalResult.failure("Пароль слишком длинный"));
            return;
        }
        cancelPending();
        this.phone = normalizeNumber;
        this.pendingPassword = str2;
        this.passwordLoginFlow = true;
        this.loginCallback = loginCallback;
        this.state = State.LOADING_PASSWORD;
        this.requestScriptInjected = false;
        this.otpScriptInjected = false;
        this.passwordScriptInjected = false;
        this.passwordFallbackTried = false;
        this.terminalCallbackSent = false;
        this.otpInspectAttempts = 0;
        this.loginInspectAttempts = 0;
        this.passwordTabAttempts = 0;
        armLoginHardTimeout();
        this.cookieManager.removeAllCookies(new t(this, 4));
    }

    public void readAuthenticatedPage(String str, String str2, DataPageCallback dataPageCallback) {
        readAuthenticatedPageForMonth(str, str2, 0, 0, dataPageCallback);
    }

    public void readAuthenticatedPageForMonth(String str, String str2, int i, int i2, DataPageCallback dataPageCallback) {
        String normalizeDataPath = normalizeDataPath(str);
        if (normalizeDataPath.isEmpty()) {
            dataPageCallback.onResult(PortalResult.failure("Недопустимый адрес раздела кабинета"));
            return;
        }
        if (this.state != State.IDLE) {
            dataPageCallback.onResult(PortalResult.failure("Браузерный адаптер занят. Повторите загрузку через несколько секунд."));
            return;
        }
        cancelPending();
        this.dataPageCallback = dataPageCallback;
        this.dataPagePath = normalizeDataPath;
        this.dataPeriodYear = i;
        this.dataPeriodMonth = i2;
        this.dataInspectAttempts = 0;
        this.dataLastLength = -1;
        this.dataStablePasses = 0;
        this.dataReadOnlyFormTriggered = false;
        this.terminalCallbackSent = false;
        this.state = State.READING_DATA;
        syncSessionCookies(str2);
        this.handler.removeCallbacks(this.dataPageHardTimeout);
        this.handler.postDelayed(this.dataPageHardTimeout, DATA_PAGE_HARD_TIMEOUT_MS);
        try {
            this.webView.stopLoading();
            this.webView.clearCache(true);
            this.webView.loadUrl("https://services.intertelecom.ua".concat(normalizeDataPath));
        } catch (Exception e) {
            failDataPage("Не удалось открыть раздел кабинета: ".concat(e.getClass().getSimpleName()), false);
        }
    }

    public void requestOtp(String str, OtpCallback otpCallback) {
        String normalizeNumber = normalizeNumber(str);
        if (normalizeNumber == null) {
            otpCallback.onResult(PortalResult.failure("Введите номер Интертелекома: 9 цифр без начального нуля"));
            return;
        }
        cancelPending();
        this.phone = normalizeNumber;
        this.passwordLoginFlow = false;
        this.otpCallback = otpCallback;
        this.state = State.LOADING_NUMBER;
        this.requestScriptInjected = false;
        this.otpScriptInjected = false;
        this.passwordScriptInjected = false;
        this.passwordFallbackTried = false;
        this.terminalCallbackSent = false;
        this.otpInspectAttempts = 0;
        this.loginInspectAttempts = 0;
        this.passwordTabAttempts = 0;
        this.cookieManager.removeAllCookies(new t(this, 3));
    }

    public void resetAuth() {
        cancelPending();
        try {
            this.webView.stopLoading();
            this.webView.loadUrl("about:blank");
        } catch (Exception unused) {
        }
    }

    /* compiled from: r8-map-id-5d3ff7fb16909a7e6ee886360b4644e2c341965d08cc2e57589da44a1b2babc1 */
    public final class AuthClient extends WebViewClient {
        public static final /* synthetic */ int a = 0;

        private AuthClient() {
        }

        @Override // android.webkit.WebViewClient
        public void onPageFinished(WebView webView, String str) {
            super.onPageFinished(webView, str);
            if (PortalWebAuthBridge.isAllowed(Uri.parse(str))) {
                if (PortalWebAuthBridge.this.state == State.LOADING_NUMBER && !PortalWebAuthBridge.this.requestScriptInjected) {
                    PortalWebAuthBridge.this.requestScriptInjected = true;
                    PortalWebAuthBridge.this.handler.postDelayed(new s(PortalWebAuthBridge.this, 7), 250L);
                    return;
                }
                if (PortalWebAuthBridge.this.state == State.LOADING_PASSWORD && !PortalWebAuthBridge.this.passwordScriptInjected) {
                    PortalWebAuthBridge.this.passwordScriptInjected = true;
                    PortalWebAuthBridge.this.handler.postDelayed(new s(PortalWebAuthBridge.this, 8), 250L);
                    return;
                }
                if (PortalWebAuthBridge.this.state == State.WAITING_OTP_PAGE || PortalWebAuthBridge.this.state == State.SUBMITTING_NUMBER) {
                    PortalWebAuthBridge.this.handler.postDelayed(new s(PortalWebAuthBridge.this, 9), 650L);
                    return;
                }
                if (PortalWebAuthBridge.this.state == State.WAITING_LOGIN_RESULT || PortalWebAuthBridge.this.state == State.SUBMITTING_OTP || PortalWebAuthBridge.this.state == State.SUBMITTING_PASSWORD) {
                    PortalWebAuthBridge.this.armLoginHardTimeout();
                    PortalWebAuthBridge.this.handler.postDelayed(new s(PortalWebAuthBridge.this, 10), 500L);
                } else if (PortalWebAuthBridge.this.state == State.READING_DATA) {
                    PortalWebAuthBridge.this.handler.postDelayed(new s(PortalWebAuthBridge.this, 11), 900L);
                }
            }
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedError(WebView webView, WebResourceRequest webResourceRequest, WebResourceError webResourceError) {
            if (webResourceRequest.isForMainFrame()) {
                String charSequence = (webResourceError == null || webResourceError.getDescription() == null) ? "Ошибка загрузки страницы входа" : webResourceError.getDescription().toString();
                if (PortalWebAuthBridge.this.state == State.LOADING_NUMBER || PortalWebAuthBridge.this.state == State.SUBMITTING_NUMBER || PortalWebAuthBridge.this.state == State.WAITING_OTP_PAGE) {
                    PortalWebAuthBridge.this.failOtp("Портал недоступен: " + charSequence);
                    return;
                }
                if (PortalWebAuthBridge.this.state == State.LOADING_PASSWORD || PortalWebAuthBridge.this.state == State.SUBMITTING_PASSWORD || PortalWebAuthBridge.this.state == State.SUBMITTING_OTP || PortalWebAuthBridge.this.state == State.WAITING_LOGIN_RESULT) {
                    PortalWebAuthBridge.this.failLogin("Портал недоступен: " + charSequence);
                    return;
                }
                if (PortalWebAuthBridge.this.state == State.READING_DATA) {
                    PortalWebAuthBridge.this.failDataPage("Портал недоступен: " + charSequence, false);
                }
            }
        }

        @Override // android.webkit.WebViewClient
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.cancel();
            if (PortalWebAuthBridge.this.state == State.LOADING_NUMBER || PortalWebAuthBridge.this.state == State.SUBMITTING_NUMBER || PortalWebAuthBridge.this.state == State.WAITING_OTP_PAGE) {
                PortalWebAuthBridge.this.failOtp("Проверка TLS-сертификата портала завершилась ошибкой");
                return;
            }
            State state = PortalWebAuthBridge.this.state;
            State state2 = State.READING_DATA;
            PortalWebAuthBridge portalWebAuthBridge = PortalWebAuthBridge.this;
            if (state == state2) {
                portalWebAuthBridge.failDataPage("Проверка TLS-сертификата портала завершилась ошибкой", false);
            } else {
                portalWebAuthBridge.failLogin("Проверка TLS-сертификата портала завершилась ошибкой");
            }
        }

        @Override // android.webkit.WebViewClient
        public boolean shouldOverrideUrlLoading(WebView webView, WebResourceRequest webResourceRequest) {
            return !PortalWebAuthBridge.isAllowed(webResourceRequest.getUrl());
        }

        public /* synthetic */ AuthClient(PortalWebAuthBridge portalWebAuthBridge, int i) {
            this();
        }
    }
}
