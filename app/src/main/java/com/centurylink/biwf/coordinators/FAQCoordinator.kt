package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 ** FAQCoordinator  This  class is used for the purpose of Navigation
 * flow from the Faq Screen.
 *
 * @constructor Create empty Faq coordinator.
 */
@Singleton
class FAQCoordinator @Inject constructor() : Coordinator<FAQCoordinatorDestinations> {
    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to FAQ coordinator Destinations.
     *
     * @param destination
     */
    override fun navigateTo(destination: FAQCoordinatorDestinations) {
        when (destination) {
            FAQCoordinatorDestinations.SCHEDULE_CALLBACK -> navigateToScheduleCallback()
        }
    }

    /**
     * function guides us to navigate to Schedule CallBack Screen.
     *
     */
    private fun navigateToScheduleCallback() {
        navigator.navigateToScheduleCallbackFromFAQ()
    }
}

/**
 * Faq coordinator destinations used for Navigation to Other screens from FAQ Screen.
 *
 * @constructor Create  Faacoordinator destinations
 */
enum class FAQCoordinatorDestinations {
    SCHEDULE_CALLBACK;

    companion object {
        lateinit var bundle: Bundle
    }
}
