package com.centurylink.biwf.coordinators

import android.os.Bundle
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * UsageDetailsCoordinator - This Account coordinator class is used for the purpose of Navigation
 * flow from the UsageDetailsCoordinatorDestinations.
 *
 * @property navigator Navigator instance where app Navigation is implemented for each screens.
 * @constructor Create empty Usage details coordinator
 */
@Singleton
class UsageDetailsCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<UsageDetailsCoordinatorDestinations> {

    /**
     *  Navigate to UsageDetailsCoordinatorDestinations from  Usage Details Screen
     *
     * @param destination the destination enum constants for UsageDetails Screen.
     */
    override fun navigateTo(destination: UsageDetailsCoordinatorDestinations) {
        when (destination) {
            UsageDetailsCoordinatorDestinations.DEVICES_CONNECTED -> {
                Timber.e("usage details coordinator destinations")
            }
        }
    }
}

/**
 * Usage details coordinator destinations used for Navigation to Other screens from Usage details Screen.
 *
 * @constructor Create empty Usage details coordinator destinations
 */
enum class UsageDetailsCoordinatorDestinations {
    DEVICES_CONNECTED;

    companion object {
        lateinit var bundle: Bundle
    }
}
