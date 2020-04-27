package com.centurylink.biwf.coordinators

import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleCallbackCoordinator @Inject constructor(private val navigator: Navigator) {

    fun getNavigator() : Navigator {
        return navigator
    }

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
        }
    }

    private fun navigateToPhoneDialler() {
        navigator.navigateToPhoneDialler()
    }

    enum class ScheduleCallbackCoordinatorDestinations {
        SCHEDULE_CALLBACK, CALL_SUPPORT;
    }
}