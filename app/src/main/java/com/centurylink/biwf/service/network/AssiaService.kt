package com.centurylink.biwf.service.network


import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.assia.ModemRebootResponse
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.speedtest.SpeedTestRequestResult
import com.centurylink.biwf.model.speedtest.SpeedTestResponse
import com.centurylink.biwf.model.speedtest.SpeedTestStatus
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Path

interface AssiaService {

    @GET(EnvironmentPath.API_MODEM_INFO_PATH)
    suspend fun getModemInfo(@HeaderMap header: Map<String, String>): AssiaServiceResult<ModemInfoResponse>


    @GET(EnvironmentPath.API_DEVICE_LIST_PATH)
    suspend fun getDevicesList(@HeaderMap header: Map<String, String>): AssiaServiceResult<DevicesInfo>

    @JvmSuppressWildcards
    @POST(EnvironmentPath.API_SPEED_TEST_START_PATH)
    suspend fun startSpeedTest(@HeaderMap header: Map<String, Any>): AssiaServiceResult<SpeedTestRequestResult>

    @JvmSuppressWildcards
    @GET(EnvironmentPath.API_SPEED_TEST_STATUS_PATH)
    suspend fun checkSpeedTestResults(@HeaderMap header: Map<String, Any>): AssiaServiceResult<SpeedTestStatus>

    @JvmSuppressWildcards
    @GET(EnvironmentPath.API_SPEED_TEST_UP_RESULT_PATH)
    suspend fun checkSpeedTestUpStreamResults(@HeaderMap header: Map<String, Any>): AssiaServiceResult<SpeedTestResponse>

    @JvmSuppressWildcards
    @GET(EnvironmentPath.API_SPEED_TEST_DOWN_RESULT_PATH)
    suspend fun checkSpeedTestDownStreamResults(@HeaderMap header: Map<String, Any>): AssiaServiceResult<SpeedTestResponse>


    @POST(EnvironmentPath.API_REBOOT_MODEM_PATH)
    suspend fun rebootModem(
        @Path(EnvironmentPath.ASSIA_ID) id: String,
        @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<ModemRebootResponse>

    @POST(EnvironmentPath.API_BLOCK_UNBLOCK_DEVICE_PATH)
    suspend fun blockDevice(
        @Path(EnvironmentPath.ASSIA_ID) id: String,
        @Path(EnvironmentPath.STATION_MAC_ADDRESS) macAddress: String,
        @HeaderMap header: Map<String, String>
    ) : AssiaServiceResult<BlockResponse>

    @DELETE(EnvironmentPath.API_BLOCK_UNBLOCK_DEVICE_PATH)
    suspend fun unBlockDevice(
        @Path(EnvironmentPath.ASSIA_ID) id: String,
        @Path(EnvironmentPath.STATION_MAC_ADDRESS) macAddress: String,
        @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<BlockResponse>
}