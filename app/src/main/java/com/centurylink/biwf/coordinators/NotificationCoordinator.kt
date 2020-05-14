package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCoordinator @Inject constructor():Coordinator<NotificationCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: NotificationCoordinatorDestinations) {
        when (destination) {
            NotificationCoordinatorDestinations.NOTIFICATION_DETAILS -> {
                navigator.navigateToNotificationDetails()}
        }
    }
}

enum class NotificationCoordinatorDestinations {
    NOTIFICATION_DETAILS;

    companion object {
        lateinit var bundle: Bundle
    }
}
