package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<LoginCoordinatorDestinations> {

    override fun navigateTo(destination: LoginCoordinatorDestinations) {
        when (destination) {
            LoginCoordinatorDestinations.HOME -> navigateToHomeScreen()
        }
    }

    private fun navigateToHomeScreen() {
        navigator.navigateToHomeScreen()
    }
}

enum class LoginCoordinatorDestinations {
    HOME
}
