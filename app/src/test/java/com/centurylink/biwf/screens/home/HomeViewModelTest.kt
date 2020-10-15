package com.centurylink.biwf.screens.home

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.threeten.bp.LocalDateTime

@Suppress("EXPERIMENTAL_API_USAGE")
class HomeViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: HomeViewModel

    @MockK
    private lateinit var appointmentRepository: AppointmentRepository

    @MockK
    private lateinit var modemRebootMonitorService: ModemRebootMonitorService

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var assiaRepository: AssiaRepository

    @MockK
    private lateinit var mockOAuthAssiaRepository: OAuthAssiaRepository

    @MockK
    private lateinit var accountRepository: AccountRepository

    @MockK
    private lateinit var mockPreferences: Preferences

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager
    private lateinit var accountDetails: AccountDetails

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        every { mockPreferences.getHasSeenDialog() } returns true
        every { mockPreferences.getUserType() } returns true
        coEvery { userRepository.getUserInfo() } returns Either.Right(
            UserInfo()
        )
        coEvery { mockOAuthAssiaRepository.getModemInfo() } returns Either.Right(
            ModemInfo()
        )
        coEvery { userRepository.getUserDetails() } returns Either.Right(
            UserDetails()
        )
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
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
    }

    @Test
    fun onNotificationBellClicked_navigateToNotificationScreen() = runBlockingTest {
        launch {
            viewModel.onNotificationBellClicked()
        }
        Assert.assertEquals(
            "Support Screen wasn't Launched",
            HomeCoordinatorDestinations.NOTIFICATION_LIST,
            viewModel.myState.first()
        )
    }

    @Test
    fun onSubscriptionActivityClick_navigateToSubscriptionScreen() = runBlockingTest {
        launch {
            viewModel.onSubscriptionActivityClick("mockPaymentMethod")
        }
        Assert.assertEquals(
            "Support Screen wasn't Launched",
            HomeCoordinatorDestinations.SUBSCRIPTION_ACTIVITY,
            viewModel.myState.first()
        )
    }


    @Test
    fun `init Api failure case`() = runBlockingTest {
        coEvery { userRepository.getUserInfo() } returns Either.Left(error = "")
        coEvery { userRepository.getUserDetails() } returns Either.Left(error = "")
        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Left(error = "")
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
        launch {
            viewModel.initApis()
        }
    }

    @Test
    fun testAnalyticsButtonClicked() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(analyticsManagerInterface)
                viewModel.onBiometricNoResponse()
                viewModel.onSupportClicked(true)
                viewModel.onBiometricYesResponse()
            }
        }
    }

    @Test
    fun getAccountDetailsWithPendingActivation() {
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
    }

    @Test
    fun getAccountDetailsWithCompleted() {
        val accountString = readJson("account_activation_completed.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
    }

    @Test
    fun getInstallationStatusTrue() {
        every { mockPreferences.getInstallationStatus() } returns true
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
    }

    @Test
    fun getRequestAppointmentDetailsFailure() {
        every { mockPreferences.getInstallationStatus() } returns false
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)

        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Left(
            "No Appointment Records"
        )
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
    }

    @Test
    fun getRequestAppointmentDetailsFailure1() {
        every { mockPreferences.getInstallationStatus() } returns false
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)

        coEvery { appointmentRepository.getAppointmentInfo() } returns Either.Left(
            Constants.ERROR
        )
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
    }

    @Test
    fun getHasSeenDialog() {
        every { mockPreferences.getHasSeenDialog() } returns false
        val accountString = readJson("account_activation_completed.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
    }

    @Test
    fun getAccountDetailsFailure() {
        every { mockPreferences.getHasSeenDialog() } returns false
        val accountString = readJson("account_activation_completed.json")
        accountDetails = fromJson(accountString)
        coEvery { accountRepository.getAccountDetails() } returns Either.Left(Constants.ERROR)
        viewModel =
            HomeViewModel(
                mockk(),
                appointmentRepository,
                mockPreferences,
                mockk(),
                userRepository,
                assiaRepository,
                mockOAuthAssiaRepository,
                accountRepository,
                mockModemRebootMonitorService,
                analyticsManagerInterface
            )
    }

}
