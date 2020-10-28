package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 *SupportCoordinator - This Support coordinator class is used for the purpose of Navigation
 * flow from the Support Screen.
 *
 * @constructor Create empty Support coordinator
 */
@Singleton
class SupportCoordinator @Inject constructor() : Coordinator<SupportCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to SupportCoordinatorDestinations from support Screen.
     *
     * @param destination the destination enum constants for Support  Screen.
     */
    override fun navigateTo(destination: SupportCoordinatorDestinations) {
        when (destination) {
            SupportCoordinatorDestinations.FAQ -> navigateToFaq()
            SupportCoordinatorDestinations.NAVIGATE_TO_WEBSITE -> { }
            SupportCoordinatorDestinations.SCHEDULE_CALLBACK -> navigateToScheduleCallback()
            SupportCoordinatorDestinations.LIVE_CHAT -> navigateToLiveChat()
        }
    }

    /**
     * function guides us to navigate to FAQ Screen.
     *
     */
    private fun navigateToFaq() {
        navigator.navigateToFaq()
    }

    /**
     * function guides us to navigate to Schedule CallBack Screen.
     *
     */
    private fun navigateToScheduleCallback() {
        navigator.navigateToScheduleCallbackFromSupport()
    }

    private fun navigateToLiveChat() {}
}

/**
 * Support coordinator destinations used for Navigation to Other screens from Support Screen.
 *
 * @constructor Create empty Support coordinator destinations.
 */
enum class SupportCoordinatorDestinations {
    FAQ, LIVE_CHAT, SCHEDULE_CALLBACK, NAVIGATE_TO_WEBSITE;

    companion object {
        lateinit var bundle: Bundle
    }
}
