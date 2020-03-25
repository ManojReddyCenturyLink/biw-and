package com.centurylink.biwf.screens.login

import com.centurylink.biwf.base.BaseViewModel

class LoginViewModel : BaseViewModel() {

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
        //todo pass user's email and password to Account Repository
    }

    fun onForgotPasswordClicked() {
        //todo navigate to forgot password
    }

    fun onLearnMoreClicked() {
        //todo navigate to learn more page
    }
}
