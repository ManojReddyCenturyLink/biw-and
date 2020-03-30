package com.centurylink.biwf.screens.login

import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.utility.ObservableData

class LoginViewModel(
    private val accountRepository: AccountRepository
) : BaseViewModel() {

    val myState = ObservableData(LoginCoordinatorDestinations.LOGIN)

    private var userEmail = ""
    private var userPassword = ""
    private var rememberMe = false

    fun onEmailTextChanged(email: String) {
        userEmail = email
    }

    fun onPasswordTextChanged(password: String) {
        userPassword = password
    }

    fun onRememberMeCheckChanged(isChecked: Boolean) {
        rememberMe = isChecked
    }

    fun onLoginClicked() {
        if (checkForValidFields()) {
            accountRepository.login(email = userEmail, password = userPassword, rememberMeFlag = rememberMe)
            myState.value = LoginCoordinatorDestinations.HOME
        }
    }

    fun onForgotPasswordClicked() {
        myState.value = LoginCoordinatorDestinations.FORGOT_PASSWORD
    }

    fun onLearnMoreClicked() {
        myState.value = LoginCoordinatorDestinations.LEARN_MORE
    }

    private fun checkForValidFields(): Boolean {
        return true
    }
}
