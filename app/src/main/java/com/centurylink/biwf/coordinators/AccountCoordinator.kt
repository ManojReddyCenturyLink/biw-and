package com.centurylink.biwf.coordinators

import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountCoordinator @Inject constructor(private val navigator: Navigator) {

    fun getNavigator(): Navigator {
        return navigator
    }

    fun observeThis(screenState: ObservableData<AccountCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: AccountCoordinatorDestinations) {
        when (destinations) {
            AccountCoordinatorDestinations.HOME -> {
            }
            AccountCoordinatorDestinations.PROFILE_INFO -> navigateToPersonalInfoActivity()
        }
    }

    private fun navigateToPersonalInfoActivity() {
        navigator.navigateToPersonalInfoActivity()
    }

    enum class AccountCoordinatorDestinations {
        HOME, PROFILE_INFO;
    }
}