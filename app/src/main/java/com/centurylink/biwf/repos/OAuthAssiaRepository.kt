package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.devices.BlockDeviceRequest
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.service.network.OAuthAssiaService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OAuthAssiaRepository - OAuth Asia Repository class interacts with OAuthAssiaService. This Repository class
 * gets the data from the network. It handles all the Devices related information from the Asia CloudCheck
 * backend through APIGEE gateway.The View models can consume the Devices related information and display in the Activity
 * or Fragments.
 *
 * @property preferences preference Instance for storing the value in shared preferences.
 * @property oAuthAssiaService OAuthAssiaService  Instance for interacting with the AssiaCloudcheck API through Gateways..
 * @constructor Create  OAuthAssiaRepository
 */
@Singleton
class OAuthAssiaRepository @Inject constructor(
        private val preferences: Preferences,
        private val oAuthAssiaService: OAuthAssiaService
) {
    /**
     * Get Modem Info API is used for getting the details about the Modem and the Network.
     *
     * @return  ModemInfo instance if the API is success and error message in case of failure
     */
    suspend fun getModemInfo(): Either<String, ModemInfo> {
        val result = oAuthAssiaService.getLineInfo(preferences.getLineId())
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            it.let {
                if (it.code != "1000") {
                    return Either.Left(it.message)
                }
                val deviceId = it.modemInfo.apInfoList[0].deviceId
                if (!deviceId.isNullOrEmpty()) {
                    preferences.saveAssiaId(deviceId)
                }
                return Either.Right(it.modemInfo)
            }
        }
    }

    // TODO - this may currently not do anything differently - need to get Derek to confirm how
    //  to use forcePing
    // Secondary method for Line Info retrieval, which forces a ping to the modem hardware. This
    // prevents Assia from sending us cached data in the response, but is more expensive so it
    // should only be used for certain use cases which require it. Rebooting uses this method for
    // obtaining the instantaneous "isAlive" value
    /**
     * ModemRealTime Information can be obtained from this function.
     * if "forceping=true" returns non cached API response.
     *
     * @return  ModemInfo instance if the API is success and error message in case of failure.
     */
    suspend fun getModemInfoForcePing(): Either<String, ModemInfo> {
        val result = oAuthAssiaService.getLineInfo(preferences.getLineId(), forcePing = true)
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            it.let {
                if (it.code != "1000") {
                    return Either.Left(it.message)
                }
                val deviceId = it.modemInfo.apInfoList[0].deviceId
                if (!deviceId.isNullOrEmpty()) {
                    preferences.saveAssiaId(deviceId)
                }
                return Either.Right(it.modemInfo)
            }
        }
    }

    /**
     * This can be used to block device/remove device from network.
     *
     * @return  BlockResponse instance if the API is success and error message in case of failure.
     */
    suspend fun blockDevices(stationmac: String): Either<String, BlockResponse> {
        val result = oAuthAssiaService.blockDevice(
                BlockDeviceRequest(
                assiaId = preferences.getAssiaId(),
                stationMacAddress = stationmac)
        )
        return result.mapLeft { it.message?.message.toString()}.flatMap { it ->
            it.let {
                if (it.code != "1000") {
                    return Either.Left(it.message)
                }
                return Either.Right(it)
            }
        }
    }

    suspend fun getDevicesDetails(): Either<String, List<DevicesData>> {
        val result = oAuthAssiaService.getDevicesList(preferences.getLineId())
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            it.let {
                if (it.code != "1000") {
                    return Either.Left(it.message!!)
                }
                return Either.Right(it.devicesDataList)
            }
        }
    }
}
