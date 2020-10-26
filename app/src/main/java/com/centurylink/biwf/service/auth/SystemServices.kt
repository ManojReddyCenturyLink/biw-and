package com.centurylink.biwf.service.auth

import android.annotation.TargetApi
import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build

/**
 * This class contains System services
 *
 * @property context
 * @constructor Create empty System services
 */
@TargetApi(Build.VERSION_CODES.M)
class SystemServices(private val context: Context) {

    companion object {
        fun hasMarshmallow() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * There is a nice [FingerprintManagerCompat] class that makes all dirty work for us, but as always, shit happens.
     * Behind the scenes it is using `Context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)`
     * method, that is returning false on 23 API emulators, when in fact [FingerprintManager] is there and is working fine.
     */
    private var fingerprintManager: FingerprintManager? = null

    init {
        if (hasMarshmallow()) {
            fingerprintManager =
                context.getSystemService(Context.FINGERPRINT_SERVICE) as FingerprintManager
        }
    }

}