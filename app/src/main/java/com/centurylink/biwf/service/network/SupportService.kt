package com.centurylink.biwf.service.network


import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.support.SupportServicesReq
import com.centurylink.biwf.model.support.SupportServicesResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.*

/**
 * Support service Interface
 *
 */
interface SupportService {

    @POST(EnvironmentPath.API_SUPPORT_SERVICES_PATH)
    suspend fun supportServiceInfo(
        @HeaderMap header: Map<String, String>,
        @Body supportServicesReq: SupportServicesReq
    ): FiberServiceResult<SupportServicesResponse>

}