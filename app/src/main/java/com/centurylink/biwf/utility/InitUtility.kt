package com.centurylink.biwf.utility

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.BuildConfig
import com.centurylink.biwf.di.component.DaggerApplicationComponent
import com.centurylink.biwf.di.module.AuthServiceConfigModule
import com.centurylink.biwf.di.module.RestServiceConfigModule
import com.centurylink.biwf.service.impl.integration.IntegrationServer
import com.centurylink.biwf.service.integration.IntegrationServerService

class InitUtility {
    companion object {
        fun initDependencyInjection(app: BIWFApp) {
            DaggerApplicationComponent
                .builder()
                .applicationContext(app)
                .authServiceConfig(authServiceConfig)
                .restServiceConfig(restServiceConfig)
                .build().inject(app)
        }
    }
}

private val authServiceConfig = AuthServiceConfigModule(
    authorizationEndpoint = BuildConfig.AUTHORIZATION_ENDPOINT,
    tokenEndpoint = BuildConfig.TOKEN_ENDPOINT,
    clientId = BuildConfig.CLIENT_ID,
    redirectUrl = BuildConfig.REDIRECT_URL,
    scope = BuildConfig.SCOPE,
    revokeTokenEndpoint = BuildConfig.REVOKE_TOKEN_ENDPOINT
)

private val restServiceConfig = RestServiceConfigModule(
    baseUrlFiberServices = BuildConfig.BASE_SALESFORCE_URL,
    baseUrlForAwsBucket = "https://bucketforapi.s3-eu-west-1.amazonaws.com/",
    baseUrlForAssiaServices = " https://ctlink-biwf-staging.cloudcheck.net:443/cloudcheck-sp/",
    integrationServerService = object : IntegrationServerService {
        override val baseUrl: String = IntegrationServer.baseUrl

        override fun start() {
            IntegrationServer.start()
        }

        override fun stop() {
            IntegrationServer.stop()
        }
    }
)
