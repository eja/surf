package it.eja.surf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.InputType;
import android.view.View;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;

public class Chrome extends WebChromeClient {
    private View mCustomView;
    private WebChromeClient.CustomViewCallback mCustomViewCallback;
    private int mOriginalOrientation;
    private int mOriginalSystemUiVisibility;
    public Activity activity;

    Chrome(MainActivity mainActivity) {
        this.activity = mainActivity;
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
        return BitmapFactory.decodeResource(this.activity.getResources(), 2130837573);
    }

    public void onHideCustomView() {
        ((FrameLayout) this.activity.getWindow().getDecorView()).removeView(this.mCustomView);
        this.mCustomView = null;
        this.activity.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
        this.activity.setRequestedOrientation(this.mOriginalOrientation);
        this.mCustomViewCallback.onCustomViewHidden();
        this.mCustomViewCallback = null;
    }

    public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
        if (this.mCustomView != null) {
            onHideCustomView();
            return;
        }
        this.mCustomView = paramView;
        this.mOriginalSystemUiVisibility = this.activity.getWindow().getDecorView().getSystemUiVisibility();
        this.mOriginalOrientation = this.activity.getRequestedOrientation();
        this.mCustomViewCallback = paramCustomViewCallback;
        ((FrameLayout) this.activity.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
        this.activity.getWindow().getDecorView().setSystemUiVisibility(3846 | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}