package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalInfoCoordinator @Inject constructor(val navigator: Navigator) :
    Coordinator<PersonalInfoCoordinatorDestinations> {

    override fun navigateTo(destination: PersonalInfoCoordinatorDestinations) {
        when (destination) {
            PersonalInfoCoordinatorDestinations.DONE -> Unit
        }
    }
}

enum class PersonalInfoCoordinatorDestinations {
    DONE
}
