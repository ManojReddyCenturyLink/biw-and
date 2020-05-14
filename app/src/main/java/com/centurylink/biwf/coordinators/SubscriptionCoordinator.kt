package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionCoordinator @Inject constructor(val navigator: Navigator) {

    fun observeThis(screenState: ObservableData<SubscriptionCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: SubscriptionCoordinatorDestinations) {
        when (destinations) {
            SubscriptionCoordinatorDestinations.SUBSCRIPTION -> {
            }
            SubscriptionCoordinatorDestinations.STATEMENT -> {
                navigateToInvoiceDetails()
            }
            SubscriptionCoordinatorDestinations.MANAGE_MY_SUBSCRIPTION -> {
                navigateToManageSubscription()
            }
        }
    }

    private fun navigateToInvoiceDetails() {
        navigator.navigateToBillStatement()
    }

    private fun navigateToManageSubscription() {
        navigator.navigateToMangeSubscription()
    }

    enum class SubscriptionCoordinatorDestinations {
        SUBSCRIPTION, STATEMENT, MANAGE_MY_SUBSCRIPTION;

        companion object {
            lateinit var bundle: Bundle
        }
    }
}