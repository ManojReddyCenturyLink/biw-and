package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.UpdatedServiceCallsAndTexts
import com.centurylink.biwf.model.user.UserInfo
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface AccountApiService {

    @GET("sobjects/Account/{account-id}")
    suspend fun getAccountDetails(@Path("account-id") id: String): FiberServiceResult<AccountDetails>

    @PATCH("sobjects/Account/{account-id}")
    suspend fun submitServiceCallDetails(
        @Path("account-id") id: String,
        @Body updateCallsAndText: UpdatedServiceCallsAndTexts
    ):FiberServiceResult<Unit>
}