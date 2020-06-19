package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.service.network.IntegrationRestServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicesRepository @Inject constructor(
    private val deviceApiService: IntegrationRestServices
) {
    suspend fun getDevicesDetails(): Either<String, DevicesInfo> {
        val result: FiberServiceResult<DevicesInfo> =
            deviceApiService.getDevicesDetails("ZZZZZZ")
        return result.mapLeft { it.message?.message.toString() }
    }
}