package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardCoordinator @Inject constructor() : Coordinator<DashboardCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: DashboardCoordinatorDestinations) {
        when (destination) {
            DashboardCoordinatorDestinations.APPOINTMENT_SCHEDULED -> loadAppointmentFragment()
            DashboardCoordinatorDestinations.ENROUTE -> loadEnrouteFragment()
            DashboardCoordinatorDestinations.IN_PROGRESS -> loadInProgressFragment()
            DashboardCoordinatorDestinations.COMPLETED -> loadCompletedFragment()
            DashboardCoordinatorDestinations.NORMAL -> loadNormalFragment()
            DashboardCoordinatorDestinations.CHANGE_APPOINTMENT -> navigateToChangeAppointment()
            DashboardCoordinatorDestinations.NOTIFICATION_DETAILS -> navigateToNotificationDetails()
        }
    }

    private fun loadAppointmentFragment() {
        //For future reference
    }

    private fun loadEnrouteFragment() {
        //For future reference
    }

    private fun loadInProgressFragment() {
        //For future reference
    }

    private fun loadCompletedFragment() {
        //For future reference
    }

    private fun loadNormalFragment() {
        //For future reference
    }

    private fun navigateToChangeAppointment() {
        navigator.navigateToChangeAppointment()
    }

    private fun navigateToNotificationDetails() {
        navigator.navigateToNotificationDetails()
    }
}

enum class DashboardCoordinatorDestinations {
    APPOINTMENT_SCHEDULED, ENROUTE, IN_PROGRESS, COMPLETED, NORMAL, CHANGE_APPOINTMENT, NOTIFICATION_DETAILS;

    companion object {
        lateinit var bundle: Bundle
    }
}
