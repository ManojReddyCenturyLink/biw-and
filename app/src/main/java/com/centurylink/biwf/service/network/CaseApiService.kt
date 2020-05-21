package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.cases.CaseCreate
import retrofit2.http.Body
import retrofit2.http.POST

interface CaseApiService {

    @POST("sobjects/Case/")
    suspend fun submitCaseForSubscription(
        @Body updatedCall: CaseCreate
    ): FiberServiceResult<Unit>
}