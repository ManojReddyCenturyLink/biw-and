package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.assia.ModemRebootResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.speedtest.SpeedTestRequestResult
import com.centurylink.biwf.model.speedtest.SpeedTestResponse
import com.centurylink.biwf.model.speedtest.SpeedTestStatus
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path

interface AssiaService {

    //Todo add 'api/v2' to base url
    @GET("api/v2/wifi/diags/apinfo")
    suspend fun getModemInfo(@HeaderMap header: Map<String, String>): AssiaNetworkResponse<ModemInfoResponse, AssiaError>

    @GET("api/v2/wifi/diags/stationinfo")
    suspend fun getDevicesList(@HeaderMap header: Map<String, String>): AssiaNetworkResponse<DevicesInfo, AssiaError>

    @JvmSuppressWildcards
    @POST("api/v2/wifi/diagsrt/rtactions/start")
    suspend fun startSpeedTest(@HeaderMap header: Map<String, Any>): AssiaNetworkResponse<SpeedTestRequestResult, AssiaError>

    @JvmSuppressWildcards
    @GET("api/v2/wifi/diagsrt/rtactions/status")
    suspend fun checkSpeedTestResults(@HeaderMap header: Map<String, Any>): AssiaNetworkResponse<SpeedTestStatus, AssiaError>

    @JvmSuppressWildcards
    @GET("api/v2/wifi/diagsrt/ap/broadbandusthroughputsummary")
    suspend fun checkSpeedTestUpStreamResults(@HeaderMap header: Map<String, Any>): AssiaNetworkResponse<SpeedTestResponse, AssiaError>

    @JvmSuppressWildcards
    @GET("api/v2/wifi/diagsrt/ap/broadbanddsthroughputsummary")
    suspend fun checkSpeedTestDownStreamResults(@HeaderMap header: Map<String, Any>): AssiaNetworkResponse<SpeedTestResponse, AssiaError>

    @GET("api/v3/wifi/line/info")
    suspend fun checkStatusOfAp(): Any

    @POST("api/v2/wifi/operations/ap/{assiaId}/reboot")
    suspend fun rebootModem(@Path("assiaId") id: String, @HeaderMap header: Map<String, String>): ModemRebootResponse
}