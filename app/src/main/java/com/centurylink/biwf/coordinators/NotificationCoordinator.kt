package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<NotificationCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: NotificationCoordinatorDestinations) {
        when (destinations) {
            NotificationCoordinatorDestinations.NOTIFICATION_DETAILS -> {
                navigator.navigateToNotificationDetails()}
        }
    }

    enum class NotificationCoordinatorDestinations {
        NOTIFICATION_DETAILS, NOTIFICATION_LIST;

        companion object {
            lateinit var bundle: Bundle
            fun get(): Bundle = bundle
            fun set(bundleValue: Bundle) {
                bundle = bundleValue
            }
        }
    }
}