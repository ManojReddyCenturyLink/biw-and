package com.centurylink.biwf.repos

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
 * McafeeRepository - This class interacts with McafeeApiService Services. This Repository class
 * gets the data from the network . It handles all the Devices related  information from the McAfee Apis
 * backend  and the View models can consume the McAfee Devices related information and display in the Activity
 * or Fragments.
 *
 * @property preferences preferences preference Instance for storing the value in shared preferences.
 * @property mcaFeeService Instance for interacting with the McAfee API.
 * @constructor Create empty Mcafee repository
 */
@Singleton
class McafeeRepository @Inject constructor(
    private val preferences: Preferences,
    private val mcaFeeService: McafeeApiService
) {
    /**
     * The suspend function is used to get the list of Devices connected to the Modem.
     *
     * @param deviceMacAddresses The List of MAcAddress will be passed from Cloudcheck API
     * @return list of MacDevicesList in case of Success and Error Message in case of Errors.
     */
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

    /**
     * The suspend function returns the Status of the device in case of Pause / Failure
     *
     * @param deviceId The DeviceID of the devices
     * @return DevicePauseStatus in case of Success and error message in case of Errors
     */
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

    /**
     * Updates the  device pause/ resume status to the backend.
     *
     * @param deviceId the DeviceId
     * @param isPaused the status to be updated for the device.
     * @return DevicePauseStatus in case of Success and Error message in case of errors.
     */
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

    /**
     * Suspend function used to Update the device details
     *
     * @param deviceType The Devicetype info to be updated.
     * @param deviceName The DeviceName info to be updated.
     * @param deviceId The DeviceId to be Updated.
     * @return Success message in case of Success and Error message in case of Error.
     */
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

    /**
     * The Suspend function used to fetch the device information from the McAfee server
     *
     * @return The List of deviceItem in case of Success and error message in case of Failure.
     */
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

    /**
     * sets the HeaderMap Needed for McAfee endPoints
     *
     * @return Map includes the McAfee Header Information
     */
    private fun getMcAfeeHeaderMap(): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Content-Type"] = "application/json"
        return headerMap
    }
}
