package com.centurylink.biwf.utility.preferences

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class SharedPreferencesStore(private val context: Context) :
    KeyValueStore {

    override fun put(key: String, value: String): Boolean {
        val editor = sharedPreferences().edit()
        editor.putString(key, value)
        return editor.commit()
    }

    override fun put(key: String, value: Int): Boolean {
        val editor = sharedPreferences().edit()
        editor.putInt(key, value)
        return editor.commit()
    }

    override fun get(key: String): String? {
        return sharedPreferences().getString(key, "")
    }

    override fun getInt(key: String): Int? {
        return sharedPreferences().getInt(key, 0)
    }

    override fun remove(key: String): Boolean {
        val editor = sharedPreferences().edit()
        editor.remove(key)
        return editor.commit()
    }

    override fun getBoolean(key: String): Boolean? {
        return sharedPreferences().getBoolean(key, false)
    }

    override fun putBoolean(key: String, value: Boolean): Boolean {
        val editor = sharedPreferences().edit()
        editor.putBoolean(key, value)
        return editor.commit()
    }

    private fun sharedPreferences(): SharedPreferences {

        val spec = KeyGenParameterSpec.Builder(
            PREF_NAME,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(MasterKey.DEFAULT_AES_GCM_MASTER_KEY_SIZE)
            .build()

        val masterKey: MasterKey = MasterKey.Builder(context)
            .setKeyGenParameterSpec(spec)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey, // masterKey created above
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        );
    }

    companion object {
        private val PREF_NAME = "_androidx_security_master_key_"
    }
}