package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.CaseCreate
import com.centurylink.biwf.model.cases.CaseResponse
import com.centurylink.biwf.model.cases.Cases
import com.centurylink.biwf.model.cases.RecordId
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CaseApiService {

    @POST("sobjects/Case")
    suspend fun submitCaseForSubscription(@Body createCase: CaseCreate): FiberServiceResult<CaseResponse>

    @GET("sobjects/Case")
    suspend fun getCaseNumber(): FiberServiceResult<Cases>

    @GET("query")
    suspend fun getRecordTpeId(@Query("q") id: String): FiberServiceResult<RecordId>
}