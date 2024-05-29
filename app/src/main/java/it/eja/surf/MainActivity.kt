// Copyright (C) 2021-2024 by Ubaldo Porcheddu <ubaldo@eja.it>

package it.eja.surf

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebStorage
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import org.json.JSONException
import java.net.MalformedURLException
import java.net.URL

class MainActivity : Activity() {
    private val dnsCache = HashMap<String, String>()
    private lateinit var swipe: SwipeRefreshLayout
    private lateinit var webView: WebView

    override fun onDestroy() {
        super.onDestroy()
        if (Setting.reset) {
            webReset()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Setting.path = filesDir.toString()
        try {
            Setting.load()
            Setting.version = packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        webView = findViewById(R.id.web)
        swipe = findViewById(R.id.swipeContainer)

        if (Setting.reset) {
            webReset()
        } else {
            webView.clearHistory()
        }

        setupWebView()
        setupSwipeRefreshLayout()

        if (savedInstanceState == null) {
            webView.loadUrl(Setting.home)
        } else {
            webView.restoreState(savedInstanceState)
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        webView.saveState(outState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        webView.restoreState(savedInstanceState)
    }

    private fun webReset() {
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        webView.post {
            webView.clearCache(true)
            webView.clearFormData()
            webView.clearHistory()
            webView.clearSslPreferences()
        }
    }

    private fun setupWebView() {
        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = true
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
            }
            addJavascriptInterface(Javascript(), "android")
            webChromeClient = Chrome(this@MainActivity)

            setOnLongClickListener { v: View ->
                val hitTestResult = (v as WebView).hitTestResult
                if (hitTestResult.type == WebView.HitTestResult.SRC_ANCHOR_TYPE) {
                    hitTestResult.extra?.let { url ->
                        evaluateJavascript("(function() { return prompt(\"Bookmark?\",\"$url\"); })();") { s: String? ->
                            s?.let {
                                Setting.bookAdd(it.substring(1, it.length - 1))
                            }
                        }
                    }
                }
                false
            }

            setDownloadListener { url, _, _, _, _ ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.startsWith("tel:") || url.startsWith("sms:")) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        return true
                    }
                    return super.shouldOverrideUrlLoading(view, url)
                }

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    try {
                        hostCurrent = URL(url).host
                    } catch (e: MalformedURLException) {
                        e.printStackTrace()
                    }
                    swipe.isRefreshing = true
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView, url: String) {
                    swipe.isRefreshing = false
                    super.onPageFinished(view, url)
                }

                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ): WebResourceResponse? {
                    var permit = true
                    if (request.url != null) {
                        val scheme = request.url.scheme?.trim()
                        if (scheme.equals("http", ignoreCase = true) || scheme.equals(
                                "https",
                                ignoreCase = true
                            )
                        ) {
                            val host = request.url.host?.lowercase() ?: ""
                            Log.d("eja",Setting.doh)
                            if (Setting.doh.isNotEmpty()) {
                                host.let { safeHost ->
                                    if (!dnsCache.containsKey(safeHost)) {
                                        dnsCache[safeHost] = Setting.dohToIp(safeHost)
                                    }
                                    if (dnsCache[safeHost] == "0.0.0.0") {
                                        permit = false
                                    }
                                }
                            }
                        }
                    }
                    return if (permit) {
                        super.shouldInterceptRequest(view, request)
                    } else {
                        WebResourceResponse(null, null, null)
                    }
                }
            }
        }
    }

    private fun setupSwipeRefreshLayout() {
        swipe.setOnRefreshListener {
            webView.loadUrl(Setting.home)
        }
    }

    companion object {
        var hostCurrent: String? = null
    }
}