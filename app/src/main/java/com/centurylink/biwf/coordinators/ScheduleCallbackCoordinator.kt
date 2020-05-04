package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleCallbackCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<ScheduleCallbackCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: ScheduleCallbackCoordinatorDestinations) {
        when (destinations) {
            ScheduleCallbackCoordinatorDestinations.SCHEDULE_CALLBACK -> {
            }
            ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT -> {
                navigateToPhoneDialler()
            }
            ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO -> {
                navigateToAdditionalInfo()
            }
        }
    }

    private fun navigateToAdditionalInfo() {
        navigator.navigateToAdditionalInfo()
    }

    private fun navigateToPhoneDialler() {
        navigator.navigateToPhoneDialler()
    }

    enum class ScheduleCallbackCoordinatorDestinations {
        SCHEDULE_CALLBACK, CALL_SUPPORT, ADDITIONAL_INFO;

        companion object {
            lateinit var bundle: Bundle
        }
    }
}