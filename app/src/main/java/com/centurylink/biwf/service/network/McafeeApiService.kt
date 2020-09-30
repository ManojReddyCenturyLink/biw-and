package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.McafeeServiceResult
import com.centurylink.biwf.model.mcafee.BlockRequest
import com.centurylink.biwf.model.mcafee.DeviceDetailsResponse
import com.centurylink.biwf.model.mcafee.DeviceInfoRequest
import com.centurylink.biwf.model.mcafee.DeviceInfoResponse
import com.centurylink.biwf.model.mcafee.DevicePauseStatusRequest
import com.centurylink.biwf.model.mcafee.DevicePauseStatusResponse
import com.centurylink.biwf.model.mcafee.DeviceUpdateResponse
import com.centurylink.biwf.model.mcafee.DevicesMapping
import com.centurylink.biwf.model.mcafee.MappingRequest
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.*

/**
 * Mcafee Api services interface
 */
interface McafeeApiService {

    @POST(EnvironmentPath.API_DEVICES_MAPPING_PATH)
    suspend fun getDevicesMapping(@Body mappingRequest: MappingRequest): McafeeServiceResult<DevicesMapping>

    @POST(EnvironmentPath.API_GET_NETWORK_ACCESS_PATH)
    suspend fun getNetworkInfo(@Body blockRequest: BlockRequest): McafeeServiceResult<DevicePauseStatusResponse>

    @PUT(EnvironmentPath.API_UPDATE_NETWORK_ACCESS_PATH)
    suspend fun updateNetWorkInfo(@Body devicePauseStatusRequest: DevicePauseStatusRequest): McafeeServiceResult<DeviceUpdateResponse>

    @PUT(EnvironmentPath.API_UPDATE_DEVICE_INFO_PATH)
    suspend fun updateDeviceInfo(@Body deviceInfoRequest: DeviceInfoRequest): McafeeServiceResult<DeviceInfoResponse>

    @GET(EnvironmentPath.API_GET_DEVICE_INFO_PATH)
    suspend fun getDeviceDetails(@Query("serialNumber") serialNumber: String,@HeaderMap header: Map<String, String>): McafeeServiceResult<DeviceDetailsResponse>
}
