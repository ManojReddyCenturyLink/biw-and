package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.*
import retrofit2.http.*

interface AppointmentService {

    @GET("query")
    suspend fun getAppointmentDetails(@Query("q") id: String): FiberServiceResult<Appointments>

    @GET("sobjects/ServiceAppointment/{account-id}")
    suspend fun getServiceAppointments(@Path("account-id") id: String): FiberServiceResult<ServiceAppointments>

    @GET("/services/apexrest/AppointmentSlotsMobile/")
    suspend fun getAppointmentSlots(@Query("ServiceAppointmentId") id: String,
                                    @Query("EarliestPermittedDate") date: String):FiberServiceResult<AppointmentSlots>

    @POST("services/apexrest/AppointmentSlotsMobile/")
    suspend fun reScheduleAppointment(@Body rescheduleInfo: RescheduleInfo):FiberServiceResult<AppointmentResponse>
}