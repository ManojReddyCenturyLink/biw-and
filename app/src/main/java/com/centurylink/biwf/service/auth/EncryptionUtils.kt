package com.centurylink.biwf.service.auth

import android.content.Context
import android.os.Build
import com.centurylink.biwf.service.auth.EncryptionKeyGenerator.generateKeyPairPreM
import com.centurylink.biwf.service.auth.EncryptionKeyGenerator.generateSecretKey
import com.centurylink.biwf.service.auth.EncryptionKeyGenerator.generateSecretKeyPre18
import timber.log.Timber
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException

/**
 * EncryptionUtils class for keystore implementation
 */
object EncryptionUtils {
    fun encrypt(context: Context, token: String?): String? {
        val securityKey = getSecurityKey(context)
        return securityKey?.encrypt(token)
    }

    fun decrypt(context: Context, token: String?): String? {
        val securityKey = getSecurityKey(context)
        return securityKey?.decrypt(token)
    }

    private fun getSecurityKey(context: Context): SecurityKey? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            generateSecretKey(keyStore!!)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            generateKeyPairPreM(
                context,
                keyStore!!
            )
        } else {
            generateSecretKeyPre18(context)
        }
    }

    private val keyStore: KeyStore?
        private get() {
            var keyStore: KeyStore? = null
            try {
                keyStore = KeyStore.getInstance(EncryptionKeyGenerator.ANDROID_KEY_STORE)
                keyStore.load(null)
            } catch (e: KeyStoreException) {
                Timber.e(e)
            } catch (e: CertificateException) {
                Timber.e(e)
            } catch (e: NoSuchAlgorithmException) {
                Timber.e(e)
            } catch (e: IOException) {
                Timber.e(e)
            }
            return keyStore
        }

    fun clear() {
        val keyStore = keyStore
        try {
            if (keyStore!!.containsAlias(EncryptionKeyGenerator.KEY_ALIAS)) {
                keyStore.deleteEntry(EncryptionKeyGenerator.KEY_ALIAS)
            }
        } catch (e: KeyStoreException) {
            Timber.e(e)
        }
    }
}
