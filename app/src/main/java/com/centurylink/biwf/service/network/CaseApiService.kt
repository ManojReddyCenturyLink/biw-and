package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.CaseCreate
import com.centurylink.biwf.model.cases.Cases
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface CaseApiService {

    @POST("sobjects/Case/")
    suspend fun submitCaseForSubscription(@Body createCase: CaseCreate): FiberServiceResult<Unit>

    @GET("sobjects/Case")
    suspend fun getCaseNumber(): FiberServiceResult<Cases>
}