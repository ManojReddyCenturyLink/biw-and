package com.centurylink.biwf.service.impl.workmanager

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.centurylink.biwf.repos.AssiaRepository

/**
  Class that allows Worker classes like [ModemRebootMonitorWorker] to be created with access to [AssiaRepository]
 */
class AssiaWorkerFactory(private val assiaRepository: AssiaRepository) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when (workerClassName) {
            ModemRebootMonitorWorker::class.java.name ->
                ModemRebootMonitorWorker(appContext, workerParameters, assiaRepository)
            else -> null
        }
    }
}
