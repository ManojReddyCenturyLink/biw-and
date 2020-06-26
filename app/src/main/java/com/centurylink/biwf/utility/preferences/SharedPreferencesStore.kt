package com.centurylink.biwf.utility.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.databinding.library.BuildConfig

class SharedPreferencesStore(private val context: Context) :
    KeyValueStore {

    override fun put(key: String, value: String): Boolean {
        val editor = sharedPreferences().edit()
        editor.putString(key, value)
        return editor.commit()
    }

    override fun get(key: String): String? {
        return sharedPreferences().getString(key, "")
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
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private val PREF_NAME = BuildConfig.LIBRARY_PACKAGE_NAME + ".SharedPrefs"
    }
}