package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.devices.DevicesInfo
import retrofit2.http.GET

interface DevicesApiService {
    @GET("sobjects/devices")
    suspend fun getCaseNumber(): FiberServiceResult<DevicesInfo>
}

