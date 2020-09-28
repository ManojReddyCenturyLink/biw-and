package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.*
import com.centurylink.biwf.utility.EnvironmentPath
import retrofit2.http.*

/**
 * interface for all the Calls  related appointments
 */
interface AppointmentService {

    @GET(EnvironmentPath.SALES_FORCE_QUERY)
    suspend fun getAppointmentDetails(@Query(EnvironmentPath.SALES_FORCE_QUERY_VALUE) id: String): FiberServiceResult<Appointments>

    @GET(EnvironmentPath.API_SERVICE_APPOINTMENTS_PATH)
    suspend fun getServiceAppointments(@Path(EnvironmentPath.ACCOUNT_ID) id: String): FiberServiceResult<ServiceAppointments>

    @GET(EnvironmentPath.API_APPOINTMENT_SLOT_PATH)
    suspend fun getAppointmentSlots(
        @Query(EnvironmentPath.SERVICE_APPOINTMENT_ID) id: String,
        @Query(EnvironmentPath.EARLIEST_PERMITTED_DATE) date: String
    ): FiberServiceResult<AppointmentSlots>

    @POST(EnvironmentPath.API_RESCHEDULE_APPOINTMENT_PATH)
    suspend fun reScheduleAppointment(@Body rescheduleInfo: RescheduleInfo): FiberServiceResult<AppointmentResponse>

    @POST(EnvironmentPath.API_CANCEL_APPOINTMENT_PATH)
    suspend fun cancelAppointment(@Body cancelAppointmentInfo: CancelAppointmentInfo): FiberServiceResult<CancelResponse>
}