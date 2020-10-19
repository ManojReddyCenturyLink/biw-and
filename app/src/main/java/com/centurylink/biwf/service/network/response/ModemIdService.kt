package com.centurylink.biwf.service.network.response

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.modem.ModemIdResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Modem id service -interface to get modem id info
 *
 * @constructor Create empty Modem id service
 */
interface ModemIdService {

    @GET(EnvironmentPath.SALES_FORCE_QUERY)
    suspend fun getModemId(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<ModemIdResponse>

}