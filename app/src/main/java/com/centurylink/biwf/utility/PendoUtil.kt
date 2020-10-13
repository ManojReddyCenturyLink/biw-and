package com.centurylink.biwf.utility

import android.app.Activity
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.BuildConfig
import sdk.pendo.io.Pendo
import sdk.pendo.io.Pendo.PendoInitParams

/**
 * Pendo util- It will have pendo sdk integration related details
 *
 * @constructor Create empty Pendo util
 */
class PendoUtil {

    companion object {

        /** It will initialises the pendo sdk with out visitor
         *
         */
        fun initPendoSDKWithoutVisitor(context: BIWFApp) {
            Pendo.initSdkWithoutVisitor(context, BuildConfig.APP_KEY, null)
        }

        /**
         * It will initialises the pendo sdk with visitor
         */
        fun initPendoSDKWithVisitor(context: Activity, visitorId: String) {
            val pendoParams = PendoInitParams()
            pendoParams.visitorId = visitorId
            Pendo.initSDK(context, BuildConfig.APP_KEY, pendoParams)
        }
    }

}