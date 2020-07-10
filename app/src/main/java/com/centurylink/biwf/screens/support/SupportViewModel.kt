package com.centurylink.biwf.screens.support

import android.os.Bundle
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SupportCoordinatorDestinations
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.repos.ModemRebootRepository
import com.centurylink.biwf.repos.ModemRebootRepository.Companion.REBOOT_STARTED_SUCCESSFULLY
import com.centurylink.biwf.service.impl.workmanager.ModemRebootWorker
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SupportViewModel @Inject constructor(
    private val faqRepository: FAQRepository,
    private val modemRebootRepository: ModemRebootRepository,
    private val workManager: WorkManager
) : BaseViewModel() {

    val faqSectionInfo: Flow<UiFAQQuestionsSections> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val myState = EventFlow<SupportCoordinatorDestinations>()
    private var recordTypeId: String = ""
    var progressViewFlow = EventFlow<Boolean>()

    var modemRebootStatusFlow = EventFlow<ModemRebootRepository.Companion.RebootState>()

    init {
        initApis()
    }

    fun initApis() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestRecordId()
            requestFaqDetailsInfo()
        }
        modemRebootStatusFlow.latestValue = ModemRebootRepository.Companion.RebootState.READY
        viewModelScope.launch {
            listenToModemRebootStatus()
        }
    }

    private suspend fun requestFaqDetailsInfo() {
        progressViewFlow.latestValue = true
        val faqDetails = faqRepository.getFAQQuestionDetails(recordTypeId)
        faqDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateFaqDetails(it)
            progressViewFlow.latestValue = false
        }
    }

    private suspend fun requestRecordId() {
        val subscriptionDate = faqRepository.getKnowledgeRecordTypeId()
        subscriptionDate.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            recordTypeId = it
            progressViewFlow.latestValue = false
        }
    }

    private suspend fun listenToModemRebootStatus() {
        workManager.getWorkInfosForUniqueWorkLiveData(ModemRebootWorker.UNIQUE_NAME).asFlow().collect { workInfos ->
            modemRebootStatusFlow.latestValue =
                if (workInfos.isEmpty())
                    ModemRebootRepository.Companion.RebootState.READY
                else
                    getRebootStateFromWorkerInfo(workerState = workInfos.first().state)
        }
    }

    private fun getRebootStateFromWorkerInfo(workerState: WorkInfo.State): ModemRebootRepository.Companion.RebootState {
        return when (workerState) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.RUNNING -> ModemRebootRepository.Companion.RebootState.ONGOING
            WorkInfo.State.SUCCEEDED -> ModemRebootRepository.Companion.RebootState.SUCCESS
            WorkInfo.State.FAILED,
            WorkInfo.State.BLOCKED,
            WorkInfo.State.CANCELLED -> ModemRebootRepository.Companion.RebootState.ERROR
        }
    }

    private suspend fun sendRebootModemRequest() {
        val result = modemRebootRepository.rebootModem()
        if (result.code == REBOOT_STARTED_SUCCESSFULLY) {
            workManager.enqueueUniqueWork(
                ModemRebootWorker.UNIQUE_NAME,
                ExistingWorkPolicy.REPLACE,
                OneTimeWorkRequestBuilder<ModemRebootWorker>().build()
            )
        } else {
            modemRebootStatusFlow.latestValue = ModemRebootRepository.Companion.RebootState.ERROR
            Timber.e("Error requesting modem reboot %s", result.message)
        }
    }

    private fun updateFaqDetails(faq: Faq) {
        val questionMap: List<String> = faq.records.mapNotNull { it.sectionC }.distinct()
        faqSectionInfo.latestValue = UiFAQQuestionsSections(questionMap)
    }

    fun navigateToFAQList(faqSectionSelected: String) {
        val bundle = Bundle()
        bundle.putString(FAQActivity.FAQ_TITLE, faqSectionSelected)
        SupportCoordinatorDestinations.bundle = bundle
        myState.latestValue = SupportCoordinatorDestinations.FAQ
    }

    fun runSpeedTest() {}

    fun rebootModem() {
        viewModelScope.launch {
            modemRebootStatusFlow.latestValue = ModemRebootRepository.Companion.RebootState.ONGOING
            sendRebootModemRequest()
        }
    }

    fun onRetryModemRebootClicked() {
        rebootModem()
    }

    fun onCancelModemRebootClicked() {
        modemRebootStatusFlow.latestValue = ModemRebootRepository.Companion.RebootState.READY
    }

    fun launchScheduleCallback() {
        myState.latestValue = SupportCoordinatorDestinations.SCHEDULE_CALLBACK
    }

    data class UiFAQQuestionsSections(
        val questionMap: List<String> = emptyList()
    )
}
