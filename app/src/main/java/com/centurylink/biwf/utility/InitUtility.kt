package com.centurylink.biwf.utility

import com.centurylink.biwf.BIWFApp
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
    authorizationEndpoint = EnvironmentPath.getAuthorizationEndpoint(),
    tokenEndpoint = EnvironmentPath.getTokenEndpoint(),
    clientId = EnvironmentPath.getClientId(),
    redirectUrl = EnvironmentPath.REDIRECT_URI,
    scope = EnvironmentPath.SCOPE,
    revokeTokenEndpoint = EnvironmentPath.getRevokeTokenEndpoint()
)

private val restServiceConfig = RestServiceConfigModule(
    baseUrlFiberServices = EnvironmentPath.getSalesForceVersionURl(),
    baseUrlSupportServices = EnvironmentPath.getSalesForceBaseURl(),
    baseUrlScheduleCallbackServices = EnvironmentPath.getSalesForceBaseURl(),
    baseUrlForAwsBucket = EnvironmentPath.AWS_BASE_URL,
    baseUrlForAssiaServices = EnvironmentPath.ASSIA_BASE_URL,
    baseUrlForMcafeeServices = EnvironmentPath.getApigeeVersionUrl(),
    baseUrlForOauthAssiaServices = EnvironmentPath.geApigeeCloudCheckURl(),
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
