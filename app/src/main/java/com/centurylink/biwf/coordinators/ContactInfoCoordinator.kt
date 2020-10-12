package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactInfoCoordinator @Inject constructor() :
    Coordinator<ContactInfoCoordinatorDestinations> {

    @Inject
    lateinit var navigator: Navigator

    override fun navigateTo(destination: ContactInfoCoordinatorDestinations) {
        when (destination) {
            ContactInfoCoordinatorDestinations.SELECT_TIME
            -> {
                navigator.navigateToSelectTime()
            }
        }
    }
}

enum class ContactInfoCoordinatorDestinations {
    SELECT_TIME;

    companion object {
        lateinit var bundle: Bundle
    }
}