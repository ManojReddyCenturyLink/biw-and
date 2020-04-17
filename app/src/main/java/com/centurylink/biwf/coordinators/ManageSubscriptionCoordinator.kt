package com.centurylink.biwf.coordinators
import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ManageSubscriptionCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<ManageSubscriptionCoordinator.SubscriptionCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: ManageSubscriptionCoordinator.SubscriptionCoordinatorDestinations) {
        when (destinations) {
            ManageSubscriptionCoordinator.SubscriptionCoordinatorDestinations.LAUNCH_DATE_ACTIVITY -> {
            }
        }
    }

    enum class SubscriptionCoordinatorDestinations {
        LAUNCH_DATE_ACTIVITY;

        companion object {
            lateinit var bundle: Bundle
            fun get(): Bundle = bundle
            fun set(bundleValue: Bundle) {
                bundle = bundleValue
            }
        }
    }
}