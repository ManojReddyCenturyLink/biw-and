package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentInfoResponse
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path
import retrofit2.http.Query

interface AccountApiService {

    @GET(EnvironmentPath.API_ACCOUNT_DETAILS_PATH)
    suspend fun getAccountDetails(@Path(EnvironmentPath.ACCOUNT_ID) id: String): FiberServiceResult<AccountDetails>

    @PATCH(EnvironmentPath.API_ACCOUNT_DETAILS_PATH)
    suspend fun submitServiceCallDetails(
        @Path(EnvironmentPath.ACCOUNT_ID) id: String,
        @Body updateCallsAndText: UpdatedServiceCallsAndTexts
    ): FiberServiceResult<Unit>

    @GET(EnvironmentPath.SALES_FORCE_QUERY)
    suspend fun getLiveCardInfo(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<PaymentInfoResponse>
}
