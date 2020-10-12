package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AccountCoordinator- This Account coordinator class is used for the purpose of Navigation
 * flow from the Account Fragment.
 * @property navigator - Navigator instance where app Navigation is implemented for each screens.
 * @constructor Create  Account coordinator
 */
@Singleton
class AccountCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<AccountCoordinatorDestinations> {

    /**
     * Navigate to AccountCoordinatorDestinations from AccountFragment screen.
     *
     * @param destination The destination enum constants for Screens.
     */
    override fun navigateTo(destination: AccountCoordinatorDestinations) {
        when (destination) {
            AccountCoordinatorDestinations.PROFILE_INFO -> navigateToPersonalInfoActivity()
            AccountCoordinatorDestinations.LOG_IN -> navigateToLogInActivity()
        }
    }

    /**
     * Method helps us to navigate to login Activity from Account Fragment
     */
    private fun navigateToLogInActivity() {
        navigator.navigateToLoginScreen()
    }
    /**
     * Method helps us to navigate to Personalinfo Activity from Account Fragment
     */
    private fun navigateToPersonalInfoActivity() {
        navigator.navigateToPersonalInfoActivity()
    }
}

/**
 * Account coordinator destinations for each screen navigation from Accounts
 *
 * @constructor Create  Account coordinator destinations
 */
enum class AccountCoordinatorDestinations {
    PROFILE_INFO, LOG_IN;

    companion object {
        lateinit var bundle: Bundle
    }
}
