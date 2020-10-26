package com.centurylink.biwf.utility

import android.content.Context
import android.net.ConnectivityManager

class AppUtil {

    companion object {
        /**
         * Check internet connection
         */
        @Suppress("DEPRECATION")
        fun isOnline(context: Context?): Boolean {
            var isOnline = false
            context?.let {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = connectivityManager.activeNetworkInfo
                isOnline = networkInfo != null && networkInfo.isConnected
            }
            return isOnline
        }
    }
}
