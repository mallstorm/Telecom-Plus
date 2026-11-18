package ua.telecomplus.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Получает токен Cloudflare Turnstile для входа на портал. Токен можно выпустить только в настоящем
 * браузерном контексте, поэтому здесь используется минимальный WebView с виджетом: он отдаёт только
 * токен, а весь вход выполняется нативными запросами. Виджет показывается, только если Cloudflare
 * требует ручное подтверждение (галочка).
 */
final class TurnstileSolver {
    interface Callback {
        void onFailure(String str);

        void onToken(String str);
    }

    private static final String ORIGIN = "https://services.intertelecom.ua/login/number/";

    private TurnstileSolver() {
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    static void solve(final Activity activity, String str, String str2, final Callback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] done = {false};
        final WebView webView = new WebView(activity);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        LinearLayout box = new LinearLayout(activity);
        box.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * activity.getResources().getDisplayMetrics().density);
        box.setPadding(pad, pad, pad, 0);
        TextView info = new TextView(activity);
        info.setText("Cloudflare проверяет, что вы не робот. Если появится галочка — отметьте её.");
        info.setTextSize(14.0f);
        box.addView(info, new LinearLayout.LayoutParams(-1, -2));
        box.addView(webView, new LinearLayout.LayoutParams(-1, (int) (96 * activity.getResources().getDisplayMetrics().density)));

        final AlertDialog dialog = new AlertDialog.Builder(activity).setTitle("Проверка безопасности").setView(box).setCancelable(false).setNegativeButton("Отмена", (DialogInterface.OnClickListener) null).create();
        final Runnable cleanup = new Runnable() {
            @Override
            public void run() {
                try {
                    webView.stopLoading();
                    webView.removeJavascriptInterface("TsBridge");
                    ViewGroup parent = (ViewGroup) webView.getParent();
                    if (parent != null) {
                        parent.removeView(webView);
                    }
                    webView.destroy();
                } catch (Exception unused) {
                }
                try {
                    dialog.dismiss();
                } catch (Exception unused2) {
                }
            }
        };
        final Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if (!done[0]) {
                    done[0] = true;
                    cleanup.run();
                    callback.onFailure("Проверка безопасности не пройдена за отведённое время. Повторите попытку.");
                }
            }
        };
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void token(final String t) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (done[0]) {
                            return;
                        }
                        done[0] = true;
                        handler.removeCallbacks(timeout);
                        cleanup.run();
                        callback.onToken(t);
                    }
                });
            }

            @JavascriptInterface
            public void error(final String e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (done[0]) {
                            return;
                        }
                        done[0] = true;
                        handler.removeCallbacks(timeout);
                        cleanup.run();
                        callback.onFailure("Проверка безопасности не пройдена (" + e + "). Повторите попытку.");
                    }
                });
            }
        }, "TsBridge");
        webView.setWebViewClient(new WebViewClient());
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new android.view.View.OnClickListener() {
                    @Override
                    public void onClick(android.view.View view) {
                        if (!done[0]) {
                            done[0] = true;
                            handler.removeCallbacks(timeout);
                            cleanup.run();
                            callback.onFailure("Вход отменён");
                        }
                    }
                });
            }
        });
        String html = "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<script src=\"https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit&onload=tsReady\" async defer></script>"
                + "</head><body style=\"margin:0;display:flex;justify-content:center;align-items:center;min-height:90px;background:#fff\"><div id=\"w\"></div><script>"
                + "function tsReady(){turnstile.render('#w',{sitekey:'" + str.replaceAll("[^A-Za-z0-9_-]", "") + "',action:'" + str2.replaceAll("[^A-Za-z0-9_-]", "") + "',appearance:'interaction-only',"
                + "callback:function(t){TsBridge.token(t)},"
                + "'error-callback':function(e){TsBridge.error(String(e));return true},"
                + "'timeout-callback':function(){TsBridge.error('timeout')}});}"
                + "</script></body></html>";
        dialog.show();
        webView.loadDataWithBaseURL(ORIGIN, html, "text/html", "UTF-8", null);
        handler.postDelayed(timeout, 90000L);
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    static Runnable solveEmbedded(final Activity activity, final ViewGroup container, String str, String str2, final Callback callback) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final boolean[] done = {false};
        final WebView webView = new WebView(activity);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        webView.setBackgroundColor(0);
        container.addView(webView, new ViewGroup.LayoutParams(-1, -1));
        final Runnable cleanup = new Runnable() {
            @Override
            public void run() {
                try {
                    webView.stopLoading();
                    webView.removeJavascriptInterface("TsBridge");
                    ViewGroup parent = (ViewGroup) webView.getParent();
                    if (parent != null) {
                        parent.removeView(webView);
                    }
                    webView.destroy();
                } catch (Exception unused) {
                }
            }
        };
        final Runnable timeout = new Runnable() {
            @Override
            public void run() {
                if (!done[0]) {
                    done[0] = true;
                    cleanup.run();
                    callback.onFailure("Проверка безопасности не пройдена за отведённое время. Повторите попытку.");
                }
            }
        };
        webView.addJavascriptInterface(new Object() {
            @JavascriptInterface
            public void token(final String t) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (done[0]) {
                            return;
                        }
                        done[0] = true;
                        handler.removeCallbacks(timeout);
                        cleanup.run();
                        callback.onToken(t);
                    }
                });
            }

            @JavascriptInterface
            public void error(final String e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (done[0]) {
                            return;
                        }
                        done[0] = true;
                        handler.removeCallbacks(timeout);
                        cleanup.run();
                        callback.onFailure("Проверка безопасности не пройдена (" + e + "). Повторите попытку.");
                    }
                });
            }
        }, "TsBridge");
        webView.setWebViewClient(new WebViewClient());
        String html = "<!doctype html><html><head><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<script src=\"https://challenges.cloudflare.com/turnstile/v0/api.js?render=explicit&onload=tsReady\" async defer></script>"
                + "</head><body style=\"margin:0;display:flex;justify-content:center;align-items:center;min-height:80px;background:transparent\"><div id=\"w\"></div><script>"
                + "function tsReady(){turnstile.render('#w',{sitekey:'" + str.replaceAll("[^A-Za-z0-9_-]", "") + "',action:'" + str2.replaceAll("[^A-Za-z0-9_-]", "") + "',appearance:'interaction-only',"
                + "callback:function(t){TsBridge.token(t)},"
                + "'error-callback':function(e){TsBridge.error(String(e));return true},"
                + "'timeout-callback':function(){TsBridge.error('timeout')}});}"
                + "</script></body></html>";
        webView.loadDataWithBaseURL(ORIGIN, html, "text/html", "UTF-8", null);
        handler.postDelayed(timeout, 90000L);
        return new Runnable() {
            @Override
            public void run() {
                if (!done[0]) {
                    done[0] = true;
                    handler.removeCallbacks(timeout);
                    cleanup.run();
                }
            }
        };
    }
}
