package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.usagedetails.TrafficUsageResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.HeaderMap

interface AssiaTrafficUsageService {

    @GET(EnvironmentPath.API_USAGE_INFO_PATH)
    suspend fun getUsageDetails(
        @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<TrafficUsageResponse>
}