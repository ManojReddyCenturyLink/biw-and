package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.AssiaServiceResult
import com.centurylink.biwf.model.speedtest.SpeedTestRes
import com.centurylink.biwf.model.speedtest.SpeedTestStatusRequest
import com.centurylink.biwf.model.speedtest.SpeedTestStatusResponse
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface SpeedTestService {

    @POST(EnvironmentPath.API_SPEED_TEST_PATH)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun getSpeedTestDetails(@Body speedTestStatusRequest: SpeedTestStatusRequest): AssiaServiceResult<SpeedTestRes>

    @POST(EnvironmentPath.API_SPEED_TEST_STATUS)
    @Headers(EnvironmentPath.APIGEE_MOBILE_HEADER)
    suspend fun getSpeedTestStatusDetails(@Body speedTestStatusRequest: SpeedTestStatusRequest): AssiaServiceResult<SpeedTestStatusResponse>
}
