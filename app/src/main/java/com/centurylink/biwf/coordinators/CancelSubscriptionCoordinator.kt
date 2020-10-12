package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CancelSubscriptionCoordinator- This  class is used for the purpose of Navigation
 * flow from the CancelSubscription Activity.
 *
 * @constructor Create  Cancel subscription coordinator
 */
@Singleton
class CancelSubscriptionCoordinator @Inject constructor() :
    Coordinator<CancelSubscriptionCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    /**
     * Navigate to CancelSubscriptionCoordinatorDestinations from CancelSubscription screen.
     *
     * @param destination The destination enum constants for Screens.
     */
    override fun navigateTo(destination: CancelSubscriptionCoordinatorDestinations) {
        when (destination) {
            CancelSubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION -> {
                navigator.navigateToCancelSubscriptionDetails()
            }
        }
    }
}

/**
 * Cancel subscription coordinator destinations
 *
 * @constructor Create  Cancel subscription coordinator destinations
 */
enum class CancelSubscriptionCoordinatorDestinations {

    CANCEL_SELECT_DATE_SUBSCRIPTION;

    companion object {
        lateinit var bundle: Bundle
    }
}
