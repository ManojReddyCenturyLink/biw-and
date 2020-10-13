package com.centurylink.biwf.utility

import androidx.fragment.app.FragmentActivity
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
        /**
         * It will initialises the pendo sdk
         */
        fun initPendoSDK(context: FragmentActivity?, visitorId: String) {
            val pendoParams = PendoInitParams()
            pendoParams.visitorId = visitorId

            //send Visitor Level Data
            val userData: MutableMap<String, Any> = HashMap()
            userData["age"] = 27
            userData["country"] = "USA"
            pendoParams.userData = userData

            Pendo.initSDK(context, BuildConfig.PENDO_APIKEY, pendoParams)
        }
    }
}