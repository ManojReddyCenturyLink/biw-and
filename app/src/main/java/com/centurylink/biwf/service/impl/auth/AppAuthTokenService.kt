package com.centurylink.biwf.service.impl.auth

import com.centurylink.biwf.service.auth.AccessTokenGenerator
import com.centurylink.biwf.service.auth.TokenService
import com.centurylink.biwf.service.auth.TokenStorage
import net.openid.appauth.AuthState
import javax.inject.Inject

class AppAuthTokenService @Inject constructor(
    override val tokenStorage: TokenStorage<AuthState>,
    override val accessTokenGenerator: AccessTokenGenerator
) : TokenService {

    override fun clearToken() {
        tokenStorage.state = null
    }

    override fun invalidateToken() {
        (accessTokenGenerator as AppAuthAccessTokenGenerator)
            .tokenIsInvalidated
            .set(true)
    }
}
