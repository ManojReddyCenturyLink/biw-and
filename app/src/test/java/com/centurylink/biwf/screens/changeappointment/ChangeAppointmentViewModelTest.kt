package com.centurylink.biwf.screens.changeappointment

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.AppointmentResponse
import com.centurylink.biwf.model.appointment.AppointmentSlots
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.DateUtils
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

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
                        timeZone = "", appointmentNumber = ""
                    )
                )
                val date12Format = SimpleDateFormat("hh:mm a")
                val currentDate: String = date12Format.format(Date())
                val dates = listOf(currentDate, currentDate)

                val map = HashMap<String, List<String>>()
                map.put("1", dates)
                coEvery {
                    appointmentRepository.getAppointmentSlots(
                        any(),
                        any()
                    )
                } returns Either.Right(
                    AppointmentSlots(totalSize = "1", slots = map, serviceAppointmentId = "")
                )
                viewModel.initApis()
                Assert.assertEquals(viewModel.appointmentId, "")
            }
        }
    }

    @Test
    fun testRequestAppointmentDetailsError() {
        runBlockingTest {
            launch {
                val date: LocalDateTime = LocalDateTime.now()
                coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Left(
                    Constants.ERROR
                )
                val dates = listOf("", "")

                val map = HashMap<String, List<String>>()
                map.put("1", dates)
                coEvery {
                    appointmentRepository.getAppointmentSlots(
                        any(),
                        any()
                    )
                } returns Either.Left(
                    Constants.ERROR
                )
                viewModel.initApis()
                Assert.assertEquals(viewModel.errorMessageFlow.first(), Constants.ERROR)
            }
        }
    }

    @Ignore
    @Test
    fun testOnAppointmentSelectedDate() {
        runBlockingTest {
            launch {
                viewModel.onAppointmentSelectedDate(Date())
                Assert.assertEquals(
                    viewModel.appointmentSlotsInfo.first().serviceDate,
                    "2020-09-09"
                )
            }
        }
    }

    @Test
    fun navigateToAppointmentConfirmed() {
        runBlockingTest {
            launch {
                viewModel.appointmentDate = "03/08/2020"
                viewModel.appointmentSlots = "12.30PM"
                viewModel.appointmentId = "1"
                viewModel.navigateToAppointmentConfirmed()
                Assert.assertEquals(viewModel.myState.first().name, "APPOINTMENT_CONFIRMED")
            }
        }
    }

    @Test
    fun modifyAppointmentInfoFailure() {
        runBlockingTest {
            launch {
                coEvery { appointmentRepository.modifyAppointmentInfo(any()) } returns Either.Left(
                    Constants.ERROR
                )
                val date12Format = SimpleDateFormat("hh:mm a")
                val date: String = date12Format.format(Date())
                viewModel.appointmentId = "1"
                viewModel.onNextClicked(date, date.plus("-").plus(date))
                Assert.assertSame(viewModel.appointmenterrorEvents.hasObservers(), false)
            }
        }
    }

    @Test
    fun modifyAppointmentInfoSuccess() {
        runBlockingTest {
            launch {
                coEvery { appointmentRepository.modifyAppointmentInfo(any()) } returns Either.Right(
                    AppointmentResponse(serviceAppointmentId = "1", status = "Scheduled")
                )
                val date12Format = SimpleDateFormat("hh:mm a")
                val date: String = date12Format.format(Date())
                viewModel.appointmentId = "1"
                viewModel.onNextClicked(date, date.plus("-").plus(date))
                Assert.assertSame(viewModel.appointmenterrorEvents.hasObservers(), false)
            }
        }
    }

    @Test
    fun onNextClickedWithEmptySlots() {
        runBlockingTest {
            launch {
                val date12Format = SimpleDateFormat("hh:mm a")
                val date: String = date12Format.format(Date())
                viewModel.appointmentDate = "03/08/2020"
                viewModel.appointmentSlots = ""
                viewModel.appointmentId = "1"
                viewModel.onNextClicked(date, "")
                Assert.assertSame(viewModel.sloterrorEvents.hasObservers(), false)
            }
        }
    }

    @Test
    fun getNextEstimatedDateForSlots() {
        runBlockingTest {
            launch {
                val date12Format = SimpleDateFormat(DateUtils.STANDARD_FORMAT)
                val date: String = date12Format.format(Date())
                viewModel.appointmentDate = "03/08/2020"
                viewModel.appointmentSlots = ""
                viewModel.appointmentId = "1"
                Assert.assertNotNull(viewModel.getNextEstimatedDateForSlots(date))
            }
        }
    }

    @Test
    fun checkNextSlotFallsAfter() {
        runBlockingTest {
            launch {
                val date12Format = SimpleDateFormat(DateUtils.STANDARD_FORMAT)
                val date: String = date12Format.format(Date())
                viewModel.appointmentDate = "03/08/2020"
                viewModel.appointmentSlots = ""
                viewModel.appointmentId = "1"

                Assert.assertNotNull(viewModel.appointmentDate)
                Assert.assertSame(viewModel.checkNextSlotFallsAfter(date, date), false)
            }
        }
    }

    @Test
    fun logBackClick() {
        Assert.assertNotNull(viewModel.logBackClick())
    }
}