package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleCallbackCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<ScheduleCallbackCoordinatorDestinations> {

    override fun navigateTo(destination: ScheduleCallbackCoordinatorDestinations) {
        when (destination) {
            ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT -> navigateToPhoneDialler()
            ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO -> navigateToAdditionalInfo()
        }
    }

    private fun navigateToAdditionalInfo() {
        navigator.navigateToAdditionalInfo()
    }

    private fun navigateToPhoneDialler() {
        navigator.navigateToPhoneDialler()
    }
}

enum class ScheduleCallbackCoordinatorDestinations {
    CALL_SUPPORT, ADDITIONAL_INFO;

    companion object {
        lateinit var bundle: Bundle
    }
}
