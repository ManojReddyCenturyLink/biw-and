package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.sumup.SumUpInput
import com.centurylink.biwf.model.sumup.SumUpResult
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * This interface bundles are services that are not yet ready on the backend, to
 * (temporarily) unblock the app's integration with the backend.
 */
interface IntegrationRestServices {
    /**
     * A dummy/test integration service that just sums up two values.
     */
    @Deprecated("This service is just a proof-of-concept.")
    @POST("sumUp/{value1}")
    suspend fun calculateSum(
        @Path("value1") value1: Int,
        @Query("value2") value2: Int,
        @Body input: SumUpInput
    ): SumUpResult
}