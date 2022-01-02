package it.eja.surf;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends Activity {
    HashMap<String, String> dnsCache = new HashMap<String, String>();
    public static String hostCurrent;
    SwipeRefreshLayout swipe;
    WebView webView;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Setting.reset) {
            webReset();
        }
        super.onStop();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Setting.path = getFilesDir().toString();
        try {
            Setting.load();
            Setting.version = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionCode;
        } catch (JSONException | PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        webView = findViewById(R.id.web);
        if (Setting.reset) {
            webReset();
        } else {
            webView.clearHistory();
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.addJavascriptInterface(new Javascript(), "android");
        webView.setWebChromeClient(new Chrome(this));
        webView.setOnLongClickListener(v -> {
            WebView webView = (WebView) v;
            WebView.HitTestResult hitTestResult = webView.getHitTestResult();
            if (hitTestResult.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                String url = hitTestResult.getExtra();
                webView.evaluateJavascript("(function() { return prompt(\"Bookmark?\",\""+url+"\"); })();", s -> {
                    if (s != null) {
                        Setting.bookAdd(s.substring(1,s.length()-1));
                    }
                });
            }
            return false;
        });
        webView.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:") || url.startsWith("sms:")) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                try {
                    hostCurrent = new URL(url).getHost();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                super.onPageStarted(view, url, favicon);
                swipe.setRefreshing(true);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipe.setRefreshing(false);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, final WebResourceRequest request) {
                boolean permit = true;
                if (request != null && request.getUrl() != null) {
                    String scheme = request.getUrl().getScheme().trim();
                    if (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https")) {
                        String host = request.getUrl().getHost().toLowerCase(Locale.ROOT);
                        if (Setting.allow.length() > 0) {
                            permit = Setting.checkList(Setting.allow, host);
                        }
                        if (Setting.block.length() > 0 && Setting.checkList(Setting.block, host)) {
                            permit = false;
                        }
                        if (permit && !Setting.doh.isEmpty()) {
                            if (!dnsCache.containsKey(host)) {
                                dnsCache.put(host, Setting.dohToIp(host));
                            }
                            if (dnsCache.get(host).equals("0.0.0.0")) {
                                permit = false;
                            }
                        }
                    }
                }
                if (permit) {
                    return super.shouldInterceptRequest(view, request);
                } else {
                    return new WebResourceResponse("", "", null);
                }
            }
        });
        swipe = this.findViewById(R.id.swipeContainer);
        swipe.setOnRefreshListener(
                () -> webView.loadUrl(Setting.home)
        );
        if (savedInstanceState == null) {
            webView.loadUrl(Setting.home);
        }
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    private void webReset() {
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        webView.post(() -> {
            webView.clearCache(true);
            webView.clearFormData();
            webView.clearHistory();
            webView.clearSslPreferences();
        });
    }
}