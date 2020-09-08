package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.flatMap
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.service.network.OAuthAssiaService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject

class OAuthAssiaRepository @Inject constructor(
    private val preferences: Preferences,
    private val oAuthAssiaService: OAuthAssiaService
) {
    suspend fun getModemInfo(): Either<String, ModemInfo> {
        val result = oAuthAssiaService.getLineInfo(preferences.getLineId())
        return result.mapLeft { it.message?.message.toString() }.flatMap { it->
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
}
