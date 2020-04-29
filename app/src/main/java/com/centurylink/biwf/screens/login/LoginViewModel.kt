package com.centurylink.biwf.screens.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.ObservableData
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.preferences.Preferences
import com.centurylink.biwf.utility.viewModelFactory
import timber.log.Timber
import javax.inject.Inject

class LoginViewModel internal constructor(
    private val accountRepository: AccountRepository,
    private val sharedPreferences: Preferences,
    private val authService: AuthService<*>
) : BaseViewModel() {

    class Factory @Inject constructor(
        private val accountRepository: AccountRepository,
        private val sharedPreferences: Preferences,
        private val authServiceFactory: AuthServiceFactory<*>
    ) : ViewModelFactoryWithInput<AuthServiceHost> {

        override fun withInput(input: AuthServiceHost): ViewModelProvider.Factory {
            return viewModelFactory {
                LoginViewModel(
                    accountRepository,
                    sharedPreferences,
                    authServiceFactory.create(input)
                )
            }
        }
    }

    val myState = ObservableData(LoginCoordinatorDestinations.LOGIN)
    val errorEvents: EventLiveData<String> = MutableLiveData()

    private var userEmail: String? = null
    private var userPassword: String? = null

    fun onEmailTextChanged(email: String) {
        userEmail = email
    }

    fun onPasswordTextChanged(password: String) {
        userPassword = password
    }

    fun onLoginClicked() {
        authService.launchSignInFlow()
            .doOnError(Timber::e)
            .onErrorComplete()
            .subscribe()
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
}
