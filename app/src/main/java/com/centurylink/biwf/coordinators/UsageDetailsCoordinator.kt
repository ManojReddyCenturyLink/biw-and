package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UsageDetailsCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<UsageDetailsCoordinatorDestinations> {

    override fun navigateTo(destination: UsageDetailsCoordinatorDestinations) {
        when (destination) {
            UsageDetailsCoordinatorDestinations.DEVICES_CONNECTED -> navigateToDevices()
        }
    }

    private fun navigateToDevices() {
    }
}

enum class UsageDetailsCoordinatorDestinations {
    DEVICES_CONNECTED;

    companion object {
        lateinit var bundle: Bundle
    }
}