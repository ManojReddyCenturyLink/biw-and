package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.appointment.*
import com.centurylink.biwf.service.network.AppointmentService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.any
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

class AppointmentRepositoryTest : BaseRepositoryTest() {

    private lateinit var appointmentRepository: AppointmentRepository

    @MockK(relaxed = true)
    private lateinit var appointmentService: AppointmentService

    @MockK(relaxed = true)
    private lateinit var integrationRestServices: IntegrationRestServices

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var appointments: Appointments

    private lateinit var appointmentSlots: AppointmentSlots

    private lateinit var appointmentResponse: AppointmentResponse

    private lateinit var cancelResponse: CancelResponse

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        val appointmentString = readJson("appointments.json")
        appointments = fromJson(appointmentString)
        val appointmentSlotsString = readJson("getslots.json")
        appointmentSlots = fromJson(appointmentSlotsString)

        val appointmentChangeString = readJson("appointmentchange.json")
        appointmentResponse = fromJson(appointmentChangeString)

        val cancelResponseString = readJson("cancelresponse.json")
        cancelResponse = fromJson(cancelResponseString)

        coEvery {
            appointmentService.getAppointmentDetails(any())
        } returns Either.Right(appointments)
        appointmentRepository =
            AppointmentRepository(mockPreferences, appointmentService, integrationRestServices)
    }

    @Test
    fun testGetAppointmentErrorInfo() {
        runBlocking {
            launch {
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(appointments)
                val appInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    appInfo.map { it.appointmentId },
                    Either.Left("Mandatory Records  is Empty")
                )
            }
        }
    }

    @Test
    fun testgetAppointmentErrorInfoAccountID() {
        runBlocking {
            launch {
                every { mockPreferences.getValueByID(any()) } returns ""
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(appointments)
                val appInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    appInfo.map { it.appointmentId },
                    Either.Left("Account ID is not available")
                )
            }
        }
    }

    @Test
    fun testgetAppointmentErrorInfoAccountIDNull() {
        runBlocking {
            launch {
                val emptyString: String? = null
                every { mockPreferences.getValueByID(any()) } returns emptyString
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(appointments)
                val appInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    appInfo.map { it.appointmentId },
                    Either.Left("Account ID is not available")
                )
            }
        }
    }

    @Test
    fun testgetAppointmentErrorEmptyRecords() {
        runBlocking {
            launch {
                val modifiedAppointments = Appointments(records = emptyList())
                every { mockPreferences.getValueByID(any()) } returns Constants.ID
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(modifiedAppointments)
                val appInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    appInfo.map { it.appointmentId },
                    Either.Left("No Appointment Records")
                )
            }
        }
    }

    @Test
    fun testGetAppointmentSuccess() {
        runBlocking {
            launch {
                val serviceRecord = serviceRecords(ServiceResource(id = "12345", name = "Pravin"))
                val serviceResources = ServiceResources(records = listOf(serviceRecord))
                val tempRecords = AppointmentRecords(
                    id = "08pf00000008dTjAAI",
                    arrivalWindowEndTime = LocalDateTime.now(),
                    arrivalWindowStarTime = LocalDateTime.now(),
                    appointmentStatus = ServiceStatus.SCHEDULED,
                    JobType = "Installation",
                    appointmentNumber = "1111",
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = serviceResources
                )
                val modifiedAppointments = Appointments(totalSize = 1, done = true, records = listOf(tempRecords))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(modifiedAppointments)
                val appInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    appInfo.map { it.appointmentId },
                    Either.Right("08pf00000008dTjAAI")
                )
            }
        }
    }

    @Test
    fun testAllEmptyDateandJobStatusConditions() {
        runBlocking {
            launch {
                val emptyString: String? = null
                val serviceRecord = serviceRecords(ServiceResource(id = "12345", name = "Pravin"))
                val serviceResources = ServiceResources(records = listOf(serviceRecord))
                val emptyJobtype = AppointmentRecords(
                    id = "08pf00000008dTjAAI",
                    arrivalWindowEndTime = LocalDateTime.now(),
                    arrivalWindowStarTime = LocalDateTime.now(),
                    appointmentStatus = ServiceStatus.SCHEDULED,
                    JobType = emptyString,
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = serviceResources
                )
                val emptyJobtypeApp = Appointments(records = listOf(emptyJobtype))

                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(emptyJobtypeApp)
                val appInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    appInfo.mapLeft { it },
                    Either.Left("Mandatory Records  is Empty")
                )
            }
        }
    }

    @Test
    fun testAllEmptyDateStartTime() {
        runBlocking {
            launch {
                val emptyDate: LocalDateTime? = null
                val serviceRecord = serviceRecords(ServiceResource(id = "12345", name = "Pravin"))
                val serviceResources = ServiceResources(records = listOf(serviceRecord))
                val emptyStartTime = AppointmentRecords(
                    id = "08pf00000008dTjAAI",
                    arrivalWindowEndTime = emptyDate,
                    arrivalWindowStarTime = LocalDateTime.now(),
                    appointmentStatus = ServiceStatus.SCHEDULED,
                    JobType = "Installation",
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = serviceResources
                )
                val modifiedAppointments = Appointments(records = listOf(emptyStartTime))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(modifiedAppointments)
                val emptyStartTimeAppInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    emptyStartTimeAppInfo.mapLeft { it },
                    Either.Left("Mandatory Records  is Empty")
                )
            }
        }
    }

    @Test
    fun testAllEmptyDateEndTime() {
        runBlocking {
            launch {
                val emptyDate: LocalDateTime? = null
                val serviceRecord = serviceRecords(ServiceResource(id = "12345", name = "Pravin"))
                val serviceResources = ServiceResources(records = listOf(serviceRecord))
                val emptyendTime = AppointmentRecords(
                    id = "08pf00000008dTjAAI",
                    arrivalWindowEndTime = LocalDateTime.now(),
                    arrivalWindowStarTime = emptyDate,
                    appointmentStatus = ServiceStatus.SCHEDULED,
                    JobType = "Installation",
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = serviceResources
                )
                val emptyendAppointments = Appointments(records = listOf(emptyendTime))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(emptyendAppointments)
                val emptySendTimeAppInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    emptySendTimeAppInfo.mapLeft { it },
                    Either.Left("Mandatory Records  is Empty")
                )
            }
        }
    }

    @Test
    fun testEmptyServiceResourcesId() {
        runBlocking {
            launch {
                val appointmentRecords = AppointmentRecords(
                    id = "",
                    arrivalWindowEndTime = LocalDateTime.now(),
                    arrivalWindowStarTime = LocalDateTime.now(),
                    appointmentStatus = ServiceStatus.SCHEDULED,
                    JobType = "Installation",
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = ServiceResources()
                )
                val emptyEndAppointments = Appointments(records = listOf(appointmentRecords))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(emptyEndAppointments)
                val emptySendTimeAppInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    emptySendTimeAppInfo.mapLeft { it },
                    Either.Left("Appointment id is Empty")
                )
            }
        }
    }

    @Test
    fun testEmptyJobType() {
        runBlocking {
            launch {
                val emptyString: String? = null
                val emptyserviceResources =
                    ServiceResources(records = listOf(serviceRecords(ServiceResource())))

                val emptyJobtype = AppointmentRecords(
                    id = "08pf00000008dTjAAI",
                    arrivalWindowEndTime = LocalDateTime.now(),
                    arrivalWindowStarTime = LocalDateTime.now(),
                    appointmentStatus = ServiceStatus.SCHEDULED,
                    JobType = emptyString,
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = emptyserviceResources
                )
                val emptyJobtypeApp = Appointments(records = listOf(emptyJobtype))

                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(emptyJobtypeApp)
                val appInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    appInfo.mapLeft { it },
                    Either.Left("Mandatory Records  is Empty")
                )
            }
        }
    }

    @Test
    fun getAppointmentSlots() {
        runBlocking {
            launch {
                coEvery {
                    appointmentService.getAppointmentSlots(
                        any(),
                        any()
                    )
                } returns Either.Right(
                    appointmentSlots
                )
                val appointmentSlotsInfo = appointmentRepository.getAppointmentSlots("", "")
                Assert.assertEquals(
                    appointmentSlotsInfo.map { it.serviceAppointmentId },
                    Either.Right(Constants.SERVICE_APPOINTMENT_NUMBER)
                )
            }
        }
    }

    @Test
    fun modifyAppointmentInfo() {
        runBlocking {
            launch {
                coEvery { appointmentService.reScheduleAppointment(any()) } returns Either.Right(
                    appointmentResponse
                )
                val appointmentInfo = appointmentRepository.modifyAppointmentInfo(any())
                Assert.assertEquals(
                    appointmentInfo.map { it.serviceAppointmentId },
                    Either.Right(Constants.SERVICE_APPOINTMENT_NUMBER)
                )
            }
        }
    }

    @Test
    fun cancelAppointment() {
        runBlocking {
            launch {
                coEvery { appointmentService.cancelAppointment(any()) } returns Either.Right(
                    cancelResponse
                )
                val cancelAppointmentInfo = appointmentRepository.cancelAppointment(any())
                Assert.assertEquals(cancelAppointmentInfo.map { it.status }, Either.Right("OK"))
                Assert.assertEquals(
                    cancelAppointmentInfo.map { it.serviceAppointmentNumber },
                    Either.Right(Constants.SERVICE_APPOINTMENT_NUMBER)
                )
            }
        }
    }
}
