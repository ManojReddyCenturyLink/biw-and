package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Notification coordinator -- This NotificationCoordinator class is used for the purpose of Navigation
 * flow from the Notification Screen.
 *
 * @constructor Create Notification coordinator
 */
@Singleton
class NotificationCoordinator @Inject constructor() : Coordinator<NotificationCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to NotificationCoordinatorDestinations from  Notification Screen
     *
     * @param destination NotificationCoordinatorDestinations.
     */
    override fun navigateTo(destination: NotificationCoordinatorDestinations) {
        when (destination) {
            NotificationCoordinatorDestinations.NOTIFICATION_DETAILS -> {
                navigator.navigateToNotificationDetails() }
        }
    }
}

/**
 * Notification coordinator destinations used for Navigation to Other screens from Notifications Screen.
 *
 * @constructor Create Notification coordinator destinations
 */
enum class NotificationCoordinatorDestinations {
    NOTIFICATION_DETAILS;

    companion object {
        lateinit var bundle: Bundle
    }
}
