package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportCoordinator @Inject constructor() : Coordinator<SupportCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: SupportCoordinatorDestinations) {
        when (destination) {
            SupportCoordinatorDestinations.FAQ -> navigateToFaq()
            SupportCoordinatorDestinations.NAVIGATE_TO_WEBSITE -> { }
            SupportCoordinatorDestinations.SCHEDULE_CALLBACK -> navigateToScheduleCallback()
            SupportCoordinatorDestinations.LIVE_CHAT -> navigateToLiveChat()
        }
    }

    private fun navigateToFaq() {
        navigator.navigateToFaq()
    }

    private fun navigateToScheduleCallback() {
        navigator.navigateToScheduleCallback()
    }

    private fun navigateToLiveChat() {}
}

enum class SupportCoordinatorDestinations {
    FAQ, LIVE_CHAT, SCHEDULE_CALLBACK, NAVIGATE_TO_WEBSITE;

    companion object {
        lateinit var bundle: Bundle
    }
}