package com.centurylink.biwf.coordinators

import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<LoginCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: LoginCoordinatorDestinations) {
        when (destinations) {
            LoginCoordinatorDestinations.FORGOT_PASSWORD -> navigateToForgotPassword()
            LoginCoordinatorDestinations.LEARN_MORE -> navigateToLearnMore()
            LoginCoordinatorDestinations.HOME -> navigateToHomeScreen(false)
            LoginCoordinatorDestinations.LOGIN -> navigateToLoginScreen()
            LoginCoordinatorDestinations.EXISTING_USER -> navigateToHomeScreen(true)
        }
    }

    private fun navigateToLoginScreen() {
        // Do Nothing
    }

    private fun navigateToForgotPassword() {
        navigator.navigateToForgotPassword()
    }

    private fun navigateToLearnMore() {
        navigator.navigateToLearnMore()
    }

    private fun navigateToHomeScreen(existingUser: Boolean) {
        navigator.navigateToHomeScreen(existingUser)
    }
}

enum class LoginCoordinatorDestinations {
    FORGOT_PASSWORD, LEARN_MORE, HOME, LOGIN, EXISTING_USER
}