package com.centurylink.biwf.service.impl.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.centurylink.biwf.service.auth.EncryptionUtils
import com.centurylink.biwf.service.auth.TokenStorage
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EnvironmentPath
import kotlinx.coroutines.flow.Flow
import net.openid.appauth.AuthState
import javax.inject.Inject

class AppAuthTokenStorage @Inject constructor(
    private val appContext: Context
) : TokenStorage<AuthState> {

    override var state: AuthState?
        get() = synchronized(this) {
            EncryptionUtils.decrypt(
                appContext,
                preferences.getString(EnvironmentPath.getAuthTokenKey(), null))?.let { AuthState.jsonDeserialize(it) }
        }
        set(value) = synchronized(this) {
            val hasToken = if (value != null) {
                EncryptionUtils.encrypt(appContext, value.jsonSerializeString())
                preferences.edit()
                    .putString(
                        EnvironmentPath.getAuthTokenKey(),
                        EncryptionUtils.encrypt(appContext, value.jsonSerializeString())
                    )
                    .apply()
                true
            } else {
                EncryptionUtils.clear()
                preferences.edit().clear().commit()
                false
            }
            _stateChanges.value = hasToken
        }

    private val preferences: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "AccountPrefData",
            "${appContext.packageName}._preferences_data_",
            appContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val _stateChanges = BehaviorStateFlow<Boolean>()

    @Suppress("EXPERIMENTAL_API_USAGE")
    override val hasToken: Flow<Boolean> = _stateChanges
}
