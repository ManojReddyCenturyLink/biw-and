package com.centurylink.biwf.service.impl.workmanager

import androidx.lifecycle.asFlow
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.centurylink.biwf.repos.ModemRebootRepository
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Class that handles kicking off a modem reboot and kicking off [ModemRebootMonitorWorker] to
 * keep tabs on whether the modem has finished its reboot. This class exposes a field for
 * observing the modem reboot status.
 */
@Singleton
class ModemRebootMonitorService @Inject constructor(
    private val modemRebootRepository: ModemRebootRepository,
    private val workManager: WorkManager
) {

    /**
     * Observe this Flow to get updates representing the status of a modem reboot operation.
     */
    val modemRebootStateFlow: Flow<RebootState>

    private val manualEventFlow = EventFlow<RebootState>()

    init {
        modemRebootStateFlow = merge(
            workManager.getWorkInfosForUniqueWorkLiveData(ModemRebootMonitorWorker.UNIQUE_NAME).asFlow()
                .map { workInfos ->
                    return@map if (workInfos.isEmpty())
                        RebootState.READY
                    else
                        getRebootStateFromWorkerInfo(workerState = workInfos.first().state)
                },
            manualEventFlow
        )
    }

    private fun getRebootStateFromWorkerInfo(workerState: WorkInfo.State): RebootState {
        return when (workerState) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING -> RebootState.ONGOING
            WorkInfo.State.SUCCEEDED -> RebootState.SUCCESS
            WorkInfo.State.FAILED,
            WorkInfo.State.BLOCKED,
            WorkInfo.State.CANCELLED -> RebootState.ERROR
        }
    }

    /**
     * Call this method to attempt a modem reboot.
     */
    suspend fun sendRebootModemRequest() {
        manualEventFlow.postValue(RebootState.ONGOING)

        val result = modemRebootRepository.rebootModem()
        result.fold(ifLeft = {
            manualEventFlow.postValue(RebootState.ERROR)
            Timber.e("Error requesting modem reboot %s", it.message)
        },ifRight = {
            if (it.code == ModemRebootRepository.REBOOT_STARTED_SUCCESSFULLY) {
                enqueueModemRebootWork()
            } else {
                manualEventFlow.postValue(RebootState.ERROR)
                Timber.e("Error requesting modem reboot %s", it.message)
            }
        })
    }

    private fun enqueueModemRebootWork() {
        workManager.enqueueUniqueWork(
            ModemRebootMonitorWorker.UNIQUE_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<ModemRebootMonitorWorker>().build()
        )
    }

    /**
     * Calling this will prevent modemRebootStatusFlow from reporting SUCCESS/ERROR redundantly.
     * Call this method after a Success/Failure dialog has been shown to the user.
     */
    fun pruneFinishedWork() {
        workManager.pruneWork()
    }

    /**
     * Currently this is just done on logout, since this task should not continue when not logged
     * in.
     */
    fun cancelWork() {
        workManager.cancelUniqueWork(ModemRebootMonitorWorker.UNIQUE_NAME)
    }

    enum class RebootState {
        READY, ONGOING, SUCCESS, ERROR
    }
}
