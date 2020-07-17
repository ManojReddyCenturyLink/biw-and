package com.centurylink.biwf.service.network

import android.net.MacAddress
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.assia.ModemRebootResponse
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.speedtest.SpeedTestRequestResult
import com.centurylink.biwf.model.speedtest.SpeedTestResponse
import com.centurylink.biwf.model.speedtest.SpeedTestStatus
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import retrofit2.http.*

interface AssiaService {

    //Todo add 'api/v2' to base url
    @GET("api/v2/wifi/diags/apinfo")
    suspend fun getModemInfo(@HeaderMap header: Map<String, String>): AssiaNetworkResponse<ModemInfoResponse, AssiaError>

    @GET("api/v2/wifi/diags/stationinfo")
    suspend fun getDevicesList(@HeaderMap header: Map<String, String>): AssiaNetworkResponse<DevicesInfo, AssiaError>

    @JvmSuppressWildcards
    @POST("api/v2/wifi/diagsrt/rtactions/start")
    suspend fun startSpeedTest(@HeaderMap header: Map<String, Any>): SpeedTestRequestResult

    @JvmSuppressWildcards
    @GET("api/v2/wifi/diagsrt/rtactions/status")
    suspend fun checkSpeedTestResults(@HeaderMap header: Map<String, Any>): SpeedTestStatus

    @JvmSuppressWildcards
    @GET("api/v2/wifi/diagsrt/ap/broadbandusthroughputsummary")
    suspend fun checkSpeedTestUpStreamResults(@HeaderMap header: Map<String, Any>): SpeedTestResponse

    @JvmSuppressWildcards
    @GET("api/v2/wifi/diagsrt/ap/broadbanddsthroughputsummary")
    suspend fun checkSpeedTestDownStreamResults(@HeaderMap header: Map<String, Any>): SpeedTestResponse

    @GET("api/v3/wifi/line/info")
    suspend fun checkStatusOfAp(): Any

    @POST("api/v2/wifi/operations/ap/{assiaId}/reboot")
    suspend fun rebootModem(
        @Path("assiaId") id: String,
        @HeaderMap header: Map<String, String>
    ): ModemRebootResponse

    @POST("api/v2/wifi/operations/station/{assiaId}/{stationMacAddress}/block")
    suspend fun blockDevice(
        @Path("assiaId") id: String,
        @Path("stationMacAddress") macAddress: String,
        @HeaderMap header: Map<String, String>
    ) : AssiaNetworkResponse<BlockResponse, AssiaError>

    @DELETE("api/v2/wifi/operations/station/{assiaId}/{stationMacAddress}/block")
    suspend fun unBlockDevice(
        @Path("assiaId") id: String,
        @Path("stationMacAddress") macAddress: String,
        @HeaderMap header: Map<String, String>
    ): AssiaNetworkResponse<BlockResponse, AssiaError>
}