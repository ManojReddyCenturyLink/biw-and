package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.usagedetails.TrafficUsageResponse
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface AssiaTrafficUsageService {

    @GET("api/v2/wifi/diags/station/{traffic}")
    suspend fun getUsageDetails(
        @Path("traffic") value: String, @HeaderMap header: Map<String, String>
    ): TrafficUsageResponse
}