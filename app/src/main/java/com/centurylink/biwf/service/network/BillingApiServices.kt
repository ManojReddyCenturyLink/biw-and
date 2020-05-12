package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.billing.BillingDetails
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BillingApiServices {

    @GET("query/")
    suspend fun getZuoraPaymentDetails(@Query("q") id: String): BillingDetails


}