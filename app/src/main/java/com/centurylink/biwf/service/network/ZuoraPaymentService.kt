package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.payment.PaymentDetails
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ZuoraPaymentService {

    @GET("query/")
    suspend fun getZuoraPaymentDetails(@Query("q") id: String): FiberServiceResult<PaymentList>

    @GET("sobjects/Zuora__Payment__c/{invoice-id}")
    suspend fun getPaymentDetails(@Path("invoice-id") id: String): FiberServiceResult<PaymentDetails>

    @GET("query/")
    suspend fun getSubscriptionDetails(@Query("q") id: String): FiberServiceResult<PaymentList>
}