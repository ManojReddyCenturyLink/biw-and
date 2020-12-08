package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.subscription.SubscriptionDates
import com.centurylink.biwf.model.subscriptionDetails.SubscriptionDetails
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.Query

interface ZuoraSubscriptionApiService {

    @GET(EnvironmentPath.SALES_FORCE_QUERY)
    suspend fun getSubscriptionDetails(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<SubscriptionDetails>

    @GET(EnvironmentPath.SALES_FORCE_QUERY_SLASH)
    suspend fun getSubscriptionDate(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<SubscriptionDates>
}
