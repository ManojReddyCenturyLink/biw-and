package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.wifi.*

import retrofit2.http.*

interface WifiNetworkApiService {

    @GET("api/v2/wifi/operations/ssid/{wifiDeviceId}/{interface}")
    suspend fun getNetworkName(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<NetworkDetails>

    @POST("api/v2/wifi/operations/ssid/{wifiDeviceId}/{interface}")
    suspend fun updateNetworkName(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>,
        @Body updateNetworkName: UpdateNetworkName
    ): AssiaServiceResult<UpdateNetworkResponse>

    @POST("api/v2/wifi/operations/enableintf/{wifiDeviceId}/{interface}")
    suspend fun enableNetwork(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<UpdateNetworkResponse>

    @POST("api/v2/wifi/operations/wifipwd/{wifiDeviceId}/{interface}")
    suspend fun updateNetworkPassword(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>,
        @Body updateNwPwd: UpdateNWPassword
    ): AssiaServiceResult<UpdateNetworkResponse>

    @POST("api/v2/wifi/operations/disableintf/{wifiDeviceId}/{interface}")
    suspend fun disableNetwork(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<UpdateNetworkResponse>

    @GET("api/v2/wifi/operations/wifipwd/{wifiDeviceId}/{interface}")
    suspend fun getNetworkPassword(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<NetworkDetails>
}