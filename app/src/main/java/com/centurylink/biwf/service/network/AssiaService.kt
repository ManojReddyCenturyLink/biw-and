package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.*

interface AssiaService {

    @GET(EnvironmentPath.API_MODEM_INFO_PATH)
    suspend fun getModemInfo(@HeaderMap header: Map<String, String>): AssiaServiceResult<ModemInfoResponse>

    @GET(EnvironmentPath.API_DEVICE_LIST_PATH)
    suspend fun getDevicesList(@HeaderMap header: Map<String, String>): AssiaServiceResult<DevicesInfo>

    @POST(EnvironmentPath.API_BLOCK_UNBLOCK_DEVICE_PATH)
    suspend fun blockDevice(
        @Path(EnvironmentPath.ASSIA_ID) id: String,
        @Path(EnvironmentPath.STATION_MAC_ADDRESS) macAddress: String,
        @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<BlockResponse>

    @DELETE(EnvironmentPath.API_BLOCK_UNBLOCK_DEVICE_PATH)
    suspend fun unBlockDevice(
        @Path(EnvironmentPath.ASSIA_ID) id: String,
        @Path(EnvironmentPath.STATION_MAC_ADDRESS) macAddress: String,
        @HeaderMap header: Map<String, String>
    ): AssiaServiceResult<BlockResponse>
}
