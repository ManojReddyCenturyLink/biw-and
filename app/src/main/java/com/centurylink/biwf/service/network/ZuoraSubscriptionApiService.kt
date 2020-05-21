package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.account.PaymentList
import com.centurylink.biwf.model.subscription.SubscriptionDates
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ZuoraSubscriptionApiService {

    @GET("sobjects/Zuora__Subscription__c/{account-id}")
    suspend fun getSubscriptionDetails(@Path("account-id") id: String): FiberServiceResult<Unit>

    @GET("query/")
    suspend fun getSubscriptionDate(@Query("q") id: String): FiberServiceResult<SubscriptionDates>
}