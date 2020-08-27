package com.centurylink.biwf.screens.support

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SupportCoordinatorDestinations
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.screens.home.dashboard.DashboardViewModel
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SupportViewModel @Inject constructor(
    private val faqRepository: FAQRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    private val assiaRepository: AssiaRepository,
    private val sharedPreferences: Preferences,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val faqSectionInfo: Flow<UiFAQQuestionsSections> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val myState = EventFlow<SupportCoordinatorDestinations>()
    val downloadSpeed: Flow<String> = BehaviorStateFlow()
    val uploadSpeed: Flow<String> = BehaviorStateFlow()
    val progressVisibility: Flow<Boolean> = BehaviorStateFlow(false)
    val latestSpeedTest: Flow<String> = BehaviorStateFlow()
    val speedTestButtonState: Flow<Boolean> = BehaviorStateFlow()
    val modemResetButtonState: Flow<Boolean> = BehaviorStateFlow()
    private var recordTypeId: String = ""
    var progressViewFlow = EventFlow<Boolean>()

    private var rebootOngoing = false

    init {
        initApis()
    }

    private suspend fun checkForRunningSpeedTest() {
        var keepChecking = true
        while (keepChecking) {
            progressVisibility.latestValue = sharedPreferences.getSpeedTestFlag()
            speedTestButtonState.latestValue = !progressVisibility.latestValue
            modemResetButtonState.latestValue = !progressVisibility.latestValue
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
        if (oldDownLoad?.isNotEmpty()!! && oldUpload?.isNotEmpty()!! && oldTime?.isNotEmpty()!!) {
            downloadSpeed.latestValue = oldDownLoad
            uploadSpeed.latestValue = oldUpload
            latestSpeedTest.latestValue = oldTime
        } else {
            displayEmptyResponse()
        }
    }

    fun initApis() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_SUPPORT)
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestRecordId()
            requestFaqDetailsInfo()
            checkForRunningSpeedTest()
        }
    }

    override suspend fun handleRebootStatus(status: ModemRebootMonitorService.RebootState) {
        super.handleRebootStatus(status)
        rebootOngoing = status == ModemRebootMonitorService.RebootState.ONGOING
        if (rebootOngoing) {
            speedTestButtonState?.latestValue = false
        } else {
            if (status == ModemRebootMonitorService.RebootState.SUCCESS) speedTestButtonState?.latestValue =
                true
        }
    }

    fun startSpeedTest() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_RUN_SPEED_TEST_SUPPORT)
        if (!progressVisibility.latestValue && !rebootOngoing) {
            getSpeedTestId()
        }
    }

    private fun getSpeedTestId() {
        progressVisibility.latestValue = true
        speedTestButtonState.latestValue = false
        modemResetButtonState.latestValue = false
        latestSpeedTest.latestValue = DashboardViewModel.EMPTY_RESPONSE
        viewModelScope.launch {
            when (val speedTestRequest = assiaRepository.startSpeedTest()) {
                is AssiaNetworkResponse.Success -> {
                    analyticsManagerInterface.logApiCall(AnalyticsKeys.START_SPEED_TEST_SUCCESS)
                    if (speedTestRequest.body.code == 1000) {
                        sharedPreferences.saveSupportSpeedTest(boolean = true)
                        sharedPreferences.saveSpeedTestId(speedTestId = speedTestRequest.body.speedTestId)
                        checkSpeedTestStatus(requestId = speedTestRequest.body.speedTestId)
                    } else {
                        displayEmptyResponse()
                    }
                }
                else -> {
                    analyticsManagerInterface.logApiCall(AnalyticsKeys.START_SPEED_TEST_FAILURE)
                    displayEmptyResponse()
                }
            }
        }
    }

    private fun checkSpeedTestStatus(requestId: Int) {
        viewModelScope.launch {
            var keepChecking = true
            var isSuccessful = false
            while (keepChecking) {
                when (val status = assiaRepository.checkSpeedTestStatus(speedTestId = requestId)) {
                    is AssiaNetworkResponse.Success -> {
                        analyticsManagerInterface.logApiCall(AnalyticsKeys.CHECK_SPEED_TEST_SUCCESS)
                        if (status.body.code == 1000) {
                            if (status.body.data.isFinished) {
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
                    else -> {
                        analyticsManagerInterface.logApiCall(AnalyticsKeys.CHECK_SPEED_TEST_FAILURE)
                        displayEmptyResponse()
                        keepChecking = false
                        sharedPreferences.saveSupportSpeedTest(false)
                    }
                }
            }
            if (isSuccessful) getResults()
        }
    }

    private suspend fun getResults() {
        when (val upstreamData = assiaRepository.getUpstreamResults()) {
            is AssiaNetworkResponse.Success -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_UPSTREAM_RESULTS_SUCCESS)
                if (upstreamData.body.data.listOfData.isNotEmpty()) {
                    val uploadMb = upstreamData.body.data.listOfData[0].speedAvg / 1000
                    uploadSpeed.latestValue = uploadMb.toString()
                } else {
                    uploadSpeed.latestValue = DashboardViewModel.EMPTY_RESPONSE
                }
            }
            else -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_UPSTREAM_RESULTS_FAILURE)
                uploadSpeed.latestValue = DashboardViewModel.EMPTY_RESPONSE
            }
        }

        when (val downStreamData = assiaRepository.getDownstreamResults()) {
            is AssiaNetworkResponse.Success -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DOWNSTREAM_RESULT_SUCCESS)
                if (downStreamData.body.data.listOfData.isNotEmpty()) {
                    val downloadMb = downStreamData.body.data.listOfData[0].speedAvg / 1000
                    downloadSpeed.latestValue = downloadMb.toString()
                    latestSpeedTest.latestValue =
                        formatUtcString(downStreamData.body.data.listOfData[0].timeStamp)
                } else {
                    downloadSpeed.latestValue = EMPTY_RESPONSE
                    latestSpeedTest.latestValue = EMPTY_RESPONSE
                }
            }
            else -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DOWNSTREAM_RESULT_FAILURE)
                downloadSpeed.latestValue = EMPTY_RESPONSE
                latestSpeedTest.latestValue = EMPTY_RESPONSE
            }
        }

        sharedPreferences.saveSupportSpeedTest(false)
        progressVisibility.latestValue = false
        speedTestButtonState.latestValue = true
        modemResetButtonState.latestValue = true
    }

    private fun displayEmptyResponse() {
        downloadSpeed.latestValue = EMPTY_RESPONSE
        uploadSpeed.latestValue = EMPTY_RESPONSE
        latestSpeedTest.latestValue = EMPTY_RESPONSE
        progressVisibility.latestValue = false
        speedTestButtonState.latestValue = true
        modemResetButtonState.latestValue = true
    }

    private suspend fun requestFaqDetailsInfo() {
        progressViewFlow.latestValue = true
        val faqDetails = faqRepository.getFAQQuestionDetails(recordTypeId)
        faqDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_FAQ_QUESTION_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_FAQ_QUESTION_DETAILS_SUCCESS)
            updateFaqDetails(it)
            progressViewFlow.latestValue = false
        }
    }

    private suspend fun requestRecordId() {
        val subscriptionDate = faqRepository.getKnowledgeRecordTypeId()
        subscriptionDate.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_RECORD_TYPE_ID_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_RECORD_TYPE_ID_SUCCESS)
            recordTypeId = it
            progressViewFlow.latestValue = false
        }
    }

    private fun updateFaqDetails(faq: Faq) {
        val questionMap: List<String> = faq.records.mapNotNull { it.sectionC }.distinct()
        faqSectionInfo.latestValue = UiFAQQuestionsSections(questionMap)
    }

    fun navigateToFAQList(faqSectionSelected: String) {
        analyticsManagerInterface.logListItemClickEvent(AnalyticsKeys.FAQ_ITEM_SUPPORT)
        val bundle = Bundle()
        bundle.putString(FAQActivity.FAQ_TITLE, faqSectionSelected)
        SupportCoordinatorDestinations.bundle = bundle
        myState.latestValue = SupportCoordinatorDestinations.FAQ
    }

    fun launchScheduleCallback() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.SCHEDULE_A_CALLBACK_SUPPORT)
        myState.latestValue = SupportCoordinatorDestinations.SCHEDULE_CALLBACK
    }

    fun logDoneButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_SUPPORT)
    }

//    fun logVisitWebsite() {
//        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_VISIT_WEBSITE_SUPPORT)
//    }

    fun logLiveChatLaunch() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.LIVE_CHAT_SUPPORT)
    }

    companion object {
        const val EMPTY_RESPONSE = "- -"
    }

    data class UiFAQQuestionsSections(
        val questionMap: List<String> = emptyList()
    )
}
