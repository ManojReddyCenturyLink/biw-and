package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<LoginCoordinatorDestinations> {

    override fun navigateTo(destination: LoginCoordinatorDestinations) {
        when (destination) {
            LoginCoordinatorDestinations.FORGOT_PASSWORD -> navigateToForgotPassword()
            LoginCoordinatorDestinations.LEARN_MORE -> navigateToLearnMore()
            LoginCoordinatorDestinations.HOME -> navigateToHomeScreen()
        }
    }

    private fun navigateToForgotPassword() {
        navigator.navigateToForgotPassword()
    }

    private fun navigateToLearnMore() {
        navigator.navigateToLearnMore()
    }

    private fun navigateToHomeScreen() {
        navigator.navigateToHomeScreen()
    }
}

enum class LoginCoordinatorDestinations {
    FORGOT_PASSWORD, LEARN_MORE, HOME
}
