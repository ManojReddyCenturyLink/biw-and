package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.mcafee.*
import com.centurylink.biwf.service.network.McafeeApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for all API calls from mcafee server with suspend functions
 */
@Singleton
class McafeeRepository @Inject constructor(
    private val preferences: Preferences,
    private val mcaFeeService: McafeeApiService
) {
    suspend fun getMcafeeDeviceIds(deviceMacAddresses: List<String>): Either<String, List<MacDeviceList>> {
        val result =
            mcaFeeService.getDevicesMapping(MappingRequest(preferences.getAssiaId(), deviceMacAddresses))
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "0" || it.macDeviceList.isNullOrEmpty()) {
                return Either.Left("No Mapping Devices Found ")
            }
            return Either.Right(it.macDeviceList)
        }
    }

    suspend fun getDevicePauseResumeStatus(deviceId: String): Either<String, DevicePauseStatus> {
        val result =
            mcaFeeService.getNetworkInfo(BlockRequest(preferences.getAssiaId(), deviceId))
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "0") {
                return Either.Left("No Status  Found ")
            }
            return Either.Right(DevicePauseStatus(it.blocked, deviceId))
        }
    }

    suspend fun updateDevicePauseResumeStatus(deviceId: String, isPaused: Boolean):
            Either<String, DevicePauseStatus> {
        val result = mcaFeeService.updateNetWorkInfo(
            DevicePauseStatusRequest(deviceId, preferences.getAssiaId(), isPaused)
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "0") {
                return Either.Left("No Status  Found ")
            }
            return Either.Right(DevicePauseStatus(isPaused, deviceId))
        }
    }
}