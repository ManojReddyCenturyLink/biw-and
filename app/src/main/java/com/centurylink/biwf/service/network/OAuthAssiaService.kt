package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.service.impl.aasia.AssiaError
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface OAuthAssiaService {

    // TODO - Work with Derek to ensure forcePing actually works
    @GET("wifi-line-info")
    @Headers(MOBILE_HEADER)
    suspend fun getLineInfo(
        @Query("genericId") genericId: String,
        @Query("forcePing") forcePing: Boolean = false
    ): AssiaNetworkResponse<ModemInfoResponse, AssiaError>

    companion object {
        private const val MOBILE_HEADER = "From: mobile"
    }
}
