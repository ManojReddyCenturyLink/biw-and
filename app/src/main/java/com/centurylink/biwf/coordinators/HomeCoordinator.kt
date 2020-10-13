package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HomeCoordinator - This HomeCoordinator class is used for the purpose of Navigation
 * flow from the Home Screen.
 *
 * @constructor Create empty Home coordinator
 */
@Singleton
class HomeCoordinator @Inject constructor(): Coordinator<HomeCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to HomeCoordinatorDestinations
     *
     * @param destination
     */
    override fun navigateTo(destination: HomeCoordinatorDestinations) {
        when (destination) {
            HomeCoordinatorDestinations.SUPPORT -> navigateToSupport()
            HomeCoordinatorDestinations.NOTIFICATION_LIST -> navigateToNotificationList()
            HomeCoordinatorDestinations.NOTIFICATION_DETAILS -> navigateToNavigationDetails()
            HomeCoordinatorDestinations.SUBSCRIPTION_ACTIVITY -> navigateToSubscriptionActivity()
            HomeCoordinatorDestinations.NETWORK_STATUS -> navigateToNetworkStatusActivity()
        }
    }

    /**
     * Function guides us to navigate to Network Status Activity.
     *
     */
    private fun navigateToNetworkStatusActivity() {
        navigator.navigateToNetworkInformationScreen()
    }

    /**
     * Function guides us to navigate to Notification List Activity.
     *
     */
    private fun navigateToNotificationList() {
        navigator.navigateToNotificationList()
    }

    /**
     * Function guides us to navigate to Support  Activity.
     *
     */
    private fun navigateToSupport() {
        navigator.navigateToSupport()
    }

    /**
     * Function guides us to navigate to NavigationDetails  Activity.
     *
     */
    private fun navigateToNavigationDetails() {
        navigator.navigateToNotificationDetails()
    }

    /**
     * Function guides us to navigate to Subscription  Activity.
     *
     */
    private fun navigateToSubscriptionActivity() {
        navigator.navigateToSubscriptionActivity()
    }
}

/**
 * HomeCoordinator destinations used for navigating to other screens from Home
 *
 * @constructor Create  Home coordinator destinations
 */
enum class HomeCoordinatorDestinations {
    SUPPORT, NOTIFICATION_LIST, NOTIFICATION_DETAILS, SUBSCRIPTION_ACTIVITY, NETWORK_STATUS;

    companion object {
        lateinit var bundle: Bundle
    }
}
