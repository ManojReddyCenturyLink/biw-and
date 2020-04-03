package com.centurylink.biwf.coordinators

import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<HomeCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: HomeCoordinatorDestinations) {
        when (destinations) {
            HomeCoordinatorDestinations.HOME -> navigateToHomeScreen()
            HomeCoordinatorDestinations.SUPPORT -> navigateToSupport()
            HomeCoordinatorDestinations.NOTIFICATION_LIST -> navigateToNotificationList()
        }
    }

    private fun navigateToHomeScreen() {
        // Do Nothing
    }

    private fun navigateToNotificationList() {
        navigator.navigateToNotificationList()
    }

    private fun navigateToSupport() {
        navigator.navigateToSupport()
    }
}

enum class HomeCoordinatorDestinations {
    HOME, SUPPORT,NOTIFICATION_LIST
}
