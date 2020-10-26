package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.utility.EnvironmentPath

import retrofit2.http.*

interface WifiNetworkApiService {

    @GET(EnvironmentPath.API_GET_POST_SSID_PATH)
    suspend fun getNetworkName(
        @Path(EnvironmentPath.WIFI_DEVICE_ID) wifiDeviceId: String,
        @Path(EnvironmentPath.INTERFACE_VALUE) interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<NetworkDetails>

    @POST(EnvironmentPath.API_ENABLE_REGULAR_GUEST_WIFI_PATH)
    suspend fun enableNetwork(
        @Path(EnvironmentPath.WIFI_DEVICE_ID) wifiDeviceId: String,
        @Path(EnvironmentPath.INTERFACE_VALUE) interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<UpdateNetworkResponse>

    @POST(EnvironmentPath.API_DISABLE_REGULAR_GUEST_WIFI_PATH)
    suspend fun disableNetwork(
        @Path(EnvironmentPath.WIFI_DEVICE_ID) wifiDeviceId: String,
        @Path(EnvironmentPath.INTERFACE_VALUE) interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<UpdateNetworkResponse>

    @GET(EnvironmentPath.API_GET_CHANGE_NETWORK_PASSWORD_PATH)
    suspend fun getNetworkPassword(
        @Path(EnvironmentPath.WIFI_DEVICE_ID) wifiDeviceId: String,
        @Path(EnvironmentPath.INTERFACE_VALUE) interfaceType: NetWorkBand, @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<NetworkDetails>
}