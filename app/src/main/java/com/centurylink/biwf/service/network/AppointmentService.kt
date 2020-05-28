package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.Appointments
import com.centurylink.biwf.model.appointment.ServiceAppointments
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AppointmentService {

    @GET("query")
    suspend fun getAppointmentDetails(@Query("q") id: String): FiberServiceResult<Appointments>

    @GET("sobjects/ServiceAppointment/{account-id}")
    suspend fun getServiceAppointments(@Path("account-id") id: String): FiberServiceResult<ServiceAppointments>
}