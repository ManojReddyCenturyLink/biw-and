package com.centurylink.biwf.screens.support

import android.os.Bundle
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SupportCoordinatorDestinations
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.SpeedTestRepository
import com.centurylink.biwf.screens.home.SpeedTestUtils
import com.centurylink.biwf.screens.home.dashboard.DashboardViewModel
import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackActivity
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.LiveChatUtil
import com.centurylink.biwf.utility.preferences.Preferences
import com.salesforce.android.chat.ui.ChatUIConfiguration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SupportViewModel @Inject constructor(
    private val faqRepository: FAQRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    // private val assiaRepository: AssiaRepository,
    private val oAuthAssiaRepository: OAuthAssiaRepository,
    private val speedTestRepository: SpeedTestRepository,
    private val sharedPreferences: Preferences,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    val faqSectionInfo: Flow<UiFAQQuestionsSections> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val myState = EventFlow<SupportCoordinatorDestinations>()
    val downloadSpeed: Flow<String> = BehaviorStateFlow()
    val uploadSpeed: Flow<String> = BehaviorStateFlow()
    val networkStatus: BehaviorStateFlow<Boolean> = BehaviorStateFlow()
    val progressVisibility: Flow<Boolean> = BehaviorStateFlow(false)
    val latestSpeedTest: Flow<String> = BehaviorStateFlow()
    private val speedTestButtonState: Flow<Boolean> = BehaviorStateFlow()
    val modemResetButtonState: Flow<Boolean> = BehaviorStateFlow()
    private var recordTypeId: String = ""
    var progressViewFlow = EventFlow<Boolean>()
    var speedTestError = EventFlow<Boolean>()
    private var rebootOngoing = false
    private var existingUserState = false

    init {
        initApis()
        initModemStatusRefresh()
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
        if (oldDownLoad?.isDigitsOnly()!! && oldUpload?.isDigitsOnly()!! && oldTime?.isNotEmpty()!!) {
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
            if (SpeedTestUtils.isSpeedTestAvailable()) {
                checkForRunningSpeedTest()
            }
        }
    }

    override suspend fun handleRebootStatus(status: ModemRebootMonitorService.RebootState) {
        super.handleRebootStatus(status)
        rebootOngoing = status == ModemRebootMonitorService.RebootState.ONGOING
        if (rebootOngoing) {
            speedTestButtonState?.latestValue = false
        } else {
            if (status == ModemRebootMonitorService.RebootState.SUCCESS)
                speedTestButtonState?.latestValue = true
        }
    }

    fun startSpeedTest() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_RUN_SPEED_TEST_SUPPORT)
        if (!progressVisibility.latestValue && !rebootOngoing) {
            getSpeedTestId()
        }
    }

    private fun initModemStatusRefresh() {
        viewModelScope.interval(0, MODEM_STATUS_REFRESH_INTERVAL) {
            requestModemInfo()
        }
    }

    private suspend fun requestModemInfo() {
        val modemInfo = oAuthAssiaRepository.getModemInfo()
        modemInfo.fold(ifRight = {
            val apiInfo = it.apInfoList
            if (!apiInfo.isNullOrEmpty() && apiInfo[0].isRootAp) {
                networkStatus.latestValue = apiInfo[0].isAlive
            } else {
                networkStatus.latestValue = false
            }
        },
            ifLeft = {
                // Ignoring Error API called every 30 seconds
                // errorMessageFlow.latestValue = modemInfo.toString()
            }
        )
    }

    private fun getSpeedTestId() {
        progressVisibility.latestValue = true
        speedTestButtonState.latestValue = false
        sharedPreferences.saveSpeedTestFlag(boolean = true)
        modemResetButtonState.latestValue = false
        latestSpeedTest.latestValue = EMPTY_RESPONSE
        viewModelScope.launch {
            val speedTestRequest = speedTestRepository.startSpeedTest()
            speedTestRequest.fold(
                ifRight = {
                    analyticsManagerInterface.logApiCall(AnalyticsKeys.START_SPEED_TEST_SUCCESS)
                    sharedPreferences.saveSupportSpeedTest(boolean = true)
                    sharedPreferences.saveSpeedTestId(speedTestId = it.speedTestId)
                    checkSpeedTestStatus(requestId = it.speedTestId)
                },
                ifLeft = {
                    analyticsManagerInterface.logApiCall(AnalyticsKeys.START_SPEED_TEST_FAILURE)
                    displayEmptyResponse()
                }
            )
        }
    }

    private fun checkSpeedTestStatus(requestId: String) {
        viewModelScope.launch {
            var keepChecking = true
            var isSuccessful = false
            while (keepChecking) {
                val status = speedTestRepository.checkSpeedTestStatus(speedTestId = requestId)
                status.fold(ifRight =
                {
                    if (it.data.isFinished) {
                        analyticsManagerInterface.logApiCall(AnalyticsKeys.CHECK_SPEED_TEST_SUCCESS)
                        isSuccessful = true
                        keepChecking = false
                    } else {
                        delay(SPEED_TEST_REFRESH_INTERVAL)
                    }
                },
                    ifLeft = {
                        analyticsManagerInterface.logApiCall(AnalyticsKeys.CHECK_SPEED_TEST_FAILURE)
                        displayEmptyResponse()
                        keepChecking = false
                        sharedPreferences.saveSupportSpeedTest(false)
                    }
                )
            }
            if (isSuccessful) getResults()
        }
    }

    private suspend fun getResults() {
        var uploadSpeedError = false
        var downloadSpeedError = false
        val result =
            speedTestRepository.getSpeedTestResults(sharedPreferences.getSpeedTestId()!!)
        result.fold(
            ifRight = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_UPSTREAM_RESULTS_SUCCESS)
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DOWNSTREAM_RESULT_SUCCESS)
                val uploadStreamData = it.uploadSpeedSummary.speedTestNestedResults
                val downloadStreamData = it.downloadSpeedSummary.speedTestNestedResults
                if (uploadStreamData.list!!.isNotEmpty() && uploadStreamData.list.toString() != DashboardViewModel.EMPTY_RESPONSE
                ) {
                    val uploadMb = uploadStreamData.list[0].average / 1000
                    uploadSpeed.latestValue = uploadMb.toString()
                    sharedPreferences.saveSpeedTestUpload(uploadSpeed = uploadSpeed.latestValue)
                } else {
                    uploadSpeed.latestValue = DashboardViewModel.EMPTY_RESPONSE
                    uploadSpeedError = true
                }
                if (downloadStreamData.list!!.isNotEmpty() && downloadStreamData.toString() != DashboardViewModel.EMPTY_RESPONSE
                ) {
                    val downloadMb = downloadStreamData.list[0].average / 1000
                    downloadSpeed.latestValue = downloadMb.toString()
                    latestSpeedTest.latestValue =
                        formatUtcString(downloadStreamData.list[0].timestamp)
                    sharedPreferences.saveSpeedTestDownload(downloadSpeed = downloadSpeed.latestValue)
                    sharedPreferences.saveLastSpeedTestTime(lastRanTime = latestSpeedTest.latestValue)
                } else {
                    downloadSpeed.latestValue = DashboardViewModel.EMPTY_RESPONSE
                    latestSpeedTest.latestValue = DashboardViewModel.EMPTY_RESPONSE
                    downloadSpeedError = true
                }
            },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_UPSTREAM_RESULTS_FAILURE)
                uploadSpeed.latestValue = EMPTY_RESPONSE
                uploadSpeedError = true
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DOWNSTREAM_RESULT_FAILURE)
                downloadSpeed.latestValue = EMPTY_RESPONSE
                latestSpeedTest.latestValue = EMPTY_RESPONSE
                downloadSpeedError = true
            }
        )
        if (uploadSpeedError && downloadSpeedError) {
            speedTestError.latestValue = true
        }
        sharedPreferences.saveSupportSpeedTest(false)
        progressVisibility.latestValue = false
        speedTestButtonState.latestValue = true
        modemResetButtonState.latestValue = true
    }

    private fun displayEmptyResponse() {
        speedTestError.latestValue = true
        downloadSpeed.latestValue = EMPTY_RESPONSE
        uploadSpeed.latestValue = EMPTY_RESPONSE
        latestSpeedTest.latestValue = EMPTY_RESPONSE
        progressVisibility.latestValue = false
        speedTestButtonState.latestValue = true
        modemResetButtonState.latestValue = true
        sharedPreferences.saveSupportSpeedTest(false)
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
        bundle.putBoolean(FAQActivity.IS_EXISTING_USER, existingUserState)
        SupportCoordinatorDestinations.bundle = bundle
        myState.latestValue = SupportCoordinatorDestinations.FAQ
    }

    fun setExistingUserState(isExistingUser: Boolean) {
        existingUserState = isExistingUser
    }

    fun launchScheduleCallback(isExistingUser: Boolean) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.SCHEDULE_A_CALLBACK_SUPPORT)
        val bundle = Bundle()
        bundle.putBoolean(ScheduleCallbackActivity.IS_EXISTING_USER, isExistingUser)
        SupportCoordinatorDestinations.bundle = bundle
        myState.latestValue = SupportCoordinatorDestinations.SCHEDULE_CALLBACK
    }

    fun logDoneButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_SUPPORT)
    }

    fun getLiveChatUIConfiguration(): ChatUIConfiguration {
        return LiveChatUtil.getLiveChatUIConfiguration(sharedPreferences)
    }

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
