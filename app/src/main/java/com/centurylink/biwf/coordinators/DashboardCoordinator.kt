package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DashboardCoordinator - This  class is used for the purpose of Navigation
 * flow from the DashboardFragment.
 *
 * @constructor Create empty DashboardCoardinator
 */
@Singleton
class DashboardCoordinator @Inject constructor() : Coordinator<DashboardCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to DashboardCoordinatorDestinations from  Dashboard Fragment info Screen
     *
     * @param destination The destination enum constants for Dashboard  Screens.
     */
    override fun navigateTo(destination: DashboardCoordinatorDestinations) {
        when (destination) {
            DashboardCoordinatorDestinations.APPOINTMENT_SCHEDULED -> loadAppointmentFragment()
            DashboardCoordinatorDestinations.ENROUTE -> loadEnrouteFragment()
            DashboardCoordinatorDestinations.IN_PROGRESS -> loadInProgressFragment()
            DashboardCoordinatorDestinations.COMPLETED -> loadCompletedFragment()
            DashboardCoordinatorDestinations.NORMAL -> loadNormalFragment()
            DashboardCoordinatorDestinations.CHANGE_APPOINTMENT -> navigateToChangeAppointment()
            DashboardCoordinatorDestinations.NOTIFICATION_DETAILS -> navigateToNotificationDetails()
            DashboardCoordinatorDestinations.NETWORK_INFORMATION -> navigateToNetworkInformation()
            DashboardCoordinatorDestinations.QR_CODE_SCANNING -> navigateToQRCodeScanning()
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

    /**
     * Navigate to change appointmentment Screen
     *
     */
    private fun navigateToChangeAppointment() {
        navigator.navigateToChangeAppointment()
    }

    /**
     * Method helps us to navigate to to Notification Details.
     *
     */
    private fun navigateToNotificationDetails() {
        navigator.navigateToNotificationDetails()
    }

    /**
     * Method helps us to navigate to to Network information Details Screen.
     *
     */
    private fun navigateToNetworkInformation(){
        navigator.navigateToNetworkInformationScreen()
    }

    /**
     * Method helps us to navigate to to QRCode Information Screen.
     *
     */
    private fun navigateToQRCodeScanning(){
        navigator.navigateToQRCodeScan()
    }
}

/**
 * Dashboard coordinator destinations used for Navigation to Other screens from Dashboard Screen.
 *
 * @constructor Create empty Dashboard coordinator destinations
 */
enum class DashboardCoordinatorDestinations {
    APPOINTMENT_SCHEDULED, ENROUTE, IN_PROGRESS, COMPLETED, NORMAL, CHANGE_APPOINTMENT, NOTIFICATION_DETAILS, NETWORK_INFORMATION,QR_CODE_SCANNING;

    companion object {
        lateinit var bundle: Bundle
    }
}
