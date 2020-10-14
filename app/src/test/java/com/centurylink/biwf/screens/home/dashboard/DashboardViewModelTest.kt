package com.centurylink.biwf.screens.home.dashboard

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.SpeedTestRepository
import com.centurylink.biwf.model.speedtest.SpeedTestRequestResult
import com.centurylink.biwf.model.speedtest.SpeedTestResponse
import com.centurylink.biwf.model.speedtest.SpeedTestStatus
import com.centurylink.biwf.model.speedtest.SpeedTestStatusNestedResults
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.repos.*
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.repos.assia.WifiStatusRepository
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
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
import org.junit.Ignore
import org.junit.Test
import org.threeten.bp.LocalDateTime

@Suppress("EXPERIMENTAL_API_USAGE")
class DashboardViewModelTest : ViewModelBaseTest() {

    @MockK
    lateinit var notificationRepository: NotificationRepository

    @MockK
    lateinit var appointmentRepository: AppointmentRepository

    @MockK
    lateinit var accountRepository: AccountRepository

    @MockK
    lateinit var speedTestRepository: SpeedTestRepository

    @MockK
    lateinit var modemRebootMonitorService: ModemRebootMonitorService

    @MockK
    lateinit var devicesRepository: DevicesRepository

    @MockK
    lateinit var wifiNetworkManagementRepository: WifiNetworkManagementRepository

    @MockK
    lateinit var wifiStatusRepository: WifiStatusRepository



    @MockK
    lateinit var mockPreferences: Preferences

    private lateinit var appointmentComplete: DashboardViewModel.AppointmentComplete
    private lateinit var appointmentWIP: DashboardViewModel.AppointmentEngineerWIP
    private lateinit var appointmentEnroute: DashboardViewModel.AppointmentEngineerStatus
    private lateinit var appointmentSchedule: DashboardViewModel.AppointmentScheduleState
    private lateinit var accountDetails: AccountDetails
    @MockK
    private lateinit var mockAssiaRepository: AssiaRepository
    @MockK
    private lateinit var mockOAuthAssiaRepository: OAuthAssiaRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

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

    private lateinit var speedTestResponse: SpeedTestResponse

    private lateinit var modemInfo: ModemInfo

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val notificationSource = NotificationSource()
        notificationSource.notificationlist = notificationList
        // result.value = Resource(Status.SUCCESS, notificationSource, "")
        val date: LocalDateTime = LocalDateTime.now()
        every { mockPreferences.getUserType() } returns true
        run { analyticsManagerInterface }
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
            serviceAppointmentDate = "",appointmentNumber = ""
        )
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        speedTestResponse = fromJson(readJson("speedtest-response.json"))
        modemInfo = fromJson(readJson("modemInfo.json"))
        coEvery { mockOAuthAssiaRepository.getModemInfo() } returns  Either.Right(modemInfo)
        viewModel = DashboardViewModel(
            notificationRepository = notificationRepository,
            appointmentRepository = appointmentRepository,
            sharedPreferences = mockPreferences,
            assiaRepository = mockAssiaRepository,
            oAuthAssiaRepository = mockOAuthAssiaRepository,
            devicesRepository = devicesRepository,
            accountRepository = accountRepository,
            wifiNetworkManagementRepository = wifiNetworkManagementRepository,
            wifiStatusRepository = wifiStatusRepository,
            modemRebootMonitorService = modemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface,
            speedTestRepository = speedTestRepository
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
        val method = viewModel.javaClass.getDeclaredMethod("initDevicesApis")
        method.isAccessible = true
    }

    @Ignore
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

    @Ignore
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
                timeZone = "",appointmentNumber = ""

            )
        )
        viewModel = DashboardViewModel(
            notificationRepository = notificationRepository,
            appointmentRepository = appointmentRepository,
            sharedPreferences = mockPreferences,
            assiaRepository = mockAssiaRepository,
            oAuthAssiaRepository = mockOAuthAssiaRepository,
            devicesRepository = devicesRepository,
            accountRepository = accountRepository,
            wifiNetworkManagementRepository = wifiNetworkManagementRepository,
            wifiStatusRepository = wifiStatusRepository,
            modemRebootMonitorService = modemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface,
            speedTestRepository = speedTestRepository
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

    @Test
    fun testAnalyticsButtonClicked(){
        runBlockingTest {
            launch {
                Assert.assertNotNull(analyticsManagerInterface)
                viewModel.logCancelAppointmentClick()
                viewModel.navigateToNetworkInformation()
                viewModel.navigateToQRScan(WifiInfo())
                viewModel.getStartedClicked()
                viewModel.logViewDevicesClick()
                viewModel.logDismissNotification()
                viewModel.startSpeedTest(true)
                viewModel.logCancelAppointmentAlertClick(true)
            }
        }
    }
    @Test
    fun testLogAppointmentStatusState(){
        runBlockingTest {
            launch {
                Assert.assertNotNull( viewModel.logAppointmentStatusState(1))
                Assert.assertNotNull( viewModel.logAppointmentStatusState(2))
                Assert.assertNotNull( viewModel.logAppointmentStatusState(3))
                Assert.assertNotNull( viewModel.logAppointmentStatusState(4))
                Assert.assertNotNull( viewModel.logAppointmentStatusState(5))
            }
        }
    }

    @Test
    fun testLogCancelAppointmentAlertClickFailure(){
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logCancelAppointmentAlertClick(false))
            }
        }
    }

    @Test
    fun testCheckForOngoingSpeedTest(){
        runBlockingTest {
            launch {
                Assert.assertNotNull( viewModel.checkForOngoingSpeedTest())
            }
        }
    }

    @Test
    fun testHandleRebootStatus(){
        runBlockingTest {
            launch {
                Assert.assertNotNull(
                    viewModel.handleRebootStatus(ModemRebootMonitorService.RebootState.ONGOING)
                )
            }
        }
    }

    @Test
    fun testRequestAppointmentCancellationSucess(){
        runBlockingTest {
            launch {

            }
        }
    }
}