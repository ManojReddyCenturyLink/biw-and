package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.wifi.NetWorkBand
import com.centurylink.biwf.model.wifi.UpdateNWPassword
import com.centurylink.biwf.model.wifi.UpdateNetworkName
import com.centurylink.biwf.model.wifi.UpdateNetworkResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.*

interface WifiStatusService {

    @POST(EnvironmentPath.API_WIFI_OPERATIONS_ENABLE)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun enableNetwork(
        @QueryMap query: Map<String, String>
    ): AssiaServiceResult<UpdateNetworkResponse>

    @POST(EnvironmentPath.API_WIFI_OPERATIONS_DISABLE)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun disableNetwork(
        @QueryMap query: Map<String, String>
    ): AssiaServiceResult<UpdateNetworkResponse>

    @POST(EnvironmentPath.API_CHANGE_SSID)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun updateNetworkName(
        @Body updateNetworkName: UpdateNetworkName
    ): AssiaServiceResult<UpdateNetworkResponse>

    @POST(EnvironmentPath.API_CHANGE_PASSWORD)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun updateNetworkPassword(
        @Query(EnvironmentPath.WIFI_DEVICE_ID) wifiDeviceId: String,
        @Query(EnvironmentPath.INTERFACE_VALUE) interfaceType: NetWorkBand,
        @Body updateNwPwd: UpdateNWPassword
    ): AssiaServiceResult<UpdateNetworkResponse>
}