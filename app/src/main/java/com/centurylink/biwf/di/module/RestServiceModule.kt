@file:Suppress("unused")

package com.centurylink.biwf.di.module

import com.centurylink.biwf.di.qualifier.ClientType
import com.centurylink.biwf.di.qualifier.HttpClient
import com.centurylink.biwf.service.impl.network.NoAuthHttpClient
import com.centurylink.biwf.service.impl.network.OAuthHttpClient
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

/**
 * Configures the D.I. graph for the various REST services.
 *
 * This module configures an OkHttpClient that provides the proper access-token for
 * the HTTP request that need authentication/authorization.
 */
@Module
abstract class RestServiceModule {
    @Singleton
    @Binds
    @HttpClient(ClientType.OAUTH)
    abstract fun provideOAuthCallFactory(client: OAuthHttpClient): okhttp3.Call.Factory

    @Singleton
    @Binds
    @HttpClient(ClientType.NONE)
    abstract fun provideOkHttpClientFactory(client: NoAuthHttpClient): okhttp3.Call.Factory
}
