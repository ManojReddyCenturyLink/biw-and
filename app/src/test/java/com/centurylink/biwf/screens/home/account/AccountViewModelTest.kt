package com.centurylink.biwf.screens.home.account

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.account.PaymentInfoResponse
import com.centurylink.biwf.model.contact.ContactDetails
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AccountViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: AccountViewModel

    @MockK
    private lateinit var mockUserRepository: UserRepository

    @MockK
    private lateinit var mockAccountRepository: AccountRepository

    @MockK
    private lateinit var mockContactRepository: ContactRepository

    @MockK
    private lateinit var mockSharedPreferences: Preferences

    @MockK
    private lateinit var mockAuthService: AuthService<*>

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager
    private lateinit var paymentInfoResponse: PaymentInfoResponse
    private lateinit var uiAccountDetails: AccountViewModel.UiAccountDetails
    private var accountDetailsInfo: Flow<AccountViewModel.UiAccountDetails> = BehaviorStateFlow()
    private lateinit var accountDetails: AccountDetails

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val paymentString = readJson("paymentinfo.json")
        paymentInfoResponse = fromJson(paymentString)
        every { mockSharedPreferences.getBioMetrics() } returns true
        coEvery { mockAccountRepository.setServiceCallsAndTexts(true) } returns Constants.ACCOUNT_NAME
        coEvery { mockAccountRepository.getLiveCardDetails() } returns Either.Right(
            paymentInfoResponse
        )
        coEvery { mockContactRepository.getContactDetails() } returns Either.Right(
            ContactDetails("", "", "", "", "", "", false, true, true)
        )
        coEvery { mockUserRepository.getUserInfo() } returns Either.Right(UserInfo())
        coEvery { mockUserRepository.getUserDetails() } returns Either.Right(UserDetails())
        val accountString = readJson("account.json")
        accountDetails = fromJson(accountString)
        coEvery { mockAccountRepository.getAccountDetails() } returns Either.Right(accountDetails)
        run { analyticsManagerInterface }
        viewModel = AccountViewModel(
            accountRepository = mockAccountRepository,
            contactRepository = mockContactRepository,
            sharedPreferences = mockSharedPreferences,
            userRepository = mockUserRepository,
            authService = mockAuthService,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
        uiAccountDetails = AccountViewModel.UiAccountDetails(
            name = null,
            formattedServiceAddressLine1 = "",
            formattedServiceAddressLine2 = "",
            planName = null,
            planSpeed = null,
            paymentDate = null,
            paymentMethod = null,
            email = null,
            password = null,
            cellPhone = null,
            homePhone = null,
            workPhone = null,
            biometricStatus = false,
            serviceCallsAndText = false,
            marketingEmails = true,
            marketingCallsAndText = false
        )
    }

    @Test
    fun onBiometricSwitchChange_fromTrueToFalse() {
        Assert.assertNotNull(viewModel.onBiometricChange(true))
    }

    @Test
    fun onServiceCallsSwitchChange_fromTrueToFalse() {
        Assert.assertNotNull(viewModel.onServiceCallsAndTextsChange(true))
    }

    @Test
    fun onMarketingCallsSwitchChange_fromTrueToFalse() {
        Assert.assertNotNull(viewModel.onMarketingCallsAndTextsChange(true, "1234567890"))
    }

    @Test
    fun onMarketingEmailsSwitchChange_fromTrueToFalse() {
        Assert.assertNotNull(viewModel.onMarketingEmailsChange(true))
    }

    @Test
    fun refreshBiometrics_fromTrueToFalse() {
        Assert.assertNotNull(viewModel.refreshBiometrics())
    }

    @Test
    fun onPersonalInfoCardClick_navigateToPersonalInfoScreen() {
        Assert.assertNotNull(viewModel.onPersonalInfoCardClick())
    }

    @Test
    fun onSubscriptionInfoCardClick_navigateToSubscriptionScreen() = runBlockingTest {
        launch {
            Assert.assertNotNull(viewModel.onSubscriptionCardClick())
        }
    }

    @Test
    fun getLiveCardDetailsApiFailureTest() = runBlockingTest {
        launch {
            coEvery { mockAccountRepository.getLiveCardDetails() } returns Either.Left(
                ""
            )
            viewModel.initApiCalls()
        }
    }

    @Test
    fun initAccountAndContactApiCallsTest() = runBlockingTest {
        launch {
            viewModel.initAccountAndContactApiCalls()
        }
    }

    @Test
    fun initAccountAndContactApiCallsErrorCaseTest() = runBlockingTest {
        launch {
            coEvery { mockContactRepository.getContactDetails() } returns Either.Left(
                ""
            )
            coEvery { mockUserRepository.getUserInfo() } returns Either.Left("")
            coEvery { mockUserRepository.getUserDetails() } returns Either.Left("")
            coEvery { mockAccountRepository.getAccountDetails() } returns Either.Left("")
            viewModel.initAccountAndContactApiCalls()
        }
    }

    @Test
    fun onLogOutClickTest() = runBlockingTest {
        launch {
            coEvery { mockAuthService.revokeToken() } returns true
//            viewModel.onLogOutClick(this)
//            Assert.assertNotNull(viewModel.onLogOutClick(this))
        }
    }

    @Test
    fun `get values from UiAccountDetails`() {
        Assert.assertEquals(uiAccountDetails, AccountViewModel.UiAccountDetails())
    }
}