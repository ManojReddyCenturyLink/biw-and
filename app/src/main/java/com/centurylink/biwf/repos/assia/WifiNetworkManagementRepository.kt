package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.network.WifiNetworkApiService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WifiNetworkManagementRepository @Inject constructor(
    private val preferences: Preferences,
    private val wifiNetworkApiService: WifiNetworkApiService,
    private val assiaTokenManager: AssiaTokenManager
) {
    suspend fun getNetworkName(interfaceType: NetWorkBand) :AssiaNetworkResponse<NetworkDetails, AssiaError> {
        return wifiNetworkApiService.getNetworkName(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMap(token = assiaTokenManager.getAssiaToken()))
    }

    suspend fun updateNetworkName(
        interfaceType: NetWorkBand,
        updateNetworkName: UpdateNetworkName
    ): AssiaNetworkResponse<UpdateNetworkResponse, AssiaError> {
        return wifiNetworkApiService.updateNetworkName(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMap(token = assiaTokenManager.getAssiaToken()),
            updateNetworkName
        )
    }

    suspend fun enableNetwork(interfaceType: NetWorkBand): AssiaNetworkResponse<UpdateNetworkResponse, AssiaError> {
        return wifiNetworkApiService.enableNetwork(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMap(token = assiaTokenManager.getAssiaToken())
        )
    }

    suspend fun disableNetwork(interfaceType: NetWorkBand): AssiaNetworkResponse<UpdateNetworkResponse, AssiaError> {
        return wifiNetworkApiService.disableNetwork(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMap(token = assiaTokenManager.getAssiaToken())
        )
    }

    suspend fun getNetworkPassword(interfaceType: NetWorkBand): AssiaNetworkResponse<NetworkDetails, AssiaError> {
        return wifiNetworkApiService.getNetworkPassword(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMapWithContent(token = assiaTokenManager.getAssiaToken())
        )
    }

    suspend fun updateNetworkPassword(
        interfaceType: NetWorkBand,
        updateNWPassword: UpdateNWPassword
    ): AssiaNetworkResponse<UpdateNetworkResponse, AssiaError> {
        return wifiNetworkApiService.updateNetworkPassword(
            preferences.getAssiaId(),
            interfaceType,
            getHeaderMapWithContent(token = assiaTokenManager.getAssiaToken()), updateNWPassword
        )
    }

    private fun getHeaderMap(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        return headerMap
    }

    private fun getHeaderMapWithContent(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["Content-Type"] = "application/json"
        return headerMap
    }
}