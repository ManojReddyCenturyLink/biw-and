@file:Suppress("unused")

package com.centurylink.biwf.di.module

import com.centurylink.biwf.service.auth.AccessTokenGenerator
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.TokenService
import com.centurylink.biwf.service.auth.TokenStorage
import com.centurylink.biwf.service.impl.auth.AppAuthAccessTokenGenerator
import com.centurylink.biwf.service.impl.auth.AppAuthAuthServiceFactory
import com.centurylink.biwf.service.impl.auth.AppAuthTokenService
import com.centurylink.biwf.service.impl.auth.AppAuthTokenStorage
import dagger.Binds
import dagger.Module
import net.openid.appauth.AuthState
import javax.inject.Singleton

/**
 * Configures the D.I. graph for the various authorization services.
 *
 * This module configures the AppAuth library for Android to be used to handle the implementation
 * of the various authorization services.
 */
@Module
abstract class AuthServiceModule {
    @Singleton
    @Binds
    abstract fun provideTokenStorage(storage: AppAuthTokenStorage): TokenStorage<AuthState>

    @Singleton
    @Binds
    abstract fun provideAppAuthAccessTokenGenerator(generator: AppAuthAccessTokenGenerator): AccessTokenGenerator

    @Singleton
    @Binds
    abstract fun provideAppAuthAuthServiceFactory(factory: AppAuthAuthServiceFactory): AuthServiceFactory<*>

    @Singleton
    @Binds
    abstract fun provideAppAuthTokenService(service: AppAuthTokenService): TokenService
}
