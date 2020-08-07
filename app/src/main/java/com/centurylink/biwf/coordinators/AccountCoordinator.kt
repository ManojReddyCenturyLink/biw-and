package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<AccountCoordinatorDestinations> {

    override fun navigateTo(destination: AccountCoordinatorDestinations) {
        when (destination) {
            AccountCoordinatorDestinations.PROFILE_INFO -> navigateToPersonalInfoActivity()
            AccountCoordinatorDestinations.LOG_IN -> navigateToLogInActivity()
        }
    }

    private fun navigateToLogInActivity() {
        navigator.navigateToLoginScreen()
    }

    private fun navigateToPersonalInfoActivity() {
        navigator.navigateToPersonalInfoActivity()
    }
}

enum class AccountCoordinatorDestinations {
    PROFILE_INFO, LOG_IN;

    companion object {
        lateinit var bundle: Bundle
    }
}
