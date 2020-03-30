package com.centurylink.biwf.screens

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepositoryImpl
import com.centurylink.biwf.screens.login.LoginViewModel
import com.nhaarman.mockitokotlin2.any
import io.mockk.every
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertSame
import org.junit.Before
import org.junit.Test

class LoginViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: LoginViewModel

    @MockK
    private lateinit var mockAccountRepository: AccountRepositoryImpl

    @Before
    fun setup() {
        every { mockAccountRepository.login("", "", false) } returns any()
        viewModel = LoginViewModel(accountRepository = mockAccountRepository)
    }

    @Test
    fun onLoginClicked_sendCorrectDataToAccountRepo() {
        viewModel.onLoginClicked()
        assertSame("Not the same", LoginCoordinatorDestinations.HOME, viewModel.myState.value)
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