package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.usagedetails.UsageDetails
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Path

interface AssiaTrafficUsageService {

    @GET("api/v2/wifi/diags/station/{apiPath}")
    suspend fun getUsageDetails(
        @Path("apiPath") value1: String, @HeaderMap header: Map<String, String>
    ): UsageDetails
}