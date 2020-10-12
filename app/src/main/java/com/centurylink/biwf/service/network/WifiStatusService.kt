package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.wifi.UpdateNetworkResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.Headers

import retrofit2.http.POST
import retrofit2.http.QueryMap

interface WifiStatusService {

    @POST(EnvironmentPath.API_WIFI_OPERATIONS_ENABLE)
    @Headers(MOBILE_HEADER)
    suspend fun enableNetwork(
        @QueryMap query: Map<String, String>
    ): AssiaServiceResult<UpdateNetworkResponse>

    @POST(EnvironmentPath.API_WIFI_OPERATIONS_DISABLE)
    @Headers(MOBILE_HEADER)
    suspend fun disableNetwork(
        @QueryMap query: Map<String, String>
    ): AssiaServiceResult<UpdateNetworkResponse>

    companion object {
        private const val MOBILE_HEADER = "From: mobile"
    }
}