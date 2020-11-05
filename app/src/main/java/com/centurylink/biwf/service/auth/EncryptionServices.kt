package com.centurylink.biwf.service.auth

import android.content.Context

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

        val MASTER_KEY = "MASTER_KEY"
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
    fun encrypt(data: String, keyPassword: String? = null): String {
        return if (SystemServices.hasMarshmallow()) {
            encryptWithAndroidSymmetricKey(data)
        } else {
            encryptWithDefaultSymmetricKey(data, keyPassword ?: "")
        }
    }

    /**
     * Decrypt data and Secrets with created master key.
     */
    fun decrypt(data: String, keyPassword: String? = null): String? {
        return if (SystemServices.hasMarshmallow()) {
            decryptWithAndroidSymmetricKey(data)
        } else {
            decryptWithDefaultSymmetricKey(data, keyPassword ?: "")
        }
    }

    private fun createAndroidSymmetricKey() {
        keyStoreWrapper.createAndroidKeyStoreSymmetricKey(MASTER_KEY)
    }

    private fun encryptWithAndroidSymmetricKey(data: String): String {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).encrypt(data, masterKey, true)
    }

    private fun decryptWithAndroidSymmetricKey(data: String): String? {
        val masterKey = keyStoreWrapper.getAndroidKeyStoreSymmetricKey(MASTER_KEY)
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
        keyStoreWrapper.createDefaultKeyStoreSymmetricKey(MASTER_KEY, password)
    }

    private fun encryptWithDefaultSymmetricKey(data: String, keyPassword: String): String {
        val masterKey = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(MASTER_KEY, keyPassword)
        return CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).encrypt(data, masterKey, true)
    }

    private fun decryptWithDefaultSymmetricKey(data: String, keyPassword: String): String {
        val masterKey = keyStoreWrapper.getDefaultKeyStoreSymmetricKey(MASTER_KEY, keyPassword)
        return masterKey?.let {
            CipherWrapper(CipherWrapper.TRANSFORMATION_SYMMETRIC).decrypt(
                data,
                masterKey,
                true
            )
        } ?: ""
    }
}
