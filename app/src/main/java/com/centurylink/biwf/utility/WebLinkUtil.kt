package com.centurylink.biwf.utility

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
    }
}