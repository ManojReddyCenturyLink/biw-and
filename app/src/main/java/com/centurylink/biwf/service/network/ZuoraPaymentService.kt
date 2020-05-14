package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.account.PaymentList
import retrofit2.http.GET
import retrofit2.http.Query

interface ZuoraPaymentService {

    @GET("query/")
    suspend fun getZuoraPaymentDetails(@Query("q", encoded = true) id: String): PaymentList
}