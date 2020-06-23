package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeCoordinator @Inject constructor(): Coordinator<HomeCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: HomeCoordinatorDestinations) {
        when (destination) {
            HomeCoordinatorDestinations.SUPPORT -> navigateToSupport()
            HomeCoordinatorDestinations.NOTIFICATION_LIST -> navigateToNotificationList()
            HomeCoordinatorDestinations.NOTIFICATION_DETAILS -> navigateToNavigationDetails()
            HomeCoordinatorDestinations.SUBSCRIPTION_ACTIVITY -> navigateToSubscriptionActivity()
            HomeCoordinatorDestinations.NETWORK_STATUS -> navigateToNetworkStatusActivity()
        }
    }

    private fun navigateToNetworkStatusActivity() {
        navigator.navigateToNetworkStatus()
    }

    private fun navigateToNotificationList() {
        navigator.navigateToNotificationList()
    }

    private fun navigateToSupport() {
        navigator.navigateToSupport()
    }

    private fun navigateToNavigationDetails() {
        navigator.navigateToNotificationDetails()
    }

    private fun navigateToSubscriptionActivity() {
        navigator.navigateToSubscriptionActivity()
    }
}

enum class HomeCoordinatorDestinations {
    SUPPORT, NOTIFICATION_LIST, NOTIFICATION_DETAILS, SUBSCRIPTION_ACTIVITY, NETWORK_STATUS
}
