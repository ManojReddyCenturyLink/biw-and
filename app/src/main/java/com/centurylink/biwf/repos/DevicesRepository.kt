package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.wifi.WifiDetails
import com.centurylink.biwf.service.network.IntegrationRestServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicesRepository @Inject constructor(
    private val deviceApiService: IntegrationRestServices
) {
    suspend fun getDevicesDetails(): Either<String, List<DevicesData>> {
        val result: FiberServiceResult<DevicesInfo> =
            deviceApiService.getDevicesDetails("ZZZZZZ")
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            it.let {
                if (it.code != "1000") {
                    return Either.Left(it.message!!)
                }
                return Either.Right(it.devicesDataList)
            }
        }
    }

    suspend fun getWifiListAndCredentials(): Either<String, WifiDetails> {
        val result: FiberServiceResult<WifiDetails> =
            deviceApiService.getWifiListandCredentials("ZZZZZZ")
        return result.mapLeft { it.message?.message.toString() }
    }
}