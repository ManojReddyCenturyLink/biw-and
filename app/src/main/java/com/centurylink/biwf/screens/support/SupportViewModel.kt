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
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.repos.ModemRebootRepository
import com.centurylink.biwf.repos.ModemRebootRepository.Companion.REBOOT_STARTED_SUCCESSFULLY
import com.centurylink.biwf.screens.home.dashboard.DashboardViewModel
import com.centurylink.biwf.service.impl.workmanager.ModemRebootWorker
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class SupportViewModel @Inject constructor(
    private val faqRepository: FAQRepository,
    private val modemRebootRepository: ModemRebootRepository,
    private val workManager: WorkManager,
    private val assiaRepository: AssiaRepository,
    private val sharedPreferences: Preferences
) : BaseViewModel() {

    val faqSectionInfo: Flow<UiFAQQuestionsSections> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val myState = EventFlow<SupportCoordinatorDestinations>()
    val downloadSpeed: Flow<String> = BehaviorStateFlow()
    val uploadSpeed: Flow<String> = BehaviorStateFlow()
    val progressVisibility: Flow<Boolean> = BehaviorStateFlow(false)
    val latestSpeedTest: Flow<String> = BehaviorStateFlow()
    private var recordTypeId: String = ""
    var progressViewFlow = EventFlow<Boolean>()
    var modemRebootStatusFlow = EventFlow<ModemRebootRepository.Companion.RebootState>()

    init {
        initApis()
    }

    private suspend fun checkForRunningSpeedTest() {
        var keepChecking = true
        while (keepChecking) {
            progressVisibility.latestValue = sharedPreferences.getSpeedTestFlag()
            if (progressVisibility.latestValue) {
                latestSpeedTest.latestValue = EMPTY_RESPONSE
                delay(SPEED_TEST_REFRESH_INTERVAL)
            } else {
                keepChecking = false
            }
        }
        val oldDownLoad = sharedPreferences.getSpeedTestDownload()
        val oldUpload = sharedPreferences.getSpeedTestUpload()
        val oldTime = sharedPreferences.getLastSpeedTestTime()
        if (oldDownLoad != null && oldUpload != null && oldTime != null) {
            downloadSpeed.latestValue = oldDownLoad
            uploadSpeed.latestValue = oldUpload
            latestSpeedTest.latestValue = oldTime
        } else {
            displayEmptyResponse()
        }
    }

    fun initApis() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestRecordId()
            requestFaqDetailsInfo()
            checkForRunningSpeedTest()
        }
        modemRebootStatusFlow.latestValue = ModemRebootRepository.Companion.RebootState.READY
        viewModelScope.launch {
            listenToModemRebootStatus()
        }
    }

    fun startSpeedTest() {
        if (!progressVisibility.latestValue) {
            getSpeedTestId()
        }
    }

    private fun getSpeedTestId() {
        progressVisibility.latestValue = true
        latestSpeedTest.latestValue = DashboardViewModel.EMPTY_RESPONSE
        viewModelScope.launch {
            val speedTestRequest = assiaRepository.startSpeedTest()
            if (speedTestRequest.code == 1000) {
                sharedPreferences.saveSupportSpeedTest(boolean = true)
                sharedPreferences.saveSpeedTestId(speedTestId = speedTestRequest.speedTestId)
                checkSpeedTestStatus(requestId = speedTestRequest.speedTestId)
            } else {
                displayEmptyResponse()
            }
        }
    }

    private fun checkSpeedTestStatus(requestId: Int) {
        viewModelScope.launch {
            var keepChecking = true
            var isSuccessful = false
            while (keepChecking) {
                val status = assiaRepository.checkSpeedTestStatus(speedTestId = requestId)
                if (status.code == 1000) {
                    if (status.data.isFinished) {
                        isSuccessful = true
                        keepChecking = false
                    } else {
                        delay(SPEED_TEST_REFRESH_INTERVAL)
                    }
                } else {
                    displayEmptyResponse()
                    keepChecking = false
                    sharedPreferences.saveSupportSpeedTest(false)
                }
            }
            if (isSuccessful) getResults()
        }
    }

    private suspend fun getResults() {
        val upstreamData = assiaRepository.getUpstreamResults()
        if (upstreamData.data.listOfData.isNotEmpty()) {
            val uploadMb = upstreamData.data.listOfData[0].speedAvg / 1000
            uploadSpeed.latestValue = uploadMb.toString()
        } else {
            uploadSpeed.latestValue = DashboardViewModel.EMPTY_RESPONSE
        }

        val downStreamData = assiaRepository.getDownstreamResults()
        if (downStreamData.data.listOfData.isNotEmpty()) {
            val downloadMb = downStreamData.data.listOfData[0].speedAvg / 1000
            downloadSpeed.latestValue = downloadMb.toString()
            latestSpeedTest.latestValue = formatUtcString(downStreamData.data.listOfData[0].timeStamp)
        } else {
            downloadSpeed.latestValue = EMPTY_RESPONSE
            latestSpeedTest.latestValue = EMPTY_RESPONSE
        }
        sharedPreferences.saveSupportSpeedTest(false)
        progressVisibility.latestValue = false
    }

    private fun displayEmptyResponse() {
        downloadSpeed.latestValue = EMPTY_RESPONSE
        uploadSpeed.latestValue = EMPTY_RESPONSE
        latestSpeedTest.latestValue = EMPTY_RESPONSE
        progressVisibility.latestValue = false
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

    companion object {
        const val EMPTY_RESPONSE = "- -"
    }

    data class UiFAQQuestionsSections(
        val questionMap: List<String> = emptyList()
    )
}
