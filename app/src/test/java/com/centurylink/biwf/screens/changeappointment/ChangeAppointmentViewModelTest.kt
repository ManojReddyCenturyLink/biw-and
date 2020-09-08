package com.centurylink.biwf.screens.changeappointment

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.AppointmentSlots
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.repos.AppointmentRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class ChangeAppointmentViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: ChangeAppointmentViewModel

    @MockK
    private lateinit var appointmentRepository: AppointmentRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = ChangeAppointmentViewModel(
            appointmentRepository = appointmentRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun testRequestAppointmentDetailsSuccess() {
        runBlockingTest {
            launch {
                val date: LocalDateTime = LocalDateTime.now()
                coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Right(
                    AppointmentRecordsInfo(
                        serviceAppointmentStartDate = date,
                        serviceAppointmentEndTime = date,
                        serviceEngineerName = "",
                        serviceEngineerProfilePic = "",
                        serviceStatus = ServiceStatus.COMPLETED,
                        serviceLatitude = "",
                        serviceLongitude = "",
                        jobType = "",
                        appointmentId = "",
                        timeZone = "",appointmentNumber = ""
                    )
                )
                val dates= listOf("","")

                val map = HashMap<String, List<String>>()
                map.put("1", dates)
                coEvery { appointmentRepository.getAppointmentSlots(any(),any()) } returns Either.Right(
                    AppointmentSlots(totalSize = "1",slots = map,serviceAppointmentId="")
                )
                viewModel.initApis()

            }
        }
    }

    @Test
    fun testRequestAppointmentDetailsError() {
        runBlockingTest {
            launch {
                val date: LocalDateTime = LocalDateTime.now()
                coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Left(
                   "Error"
                )
                val dates= listOf("","")

                val map = HashMap<String, List<String>>()
                map.put("1", dates)
                coEvery { appointmentRepository.getAppointmentSlots(any(),any()) } returns Either.Left(
                    "Error"
                )
                viewModel.initApis()

            }
        }
    }

}