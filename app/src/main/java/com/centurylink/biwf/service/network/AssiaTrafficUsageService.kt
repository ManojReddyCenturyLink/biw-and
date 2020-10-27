package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.usagedetails.TrafficUsageResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.Query

interface AssiaTrafficUsageService {

    @GET(EnvironmentPath.API_USAGE_INFO_PATH)
    suspend fun getUsageDetails(
        @HeaderMap header: Map<String, String>,
        @Query(EnvironmentPath.AASIA_ID_TRAFFIC) assiaIdTraffic: String,
        @Query(EnvironmentPath.START_DATE) startDateTraffic: String,
        @Query(EnvironmentPath.STAT_MAC) staMacTraffic: String
    ): AssiaServiceResult<TrafficUsageResponse>
}
