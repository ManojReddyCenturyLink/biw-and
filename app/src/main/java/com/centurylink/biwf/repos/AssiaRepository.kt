package com.centurylink.biwf.repos


import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.speedtest.SpeedTestRequestResult
import com.centurylink.biwf.model.speedtest.SpeedTestResponse
import com.centurylink.biwf.model.speedtest.SpeedTestStatus
import com.centurylink.biwf.repos.assia.AssiaTokenManager
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssiaRepository @Inject constructor(
    private val preferences: Preferences,
    private val assiaService: AssiaService,
    private val assiaTokenManager: AssiaTokenManager
) {

    suspend fun getModemInfo(): AssiaNetworkResponse<ModemInfoResponse, AssiaError> {
        return assiaService.getModemInfo(getHeaderMap(token = assiaTokenManager.getAssiaToken()))
    }

    suspend fun getDevicesDetails(): AssiaNetworkResponse<DevicesInfo, AssiaError> {
        return assiaService.getDevicesList(getHeaderMap(token = assiaTokenManager.getAssiaToken()))
    }

    suspend fun startSpeedTest(): SpeedTestRequestResult {
        return assiaService.startSpeedTest(getHeaderMapWithXhours(token = assiaTokenManager.getAssiaToken()))
    }

    suspend fun checkSpeedTestStatus(speedTestId: Int): SpeedTestStatus {
        return assiaService.checkSpeedTestResults(
            getHeaderStatus(
                token = assiaTokenManager.getAssiaToken(),
                requestId = speedTestId
            )
        )
    }

    suspend fun getUpstreamResults(): SpeedTestResponse {
        return assiaService.checkSpeedTestUpStreamResults(getHeaderMapWithXhours(token = assiaTokenManager.getAssiaToken()))
    }

    suspend fun getDownstreamResults(): SpeedTestResponse {
        return assiaService.checkSpeedTestDownStreamResults(getHeaderMapWithXhours(token = assiaTokenManager.getAssiaToken()))
    }

    private fun getHeaderMap(token: String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = preferences.getAssiaId()
        return headerMap
    }

    private fun getHeaderMapWithXhours(token: String): Map<String, Any> {
        val headerMap = mutableMapOf<String, Any>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = preferences.getAssiaId()
        headerMap["pastXHours"] = 1
        return headerMap
    }

    private fun getHeaderStatus(token: String, requestId: Int): Map<String, Any> {
        val headerMap = mutableMapOf<String, Any>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = preferences.getAssiaId()
        headerMap["requestId"] = requestId
        return headerMap
    }
}