package com.centurylink.biwf.service.impl.workmanager

import androidx.work.DelegatingWorkerFactory
import com.centurylink.biwf.repos.AssiaRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * [DelegatingWorkerFactory] for passing dependencies to WorkManager Worker classes. Note: Worker
 * classes requiring different dependencies will need their own WorkerFactory class, like
 * [AssiaWorkerFactory]. Classes are then hooked in here via the init block:
 *
 * init {
 *      addFactory(MyNewFactory(dependency1, ...))
 * }
 *
 */
@Singleton
class MainWorkerFactory @Inject constructor(
    assiaRepository: AssiaRepository
) : DelegatingWorkerFactory() {

    init {
        addFactory(AssiaWorkerFactory(assiaRepository))
    }
}
