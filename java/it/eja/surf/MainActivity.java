package it.eja.surf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends Activity {
    public static WebView webView;
    public static String filePath;
    SwipeRefreshLayout swipe;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (Setting.eja.getBoolean("reset")) {
                webReset();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onStop();
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        filePath = getFilesDir().toString();
        webView = findViewById(R.id.web);
        Setting.uuid = UUID.randomUUID().toString();
        try {
            Setting.version = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Setting.load();
            if (Setting.eja.getBoolean("reset")) {
                webReset();
            } else {
                webView.clearHistory();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.addJavascriptInterface(new Javascript(), "android");
        webView.setWebChromeClient(new ejaChromeClient());
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                WebView webView = (WebView) v;
                WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                if (hitTestResult.getType() == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                    String url = hitTestResult.getExtra();
                    String prompt = "(function() { return confirm(\"Bookmark?\"); })();";
                    webView.evaluateJavascript(prompt, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String s) {
                            if (s.equals("true")) {
                                Setting.bookAdd(url);
                            }
                        }
                    });
                }
                return false;
            }
        });
        webView.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String url, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
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
                        if (Setting.block.length() > 0) {
                            for (int i = 0; i < Setting.block.length(); i++) {
                                try {
                                    if (host.endsWith(Setting.block.getString(i))) {
                                        permit = false;
                                        break;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (Setting.allow.length() > 0) {
                            permit = false;
                            for (int i = 0; i < Setting.allow.length(); i++) {
                                try {
                                    if (host.endsWith(Setting.allow.getString(i))) {
                                        permit = true;
                                        break;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
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
        swipe = (SwipeRefreshLayout) this.findViewById(R.id.swipeContainer);
        swipe.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        webView.loadUrl(Setting.home);
                    }
                }
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

    public static void webReset() {
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();
        webView.post(new Runnable() {
            @Override
            public void run() {
                webView.clearCache(true);
                webView.clearFormData();
                webView.clearHistory();
                webView.clearSslPreferences();
            }
        });
    }

    public static String fileRead(String fileName) {
        StringBuilder contentBuilder = new StringBuilder();
        String filePath = String.format("%s%s%s", MainActivity.filePath, File.separator, fileName);
        File file = new File(filePath);
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    contentBuilder.append(sCurrentLine).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return contentBuilder.toString();
    }

    public static void fileWrite(String fileName, String value) {
        String filePath = String.format("%s%s%s", MainActivity.filePath, File.separator, fileName);
        Writer writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filePath)));
            writer.write(value);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ejaChromeClient extends WebChromeClient {
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        ejaChromeClient() {
        }

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                    .setOnDismissListener((DialogInterface dialog) -> result.confirm())
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("")
                    .setMessage(message)
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                    .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> result.cancel())
                    .setOnDismissListener((DialogInterface dialog) -> result.cancel())
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, JsPromptResult result) {
            final EditText input = new EditText(view.getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            input.setText(defaultValue);
            new AlertDialog.Builder(view.getContext())
                    .setTitle("")
                    .setMessage(message)
                    .setView(input)
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm(input.getText().toString()))
                    .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> result.cancel())
                    .setOnDismissListener((DialogInterface dialog) -> result.cancel())
                    .create()
                    .show();
            return true;
        }

        public Bitmap getDefaultVideoPoster() {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout) getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout) getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
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

    public void toast(String text) {
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}
