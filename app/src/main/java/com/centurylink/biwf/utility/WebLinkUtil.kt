package com.centurylink.biwf.utility

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import timber.log.Timber

class WebLinkUtil {

    companion object {

        fun handleClick(urlInput: Any?, activity: Activity) {
            var urlScheme: String? = null
            var url: Uri? = null

            when (urlInput) {
                is String -> {
                    url = Uri.parse(urlInput)
                    urlScheme = url.scheme
                }
            }
            when (urlScheme) {

                "tel" -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, url)
                        activity.startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Timber.e("Not supported by device!")
                    }
                }
            }
        }

        /**
         * This method is used to set up webView
         * @param isFromOnBoarding
         * @param webView
         * @param isZoomEnabled
         */
        fun setupWebView(isFromOnBoarding: Boolean, webView: WebView, isZoomEnabled: Boolean) {
            webView.settings.javaScriptEnabled = isFromOnBoarding

            webView.settings.allowFileAccess = false

            webView.settings.allowContentAccess = false

            webView.settings.allowUniversalAccessFromFileURLs = false

            webView.settings.safeBrowsingEnabled = true

            webView.settings.setGeolocationEnabled(false)

            webView.settings.allowFileAccessFromFileURLs = false

            if (isZoomEnabled) {
                webView.settings.builtInZoomControls = true
                webView.settings.displayZoomControls = false
            }
        }
    }
}
