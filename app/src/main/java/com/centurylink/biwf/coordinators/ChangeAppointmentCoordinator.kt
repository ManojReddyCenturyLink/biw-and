package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangeAppointmentCoordinator @Inject constructor() :
    Coordinator<ChangeAppointmentCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: ChangeAppointmentCoordinatorDestinations) {
        when (destination) {
            ChangeAppointmentCoordinatorDestinations.APPOINTMENT_CONFIRMED -> {
                navigator.navigateToAppointmentConfirmation()
            }
        }
    }
}

enum class ChangeAppointmentCoordinatorDestinations {
    APPOINTMENT_CONFIRMED;

    companion object {
        lateinit var bundle: Bundle
    }
}