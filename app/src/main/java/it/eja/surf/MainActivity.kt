// Copyright (C) by Ubaldo Porcheddu <ubaldo@eja.it>

package it.eja.surf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Patterns
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.net.MalformedURLException
import java.net.URL
import java.util.*

class MainActivity : Activity() {
    private val dnsCache = HashMap<String, String>()
    private lateinit var swipe: SwipeRefreshLayout
    internal lateinit var webView: WebView

    private lateinit var toolbarContainer: FrameLayout
    private lateinit var searchLayout: FrameLayout
    private lateinit var findLayout: LinearLayout

    private lateinit var searchInput: EditText
    private lateinit var menuIcon: TextView

    private lateinit var findInput: EditText

    private lateinit var imm: InputMethodManager

    override fun onDestroy() {
        super.onDestroy()
        if (Setting.reset) {
            webReset()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Setting.path = filesDir.toString()
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        try {
            Setting.load()
            Setting.version = packageManager.getPackageInfo(packageName, 0).versionCode
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val root = FrameLayout(this)
        root.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        swipe = SwipeRefreshLayout(this)
        swipe.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        webView = WebView(this)
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        swipe.addView(webView)

        toolbarContainer = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.TOP
            }
            setBackgroundColor(Color.parseColor("#F0F0F0"))
            setPadding(10, 10, 10, 10)
            elevation = 10f
        }

        searchLayout = FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        searchInput = EditText(this).apply {
            hint = "Search or enter address"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
            imeOptions = EditorInfo.IME_ACTION_GO
            setSingleLine()
            background = null
            setPadding(20, 20, 100, 20)
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE) {
                    processSearch(v.text.toString())
                    true
                } else false
            }
        }

        menuIcon = TextView(this).apply {
            text = "⋮"
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(30, 10, 30, 10)
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.END or Gravity.CENTER_VERTICAL
            }
            setOnClickListener { Menu(this@MainActivity).show() }
        }

        searchLayout.addView(searchInput)
        searchLayout.addView(menuIcon)

        findLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            visibility = View.GONE
            layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
        }

        findInput = EditText(this).apply {
            hint = "Find..."
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setSingleLine()
            background = null
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f)
            addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (!s.isNullOrEmpty()) webView.findAllAsync(s.toString())
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_NEXT) {
                    webView.findNext(true)
                    true
                } else false
            }
        }

        val btnPrev = Button(this).apply {
            text = "↑"
            layoutParams = LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT)
            setOnClickListener { webView.findNext(false) }
        }

        val btnNext = Button(this).apply {
            text = "↓"
            layoutParams = LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT)
            setOnClickListener { webView.findNext(true) }
        }

        val btnClose = Button(this).apply {
            text = "✕"
            layoutParams = LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT)
            setOnClickListener { closeFindMode() }
        }

        findLayout.addView(findInput)
        findLayout.addView(btnPrev)
        findLayout.addView(btnNext)
        findLayout.addView(btnClose)

        toolbarContainer.addView(searchLayout)
        toolbarContainer.addView(findLayout)

        root.addView(swipe)
        root.addView(toolbarContainer)
        setContentView(root)

        if (Setting.reset) {
            webReset()
        }

        setupWebView()

        swipe.setOnRefreshListener {
            showHomeMode()
            swipe.isRefreshing = false
        }

        if (savedInstanceState == null) {
            webView.loadUrl(Setting.home)
            showBrowserMode()
        } else {
            showHomeMode()
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_SEARCH) {
            if (toolbarContainer.visibility == View.GONE) showHomeMode() else showBrowserMode()
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && toolbarContainer.visibility == View.GONE) {
            if (webView.scrollY == 0) {
                showHomeMode()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun showHomeMode() {
        toolbarContainer.visibility = View.VISIBLE
        searchLayout.visibility = View.VISIBLE
        findLayout.visibility = View.GONE
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }

    private fun showBrowserMode() {
        toolbarContainer.visibility = View.GONE
        closeFindMode()
        imm.hideSoftInputFromWindow(searchInput.windowToken, 0)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }

    internal fun showFindMode() {
        toolbarContainer.visibility = View.VISIBLE
        searchLayout.visibility = View.GONE
        findLayout.visibility = View.VISIBLE
        findInput.requestFocus()
        imm.showSoftInput(findInput, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun closeFindMode() {
        webView.clearMatches()
        findInput.setText("")
        searchLayout.visibility = View.VISIBLE
        findLayout.visibility = View.GONE
        imm.hideSoftInputFromWindow(findInput.windowToken, 0)
    }

    internal fun processSearch(query: String) {
        if (query.isBlank()) return
        var url = query.trim()
        val isUrl = Patterns.WEB_URL.matcher(url).matches() || url.contains("://") || url.startsWith("www.")

        if (!isUrl && !url.contains(".")) {
            val host = if (Setting.home.endsWith("/")) Setting.home else "${Setting.home}/"
            url = "${host}?q=$url"
        } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        webView.loadUrl(url)
        showBrowserMode()
    }

    override fun onBackPressed() {
        if (findLayout.visibility == View.VISIBLE) {
            closeFindMode()
            return
        }
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            if (toolbarContainer.visibility == View.GONE) {
                showHomeMode()
                webView.stopLoading()
            } else {
                super.onBackPressed()
            }
        }
    }

    private fun webReset() {
        WebStorage.getInstance().deleteAllData()
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
        webView.clearCache(true)
        webView.clearFormData()
        webView.clearHistory()
        webView.clearSslPreferences()
    }

    private fun setupWebView() {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)

        webView.apply {
            settings.apply {
                javaScriptEnabled = true
                allowFileAccess = false
                domStorageEnabled = true
                builtInZoomControls = true
                displayZoomControls = false
                databaseEnabled = true
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            webChromeClient = Chrome(this@MainActivity)

            setDownloadListener { url, _, _, _, _ ->
                try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch(e:Exception){}
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    if (url.startsWith("http")) return false
                    try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) } catch (e: Exception) { }
                    return true
                }

                override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    try {
                        hostCurrent = URL(url).host
                        searchInput.setText(url)
                    } catch (e: MalformedURLException) { }
                    showBrowserMode()
                }

                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
                    if (Setting.doh.isNotEmpty() && request.url != null && request.url.scheme?.startsWith("http") == true) {
                        val host = request.url.host?.lowercase() ?: ""
                        if (!dnsCache.containsKey(host)) {
                            val ip = Setting.dohToIp(host)
                            dnsCache[host] = ip
                        }
                        if (dnsCache[host] == "0.0.0.0") return WebResourceResponse(null, null, null)
                    }
                    return super.shouldInterceptRequest(view, request)
                }
            }

            setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
                if (scrollY > oldScrollY + 20) {
                    if (toolbarContainer.visibility == View.VISIBLE && findLayout.visibility == View.GONE) {
                        showBrowserMode()
                    }
                }
            }
        }
    }

    companion object {
        var hostCurrent: String? = null
    }
}