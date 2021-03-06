package com.centurylink.biwf.screens.login

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.service.auth.AuthService
import com.centurylink.biwf.service.auth.AuthServiceFactory
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.service.impl.auth.AppAuthTokenStorage
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.preferences.Preferences
import com.centurylink.biwf.utility.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Login view model
 *
 * @property sharedPreferences - preferences instance to handle shared preferences
 * @property authService - service instance to handle auth api calls
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class LoginViewModel internal constructor(
    val sharedPreferences: Preferences,
    private val authService: AuthService<*>,
    // TODO We should remove this, as outstanding work is cancelled on logout and we won't
    //  support showing the modem reboot dialogs on the Login screen
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    class Factory @Inject constructor(
        private val sharedPreferences: Preferences,
        private val authServiceFactory: AuthServiceFactory<*>,
        private val modemRebootMonitorService: ModemRebootMonitorService,
        private val analyticsManagerInterface: AnalyticsManager
    ) : ViewModelFactoryWithInput<AuthServiceHost> {

        override fun withInput(input: AuthServiceHost): ViewModelProvider.Factory {
            return viewModelFactory {
                LoginViewModel(
                    sharedPreferences,
                    authServiceFactory.create(input),
                    modemRebootMonitorService,
                    analyticsManagerInterface
                )
            }
        }
    }

    val myState = EventFlow<LoginCoordinatorDestinations>()
    val showBioMetricsLogin: Flow<Boolean> = BehaviorStateFlow()

    val biometricPromptMessage = BiometricPromptMessage(
        title = R.string.biometric_prompt_title,
        subTitle = R.string.biometric_prompt_message,
        negativeText = R.string.cancel
    )

    /**
     * Show login screen - It will handle authentication of login screen
     *
     */
    private fun showLoginScreen() {
        viewModelScope.launch {
            try {
                authService.launchSignInFlow()
            } catch (error: Throwable) {
                Timber.e(error)
            }
        }
    }

    /**
     * Handle sign in flow
     *
     */
    fun handleSignInFlow() {
        val showBiometrics = sharedPreferences.getBioMetrics() ?: false
        val hasToken = !(authService.tokenStorage as AppAuthTokenStorage).state?.accessToken.isNullOrEmpty()

        showBioMetricsLogin.latestValue = false
        if (hasToken && showBiometrics) {
            showBioMetricsLogin.latestValue = true
        } else if (hasToken) {
            onLoginSuccess()
        } else {
            showLoginScreen()
        }
    }

    /**
     * On login success - It will navigate to home screen
     *
     */
    fun onLoginSuccess() {
        myState.latestValue = LoginCoordinatorDestinations.HOME
    }

    /**
     * On biometric success - It will navigate to home screen
     *
     */
    fun onBiometricSuccess() {
        myState.latestValue = LoginCoordinatorDestinations.HOME
    }

    /**
     * On biometric failure - reloads the same(login) screen
     *
     */
    fun onBiometricFailure() {
        showLoginScreen()
    }
}

data class BiometricPromptMessage(val title: Int, val subTitle: Int, val negativeText: Int)
