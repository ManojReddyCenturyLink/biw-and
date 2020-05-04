package com.centurylink.biwf.service.impl.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import com.centurylink.biwf.service.auth.TokenStorage
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.BehaviorProcessor
import io.reactivex.rxjava3.processors.FlowableProcessor
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

            _stateChanges.onNext(hasToken)
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

    private val _stateChanges: FlowableProcessor<Boolean> =
        BehaviorProcessor.create()

    override val hasToken: Flowable<Boolean> = _stateChanges.distinctUntilChanged()
}
