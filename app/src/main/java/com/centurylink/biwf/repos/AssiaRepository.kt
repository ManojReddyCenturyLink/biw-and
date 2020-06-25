package com.centurylink.biwf.repos

import android.content.SharedPreferences
import android.util.Log
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssiaRepository @Inject constructor(
    private val preferences: Preferences,
    private val assiaService: AssiaService
) {
    //todo will be removed post-Apigee
    suspend fun getAssiaToken(): AssiaToken {
        return assiaService.getAssiaTokenWithTokenObject()
    }

    suspend fun getModemInfo(): ModemInfoResponse {
        return assiaService.getModemInfo(getHeaderMap(token = getAssiaToken().accessToken))
    }

    suspend fun getDevicesDetails(): DevicesInfo {
        return assiaService.getDevicesList(getHeaderMap(token = getAssiaToken().accessToken))
    }

    private fun getHeaderMap(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = preferences.getAssiaId()!!
        return headerMap
    }
}