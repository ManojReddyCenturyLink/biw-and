package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.mcafee.DevicesMapping
import com.centurylink.biwf.model.mcafee.MappingRequest
import com.centurylink.biwf.model.McafeeServiceResult
import com.centurylink.biwf.model.mcafee.*
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

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
}
