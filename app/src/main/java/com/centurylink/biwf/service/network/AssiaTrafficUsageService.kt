package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.usagedetails.TrafficUsageResponse
import retrofit2.http.GET
import retrofit2.http.HeaderMap

interface AssiaTrafficUsageService {

    @GET("api/v2/wifi/diags/station/traffic")
    suspend fun getUsageDetails(
        @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<TrafficUsageResponse>
}