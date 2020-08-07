package com.centurylink.biwf.screens.home.account

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.AccountCoordinatorDestinations
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.utility.DateUtils
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

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockSharedPreferences.getBioMetrics() } returns true
        coEvery { mockUserRepository.getUserInfo() } returns Either.Right(UserInfo())
        coEvery { mockUserRepository.getUserDetails() } returns Either.Right(UserDetails())
        coEvery { mockAccountRepository.getAccountDetails() } returns Either.Right(
            AccountDetails(
                marketingOptInC = "",
                emailOptInC = false,
                cellPhoneOptInC = false,
                isBillingAddressUpdated = false,
                lastViewedDate = DateUtils.formatInvoiceDate("2020-05-14T14:09:58.000+0000")
            )
        )
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
    }

    @Test
    fun onBiometricSwitchChange_fromTrueToFalse() {
        //Need to Revisit this
        /*assertSame(true, viewModel.accountDetailsInfo)
        viewModel.onBiometricChange(false)
        assertSame(false, viewModel.biometricStatus.value)*/
    }

    @Test
    fun onServiceCallsSwitchChange_fromTrueToFalse() {
        //Need To Revisit this
       /* assertSame(true, viewModel.serviceCallsAndTextStatus.value)
        viewModel.onServiceCallsAndTextsChange(false)
        assertSame(false, viewModel.serviceCallsAndTextStatus.value)*/
    }

    @Test
    fun onMarketingCallsSwitchChange_fromTrueToFalse() {
        //Need To Revisit this
        /*assertSame(true, viewModel.marketingCallsAndTextStatus.value)
        viewModel.onMarketingCallsAndTextsChange(false)
        assertSame(false, viewModel.marketingCallsAndTextStatus.value)*/
    }

    @Test
    fun onMarketingEmailsSwitchChange_fromTrueToFalse() {
        //Need To Revisit this
       /* assertSame(true, viewModel.marketingEmailStatus.value)
        viewModel.onMarketingEmailsChange(false)
        assertSame(false, viewModel.marketingEmailStatus.value)*/
    }

    @Ignore
    @Test
    fun onPersonalInfoCardClick_navigateToPersonalInfoScreen() = runBlockingTest {
        launch {
            viewModel.onPersonalInfoCardClick()
        }

        Assert.assertEquals(
            "Personal Info Screen wasn't Launched",
            AccountCoordinatorDestinations.PROFILE_INFO,
            viewModel.myState.first()
        )
    }

}