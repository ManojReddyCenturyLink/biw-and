package com.centurylink.biwf.coordinators

import javax.inject.Inject

class DevicesCoordinator @Inject constructor(
    val navigator: Navigator
) : Coordinator<DevicesCoordinatorDestinations> {
    override fun navigateTo(destination: DevicesCoordinatorDestinations) {
        TODO("Not yet implemented")
    }
}

enum class DevicesCoordinatorDestinations {

}