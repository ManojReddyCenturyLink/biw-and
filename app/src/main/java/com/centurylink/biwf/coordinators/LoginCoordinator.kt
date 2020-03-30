package com.centurylink.biwf.coordinators

import com.centurylink.biwf.utility.MyObservable
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: MyObservable<LoginCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: LoginCoordinatorDestinations) {
        when (destinations) {
            LoginCoordinatorDestinations.FORGOT_PASSWORD -> navigateToForgotPassword()
            LoginCoordinatorDestinations.LEARN_MORE -> navigateToLearnMore()
            LoginCoordinatorDestinations.HOME -> navigateToHomeScreen()
            LoginCoordinatorDestinations.LOGIN -> navigateToLoginScreen()
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

    private fun navigateToHomeScreen() {
        navigator.navigateToHomeScreen()
    }
}

enum class LoginCoordinatorDestinations {
    FORGOT_PASSWORD, LEARN_MORE, HOME, LOGIN
}