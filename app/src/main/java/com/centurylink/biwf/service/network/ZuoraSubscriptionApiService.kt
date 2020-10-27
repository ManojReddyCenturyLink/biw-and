package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.subscription.SubscriptionDates
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ZuoraSubscriptionApiService {

    @GET(EnvironmentPath.API_SUBSCRIPTION_DETAILS_PATH)
    suspend fun getSubscriptionDetails(@Path(EnvironmentPath.ACCOUNT_ID) id: String): FiberServiceResult<Unit>

    @GET(EnvironmentPath.SALES_FORCE_QUERY_SLASH)
    suspend fun getSubscriptionDate(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<SubscriptionDates>
}
