package com.centurylink.biwf.di.qualifier

import javax.inject.Qualifier

@Qualifier
@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
annotation class HttpClient(val type: ClientType)

enum class ClientType {
    OAUTH,
    NONE
}
