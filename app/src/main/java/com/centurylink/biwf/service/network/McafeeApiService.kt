package com.centurylink.biwf.service.network


import com.centurylink.biwf.model.McafeeServiceResult
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.model.mcafee.DevicesMapping
import com.centurylink.biwf.model.mcafee.MappingRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Mcafee Api services interface
 */
interface McafeeApiService {

    @POST("macaddress/mapping")
    suspend fun getDevicesMapping(@Body mappingRequest: MappingRequest): McafeeServiceResult<DevicesMapping>
}
