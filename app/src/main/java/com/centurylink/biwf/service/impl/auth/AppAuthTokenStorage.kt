package com.centurylink.biwf.service.impl.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.centurylink.biwf.service.auth.TokenStorage
import com.centurylink.biwf.utility.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import net.openid.appauth.AuthState
import javax.inject.Inject

class AppAuthTokenStorage @Inject constructor(
    private val appContext: Context
) : TokenStorage<AuthState> {

    private val preferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "AccountPrefData",
            "${appContext.packageName}._preferences_data_",
            appContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val authTokenKey = "_preferences_data_"
    private val policyKey = "_contract_data_"

    override var state: AuthState?
        get() = synchronized(this) {
            preferences.getString(authTokenKey, null)?.let { AuthState.jsonDeserialize(it) }
        }
        set(value) = synchronized(this) {
            val hasToken = if (value != null) {
                preferences.edit()
                    .putString(authTokenKey, value.jsonSerializeString())
                    .apply()
                true

            } else {
                preferences.edit()
                    .remove(authTokenKey)
                    .remove(policyKey)
                    .apply()
                false
            }

            _stateChanges.value = hasToken
        }

    override var currentPolicy: String?
        get() = synchronized(this) {
            preferences.getString(policyKey, null)
        }
        set(value) = synchronized(this) {
            if (value != null) {
                preferences.edit().putString(policyKey, value).apply()
            } else {
                preferences.edit().remove(policyKey).apply()
            }
        }

    private val _stateChanges = MutableStateFlow<Boolean>()

    @Suppress("EXPERIMENTAL_API_USAGE")
    override val hasToken: Flow<Boolean> = _stateChanges.distinctUntilChanged()
}
