package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ContactInfoCoordinator -  This  class is used for the purpose of Navigation
 * flow from the ContactInfo Activity.
 *
 * @constructor Create empty Contact info coordinator
 */
@Singleton
class ContactInfoCoordinator @Inject constructor() :
    Coordinator<ContactInfoCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to ContactInfoCoordinatorDestinations from Contact info Screen
     *
     * @param destination The destination enum constants for Contact Info Screens.
     */
    override fun navigateTo(destination: ContactInfoCoordinatorDestinations) {
        when (destination) {
            ContactInfoCoordinatorDestinations.SELECT_TIME
            -> {
                navigator.navigateToSelectTime()
            }
        }
    }
}

/**
 * Contact info coordinator destinations
 *
 * @constructor Create  Contact info coordinator destinations
 */
enum class ContactInfoCoordinatorDestinations {
    SELECT_TIME;

    companion object {
        lateinit var bundle: Bundle
    }
}
