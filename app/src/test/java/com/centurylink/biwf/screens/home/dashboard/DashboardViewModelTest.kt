package com.centurylink.biwf.screens.home.dashboard

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.threeten.bp.LocalDateTime

@Suppress("EXPERIMENTAL_API_USAGE")
class DashboardViewModelTest : ViewModelBaseTest() {

    @MockK
    lateinit var notificationRepository: NotificationRepository

    @MockK
    lateinit var appointmentRepository: AppointmentRepository

    @MockK
    lateinit var mockPreferences: Preferences

    private lateinit var appointmentComplete: DashboardViewModel.AppointmentComplete
    private lateinit var appointmentWIP: DashboardViewModel.AppointmentEngineerWIP
    private lateinit var appointmentEnroute: DashboardViewModel.AppointmentEngineerStatus
    private lateinit var appointmentSchedule: DashboardViewModel.AppointmentScheduleState

    @MockK
    private lateinit var mockAssiaRepository: AssiaRepository

    private val notificationList = mutableListOf(
        Notification(
            NotificationActivity.KEY_UNREAD_HEADER, "",
            "", "", true, ""
        ),
        Notification(
            NotificationActivity.KEY_READ_HEADER, "",
            "", "", false, ""
        ),
        Notification("1", "", "", "", true, ""),
        Notification("2", "", "", "", false, "")
    )

    private lateinit var viewModel: DashboardViewModel


    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val notificationSource = NotificationSource()
        notificationSource.notificationlist = notificationList
       // result.value = Resource(Status.SUCCESS, notificationSource, "")
        val date: LocalDateTime = LocalDateTime.now()
        every { mockPreferences.getUserType() } returns true
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
                timeZone = ""
            )
        )
        coEvery { notificationRepository.getNotificationDetails() } returns Either.Right(
            NotificationSource()
        )
        coEvery { notificationRepository.getNotificationDetails() } returns Either.Left(
            error = ""
        )
        appointmentComplete = DashboardViewModel.AppointmentComplete(
            jobType = "",
            status = ServiceStatus.COMPLETED
        )
        appointmentWIP = DashboardViewModel.AppointmentEngineerWIP(
            jobType = "abc",
            status = ServiceStatus.WORK_BEGUN,
            serviceLatitude = "11",
            serviceLongitude = "11",
            serviceEngineerName = "abc",
            serviceEngineerProfilePic = "abc"
        )
        appointmentEnroute = DashboardViewModel.AppointmentEngineerStatus(
            jobType = "abc",
            status = ServiceStatus.EN_ROUTE,
            serviceLatitude = "11",
            serviceLongitude = "11",
            serviceAppointmentEndTime = "",
            serviceAppointmentStartTime = "",
            serviceAppointmentTime = "",
            serviceEngineerName = "abc",
            serviceEngineerProfilePic = "abc"
        )
        appointmentSchedule = DashboardViewModel.AppointmentScheduleState(
            jobType = "",
            status = ServiceStatus.SCHEDULED,
            serviceAppointmentEndTime = "",
            serviceAppointmentStartTime = "",
            serviceAppointmentDate = ""
        )
        viewModel = DashboardViewModel(
            notificationRepository = notificationRepository,
            appointmentRepository = appointmentRepository,
            sharedPreferences = mockPreferences,
            assiaRepository = mockAssiaRepository
        )
    }

    @Test
    fun onChangeAppointmentClicked_navigateToChangeAppointmentScreen() = runBlockingTest {
        launch {
            viewModel.getChangeAppointment()
        }
        Assert.assertEquals(
            "Change Appointment Screen wasn't Launched",
            DashboardCoordinatorDestinations.CHANGE_APPOINTMENT,
            viewModel.myState.first()
        )
    }

    @Test
    fun `On init Api Call`() = runBlockingTest {
        val method = viewModel.javaClass.getDeclaredMethod("initApis")
        method.isAccessible = true
    }

    @Test
    fun `On Status Appointment Completed`() = runBlockingTest {
        val method = viewModel.javaClass.getDeclaredMethod(
            "updateAppointmentStatus",
            AppointmentRecordsInfo::class.java
        )
        method.isAccessible = true
        val result = viewModel.dashBoardDetailsInfo
        Assert.assertEquals(
            "Appointment Completed Error Response",
            appointmentComplete,
            result.latestValue
        )
    }

    @Test
    fun `On Status WIP`() {
        val date: LocalDateTime = LocalDateTime.now()
        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Right(
            AppointmentRecordsInfo(
                serviceAppointmentStartDate = date,
                serviceAppointmentEndTime = date,
                serviceEngineerName = "abc",
                serviceEngineerProfilePic = "abc",
                serviceStatus = ServiceStatus.WORK_BEGUN,
                serviceLatitude = "11",
                serviceLongitude = "11",
                jobType = "abc",
                appointmentId = "123",
                timeZone = ""

            )
        )
        viewModel = DashboardViewModel(
            notificationRepository = notificationRepository,
            appointmentRepository = appointmentRepository,
            sharedPreferences = mockPreferences,
            assiaRepository = mockAssiaRepository
        )
        runBlockingTest {
            val method = viewModel.javaClass.getDeclaredMethod(
                "updateAppointmentStatus",
                AppointmentRecordsInfo::class.java
            )
            method.isAccessible = true
            Assert.assertEquals(
                "Appointment WIP Error Response",
                appointmentWIP,
                viewModel.dashBoardDetailsInfo.latestValue
            )
        }
    }

    @Test
    fun `retrieve Notification with ViewModel and Repository returns an data`() {
        with(viewModel) {
            notifications.value = notificationList
        }
        Assert.assertTrue(viewModel.notifications.value.size == notificationList.size)
    }

    @Test
    fun onNotificationClicked_navigateToNotificationDetailsScreen() = runBlockingTest {
        launch {
            viewModel.navigateToNotificationDetails(notificationList[0])
        }

        Assert.assertEquals(
            "Notification Details Screen wasn't Launched",
            DashboardCoordinatorDestinations.NOTIFICATION_DETAILS,
            viewModel.myState.first()
        )
    }

    @Test
    fun `mark Notification As Read`() = runBlockingTest {
        val notifications = emptyList<Notification>()
        launch {
            viewModel.markNotificationAsRead(notificationList[0])
        }
        Assert.assertEquals(notifications, viewModel.notifications.value)
    }

    @Test
    fun `display Sorted Notifications`() = runBlockingTest {
        val notifications = emptyList<Notification>()
        launch {
            viewModel.displaySortedNotifications(notificationList)
        }
    }

    @Test
    fun `on get Started Clicked`() = runBlockingTest {
        launch {
            val method = viewModel.javaClass.getDeclaredMethod("getStartedClicked")
            method.isAccessible = true
            Assert.assertEquals(true, mockPreferences.getUserType())
        }
    }

    @Test
    fun `get values from AppointmentEngineerWIP`() {
        Assert.assertEquals("abc", appointmentWIP.jobType)
        Assert.assertEquals("abc", appointmentWIP.serviceEngineerName)
        Assert.assertEquals("abc", appointmentWIP.serviceEngineerProfilePic)
        Assert.assertEquals("11", appointmentWIP.serviceLatitude)
        Assert.assertEquals("11", appointmentWIP.serviceLongitude)
        Assert.assertEquals(ServiceStatus.WORK_BEGUN, appointmentWIP.status)
    }

    @Test
    fun `get values from AppointmentEngineerStatus`() {
        Assert.assertEquals("abc", appointmentEnroute.jobType)
        Assert.assertEquals("abc", appointmentEnroute.serviceEngineerName)
        Assert.assertEquals("abc", appointmentEnroute.serviceEngineerProfilePic)
        Assert.assertEquals("11", appointmentEnroute.serviceLatitude)
        Assert.assertEquals("11", appointmentEnroute.serviceLongitude)
        Assert.assertEquals("", appointmentEnroute.serviceAppointmentEndTime)
        Assert.assertEquals("", appointmentEnroute.serviceAppointmentStartTime)
        Assert.assertEquals(ServiceStatus.EN_ROUTE, appointmentEnroute.status)
    }

    @Test
    fun `get values from AppointmentScheduleState`() {
        Assert.assertEquals("", appointmentSchedule.jobType)
        Assert.assertEquals("", appointmentSchedule.serviceAppointmentDate)
        Assert.assertEquals("", appointmentSchedule.serviceAppointmentEndTime)
        Assert.assertEquals("", appointmentSchedule.serviceAppointmentStartTime)
        Assert.assertEquals(ServiceStatus.SCHEDULED, appointmentSchedule.status)
    }

    @Test
    fun `get values from AppointmentComplete`() {
        Assert.assertEquals("", appointmentComplete.jobType)
        Assert.assertEquals(ServiceStatus.COMPLETED, appointmentComplete.status)
    }
}