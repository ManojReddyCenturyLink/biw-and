package com.centurylink.biwf.screens.home.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.model.FiberErrorMessage
import com.centurylink.biwf.model.FiberHttpError
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.appointment.*
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.model.speedtest.*
import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.repos.*
import com.centurylink.biwf.repos.assia.AssiaTokenManager
import com.centurylink.biwf.repos.assia.SpeedTestRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.repos.assia.WifiStatusRepository
import com.centurylink.biwf.screens.notification.NotificationActivity
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.service.network.*
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.threeten.bp.LocalDateTime
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

@Suppress("EXPERIMENTAL_API_USAGE")
class DashboardViewModelTest : ViewModelBaseTest() {

    @MockK(relaxed = true)
    private lateinit var notificationRepository: NotificationRepository

    @MockK(relaxed = true)
    private lateinit var appointmentRepository: AppointmentRepository

    @MockK(relaxed = true)
    private lateinit var assiaRepository: AssiaRepository

    @MockK(relaxed = true)
    private lateinit var oAuthAssiaRepository: OAuthAssiaRepository

    @MockK(relaxed = true)
    private lateinit var accountRepository: AccountRepository

    @MockK(relaxed = true)
    private lateinit var devicesRepository: DevicesRepository

    @MockK(relaxed = true)
    private lateinit var wifiNetworkManagementRepository: WifiNetworkManagementRepository

    @MockK(relaxed = true)
    private lateinit var wifiStatusRepository: WifiStatusRepository

    @MockK(relaxed = true)
    private lateinit var speedTestRepository: SpeedTestRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: DashboardViewModel

    @MockK(relaxed = true)
    private lateinit var assiaService: AssiaService

    @MockK(relaxed = true)
    private lateinit var assiaTokenService: AssiaTokenService

    @MockK
    private lateinit var mockPreferences: Preferences

    @MockK
    private lateinit var assiaTokenManager: AssiaTokenManager

    @MockK(relaxed = true)
    private lateinit var appointmentService: AppointmentService

    private lateinit var modemInfoResponse: ModemInfoResponse

    private lateinit var devicesInfo: DevicesInfo

    private lateinit var speedTestRes: SpeedTestRes

    private lateinit var speedTestStatus: SpeedTestStatus

    private lateinit var speedTestResponse: SpeedTestResponse

    private lateinit var speedTestStatusResponse: SpeedTestStatusResponse

    private lateinit var blockResponse: BlockResponse

    private lateinit var assiaToken: AssiaToken

    @MockK(relaxed = true)
    private lateinit var accountApiService: AccountApiService

    private lateinit var accountDetails: AccountDetails

    private lateinit var networkDetails: NetworkDetails

    private lateinit var wifiInfo: WifiInfo

    @MockK(relaxed = true)
    private lateinit var speedTestService: SpeedTestService

    private lateinit var cancelResponse: CancelResponse

    private lateinit var appointments: Appointments

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

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns "12345"
        val appointmentString = readJson("appointments.json")
        appointments = fromJson(appointmentString)
        modemInfoResponse = fromJson(readJson("lineinfo.json"))
        devicesInfo = fromJson(readJson("devicedetails.json"))
        speedTestRes = fromJson(readJson("speedtest-req.json"))
        speedTestResponse = fromJson(readJson("speedtest-response.json"))
        speedTestStatusResponse = fromJson(readJson("speedtest-response.json"))
        speedTestStatus = fromJson(readJson("speedtest-status.json"))
        blockResponse = fromJson(readJson("blockunblock-response.json"))
        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Right(
            AppointmentRecordsInfo(
                serviceAppointmentStartDate = LocalDateTime.now(),
                serviceAppointmentEndTime = LocalDateTime.now(),
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
        assiaToken = AssiaToken("", "", "")
        coEvery { assiaService.getModemInfo(any()) } returns Either.Right(modemInfoResponse)
        coEvery { assiaService.getDevicesList(any()) } returns Either.Right(devicesInfo)
        coEvery { assiaRepository.getDevicesDetails() } returns Either.Right(devicesInfo.devicesDataList)
        coEvery {
            assiaService.blockDevice(
                any(),
                any(),
                any()
            )
        } returns Either.Right(blockResponse)
        coEvery { assiaService.unBlockDevice(any(), any(), any()) } returns Either.Right(
            blockResponse
        )
        coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
        assiaTokenManager = AssiaTokenManager(assiaTokenService)
        assiaRepository = AssiaRepository(mockPreferences, assiaService, assiaTokenManager)
        coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(modemInfoResponse.modemInfo)
        coEvery { oAuthAssiaRepository.getModemInfoForcePing() } returns Either.Right(
            modemInfoResponse.modemInfo
        )

        val hashMap = hashMapOf<String, String>()
        hashMap[NetWorkBand.Band2G.name] = "123"
        networkDetails = NetworkDetails("123", message = "success", networkName = hashMap)
        coEvery { wifiNetworkManagementRepository.getNetworkPassword(any()) } returns Either.Right(
            NetworkDetails(code = "123", message = "success", networkName = hashMapOf())
        )
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        coEvery { accountApiService.getAccountDetails(any()) } returns Either.Right(
            accountDetails
        )
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)

        coEvery { speedTestService.getSpeedTestDetails(any()) } returns Either.Right(
            speedTestRes
        )
        coEvery { speedTestService.getSpeedTestStatusDetails(any()) } returns Either.Right(
            speedTestStatusResponse
        )
        coEvery { speedTestRepository.checkSpeedTestStatus(any()) } returns Either.Right(
            speedTestStatus
        )
        speedTestRepository = SpeedTestRepository(mockPreferences, speedTestService)
        viewModel = DashboardViewModel(
            notificationRepository,
            appointmentRepository,
            mockPreferences,
            assiaRepository,
            oAuthAssiaRepository,
            devicesRepository,
            accountRepository,
            wifiNetworkManagementRepository,
            wifiStatusRepository,
            speedTestRepository,
            mockModemRebootMonitorService,
            analyticsManagerInterface
        )

    }

    @Test
    fun testRequestWifiDetailsSuccess() {
        runBlockingTest {
            launch {
                viewModel.initDevicesApis()
            }
        }
    }

    @Test
    fun testRequestAppointmentDetailsWorkBegun() {
        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Right(
            AppointmentRecordsInfo(
                serviceAppointmentStartDate = LocalDateTime.now(),
                serviceAppointmentEndTime = LocalDateTime.now(),
                serviceEngineerName = "",
                serviceEngineerProfilePic = "",
                serviceStatus = ServiceStatus.WORK_BEGUN,
                serviceLatitude = "",
                serviceLongitude = "",
                jobType = "",
                appointmentId = "",
                timeZone = "", appointmentNumber = ""
            )
        )
        viewModel = DashboardViewModel(
            notificationRepository,
            appointmentRepository,
            mockPreferences,
            assiaRepository,
            oAuthAssiaRepository,
            devicesRepository,
            accountRepository,
            wifiNetworkManagementRepository,
            wifiStatusRepository,
            speedTestRepository,
            mockModemRebootMonitorService,
            analyticsManagerInterface
        )
        runBlockingTest {
            launch {
                viewModel.initDevicesApis()
            }
        }
    }

    @Test
    fun testRequestAppointmentDetailsEnRoute() {
        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Right(
            AppointmentRecordsInfo(
                serviceAppointmentStartDate = LocalDateTime.now(),
                serviceAppointmentEndTime = LocalDateTime.now(),
                serviceEngineerName = "",
                serviceEngineerProfilePic = "",
                serviceStatus = ServiceStatus.EN_ROUTE,
                serviceLatitude = "",
                serviceLongitude = "",
                jobType = "",
                appointmentId = "",
                timeZone = "", appointmentNumber = ""
            )
        )
        viewModel = DashboardViewModel(
            notificationRepository,
            appointmentRepository,
            mockPreferences,
            assiaRepository,
            oAuthAssiaRepository,
            devicesRepository,
            accountRepository,
            wifiNetworkManagementRepository,
            wifiStatusRepository,
            speedTestRepository,
            mockModemRebootMonitorService,
            analyticsManagerInterface
        )
        runBlockingTest {
            launch {
                viewModel.initDevicesApis()
            }
        }
    }

    @Test
    fun testRequestAppointmentDetailsSchedule() {
        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Right(
            AppointmentRecordsInfo(
                serviceAppointmentStartDate = LocalDateTime.now(),
                serviceAppointmentEndTime = LocalDateTime.now(),
                serviceEngineerName = "",
                serviceEngineerProfilePic = "",
                serviceStatus = ServiceStatus.SCHEDULED,
                serviceLatitude = "",
                serviceLongitude = "",
                jobType = "",
                appointmentId = "",
                timeZone = "", appointmentNumber = ""
            )
        )
        viewModel = DashboardViewModel(
            notificationRepository,
            appointmentRepository,
            mockPreferences,
            assiaRepository,
            oAuthAssiaRepository,
            devicesRepository,
            accountRepository,
            wifiNetworkManagementRepository,
            wifiStatusRepository,
            speedTestRepository,
            mockModemRebootMonitorService,
            analyticsManagerInterface
        )
        runBlockingTest {
            launch {
                viewModel.initDevicesApis()
            }
        }
    }

    @Test
    fun testRequestWifiDetailsFailure() {
        runBlockingTest {
            launch {
                coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Left("Modem Info Error")
                viewModel.initDevicesApis()
            }
        }
    }

    @Test
    fun testStartSpeedTestSuccess() {
        runBlockingTest {
            launch {
                coEvery { speedTestService.getSpeedTestDetails(any()) } returns Either.Right(
                    speedTestRes
                )
                viewModel.startSpeedTest(true)
                val speedTestInformation = speedTestRepository.startSpeedTest()
                Assert.assertEquals(
                    speedTestInformation.map { it.success },
                    Either.Right(true)
                )
                Assert.assertEquals(
                    speedTestInformation.map { it.code },
                    Either.Right(1000)
                )
            }
        }
    }

    @Test
    fun testStartSpeedTestFailure() {
        runBlocking {
            launch {
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                speedTestRes = SpeedTestRes()
                coEvery { speedTestService.getSpeedTestDetails(any()) } returns Either.Right(
                    speedTestRes
                )
                viewModel.startSpeedTest(true)
                val speedTestInfo = speedTestRepository.startSpeedTest()
                Assert.assertEquals(speedTestInfo.mapLeft { it }, Either.Left("Request not found"))
            }
        }
    }

    @Test
    fun testGetAccountDetailsError() {
        runBlocking {
            launch {
                val fiberHttpError: FiberHttpError = FiberHttpError(
                    Constants.STATUS_CODE,
                    listOf(
                        FiberErrorMessage(
                            errorCode = Constants.ERROR_CODE_1000,
                            message = Constants.ERROR
                        )
                    )
                )
                coEvery { accountApiService.getAccountDetails(any()) } returns Either.Left(
                    fiberHttpError
                )
                coEvery { accountRepository.getAccountDetails() } returns Either.Left(
                    "Error"
                )
                val accountInfo = accountRepository.getAccountDetails()
                viewModel = DashboardViewModel(
                    notificationRepository,
                    appointmentRepository,
                    mockPreferences,
                    assiaRepository,
                    oAuthAssiaRepository,
                    devicesRepository,
                    accountRepository,
                    wifiNetworkManagementRepository,
                    wifiStatusRepository,
                    speedTestRepository,
                    mockModemRebootMonitorService,
                    analyticsManagerInterface
                )
                Assert.assertEquals(accountInfo.mapLeft { it }, Either.Left(Constants.ERROR))
            }
        }
    }

    @Test
    fun testLogScreenLaunch() {
        viewModel.logScreenLaunch()
    }

    @Test
    fun testRequestToDisableNetworkSuccess() {
        runBlockingTest {
            launch {
                coEvery { wifiStatusRepository.disableNetwork(any()) } returns Either.Right(
                    UpdateNetworkResponse(
                        code = "1234",
                        message = "Success",
                        data = true,
                        createErrorRecord = true
                    )
                )
                wifiInfo = WifiInfo(
                    category = NetWorkCategory.REGULAR,
                    type = NetWorkBand.Band2G.name,
                    name = "My Home WIFI",
                    password = "barcelona",
                    enabled = true
                )
                viewModel.wifiNetworkEnablement(wifiInfo)
            }
        }
    }

    @Test
    fun testRequestToDisableNetworkFailure() {
        runBlockingTest {
            launch {
                coEvery { wifiStatusRepository.disableNetwork(any()) } returns Either.Left(
                    "Error"
                )
                wifiInfo = WifiInfo(
                    category = NetWorkCategory.REGULAR,
                    type = "regular",
                    name = "My Home WIFI",
                    password = "barcelona",
                    enabled = true
                )
                viewModel.wifiNetworkEnablement(wifiInfo)
            }
        }
    }

    @Test
    fun testRequestEnableNetworkSuccess() {
        runBlockingTest {
            launch {
                coEvery { wifiStatusRepository.enableNetwork(any()) } returns Either.Right(
                    UpdateNetworkResponse(
                        code = "1234",
                        message = "Success",
                        data = true,
                        createErrorRecord = true
                    )
                )
                wifiInfo = WifiInfo(
                    category = NetWorkCategory.REGULAR,
                    type = NetWorkBand.Band2G_Guest4.name,
                    name = "My Home WIFI",
                    password = "barcelona",
                    enabled = false
                )
                viewModel.wifiNetworkEnablement(wifiInfo)
            }
        }
    }

    @Test
    fun testRequestToEnableNetworkFailure() {
        runBlockingTest {
            launch {
                coEvery { wifiStatusRepository.enableNetwork(any()) } returns Either.Left(
                    "Error"
                )
                wifiInfo = WifiInfo(
                    category = NetWorkCategory.REGULAR,
                    type = NetWorkBand.Band2G.name,
                    name = "My Home WIFI",
                    password = "barcelona",
                    enabled = false
                )
                viewModel.wifiNetworkEnablement(wifiInfo)
            }
        }
    }

    @Test
    fun testAnalyticsButtonClicked() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(analyticsManagerInterface)
                viewModel.logCancelAppointmentClick()
                viewModel.navigateToNetworkInformation()
                viewModel.navigateToQRScan(WifiInfo())
                viewModel.getStartedClicked()
                viewModel.logViewDevicesClick()
                viewModel.logDismissNotification()
                viewModel.logCancelAppointmentAlertClick(true)
            }
        }
    }

    @Test
    fun testLogAppointmentStatusState() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logAppointmentStatusState(1))
                Assert.assertNotNull(viewModel.logAppointmentStatusState(2))
                Assert.assertNotNull(viewModel.logAppointmentStatusState(3))
                Assert.assertNotNull(viewModel.logAppointmentStatusState(4))
                Assert.assertNotNull(viewModel.logAppointmentStatusState(5))
            }
        }
    }


    @Test
    fun testRequestAccountDetailsStatusCompleted() {
        runBlockingTest {
            launch {
                val accountString = readJson("account_activation_completed.json")
                accountDetails = fromJson(accountString)
                coEvery { accountApiService.getAccountDetails(any()) } returns Either.Right(
                    accountDetails
                )
                coEvery { accountRepository.getAccountDetails() } returns Either.Right(
                    accountDetails
                )

                viewModel = DashboardViewModel(
                    notificationRepository,
                    appointmentRepository,
                    mockPreferences,
                    assiaRepository,
                    oAuthAssiaRepository,
                    devicesRepository,
                    accountRepository,
                    wifiNetworkManagementRepository,
                    wifiStatusRepository,
                    speedTestRepository,
                    mockModemRebootMonitorService,
                    analyticsManagerInterface
                )
            }
        }
    }

    @Test
    fun testLogCancelAppointmentAlertClickFailure() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logCancelAppointmentAlertClick(false))
            }
        }
    }

    @Test
    fun testCheckForOngoingSpeedTestSuccess() {
        every { mockPreferences.getSpeedTestFlag() } returns true
        every { mockPreferences.getSpeedTestDownload() } returns "test"
        every { mockPreferences.getSpeedTestUpload() } returns "test"
        every { mockPreferences.getLastSpeedTestTime() } returns "test"

        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.checkForOngoingSpeedTest())
            }
        }
    }

    @Test
    fun testCheckForOngoingSpeedTestFailure() {
        every { mockPreferences.getSpeedTestFlag() } returns false
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.checkForOngoingSpeedTest())
            }
        }
    }

    @Test
    fun testHandleRebootStatus() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(
                    viewModel.handleRebootStatus(ModemRebootMonitorService.RebootState.ONGOING)
                )
            }
        }
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
                val cancelResponseString = readJson("cancelresponse.json")
                cancelResponse = fromJson(cancelResponseString)

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
                coEvery { appointmentRepository.cancelAppointment(any()) } returns Either.Right(
                    cancelResponse
                )
                viewModel.requestAppointmentCancellation()

            }
        }
    }

    @Test
    fun onNotificationClicked_navigateToNotificationDetailsScreen() = runBlockingTest {
        val notificationSource = NotificationSource()
        notificationSource.notificationlist = notificationList
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
}