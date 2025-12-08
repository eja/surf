// Copyright (C) by Ubaldo Porcheddu <ubaldo@eja.it>

package it.eja.surf

import android.app.AlertDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.EditText
import android.widget.FrameLayout
import android.net.Uri
import android.webkit.ValueCallback

class Chrome(private val mainActivity: MainActivity) : WebChromeClient() {
    private var mCustomView: View? = null
    private var mCustomViewCallback: CustomViewCallback? = null
    private var mOriginalOrientation = 0
    private var mOriginalSystemUiVisibility = 0

    override fun onJsAlert(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
        AlertDialog.Builder(view.context).apply {
            setTitle("")
            setMessage(message)
            setPositiveButton("OK") { _, _ -> result.confirm() }
            setOnDismissListener { result.confirm() }
        }.create().show()
        return true
    }

    override fun onJsConfirm(view: WebView, url: String?, message: String?, result: JsResult): Boolean {
        AlertDialog.Builder(view.context).apply {
            setTitle("")
            setMessage(message)
            setPositiveButton("OK") { _, _ -> result.confirm() }
            setNegativeButton("Cancel") { _, _ -> result.cancel() }
            setOnDismissListener { result.cancel() }
        }.create().show()
        return true
    }

    override fun onJsPrompt(
        view: WebView,
        url: String?,
        message: String?,
        defaultValue: String?,
        result: JsPromptResult
    ): Boolean {
        val input = EditText(view.context).apply {
            inputType = InputType.TYPE_CLASS_TEXT
            setText(defaultValue)
        }
        val container = FrameLayout(mainActivity).apply {
            val params = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = 50
                rightMargin = 50
            }
            input.layoutParams = params
            addView(input)
        }
        AlertDialog.Builder(view.context).apply {
            setTitle("")
            setMessage(message)
            setView(container)
            setPositiveButton("OK") { _, _ -> result.confirm(input.text.toString()) }
            setNegativeButton("Cancel") { _, _ -> result.cancel() }
            setOnDismissListener { result.cancel() }
        }.create().show()
        return true
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        if (mainActivity.fileUploadCallback != null) {
            mainActivity.fileUploadCallback?.onReceiveValue(null)
            mainActivity.fileUploadCallback = null
        }

        mainActivity.fileUploadCallback = filePathCallback

        try {
            val intent = fileChooserParams?.createIntent()
            mainActivity.startActivityForResult(intent, 100)
        } catch (e: Exception) {
            mainActivity.fileUploadCallback = null
            return false
        }
        return true
    }

    override fun getDefaultVideoPoster(): Bitmap? {
        return if (mCustomView == null) null else BitmapFactory.decodeResource(mainActivity.resources, 2130837573)
    }

    override fun onHideCustomView() {
        (mainActivity.window.decorView as FrameLayout).removeView(mCustomView)
        mCustomView = null
        mainActivity.window.decorView.systemUiVisibility = mOriginalSystemUiVisibility
        mainActivity.requestedOrientation = mOriginalOrientation
        mCustomViewCallback?.onCustomViewHidden()
        mCustomViewCallback = null
    }

    override fun onShowCustomView(paramView: View, paramCustomViewCallback: CustomViewCallback) {
        if (mCustomView != null) {
            onHideCustomView()
            return
        }
        mCustomView = paramView
        mOriginalSystemUiVisibility = mainActivity.window.decorView.systemUiVisibility
        mOriginalOrientation = mainActivity.requestedOrientation
        mCustomViewCallback = paramCustomViewCallback
        (mainActivity.window.decorView as FrameLayout).addView(
            mCustomView,
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        )
        mainActivity.window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
    }
}