package com.centurylink.biwf.screens.login

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.ObservableData
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject

class LoginViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val sharedPreferences: Preferences
) : BaseViewModel() {

    companion object{
        val USER_ID = "USER_ID"
    }
    private var userEmail: String? = null
    private var userPassword: String? = null
    private var rememberMe = false

    val myState = ObservableData(LoginCoordinatorDestinations.LOGIN)

    val errorEvents: EventLiveData<String> = MutableLiveData()

    fun onEmailTextChanged(email: String) {
        userEmail = email
    }

    fun onPasswordTextChanged(password: String) {
        userPassword = password
    }

    fun onRememberMeCheckChanged(isChecked: Boolean, userId: String) {
        rememberMe = isChecked
        if(rememberMe && userId.isNotEmpty()){
            sharedPreferences.saveUserId(userId)
        }else{
            sharedPreferences.removeUserId(USER_ID)
        }
    }

    fun onLoginClicked() {
        if (checkForValidFields()) {
            accountRepository.login(email = userEmail!!, password = userPassword!!, rememberMeFlag = rememberMe)
            myState.value = LoginCoordinatorDestinations.HOME_NEW_USER
        } else {
            errorEvents.emit("Please give Email and / or Password")
        }
    }

    fun onExistingUserLogin() {
            myState.value = LoginCoordinatorDestinations.HOME_EXISTING_USER
    }

    fun onForgotPasswordClicked() {
        myState.value = LoginCoordinatorDestinations.FORGOT_PASSWORD
    }

    fun onLearnMoreClicked() {
        myState.value = LoginCoordinatorDestinations.LEARN_MORE
    }

    private fun checkForValidFields(): Boolean {
        return !(userEmail.isNullOrBlank() || userPassword.isNullOrBlank())
    }
}
