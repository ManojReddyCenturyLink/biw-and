package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface OAuthAssiaService {

    // TODO - Work with Derek to ensure forcePing actually works
    @GET(EnvironmentPath.API_LINE_INFO_PATH)
    @Headers(MOBILE_HEADER)
    suspend fun getLineInfo(
        @Query(EnvironmentPath.GENERIC_ID) genericId: String,
        @Query(EnvironmentPath.FORCE_PING) forcePing: Boolean = false
    ):  AssiaServiceResult<ModemInfoResponse>

    companion object {
        private const val MOBILE_HEADER = "From: mobile"
    }
}
