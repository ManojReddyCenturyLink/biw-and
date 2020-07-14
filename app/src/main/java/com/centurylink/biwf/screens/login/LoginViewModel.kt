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
import com.centurylink.biwf.service.impl.auth.AppAuthTokenStorage
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
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
    // TODO We should remove this, as outstanding work is cancelled on logout and we won't
    //  support showing the modem reboot dialogs on the Login screen
    modemRebootMonitorService: ModemRebootMonitorService
) : BaseViewModel(modemRebootMonitorService) {

    class Factory @Inject constructor(
        private val accountRepository: AccountRepository,
        private val sharedPreferences: Preferences,
        private val authServiceFactory: AuthServiceFactory<*>,
        private val modemRebootMonitorService: ModemRebootMonitorService
    ) : ViewModelFactoryWithInput<AuthServiceHost> {

        override fun withInput(input: AuthServiceHost): ViewModelProvider.Factory {
            return viewModelFactory {
                LoginViewModel(
                    accountRepository,
                    sharedPreferences,
                    authServiceFactory.create(input),
                    modemRebootMonitorService
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
        val hasToken = !(authService.tokenStorage as AppAuthTokenStorage).state?.refreshToken.isNullOrEmpty()

        if (hasToken && showBiometrics) {
            showBioMetricsLogin.latestValue = biometricPromptMessage
        } else if (hasToken) {
            onLoginSuccess()
        } else {
            showLoginFlow()
        }
    }

    private fun showLoginFlow() {
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
    }

    fun onBiometricSuccess() {
        myState.latestValue = LoginCoordinatorDestinations.HOME
    }

    fun onBiometricFailure() {
        showLoginFlow()
    }
}

data class BiometricPromptMessage(val title: Int, val subTitle: Int, val negativeText: Int)