package com.centurylink.biwf.di.qualifier

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseUrl(val type: BaseUrlType)

enum class BaseUrlType {
    FIBER_SERVICES,
    AWS_BUCKET_SERVICES,
    LOCAL_INTEGRATION
}
