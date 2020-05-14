package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FAQCoordinator @Inject constructor() : Coordinator<FAQCoordinatorDestinations> {
    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: FAQCoordinatorDestinations) {
        when (destination) {
            FAQCoordinatorDestinations.SCHEDULE_CALLBACK -> navigateToScheduleCallback()
        }
    }

    private fun navigateToScheduleCallback() {
        navigator.navigateToScheduleCallback()
    }
}

enum class FAQCoordinatorDestinations {
    SCHEDULE_CALLBACK;
}
