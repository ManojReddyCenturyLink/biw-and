package com.centurylink.biwf.service.network
import com.centurylink.biwf.model.McafeeServiceResult
import com.centurylink.biwf.model.mcafee.*
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT

/**
 * Mcafee Api services interface
 */
interface McafeeApiService {

    @POST("mcafee/macaddress/mapping")
    suspend fun getDevicesMapping(@Body mappingRequest: MappingRequest): McafeeServiceResult<DevicesMapping>

    @POST("mcafee/get-network-access")
    suspend fun getNetworkInfo(@Body blockRequest: BlockRequest) :McafeeServiceResult<DevicePauseStatusResponse>

    @PUT("mcafee/network-access")
    suspend fun updateNetWorkInfo(@Body devicePauseStatusRequest: DevicePauseStatusRequest) :McafeeServiceResult<DeviceUpdateResponse>
}
