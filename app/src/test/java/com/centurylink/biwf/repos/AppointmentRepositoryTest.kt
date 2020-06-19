package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.appointment.*
import com.centurylink.biwf.service.network.AppointmentService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns "12345"
        val appointmentString = readJson("appointments.json")
        appointments = fromJson(appointmentString)
        coEvery {
            appointmentService.getAppointmentDetails(any())
        } returns Either.Right(appointments)
        appointmentRepository =
            AppointmentRepository(mockPreferences, appointmentService, integrationRestServices)
    }

    @Test
    fun testgetAppointmentErrorInfo() {
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
                var modifiedappointments = Appointments(records = emptyList())
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(modifiedappointments)
                val appInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    appInfo.map { it.appointmentId },
                    Either.Left("Appointment Records is Empty")
                )
            }
        }
    }

    @Test
    fun testgetAppointmentSuccess() {
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
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = serviceResources
                )
                var modifiedappointments = Appointments(records = listOf(tempRecords))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(modifiedappointments)
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
                var emptyJobtypeApp = Appointments(records = listOf(emptyJobtype))

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
                var modifiedappointments = Appointments(records = listOf(emptyStartTime))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(modifiedappointments)
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
                var emptyendappointments = Appointments(records = listOf(emptyendTime))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(emptyendappointments)
                val emptySendTimeAppInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    emptySendTimeAppInfo.mapLeft { it },
                    Either.Left("Mandatory Records  is Empty")
                )
            }
        }
    }

    @Test
    fun testEmptyStatusConditions() {
        runBlocking {
            launch {
                val emptyString: String? = null
                val emptyDate: LocalDateTime? = null
                val emptyStatus: ServiceStatus? = null
                val serviceRecord = serviceRecords(ServiceResource(id = "12345", name = "Pravin"))
                val serviceResources = ServiceResources(records = listOf(serviceRecord))
                val emptyStatusApp = AppointmentRecords(
                    id = "08pf00000008dTjAAI",
                    arrivalWindowEndTime = LocalDateTime.now(),
                    arrivalWindowStarTime = LocalDateTime.now(),
                    appointmentStatus = emptyStatus,
                    JobType = "Installation",
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = serviceResources
                )
                var modifiedappointments = Appointments(records = listOf(emptyStatusApp))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(modifiedappointments)
                val emptyStartTimeAppInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    emptyStartTimeAppInfo.mapLeft { it },
                    Either.Left("Mandatory Records  is Empty")
                )
            }
        }
    }

    @Test
    fun testEmptyServiceResources() {
        runBlocking {
            launch {
                val emptyString: String? = null
                val serviceRecord = serviceRecords(ServiceResource(id = "12345", name = "Pravin"))

                val appointmentssall = AppointmentRecords(
                    id = "08pf00000008dTjAAI",
                    arrivalWindowEndTime = LocalDateTime.now(),
                    arrivalWindowStarTime = LocalDateTime.now(),
                    appointmentStatus = ServiceStatus.SCHEDULED,
                    JobType = "Installation",
                    latitude = "39.852448",
                    longitude = "39.852448", serviceResources = ServiceResources()
                )
                var emptyendappointments = Appointments(records = listOf(appointmentssall))
                every { mockPreferences.getValueByID(any()) } returns "123"
                coEvery {
                    appointmentService.getAppointmentDetails(any())
                } returns Either.Right(emptyendappointments)
                val emptySendTimeAppInfo = appointmentRepository.getAppointmentInfo()
                Assert.assertEquals(
                    emptySendTimeAppInfo.mapLeft { it },
                    Either.Left("Mandatory Records  is Empty")
                )

                //Empty ServiceResources
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
                var emptyJobtypeApp = Appointments(records = listOf(emptyJobtype))

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
                var emptyJobtypeApp = Appointments(records = listOf(emptyJobtype))

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
}