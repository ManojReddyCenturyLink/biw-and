package com.centurylink.biwf.screens.login

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
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
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
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

    @MockK
    private lateinit var factory: LoginViewModel.Factory

    @MockK
    private lateinit var authServiceHost: AuthServiceHost

    private var navFromAccountScreen: Boolean = false

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        every { mockSharedPreferences.getValueByID("USER_ID") }.returns("")
        every { mockSharedPreferences.saveUserId("USER_ID") } just runs
        every { mockSharedPreferences.removeUserId() } just runs
        every { mockSharedPreferences.getBioMetrics() } returns true
    }

    @Ignore
    @Test
    fun `show biometric flow when token available and biometrics enabled`() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns true
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.accessToken } returns "mock-token"

        initViewModel()

        assertTrue(viewModel.showBioMetricsLogin.first())
    }

    @Ignore
    @Test
    fun `show home screen when logged in and biometrics disabled`() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns false
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.accessToken } returns "mock-token"

        initViewModel()

        assertThat(viewModel.myState.first(), `is`(LoginCoordinatorDestinations.HOME))
    }

    @Ignore
    @Test
    fun `show login flow when token not available and biometrics disabled`() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns false
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.accessToken } returns null

        initViewModel()

        verify(exactly = 1) { launch { mockAuthService.launchSignInFlow() } }
    }

    @Ignore
    @Test
    fun `show login flow when token not available and biometrics enabled`() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns true
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.accessToken } returns null

        initViewModel()

        verify(exactly = 1) { launch { mockAuthService.launchSignInFlow() } }
    }

    private fun initViewModel() {
        viewModel = LoginViewModel(
            sharedPreferences = mockSharedPreferences,
            authService = mockAuthService,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun testOnBiometricSuccess() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns false
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.accessToken } returns "mock-token"
        initViewModel()
        launch {
            Assert.assertNotNull(
            viewModel.onBiometricSuccess())
        }
    }

    @Test
    fun testOnBiometricFailure() = runBlockingTest {
        every { mockSharedPreferences.getBioMetrics() } returns false
        every { (mockAuthService.tokenStorage as AppAuthTokenStorage).state?.accessToken } returns "mock-token"
        initViewModel()
        launch {
            Assert.assertNotNull(
            viewModel.onBiometricFailure())
        }
    }
}
