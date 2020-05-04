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
    private val rootUrl: String,
    private val clientId: String,
    private val scope: String,
    private val redirectUrl: String
) {
    @Singleton
    @Provides
    fun provideAuthServiceConfig(): AuthServiceConfig {
        return AuthServiceConfig(
            configurationUrl = rootUrl,
            clientId = clientId,
            scope = scope,
            authRedirectUrl = redirectUrl,
            responseType = ResponseTypeValues.TOKEN,
            extraParams = mapOf("device_id" to "611118D9-A9CC-4276-8F7A-22D09EA105AB")
        )
    }
}
