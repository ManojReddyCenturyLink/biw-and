package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.service.impl.aasia.AsiaaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface AssiaService {

    //Todo this is a temporary call and will be removed post-Apigee
    @POST("oauth/token?username=biwftest&password=BiwfTest1&client_id=spapi&client_secret=oBj2xZc&grant_type=password")
    suspend fun getAssiaTokenWithTokenObject(): AssiaNetworkResponse<AssiaToken, AsiaaError>

    //Todo add 'api/v2' to base url
    @GET("api/v2/wifi/diags/apinfo")
    suspend fun getModemInfo(@HeaderMap header: Map<String, String>): AssiaNetworkResponse<ModemInfoResponse, AsiaaError>

    @GET("api/v2/wifi/diags/stationinfo")
    suspend fun getDevicesList(@HeaderMap header: Map<String, String>): AssiaNetworkResponse<DevicesInfo,AsiaaError>
}