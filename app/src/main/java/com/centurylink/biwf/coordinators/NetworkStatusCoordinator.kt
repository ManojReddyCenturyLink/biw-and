package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStatusCoordinator @Inject constructor(val navigator: Navigator) :
    Coordinator<NetworkStatusCoordinatorDestinations> {

    override fun navigateTo(destination: NetworkStatusCoordinatorDestinations) {
        when (destination) {
            NetworkStatusCoordinatorDestinations.DONE -> Unit
        }
    }
}

enum class NetworkStatusCoordinatorDestinations {
    DONE
}
