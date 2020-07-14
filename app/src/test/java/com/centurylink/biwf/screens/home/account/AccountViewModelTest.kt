package com.centurylink.biwf.screens.home.account

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.user.UserDetails
import com.centurylink.biwf.model.user.UserInfo
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.ContactRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before

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

    @Before
    fun setup() {
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
        viewModel = AccountViewModel(
            accountRepository = mockAccountRepository,
            contactRepository = mockContactRepository,
            sharedPreferences = mockSharedPreferences,
            userRepository = mockUserRepository,
            authService = mockAuthService,
            modemRebootMonitorService = mockModemRebootMonitorService
        )
        //Need to Revisit Testcases
    }
}