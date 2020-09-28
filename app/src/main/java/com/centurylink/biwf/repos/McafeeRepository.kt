package com.centurylink.biwf.repos

import android.util.Log
import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.mcafee.BlockRequest
import com.centurylink.biwf.model.mcafee.DeviceInfoRequest
import com.centurylink.biwf.model.mcafee.DevicePauseStatus
import com.centurylink.biwf.model.mcafee.DevicePauseStatusRequest
import com.centurylink.biwf.model.mcafee.DevicesItem
import com.centurylink.biwf.model.mcafee.MacDeviceList
import com.centurylink.biwf.model.mcafee.MappingRequest
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

    suspend fun updateDeviceName(deviceType: String, deviceName: String, deviceId: String):
            Either<String, String> {
        val result = mcaFeeService.updateDeviceInfo(
            DeviceInfoRequest(deviceType, preferences.getAssiaId(), deviceName, deviceId)
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "0") {
                return Either.Left("Something went wrong!")
            }
            return Either.Right(it.message)
        }
    }

    suspend fun fetchDeviceDetails():
            Either<String, List<DevicesItem>> {
        val result = mcaFeeService.getDeviceDetails(preferences.getAssiaId(), getMcAfeeHeaderMap())
        return result.mapLeft { it.message?.message.toString() }.flatMap {
            if (it.code != "0") {
                return Either.Left("Something went wrong!")
            }
            return Either.Right(it.devices)
        }
    }

    private fun getMcAfeeHeaderMap(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Content-Type"] = "application/json"
        return headerMap
    }
}