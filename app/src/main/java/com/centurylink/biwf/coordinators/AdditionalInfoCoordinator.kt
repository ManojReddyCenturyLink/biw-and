package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdditionalInfoCoordinator @Inject constructor() :
    Coordinator<AdditionalInfoCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: AdditionalInfoCoordinatorDestinations) {
        when (destination) {
            AdditionalInfoCoordinatorDestinations.CONTACT_INFO
            -> {
                navigator.navigateToContactInfo()
            }
        }
    }
}

enum class AdditionalInfoCoordinatorDestinations {
    CONTACT_INFO;

    companion object {
        lateinit var bundle: Bundle
    }
}