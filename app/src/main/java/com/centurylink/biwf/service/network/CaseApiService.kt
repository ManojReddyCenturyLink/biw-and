package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.CaseCreate
import com.centurylink.biwf.model.cases.CaseResponse
import com.centurylink.biwf.model.cases.Cases
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CaseApiService {

    @POST(EnvironmentPath.API_CASE_FOR_SUBSCRIPTION_PATH)
    suspend fun submitCaseForSubscription(@Body createCase: CaseCreate): FiberServiceResult<CaseResponse>

    @GET(EnvironmentPath.API_CASE_FOR_SUBSCRIPTION_PATH)
    suspend fun getCaseNumber(): FiberServiceResult<Cases>

    @GET(EnvironmentPath.SALES_FORCE_QUERY)
    suspend fun getRecordTpeId(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<RecordId>
}
