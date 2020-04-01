package com.centurylink.biwf.screens.common

import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.databinding.FragmentWebviewBinding

class CustomWebFragment : BaseFragment() {

    private lateinit var binding: FragmentWebviewBinding

    private lateinit var webView: WebView

    lateinit var progressBar: ProgressBar

    private var url: String? = null

    private var reloadCount = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        url = arguments?.getString(KEY_URL)
        if (TextUtils.isEmpty(url)) {
            throw IllegalArgumentException("Empty URL passed to WebViewFragment!")
        }
        binding = FragmentWebviewBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            reloadCount = it.getInt(RELOAD_COUNT)
        }
        webView = binding.detailswebView
        progressBar =binding.progressBar
        initWebViewProperties()
    }

    private fun initWebViewProperties() {
        // Get the web view settings instance.
        val settings = webView.settings
        // Enable java script in web view.
        settings.javaScriptEnabled = true
        // Enable DOM storage API.
        settings.domStorageEnabled = true
        // Enable zooming in web view.
        settings.setSupportZoom(true)
        // Allow pinch to zoom.
        settings.builtInZoomControls = true
        // Disable the default zoom controls on the page.
        settings.displayZoomControls = false
        // Enable responsive layout.
        settings.useWideViewPort = false
        // Zoom out if the content width is greater than the width of the viewport.
        settings.loadWithOverviewMode = false

        // Set web view client.
        webView.webChromeClient = object : DefaultWebChromeClient() {
            override fun onProgressChanged(webView: WebView?, newProgress: Int) {
                if (newProgress < 100) {
                    progressBar.visibility = View.VISIBLE
                }
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                }
            }
        }
        webView.loadUrl(url)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(RELOAD_COUNT, reloadCount)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Destroy the WebView completely.
            // The WebView must be removed from the view hierarchy before calling destroy to prevent a memory leak.
            (webView.parent as ViewGroup).removeView(webView)
            webView.removeAllViews()
            webView.destroy()

    }


    fun onBackPressed(): Boolean {
        if (webView.canGoBack()) {
            // If web view have back history, then go to the web view back history.
            webView.goBack()
            return true
        }
        return false
    }

    internal open class DefaultWebChromeClient : WebChromeClient() {

    }
    internal open class DefaultWebViewClient : WebViewClient() {
        // Decide how a new url will be loaded. If this method returns false, it means current
        // webView will handle the url. If this method returns true, it means host application
        // will handle the url. By default, redirects cause jump from WebView to default
        // system browser.
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return true
        }

        override
        fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            handler?.proceed()
        }
    }

    companion object {
        const val KEY_URL = "key_url"
        private const val RELOAD_COUNT = "reload_count"
        private const val DISPLAY_BACK ="displayback"

        fun newInstance(url: String,displayBack:Boolean): CustomWebFragment {
            val fragment = CustomWebFragment()
            val args = Bundle()
            args.putString(KEY_URL, url)
            args.putBoolean(DISPLAY_BACK,displayBack)
            fragment.arguments = args
            return fragment
        }
    }
}