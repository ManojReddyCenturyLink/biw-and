package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject

/**
 * StatementCoordinator -This SubscriptionCoordinator class is used for the purpose of Navigation
 * flow from the Statement Screen.
 *
 * @constructor Create  Statement coordinator
 */
class StatementCoordinator @Inject constructor(): Coordinator<StatementCoordinatorDestinations> {
    override fun navigateTo(destination: StatementCoordinatorDestinations) { }
}

/**
 * Statement coordinator destinations used for Navigation to Other screens from Statement Screen.
 *
 * @constructor Create Statement coordinator destinations.
 */
enum class StatementCoordinatorDestinations {
    FAQ;
    companion object {
        lateinit var bundle: Bundle
    }
}
