package com.centurylink.biwf.service.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.centurylink.biwf.utility.EnvironmentPath

/**
 * This class contains the encryption and decryption details
 *
 * @constructor
 *
 * @param context application instance
 */
class EncryptionServices(context: Context) {

    /**
     * The place to keep all constants.
     */
    companion object {
        val DEFAULT_KEY_STORE_NAME = "default_keystore"
    }

    private val preferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "AccountPrefData",
            "${context.packageName}._preferences_data_",
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val keyStoreWrapper = KeyStoreWrapper(context, DEFAULT_KEY_STORE_NAME)

    /**
     * Create and save cryptography key, to protect Secrets with.
     */
    fun createMasterKey(password: String? = null) {
        if (SystemServices.hasMarshmallow()) {
            createAndroidSymmetricKey()
        } else {
            createDefaultSymmetricKey(password ?: "")
        }
    }

    /**
     * Encrypt data and Secrets with created master key.
     */
    fun encrypt(data: String, keyPassword: String? = null) {
        var encryptedValue = if (SystemServices.hasMarshmallow()) {
            encryptWithAndroidSymmetricKey(data)
        } else {
            encryptWithDefaultSymmetricKey(data, keyPassword ?: "")
        }
        preferences.edit()
            .putString(EnvironmentPath.getAuthTokenKey(), encryptedValue)
            .apply()
    }

    /**
     * Decrypt data and Secrets with created master key.
     */
    fun decrypt(keyPassword: String? = null): String? {
        var encryptedData = preferences.getString(EnvironmentPath.getAuthTokenKey(), null)
        if (encryptedData.isNullOrEmpty()) {
            return null
        }
        return if (SystemServices.hasMarshmallow()) {
            decryptWithAndroidSymmetricKey(encryptedData)
        } else {
            decryptWithDefaultSymmetricKey(encryptedData, keyPassword ?: "")
        }
    }

    private fun createAndroidSymmetricKey() {
        keyStoreWrapper.createAndroidKeyStoreSymmetricKey(EnvironmentPath.getMasterKey())
    }

    private fun encryptWithAndroidSymmetricKey(data: String): String {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(EnvironmentPath.getMasterKey())
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).encrypt(data, masterKey, true)
    }

    private fun decryptWithAndroidSymmetricKey(data: String): String? {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(EnvironmentPath.getMasterKey())
        if (data != null && !data.isNullOrEmpty()) {
            return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).decrypt(
                data,
                masterKey,
                true
            )
        }
        return null
    }

    private fun createDefaultSymmetricKey(password: String) {
        keyStoreWrapper.createDefaultKeyStoreSymmetricKey(EnvironmentPath.getMasterKey(), password)
    }

    private fun encryptWithDefaultSymmetricKey(data: String, keyPassword: String): String {
        val masterKey = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(EnvironmentPath.getMasterKey(), keyPassword)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).encrypt(data, masterKey, true)
    }

    fun cleanUp() {
        preferences.edit().remove(EnvironmentPath.getAuthTokenKey()).commit()
    }

    private fun decryptWithDefaultSymmetricKey(data: String, keyPassword: String): String {
        val masterKey = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(EnvironmentPath.getMasterKey(), keyPassword)
        return masterKey?.let {
            CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).decrypt(
                data,
                masterKey,
                true
            )
        } ?: ""
    }
}
