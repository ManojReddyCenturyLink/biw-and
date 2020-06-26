package com.centurylink.biwf.repos


import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssiaRepository @Inject constructor(
    private val preferences: Preferences,
    private val assiaService: AssiaService
) {

    private val tokenError = "Token Error"

    //todo will be removed post-Apigee
    suspend fun getAssiaToken(): String {
        val response = assiaService.getAssiaTokenWithTokenObject()
        return when (response) {
            is AssiaNetworkResponse.Success -> {
                response.body.accessToken
            }
            else -> {
                tokenError
            }
        }
    }

    suspend fun getModemInfo(): AssiaNetworkResponse<ModemInfoResponse, AssiaError> {
        return assiaService.getModemInfo(getHeaderMap(token = getAssiaToken()))
    }

    suspend fun getDevicesDetails(): AssiaNetworkResponse<DevicesInfo, AssiaError> {
        return assiaService.getDevicesList(getHeaderMap(token = getAssiaToken()))
    }

    private fun getHeaderMap(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = preferences.getAssiaId()
        return headerMap
    }
}