package com.centurylink.biwf.screens.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.preferences.Preferences
import com.centurylink.biwf.utility.viewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel internal constructor(
    private val accountRepository: AccountRepository,
    private val sharedPreferences: Preferences,
    private val authService: AuthService<*>,
    private val navFromAccountScreen: Boolean
) : BaseViewModel() {

    class Factory @Inject constructor(
        private val accountRepository: AccountRepository,
        private val sharedPreferences: Preferences,
        private val authServiceFactory: AuthServiceFactory<*>
    ) : ViewModelFactoryWithInput<AuthServiceHost> {

        fun withInput(input: AuthServiceHost, navFromAccountScreen: Boolean): ViewModelProvider.Factory {
            return viewModelFactory {
                LoginViewModel(
                    accountRepository,
                    sharedPreferences,
                    authServiceFactory.create(input),
                    navFromAccountScreen = navFromAccountScreen
                )
            }
        }

        override fun withInput(input: AuthServiceHost): ViewModelProvider.Factory {
            return viewModelFactory {
                LoginViewModel(
                    accountRepository,
                    sharedPreferences,
                    authServiceFactory.create(input),
                    navFromAccountScreen = false
                )
            }
        }
    }

    val myState = EventFlow<LoginCoordinatorDestinations>()
    val errorEvents: EventLiveData<String> = MutableLiveData()

    private var userEmail: String? = null
    private var userPassword: String? = null

    init {
        if (navFromAccountScreen) onLoginClicked()
    }

    fun onEmailTextChanged(email: String) {
        userEmail = email
    }

    fun onPasswordTextChanged(password: String) {
        userPassword = password
    }

    fun onLoginClicked() {
        viewModelScope.launch {
            try {
                authService.launchSignInFlow()
            } catch (error: Throwable) {
                Timber.e(error)
            }
        }
    }

    fun onExistingUserLogin() {
        myState.latestValue = LoginCoordinatorDestinations.HOME_EXISTING_USER
    }

    fun onForgotPasswordClicked() {
        myState.latestValue = LoginCoordinatorDestinations.FORGOT_PASSWORD
    }

    fun onLearnMoreClicked() {
        myState.latestValue = LoginCoordinatorDestinations.LEARN_MORE
    }
}