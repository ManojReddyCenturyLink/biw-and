package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FAQCoordinator @Inject constructor() {
    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<FAQCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: FAQCoordinatorDestinations) {
        when (destinations) {
            FAQCoordinatorDestinations.PLAY_VIDEO_ACTIVITY -> {
            }
            FAQCoordinatorDestinations.FAQ_LIST -> {
            }
            FAQCoordinatorDestinations.LIVE_CHAT -> {
            }
            FAQCoordinatorDestinations.CALL_BACK_SCHEDULE -> {
            }
        }
    }

    enum class FAQCoordinatorDestinations {
        PLAY_VIDEO_ACTIVITY, FAQ_LIST, LIVE_CHAT, CALL_BACK_SCHEDULE;

        companion object {
            lateinit var bundle: Bundle
            fun get(): Bundle = bundle
            fun set(bundleValue: Bundle) {
                bundle = bundleValue
            }
        }
    }
}