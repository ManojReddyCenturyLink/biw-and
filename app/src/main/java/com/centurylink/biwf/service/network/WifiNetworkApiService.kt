package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import retrofit2.http.*

interface WifiNetworkApiService {

    @GET("api/v2/wifi/operations/ssid/{wifiDeviceId}/{interface}")
    suspend fun getNetworkName(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaNetworkResponse<NetworkDetails, AssiaError>

    @POST("api/v2/wifi/operations/ssid/{wifiDeviceId}/{interface}")
    suspend fun updateNetworkName(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>,
        @Body updateNetworkName: UpdateNetworkName
    ): AssiaNetworkResponse<UpdateNetworkResponse, AssiaError>

    @POST("api/v2/wifi/operations/enableintf/{wifiDeviceId}/{interface}")
    suspend fun enableNetwork(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaNetworkResponse<UpdateNetworkResponse, AssiaError>

    @POST("api/v2/wifi/operations/wifipwd/{wifiDeviceId}/{interface}")
    suspend fun updateNetworkPassword(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>,
        @Body updateNwPwd: UpdateNWPassword
    ): AssiaNetworkResponse<UpdateNetworkResponse, AssiaError>

    @POST("api/v2/wifi/operations/disableintf/{wifiDeviceId}/{interface}")
    suspend fun disableNetwork(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaNetworkResponse<UpdateNetworkResponse, AssiaError>

    @GET("api/v2/wifi/operations/wifipwd/{wifiDeviceId}/{interface}")
    suspend fun getNetworkPassword(
        @Path("wifiDeviceId") wifiDeviceId: String,
        @Path("interface") interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaNetworkResponse<NetworkDetails, AssiaError>
}