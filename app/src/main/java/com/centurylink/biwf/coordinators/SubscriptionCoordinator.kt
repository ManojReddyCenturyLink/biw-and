package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SubscriptionCoordinator - This SubscriptionCoordinator class is used for the purpose of Navigation
 * flow from the Subscription Screen.
 *
 * @property navigator Navigator instance where app Navigation is implemented for each screens.
 * @constructor Create empty Subscription coordinator.
 */
@Singleton
class SubscriptionCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<SubscriptionCoordinatorDestinations> {

    /**
     * Navigate to SubscriptionCoordinatorDestinations from  Subscription Screen
     *
     * @param destination the destination enum constants for Subscription  Screen.
     */
    override fun navigateTo(destination: SubscriptionCoordinatorDestinations) {
        when (destination) {
            SubscriptionCoordinatorDestinations.EDIT_PAYMENT -> navigateToEditPaymentDetails()
            SubscriptionCoordinatorDestinations.STATEMENT -> navigateToInvoiceDetails()
            SubscriptionCoordinatorDestinations.MANAGE_MY_SUBSCRIPTION -> navigateToManageSubscription()
        }
    }

    /**
     * function guides us to navigate to EditPaymentDetails Screen.
     *
     */
    private fun navigateToEditPaymentDetails() {
        navigator.navigateToEditPaymentDetails()
    }

    /**
     * function guides us to navigate to InvoiceDetails Screen.
     *
     */
    private fun navigateToInvoiceDetails() {
        navigator.navigateToBillStatement()
    }

    /**
     * function guides us to navigate to ManageSubscription Screen.
     *
     */
    private fun navigateToManageSubscription() {
        navigator.navigateToMangeSubscription()
    }
}

/**
 * SubscriptionCoordinator destinations used for Navigation to Other screens from Subscription Screen.
 *
 * @constructor Create  Subscription coordinator destinations
 */
enum class SubscriptionCoordinatorDestinations {
    STATEMENT, MANAGE_MY_SUBSCRIPTION, EDIT_PAYMENT;

    companion object {
        lateinit var bundle: Bundle
    }
}
