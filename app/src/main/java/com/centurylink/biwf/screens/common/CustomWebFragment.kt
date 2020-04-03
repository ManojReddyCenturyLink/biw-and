package com.centurylink.biwf.screens.common

import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ProgressBar
import android.widget.Toast
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.databinding.FragmentWebviewBinding

class CustomWebFragment : BaseFragment() {

    companion object {
        const val KEY_URL = "key_url"
        private const val RELOAD_COUNT = "reload_count"

        fun newInstance(url: String): CustomWebFragment {
            val fragment = CustomWebFragment()
            val args = Bundle()
            args.putString(KEY_URL, url)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var binding: FragmentWebviewBinding
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
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
        webView = binding.webviewContainer
        progressBar =binding.webviewProgress
        initWebViewProperties()
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

    fun onBackPressed()=true

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

        webView.webViewClient=object :DefaultWebViewClient(){
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError
            ) {
                Toast.makeText(view!!.context, R.string.webpage_error, Toast.LENGTH_SHORT).show();
            }
        }
        webView.loadUrl(url)
    }

    internal open class DefaultWebChromeClient : WebChromeClient() {}

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
}