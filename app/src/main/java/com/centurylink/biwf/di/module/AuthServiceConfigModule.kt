package com.centurylink.biwf.di.module

import com.centurylink.biwf.service.auth.AuthServiceConfig
import dagger.Module
import dagger.Provides
import net.openid.appauth.ResponseTypeValues
import javax.inject.Singleton

/**
 * Provides the configuration for the various authorization services.
 */
@Module
class AuthServiceConfigModule(
    private val authorizationEndpoint: String,
    private val tokenEndpoint: String,
    private val clientId: String,
    private val redirectUrl: String,
    private val scope: String,
    private val revokeTokenEndpoint: String
) {
    @Singleton
    @Provides
    fun provideAuthServiceConfig(): AuthServiceConfig {
        return AuthServiceConfig(
            authorizationEndpoint = authorizationEndpoint,
            tokenEndpoint = tokenEndpoint,
            clientId = clientId,
            authRedirectUrl = redirectUrl,
            scope = scope,
            responseType = ResponseTypeValues.CODE,
            revokeTokenEndpoint = revokeTokenEndpoint
        )
    }
}
