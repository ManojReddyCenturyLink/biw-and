package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.billing.BillingDetails
import retrofit2.http.GET

interface BillingApiServices {

    @GET("invoice1.json")
    suspend fun getBillingDetails(): FiberServiceResult<List<BillingDetails>>
}