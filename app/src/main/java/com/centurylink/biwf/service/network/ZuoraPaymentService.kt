package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentList
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ZuoraPaymentService {

    @GET("query/")
    suspend fun getZuoraPaymentDetails(@Query("q", encoded = true) id: String): FiberServiceResult<PaymentList>

    @GET("sobjects/Zuora__Payment__c//{invoice-id}")
    suspend fun getPaymentDetails(@Path("account-id") id: String): FiberServiceResult<AccountDetails>
}