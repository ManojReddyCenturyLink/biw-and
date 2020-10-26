package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.appointment.Appointments
import com.centurylink.biwf.model.appointment.ServiceAppointments
import com.centurylink.biwf.service.network.AppointmentService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AppointmentServiceTest : BaseServiceTest() {

    private lateinit var appointmentService: AppointmentService

    @Before
    fun setup() {
        createServer()
        appointmentService = retrofit.create(AppointmentService::class.java)
    }

    @Test
    fun testGetAppointmentsDetailsSuccess() = runBlocking {
        enqueueResponse("appointments.json")
        val posts: FiberServiceResult<Appointments> =
            appointmentService.getAppointmentDetails("sss")
        Assert.assertEquals(posts.map { it.records[0].id }, Either.Right("08pf00000008gvRAAQ"))
        Assert.assertEquals(
            posts.map { it.records[0].WorkTypeId },
            Either.Right("08qf00000008QgoAAE")
        )
    }

    @Test
    fun testGetAppointmentsDetailsError() = runBlocking {
        val posts: FiberServiceResult<Appointments> =
            appointmentService.getAppointmentDetails("12233")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }

    @Test
    fun testGetServiceAppointmentsSuccess() = runBlocking {
        enqueueResponse("serviceappointments.json")
        val posts: FiberServiceResult<ServiceAppointments> =
            appointmentService.getServiceAppointments("sss")
        Assert.assertEquals(posts.map { it.Id }, Either.Right("08pf00000008gvRAAQ"))
        Assert.assertEquals(posts.map { it.ownerId }, Either.Right("005f0000003lAdxAAE"))
    }

    @Test
    fun testGetServiceAppointmentsError() = runBlocking {
        val posts: FiberServiceResult<ServiceAppointments> =
            appointmentService.getServiceAppointments("12233")
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}
