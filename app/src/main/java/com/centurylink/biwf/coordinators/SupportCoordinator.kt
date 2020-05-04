package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupportCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<SupportCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: SupportCoordinatorDestinations) {
        when (destinations) {
            SupportCoordinatorDestinations.SUPPORT -> {}
            SupportCoordinatorDestinations.FAQ -> { navigateToFaq() }
            SupportCoordinatorDestinations.NAVIGATE_TO_WEBSITE -> {}
            SupportCoordinatorDestinations.SCHEDULE_CALLBACK -> { navigateToScheduleCallback()}
            SupportCoordinatorDestinations.LIVE_CHAT -> navigateToLiveChat()
            //Temporary Navigation
            SupportCoordinatorDestinations.MANAGE_SUBSCRIPTION->navigateToManageSubscription()
        }
    }

    private fun navigateToFaq() {
        navigator.navigateToFaq()
    }

    private fun navigateToScheduleCallback() {
        navigator.navigateToScheduleCallback()
    }

    private fun navigateToLiveChat(){}

    private fun navigateToManageSubscription(){
        navigator.navigateToMangeSubscription()
    }

    enum class SupportCoordinatorDestinations {
        FAQ, LIVE_CHAT, SCHEDULE_CALLBACK, NAVIGATE_TO_WEBSITE, SUPPORT,MANAGE_SUBSCRIPTION;

        companion object {
            lateinit var bundle: Bundle
        }
    }
}