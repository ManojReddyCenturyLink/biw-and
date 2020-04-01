package com.centurylink.biwf.screens.home

import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.coordinators.LoginCoordinatorDestinations
import com.centurylink.biwf.utility.ObservableData

class HomeViewModel(
) : BaseViewModel() {

    val myState = ObservableData(HomeCoordinatorDestinations.HOME)

    fun onSupportClicked() {
        myState.value = HomeCoordinatorDestinations.SUPPORT
    }

}
