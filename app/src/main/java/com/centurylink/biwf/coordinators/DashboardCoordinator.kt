package com.centurylink.biwf.coordinators

import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<DashboardCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: DashboardCoordinatorDestinations) {
        when (destinations) {
            DashboardCoordinatorDestinations.HOME -> {}
            DashboardCoordinatorDestinations.APPOINTMENT_SCHEDULED -> loadAppointmentFragment()
            DashboardCoordinatorDestinations.ENROUTE -> loadEnrouteFragment()
            DashboardCoordinatorDestinations.IN_PROGRESS -> loadInProgressFragment()
            DashboardCoordinatorDestinations.COMPLETED -> loadCompletedFragment()
            DashboardCoordinatorDestinations.NORMAL -> loadNormalFragment()
            DashboardCoordinatorDestinations.CHANGE_APPOINTMENT -> navigateToChangeAppointment()
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
}

enum class DashboardCoordinatorDestinations {
    HOME, APPOINTMENT_SCHEDULED, ENROUTE, IN_PROGRESS, COMPLETED, NORMAL, CHANGE_APPOINTMENT
}