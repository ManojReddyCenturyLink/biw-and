package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

/**
 * NetworkStatusCoordinator - This NetworkStatusCoordinator class is used for the purpose of Navigation
 * flow from the NetworkStatus Screen.
 *
 * @property navigator Navigator instance where app Navigation is implemented for each screens.
 * @constructor Create  Network status coordinator
 */
@Singleton
class NetworkStatusCoordinator @Inject constructor(val navigator: Navigator) :
    Coordinator<NetworkStatusCoordinatorDestinations> {

    /**
     * Navigate to NetworkStatusCoordinatorDestinations from Network Status screen.
     *
     * @param destination NetworkStatusCoordinatorDestinations
     */
    override fun navigateTo(destination: NetworkStatusCoordinatorDestinations) {
        when (destination) {
            NetworkStatusCoordinatorDestinations.DONE -> Unit
        }
    }
}

/**
 * Network status coordinator destinations
 *
 * @constructor Create Network status coordinator destinations
 */
enum class NetworkStatusCoordinatorDestinations {
    DONE
}
