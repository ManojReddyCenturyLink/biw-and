package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.billing.BillingDetails
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET

interface BillingApiServices {

    @GET(EnvironmentPath.BILLING_DETAILS)
    suspend fun getBillingDetails(): FiberServiceResult<List<BillingDetails>>
}