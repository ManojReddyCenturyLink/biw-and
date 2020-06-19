package com.centurylink.biwf.screens.login

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import org.junit.Before

@Suppress("EXPERIMENTAL_API_USAGE")
class LoginViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: LoginViewModel

    @MockK
    private lateinit var mockAccountRepository: AccountRepository

    @MockK
    private lateinit var mockSharedPreferences: Preferences

    @MockK
    private lateinit var mockAuthService: AuthService<*>

    @MockK
    private lateinit var authServiceFactory: AuthServiceFactory<*>

    private lateinit var factory: LoginViewModel.Factory

    @MockK
    private lateinit var authServiceHost: AuthServiceHost

    private var navFromAccountScreen: Boolean = false

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockSharedPreferences.getValueByID("USER_ID") }.returns("")
        every { mockSharedPreferences.saveUserId("USER_ID") } just runs
        every { mockSharedPreferences.removeUserId() } just runs
        every { mockSharedPreferences.getBioMetrics() } returns true
        every { mockSharedPreferences.isLoggedInUser() } returns true
        viewModel = LoginViewModel(
            accountRepository = mockAccountRepository,
            sharedPreferences = mockSharedPreferences,
            authService = mockAuthService,
            navFromAccountScreen = navFromAccountScreen
        )
    }
}
