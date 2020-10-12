package com.centurylink.biwf.coordinators

import javax.inject.Inject
import javax.inject.Singleton

/**
 * PersonalInfo coordinator - This PersonalInfoCoordinator class is used for the purpose of Navigation
 * flow from the PersonalInfoActivity Screen.
 *
 * @property navigator Navigator instance where app Navigation is implemented for each screens.
 * @constructor Create Personal info coordinator
 */
@Singleton
class PersonalInfoCoordinator @Inject constructor(val navigator: Navigator) :
    Coordinator<PersonalInfoCoordinatorDestinations> {

    /**
     * Navigate to PersonalInfoCoordinatorDestinations from  PersonalInfo Screen
     *
     * @param destination PersonalInfoCoordinatorDestinations
     */
    override fun navigateTo(destination: PersonalInfoCoordinatorDestinations) {
        when (destination) {
            PersonalInfoCoordinatorDestinations.DONE -> Unit
        }
    }
}

/**
 * Personal info coordinator destinations  used for Navigation to Other screens from PersonalInfo Screen.
 *
 * @constructor Create Personal info coordinator destinations
 */
enum class PersonalInfoCoordinatorDestinations {
    DONE
}
