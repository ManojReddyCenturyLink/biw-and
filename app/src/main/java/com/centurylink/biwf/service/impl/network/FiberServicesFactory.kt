package com.centurylink.biwf.service.impl.network

import com.centurylink.biwf.di.qualifier.BaseUrl
import com.centurylink.biwf.di.qualifier.BaseUrlType
import com.centurylink.biwf.service.network.TestRestServices
import retrofit2.Retrofit
import javax.inject.Inject

class FiberServicesFactory @Inject constructor(
    @BaseUrl(BaseUrlType.FIBER_SERVICES) retrofit: Retrofit
) {
    @Deprecated("Temporary interface for P.O.C.")
    val testRestServices: TestRestServices = retrofit.create()
}
