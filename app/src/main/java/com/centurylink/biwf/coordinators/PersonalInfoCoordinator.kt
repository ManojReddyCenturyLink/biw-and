package com.centurylink.biwf.coordinators

import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalInfoCoordinator @Inject constructor() {

    @Inject
    lateinit var navigator: Navigator

    fun observeThis(screenState: ObservableData<PersonalInfoCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: PersonalInfoCoordinatorDestinations) {
        when (destinations) {
            PersonalInfoCoordinatorDestinations.PROFILE_INFO -> {
            }
            PersonalInfoCoordinatorDestinations.DONE -> {
            }
        }
    }

    enum class PersonalInfoCoordinatorDestinations {
        PROFILE_INFO, DONE;
    }
}