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
            SupportCoordinatorDestinations.SCHEDULE_CALLBACK -> {}
            SupportCoordinatorDestinations.LIVE_CHAT -> navigator.navigateToLiveChat()
        }
    }

    private fun navigateToFaq() {
        navigator.navigateToFaq()
    }

    private fun navigateToLiveChat(){
        navigator.navigateToFaq()
    }

    enum class SupportCoordinatorDestinations {
        FAQ, LIVE_CHAT, SCHEDULE_CALLBACK, NAVIGATE_TO_WEBSITE, SUPPORT;

        companion object {
            lateinit var bundle: Bundle
            fun get(): Bundle = bundle
            fun set(bundleValue: Bundle) {
                bundle = bundleValue
            }
        }
    }
}