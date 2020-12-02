package com.centurylink.biwf.screens.common

import android.annotation.SuppressLint
import android.net.http.SslError
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.databinding.FragmentWebviewBinding

/**
 * Custom web fragment - This display web pages inside our application
 *
 * @constructor Create empty Custom web fragment
 */
@SuppressLint("SetJavaScriptEnabled")
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

    override val lifecycleOwner: LifecycleOwner = this

    private lateinit var binding: FragmentWebviewBinding
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private var url: String? = null
    private var reloadCount = 0

    /**
     * On create view - The onCreateView method is called when Fragment should create its View
     *                  object hierarchy
     *
     * @param inflater - LayoutInflater: The LayoutInflater object that can be used to
     *                   inflate any views in the fragment,
     * @param container - ViewGroup: If non-null, this is the parent view that the fragment's UI
     *                    should be attached to. The fragment should not add the view itself,
     *                    but this can be used to generate the LayoutParams of the view.
     *                    This value may be null.
     * @param savedInstanceState - Bundle: If non-null, this fragment is being re-constructed
     * @return - Return the View for the fragment's UI, or null.
     */
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

    /**
     * On view created - This gives subclasses a chance to initialize themselves once they know
     *                   their view hierarchy has been completely created
     *
     * @param view-View: The View returned by onCreateView(android.view.LayoutInflater,
     *                   android.view.ViewGroup, android.os.Bundle).
     * @param savedInstanceState - Bundle: If non-null, this fragment is being re-constructed
     *                            from a previous saved state as given here. This value may be null.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        savedInstanceState?.let {
            reloadCount = it.getInt(RELOAD_COUNT)
        }
        webView = binding.webviewContainer
        progressBar = binding.webviewProgress
        initWebViewProperties()
    }

    /**
     * On save instance state - Called to ask the fragment to save its current dynamic state,
     * so it can later be reconstructed in a new instance of its process is restarted
     *
     * @param outState - Bundle: Bundle in which to place your saved state.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(RELOAD_COUNT, reloadCount)
        super.onSaveInstanceState(outState)
    }

    /**
     * On resume - Called when the fragment is visible to the user and actively running
     *
     */
    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    /**
     * On pause - Called when the Fragment is no longer resumed
     *
     */
    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    /**
     * On destroy view - Called when the view previously created by onCreateView(LayoutInflater,
     * ViewGroup, Bundle) has been detached from the fragment.
     *
     */
    override fun onDestroyView() {
        super.onDestroyView()
        // Destroy the WebView completely.
        // The WebView must be removed from the view hierarchy before calling destroy to prevent a memory leak.
        (webView.parent as ViewGroup).removeView(webView)
        webView.removeAllViews()
        webView.destroy()
    }

    /**
     * Init web view properties
     *
     */
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

        webView.webViewClient = object : DefaultWebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError
            ) {
                Toast.makeText(view!!.context, R.string.webpage_error, Toast.LENGTH_SHORT).show()
            }
        }
        webView.loadUrl(url)
    }

    /**
     * Default web chrome client
     *
     * @constructor Create empty Default web chrome client
     */
    internal open class DefaultWebChromeClient : WebChromeClient()

    /**
     * Default web view client - This class Allow the user to navigate backward and forward through
     *                           their web page history that's maintained by your WebView
     *
     * @constructor Create empty Default web view client
     */
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
