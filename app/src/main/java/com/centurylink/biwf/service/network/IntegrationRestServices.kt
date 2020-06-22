package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.Appointments
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.model.sumup.SumUpInput
import com.centurylink.biwf.model.sumup.SumUpResult
import com.centurylink.biwf.model.usagedetails.UsageDetails
import retrofit2.http.*

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

    @GET("/sobject/notification/{value1}")
    suspend fun getNotificationDetails(
        @Path("value1") value1: String
    ): FiberServiceResult<NotificationSource>

    @GET("/sobject/appointment/{value1}")
    suspend fun getAppointmentDetails(
        @Path("value1") value1: String
    ): FiberServiceResult<Appointments>

    @GET("/sobject/faq/{value1}")
    suspend fun getFaqDetails(
        @Path("value1") value1: String
    ): FiberServiceResult<Faq>

    //reference: https://ctlink-biwf-staging.cloudcheck.net:443/cloudcheck-sp/api/v2/wifi/diags/station/traffic
    @GET("/wifi/diags/station/{apiPath}")
    suspend fun getUsageDetails(
        @Path("apiPath") value1: String
    ): FiberServiceResult<UsageDetails>
}
