package com.centurylink.biwf.screens.login

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.preferences.Preferences
import com.centurylink.biwf.utility.viewModelFactory
import kotlinx.coroutines.flow.Flow
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

        fun withInput(
            input: AuthServiceHost,
            navFromAccountScreen: Boolean
        ): ViewModelProvider.Factory {
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
    val showBioMetricsLogin: Flow<BiometricPromptMessage> = BehaviorStateFlow()

    private val biometricPromptMessage = BiometricPromptMessage(
        title = R.string.biometric_prompt_title,
        subTitle = R.string.biometric_prompt_message,
        negativeText = R.string.cancel
    )

    init {
        val showBiometrics = sharedPreferences.getBioMetrics() ?: false
        val isLoggedInUser = sharedPreferences.isLoggedInUser() ?: false

        if (navFromAccountScreen) {
            onLoginClicked()
        } else if (showBiometrics && isLoggedInUser) {
            showBioMetricsLogin.latestValue = biometricPromptMessage
        } else if (isLoggedInUser) {
            onLoginSuccess()
        } else {
            onLoginClicked()
        }
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

    fun onLoginSuccess() {
        myState.latestValue = LoginCoordinatorDestinations.HOME
        sharedPreferences.saveUserLoggedInStatus(true)
    }
}

data class BiometricPromptMessage(val title: Int, val subTitle: Int, val negativeText: Int)