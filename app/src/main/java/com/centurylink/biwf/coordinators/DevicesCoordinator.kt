package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DevicesCoordinator  This  class is used for the purpose of Navigation
 * flow from the DevicesFragment.
 *
 * @property navigator Navigator instance where app Navigation is implemented for each screens.
 * @constructor Create empty Devices coordinator
 */
@Singleton
class DevicesCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<DevicesCoordinatorDestinations> {
    /**
     * Navigate to DevicesCoordinatorDestinations from Devices Fragment info Screen
     *
     * @param destination DevicesCoordinatorDestinations
     */
    override fun navigateTo(destination: DevicesCoordinatorDestinations) {
        when (destination) {
            DevicesCoordinatorDestinations.DEVICE_DETAILS -> navigateToUsageDetails()
        }
    }

    /**
     * function guides us to navigate to usage Details Screen.
     *
     */
    private fun navigateToUsageDetails(){
        navigator.navigateToUsageDetailsActivity()
    }
}

/**
 * Devices coordinator destinations
 *
 * @constructor Create  Devices coordinator destinations
 */
enum class DevicesCoordinatorDestinations {
    DEVICE_DETAILS;

    companion object {
        lateinit var bundle: Bundle
    }
}