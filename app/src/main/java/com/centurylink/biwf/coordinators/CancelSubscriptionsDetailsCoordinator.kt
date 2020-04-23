package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancelSubscriptionsDetailsCoordinator @Inject constructor() {
    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<CancelSubscriptionsDetailsCoordinator.CancelSubscriptionsDetailsCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: CancelSubscriptionsDetailsCoordinator.CancelSubscriptionsDetailsCoordinatorDestinations) {
        when (destinations) {
            CancelSubscriptionCoordinator.SubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION -> {
            }
        }
    }

    enum class CancelSubscriptionsDetailsCoordinatorDestinations {
        HOME;

        companion object {
            lateinit var bundle: Bundle
            fun get(): Bundle = bundle
            fun set(bundleValue: Bundle) {
                bundle = bundleValue
            }
        }
    }
}