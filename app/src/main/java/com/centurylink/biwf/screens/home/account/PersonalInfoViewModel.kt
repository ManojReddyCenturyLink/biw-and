package com.centurylink.biwf.screens.home.account

import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.PersonalInfoCoordinator
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class PersonalInfoViewModel @Inject constructor(
) : BaseViewModel() {

    val myState = ObservableData(PersonalInfoCoordinator.PersonalInfoCoordinatorDestinations.PROFILE_INFO)

    fun updatePassword() {
        myState.value = PersonalInfoCoordinator.PersonalInfoCoordinatorDestinations.DONE
    }
}