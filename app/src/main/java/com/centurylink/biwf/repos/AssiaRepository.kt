package com.centurylink.biwf.repos

import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.service.network.AssiaService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssiaRepository @Inject constructor(
    private val assiaService: AssiaService
) {

    suspend fun getAssiaToken(): AssiaToken {
        return assiaService.getAssiaTokenWithTokenObject()
    }

    suspend fun getModemInfo():ModemInfoResponse{
        return assiaService.getModemInfo(getHeaderMap(token = getAssiaToken().accessToken))
    }

    private fun getHeaderMap(token:String): Map<String, String> {
        val headerMap = mutableMapOf<String, String>()
        headerMap["Authorization"] = "bearer $token"
        headerMap["assiaId"] = "C4000XG1950000871"
        return headerMap
    }
}