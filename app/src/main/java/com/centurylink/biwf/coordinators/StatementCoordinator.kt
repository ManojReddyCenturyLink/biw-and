package com.centurylink.biwf.coordinators

import android.os.Bundle
import javax.inject.Inject

class StatementCoordinator @Inject constructor(): Coordinator<StatementCoordinatorDestinations> {
    override fun navigateTo(destination: StatementCoordinatorDestinations) { }
}

enum class StatementCoordinatorDestinations {
    FAQ;
    companion object {
        lateinit var bundle: Bundle
    }
}
