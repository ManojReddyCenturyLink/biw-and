package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.repos.assia.AssiaTokenManager
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Assia repository
 *
 * @property preferences
 * @property assiaService
 * @property assiaTokenManager
 * @constructor Create empty Assia repository
 */
@Singleton
class AssiaRepository @Inject constructor(
    private val preferences: Preferences,
    private val assiaService: AssiaService,
    private val assiaTokenManager: AssiaTokenManager
) {

    suspend fun getModemInfo(): Either<String, ModemInfo> {
        val result =
            assiaService.getModemInfo(getV3HeaderMap(token = assiaTokenManager.getAssiaToken()))
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

    // Secondary method for Modem Info retrieval, which forces a ping to the hardware. This 
    // prevents Assia from sending us cached data in the response, but is more expensive so it
    // should only be used for certain use cases which require it. Rebooting uses this method for
    // obtaining the instantaneous "isAlive" value
    suspend fun getModemInfoForcePing(): Either<String, ModemInfo> {
        val result = assiaService.getModemInfo(
            getV3HeaderMap(token = assiaTokenManager.getAssiaToken()).plus("forcePing" to "true")
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            it.let {
                if (it.code != "1000") {
                    return Either.Left(it.message)
                }
                return Either.Right(it.modemInfo)
            }
        }
    }

    suspend fun blockDevices(stationmac: String): Either<String, BlockResponse> {
        val result = assiaService.blockDevice(
            preferences.getAssiaId(),
            stationmac,
            getHeaderMap(token = assiaTokenManager.getAssiaToken())
        )
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            it.let {
                if (it.code != "1000") {
                    return Either.Left(it.message)
                }
                return Either.Right(it)
            }
        }
    }

    suspend fun unblockDevices(stationmac: String): Either<String, BlockResponse> {
        val result = assiaService.unBlockDevice(
            preferences.getAssiaId(),
            stationmac,
            getHeaderMap(token = assiaTokenManager.getAssiaToken()))
        return result.mapLeft { it.message?.message.toString() }.flatMap { it ->
            it.let {
                if (it.code != "1000") {
                    return Either.Left(it.message)
                }
                return Either.Right(it)
            }
        }
    }

    private fun getHeaderMap(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        // TODO remove "Authorization" from map when Cloudcheck URLs updated
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = preferences.getAssiaId()
        return headerMap
    }

    private fun getV3HeaderMap(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["genericId"] = preferences.getLineId()
        return headerMap
    }
}
