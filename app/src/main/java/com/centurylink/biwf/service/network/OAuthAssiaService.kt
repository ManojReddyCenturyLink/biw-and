package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.assia.ModemRebootResponse
import com.centurylink.biwf.model.assia.RebootModemRequest
import com.centurylink.biwf.model.devices.BlockDeviceRequest
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.*

interface OAuthAssiaService {

    // TODO - Work with Derek to ensure forcePing actually works
    @GET(EnvironmentPath.API_LINE_INFO_PATH)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun getLineInfo(
        @Query(EnvironmentPath.GENERIC_ID) genericId: String,
        @Query(EnvironmentPath.FORCE_PING) forcePing: Boolean = false
    ): AssiaServiceResult<ModemInfoResponse>

    @POST(EnvironmentPath.API_REBOOT_MODEM_PATH)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun rebootModem(
        @Body rebootModemRequest: RebootModemRequest
    ): AssiaServiceResult<ModemRebootResponse>

    @POST(EnvironmentPath.API_BLOCK_DEVICE_PATH)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun blockDevice(
        @Body blockDeviceRequest: BlockDeviceRequest
    ): AssiaServiceResult<BlockResponse>

    @GET(EnvironmentPath.STATION_INFO)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun getDevicesList(@Query(EnvironmentPath.LINE_ID) lineId: String): AssiaServiceResult<DevicesInfo>
}
