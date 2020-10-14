package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ScheduleCallbackCoordinator -This ScheduleCallbackCoordinator class is used for the purpose of Navigation
 * flow from the ScheduleCallBack Screen.
 *
 * @property navigator Navigator instance where app Navigation is implemented for each screens.
 * @constructor Create  Schedule callback coordinator
 */
@Singleton
class ScheduleCallbackCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<ScheduleCallbackCoordinatorDestinations> {

    /**
     * Navigate to ScheduleCallbackCoordinatorDestinations from  Schedulecallback Screen
     *
     * @param destination the destination enum constants for ScheduleCallBack Screen.
     */
    override fun navigateTo(destination: ScheduleCallbackCoordinatorDestinations) {
        when (destination) {
            ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT -> navigateToPhoneDialler()
            ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO -> navigateToAdditionalInfo()
        }
    }

    /**
     * Function guides us to navigate to Additional info Screen.
     *
     */
    private fun navigateToAdditionalInfo() {
        navigator.navigateToAdditionalInfo()
    }

    /**
     * Function guides us to navigate to PhoneDialler.
     *
     */
    private fun navigateToPhoneDialler() {
        navigator.navigateToPhoneDialler()
    }
}

/**
 * ScheduleCallbackCoordinatorDestinations  used for Navigation to Other screens from ScheduleCallback Screen.
 *
 * @constructor Create  ScheduleCallbackCoordinatorDestinations
 */
enum class ScheduleCallbackCoordinatorDestinations {
    CALL_SUPPORT, ADDITIONAL_INFO;

    companion object {
        lateinit var bundle: Bundle
    }
}
