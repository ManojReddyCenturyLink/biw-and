package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class StatementCoordinator @Inject constructor() {

    fun observeThis(screenState: ObservableData<StatementCoordinatorDestinations>) {
        screenState.observable.subscribe {
            navigateTo(it)
        }
    }

    private fun navigateTo(destinations: StatementCoordinatorDestinations) {

    }

    enum class StatementCoordinatorDestinations {
        FAQ;
        companion object {
            lateinit var bundle: Bundle
        }
    }

}