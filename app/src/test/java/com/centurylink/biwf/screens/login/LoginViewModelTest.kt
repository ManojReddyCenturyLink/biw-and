package com.centurylink.biwf.screens.login

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.testutils.event
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import junit.framework.Assert.assertSame
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class LoginViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: LoginViewModel

    @MockK
    private lateinit var mockAccountRepository: AccountRepository
    @MockK
    private lateinit var mockSharedPreferences: Preferences

    @MockK
    private lateinit var mockAuthService: AuthService<*>

    @Before
    fun setup() {
        every { mockAccountRepository.login(any(), any(), any()) } returns true
        every { mockSharedPreferences.getValueByID("USER_ID") }.returns("")
        every { mockSharedPreferences.saveUserId("USER_ID") } just runs
        every { mockSharedPreferences.removeUserId() } just runs
        viewModel = LoginViewModel(accountRepository = mockAccountRepository, sharedPreferences = mockSharedPreferences, authService = mockAuthService)
    }

    @Ignore
    @Test
    fun onLoginClicked_withRequiredFields_navigateToHomeScreen() {
        viewModel.onEmailTextChanged("dean@gmail.com")
        viewModel.onPasswordTextChanged("passcode")
        viewModel.onLoginClicked()
        assertSame("Not the same", LoginCoordinatorDestinations.HOME_NEW_USER, viewModel.myState.value)
    }

    @Ignore
    @Test
    fun onLoginClicked_withoutRequiredFields_displayErrorToast() {
        viewModel.onLoginClicked()
        viewModel.errorEvents.event() shouldEqual "Please give Email and / or Password"
    }

    @Test
    fun onLearnMoreClicked_navigateToLearnMoreScreen() {
        viewModel.onLearnMoreClicked()
        assertSame("Not the same", LoginCoordinatorDestinations.LEARN_MORE, viewModel.myState.value)
    }

    @Test
    fun onForgotPasswordClicked_navigateToForgotPasswordScreen() {
        viewModel.onForgotPasswordClicked()
        assertSame("Not the same", LoginCoordinatorDestinations.FORGOT_PASSWORD, viewModel.myState.value)
    }
}
