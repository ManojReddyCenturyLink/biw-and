package com.centurylink.biwf.screens

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepositoryImpl
import com.centurylink.biwf.screens.login.LoginViewModel
import com.centurylink.biwf.testutils.event
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertSame
import org.amshove.kluent.shouldEqual
import org.junit.Before
import org.junit.Test

class LoginViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: LoginViewModel

    @MockK
    private lateinit var mockAccountRepository: AccountRepositoryImpl

    @Before
    fun setup() {
        every { mockAccountRepository.login(any(), any(), any()) } returns true
        viewModel = LoginViewModel(accountRepository = mockAccountRepository)
    }

    @Test
    fun onLoginClicked_withRequiredFields_navigateToHomeScreen() {
        viewModel.onEmailTextChanged("dean@gmail.com")
        viewModel.onPasswordTextChanged("passcode")
        viewModel.onLoginClicked()
        assertSame("Not the same", LoginCoordinatorDestinations.HOME, viewModel.myState.value)
    }

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