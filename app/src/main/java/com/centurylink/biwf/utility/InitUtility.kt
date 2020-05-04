package com.centurylink.biwf.utility

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.BuildConfig
import com.centurylink.biwf.di.component.DaggerApplicationComponent
import com.centurylink.biwf.di.module.AuthServiceConfigModule
import com.centurylink.biwf.di.module.RestServiceConfigModule

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
    rootUrl = BuildConfig.ROOT_URL,
    clientId = BuildConfig.CLIENT_ID,
    scope = BuildConfig.SCOPE,
    redirectUrl = BuildConfig.REDIRECT_URL
)

private val restServiceConfig = RestServiceConfigModule(
    baseUrlFiberServices = BuildConfig.BASE_URL,
    baseUrlForAwsBucket = "https://bucketforapi.s3-eu-west-1.amazonaws.com/"
)
