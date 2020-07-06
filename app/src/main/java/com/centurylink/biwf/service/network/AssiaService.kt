package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import retrofit2.http.GET
import retrofit2.http.HeaderMap

interface AssiaService {

    //Todo add 'api/v2' to base url
    @GET("api/v2/wifi/diags/apinfo")
    suspend fun getModemInfo(@HeaderMap header: Map<String, String>): AssiaNetworkResponse<ModemInfoResponse, AssiaError>

    @GET("api/v2/wifi/diags/stationinfo")
    suspend fun getDevicesList(@HeaderMap header: Map<String, String>): AssiaNetworkResponse<DevicesInfo,AssiaError>
}