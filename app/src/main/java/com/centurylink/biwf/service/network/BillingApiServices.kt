package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.billing.BillingDetails
import retrofit2.http.GET
import retrofit2.http.Path

interface BillingApiServices {

    @GET("invoice.json")
    suspend fun getBillingDetails(): List<BillingDetails>
}