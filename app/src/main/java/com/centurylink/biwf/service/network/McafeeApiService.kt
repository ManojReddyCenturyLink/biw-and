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
import com.centurylink.biwf.model.mcafee.*
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

/**
 * Mcafee Api services interface
 */
interface McafeeApiService {

    @POST(EnvironmentPath.API_DEVICES_MAPPING_PATH)
    suspend fun getDevicesMapping(@Body mappingRequest: MappingRequest): McafeeServiceResult<DevicesMapping>

    @POST(EnvironmentPath.API_GET_NETWORK_ACCESS_PATH)
    suspend fun getNetworkInfo(@Body blockRequest: BlockRequest) :McafeeServiceResult<DevicePauseStatusResponse>

    @PUT(EnvironmentPath.API_UPDATE_NETWORK_ACCESS_PATH)
    suspend fun updateNetWorkInfo(@Body devicePauseStatusRequest: DevicePauseStatusRequest) :McafeeServiceResult<DeviceUpdateResponse>
    @PUT("mcafee/network-access")
    suspend fun updateNetWorkInfo(@Body devicePauseStatusRequest: DevicePauseStatusRequest): McafeeServiceResult<DeviceUpdateResponse>

    @PUT("mcafee/update-device")
    suspend fun updateDeviceInfo(@Body deviceInfoRequest: DeviceInfoRequest): McafeeServiceResult<DeviceInfoResponse>

    //https://centurylink-test1.apigee.net/v1/mcafee/get-device?serialNumber=C4000XG1950000308
    @GET("mcafee/get-device")
    suspend fun getDeviceDetails(@Query("serialNumber") serialNumber: String): McafeeServiceResult<DeviceDetailsResponse>
}
