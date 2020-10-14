package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AdditionalInfoCoordinator - This AdditionalInfoCoordinator class is used for the purpose of Navigation
 * flow from the Additional Info Activity.
 *
 * @constructor Create Additional info coordinator
 */
@Singleton
class AdditionalInfoCoordinator @Inject constructor() :
    Coordinator<AdditionalInfoCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to AdditionalInfoCoordinatorDestinations from Additional Info screen.
     *
     * @param destination The destination enum constants for Screens.
     */
    override fun navigateTo(destination: AdditionalInfoCoordinatorDestinations) {
        when (destination) {
            AdditionalInfoCoordinatorDestinations.CONTACT_INFO
            -> {
                navigator.navigateToContactInfo()
            }
        }
    }
}

/**
 *AdditionalInfoCoordinatorDestinations destinations for Navigation from AdditionInfoScreens
 *
 * @constructor Create  Additional info coordinator destinations
 */
enum class AdditionalInfoCoordinatorDestinations {
    CONTACT_INFO;

    companion object {
        lateinit var bundle: Bundle
    }
}