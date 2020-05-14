package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CancelSubscriptionCoordinator @Inject constructor() :
    Coordinator<CancelSubscriptionCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: CancelSubscriptionCoordinatorDestinations) {
        when (destination) {
            CancelSubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION -> {
                navigator.navigateToCancelSubscriptionDetails()
            }
        }
    }
}

enum class CancelSubscriptionCoordinatorDestinations {
    CANCEL_SELECT_DATE_SUBSCRIPTION;

    companion object {
        lateinit var bundle: Bundle
    }
}
