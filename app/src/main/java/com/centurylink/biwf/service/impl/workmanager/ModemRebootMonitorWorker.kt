package com.centurylink.biwf.service.impl.workmanager

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result.failure
import androidx.work.ListenableWorker.Result.success
import androidx.work.WorkerParameters
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

class ModemRebootMonitorWorker constructor(
    context: Context,
    workerParams: WorkerParameters,
    private val assiaRepository: AssiaRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        var timeBeforeFailure = MAX_TIMEOUT_MILLIS

        while (timeBeforeFailure > 0) {
            delay(RETRY_MILLIS)

            if (isRebootComplete()) {
                Timber.d("ModemRebootWorker - COMPLETE")
                return@withContext success()
            }
            timeBeforeFailure -= RETRY_MILLIS
            Timber.d("ModemRebootWorker - not complete - time remaining - $timeBeforeFailure")
        }
        return@withContext failure()
    }

    private suspend fun isRebootComplete(): Boolean {
        val result = assiaRepository.getModemInfoForcePing()
        return result is AssiaNetworkResponse.Success && result.body.modemInfo.isAlive
    }

    companion object {
        const val RETRY_MILLIS = 30000L
        const val MAX_TIMEOUT_MILLIS = 250000L

        const val UNIQUE_NAME = "modem-reboot"
    }
}
