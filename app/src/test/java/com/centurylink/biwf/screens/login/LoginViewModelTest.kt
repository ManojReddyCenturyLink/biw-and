package com.centurylink.biwf.screens.login

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.service.impl.auth.AppAuthTokenStorage
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.isA
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test

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
    }

    @Test
    fun `show biometric flow when token available and biometrics enabled`() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns true
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.refreshToken } returns "mock-token"

        initViewModel()

        assertThat(viewModel.showBioMetricsLogin.first(), isA(BiometricPromptMessage::class.java))
    }

    @Test
    fun `show home screen when logged in and biometrics disabled`() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns false
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.refreshToken } returns "mock-token"

        initViewModel()

        assertThat(viewModel.myState.first(), `is`(LoginCoordinatorDestinations.HOME))
    }

    @Test
    fun `show login flow when token not available and biometrics disabled`() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns false
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.refreshToken } returns null

        initViewModel()

        verify(exactly = 1) { launch { mockAuthService.launchSignInFlow() } }
    }

    @Test
    fun `show login flow when token not available and biometrics enabled`() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns true
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.refreshToken } returns null

        initViewModel()

        verify(exactly = 1) { launch { mockAuthService.launchSignInFlow() } }
    }

    private fun initViewModel() {
        viewModel = LoginViewModel(
            accountRepository = mockAccountRepository,
            sharedPreferences = mockSharedPreferences,
            authService = mockAuthService,
            modemRebootMonitorService = mockModemRebootMonitorService
        )
    }
}
