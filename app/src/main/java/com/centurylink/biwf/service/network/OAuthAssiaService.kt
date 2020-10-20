package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.assia.ModemInfoResponse
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

    @GET(EnvironmentPath.STATION_INFO)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun getDevicesList(
            @Query(EnvironmentPath.ASSIA_ID) assiaId: String,
            @Query(EnvironmentPath.LINE_ID) lineId: String,
            @Query(EnvironmentPath.STA_MAC) staMac: String): AssiaServiceResult<DevicesInfo>
}