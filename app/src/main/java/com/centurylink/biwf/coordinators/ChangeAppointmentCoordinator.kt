package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ChangeAppointmentCoordinator - This ChangeAppointmentCoordinator class is used for the purpose of Navigation
 * flow from the Change Appointment activity.
 *
 * @constructor Create  Change appointment coordinator
 */
@Singleton
class ChangeAppointmentCoordinator @Inject constructor() :
    Coordinator<ChangeAppointmentCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to ChangeAppointmentCoordinatorDestinations from Change Appointment screen.
     *
     * @param destination The destination enum constants for Screens.
     */
    override fun navigateTo(destination: ChangeAppointmentCoordinatorDestinations) {
        when (destination) {
            ChangeAppointmentCoordinatorDestinations.APPOINTMENT_CONFIRMED -> {
                navigator.navigateToAppointmentConfirmation()
            }
        }
    }
}

/**
 * Change appointment coordinator destinations.
 *
 * @constructor Create  Change appointment coordinator destinations.
 */
enum class ChangeAppointmentCoordinatorDestinations {
    APPOINTMENT_CONFIRMED;

    companion object {
        lateinit var bundle: Bundle
    }
}