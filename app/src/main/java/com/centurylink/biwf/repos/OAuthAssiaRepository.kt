package com.centurylink.biwf.repos

import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.network.OAuthAssiaService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject

class OAuthAssiaRepository @Inject constructor(
    private val preferences: Preferences,
    private val oAuthAssiaService: OAuthAssiaService
) {

    suspend fun getModemInfo(): AssiaNetworkResponse<ModemInfoResponse, AssiaError> {
        val result = oAuthAssiaService.getLineInfo(preferences.getLineId())
        if (result is AssiaNetworkResponse.Success) {
            val apInfoList = result.body.modemInfo.apInfoList
            if (apInfoList.isNotEmpty()) {
                val deviceId = apInfoList[0].deviceId
                if (!deviceId.isNullOrEmpty()) {
                    preferences.saveAssiaId(deviceId)
                }
            }
        }
        return result
    }

    // TODO - this may currently not do anything differently - need to get Derek to confirm how
    //  to use forcePing
    // Secondary method for Line Info retrieval, which forces a ping to the modem hardware. This
    // prevents Assia from sending us cached data in the response, but is more expensive so it
    // should only be used for certain use cases which require it. Rebooting uses this method for
    // obtaining the instantaneous "isAlive" value
    suspend fun getModemInfoForcePing(): AssiaNetworkResponse<ModemInfoResponse, AssiaError> {
        return oAuthAssiaService.getLineInfo(preferences.getLineId(), forcePing = true)
    }
}
