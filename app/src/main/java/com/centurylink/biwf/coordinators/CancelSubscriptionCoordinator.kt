package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancelSubscriptionCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations) {
        when (destinations) {
            CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION -> {
                navigator.navigateToCancelSubscriptionDetails()
            }
        }
    }

    enum class SubscriptionCoordinatorDestinations {
        CANCEL_SUBSCRIPTION, CANCEL_SELECT_DATE_SUBSCRIPTION;

        companion object {
            lateinit var bundle: Bundle
            fun get(): Bundle = bundle
            fun set(bundleValue: Bundle) {
                bundle = bundleValue
            }
        }
    }
}