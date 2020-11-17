package com.centurylink.biwf.service.impl.auth

import android.content.Context
import com.centurylink.biwf.service.auth.EncryptionServices
import com.centurylink.biwf.service.auth.TokenStorage
import com.centurylink.biwf.utility.BehaviorStateFlow
import kotlinx.coroutines.flow.Flow
import net.openid.appauth.AuthState
import javax.inject.Inject

class AppAuthTokenStorage @Inject constructor(
    private val appContext: Context
) : TokenStorage<AuthState> {

    private val authTokenKey = "_preferences_data_"

    override var state: AuthState?
        get() = synchronized(this) {
                EncryptionServices(appContext).decrypt(authTokenKey)
                    ?.let { AuthState.jsonDeserialize(it) }
        }
        set(value) = synchronized(this) {
            val encryptionService = EncryptionServices(appContext)
            encryptionService.createMasterKey(authTokenKey)
            val hasToken = if (value != null) {
                    EncryptionServices(appContext).encrypt(
                        value.jsonSerializeString(),
                        authTokenKey)
                true
            } else {
                EncryptionServices(appContext).cleanUp()
                false
            }
            _stateChanges.value = hasToken
        }

    private val _stateChanges = BehaviorStateFlow<Boolean>()

    @Suppress("EXPERIMENTAL_API_USAGE")
    override val hasToken: Flow<Boolean> = _stateChanges
}
