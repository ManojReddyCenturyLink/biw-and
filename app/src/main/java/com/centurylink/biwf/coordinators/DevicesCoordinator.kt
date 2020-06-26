package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DevicesCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<DevicesCoordinatorDestinations> {
    override fun navigateTo(destination: DevicesCoordinatorDestinations) {
        when (destination) {
            DevicesCoordinatorDestinations.DEVICE_DETAILS -> navigateToUsageDetails()
        }
    }

    private fun navigateToUsageDetails(){
        navigator.navigateToUsageDetailsActivity()
    }
}

enum class DevicesCoordinatorDestinations {
    DEVICE_DETAILS;

    companion object {
        lateinit var bundle: Bundle
    }
}