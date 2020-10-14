package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Login coordinator - This LoginCoordinator class is used for the purpose of Navigation
 * flow from the Login Screen.
 *
 * @property navigator Navigator instance where app Navigation is implemented for each screens.
 * @constructor Create empty Login coordinator
 */
@Singleton
class LoginCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<LoginCoordinatorDestinations> {

    /**
     * Navigate to
     *
     * @param destination
     */
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
