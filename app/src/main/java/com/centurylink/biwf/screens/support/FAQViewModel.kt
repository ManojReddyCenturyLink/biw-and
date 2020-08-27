package com.centurylink.biwf.screens.support

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.FAQCoordinatorDestinations
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class FAQViewModel @Inject constructor(
    private val faqRepository: FAQRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val faqDetailsInfo: Flow<UiFAQQuestionsDetails> = BehaviorStateFlow()
    val myState = EventFlow<FAQCoordinatorDestinations>()
    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    private var sectionSelected: String = ""
    private var recordTypeId: String = ""

    init {
        initApis()
    }

    fun setFilteredSelection(selectedSection: String) {
        sectionSelected = selectedSection
    }

    fun initApis() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_FAQ)
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestRecordId()
            requestFaqDetails()
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
        }
    }

    private suspend fun requestFaqDetails() {
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

    private fun updateFaqDetails(faq: Faq) {
        val questionMap =
            faq.records.filter { !it.sectionC.isNullOrEmpty() && it.sectionC.equals(sectionSelected, true) }
                .associateTo(HashMap(), { it.title!! to it.articleContent!! })
        faqDetailsInfo.latestValue = UiFAQQuestionsDetails(questionMap)
    }

    fun navigateToScheduleCallback() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_SCHEDULE_A_CALLBACK_FAQ_DETAILS)
        myState.latestValue = FAQCoordinatorDestinations.SCHEDULE_CALLBACK
    }

    fun logBackButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_FAQ_DETAILS)
    }

    fun logDoneButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_FAQ_DETAILS)
    }

    fun logLiveChatLaunch() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_LIVE_CHAT_FAQ_DETAILS)
    }

    fun logItemExpanded() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.EXPAND_LIST_FAQ_DETAILS)
    }

    fun logItemCollapsed() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.COLLAPSE_LIST_FAQ_DETAILS)
    }

    data class UiFAQQuestionsDetails(
        val questionMap: HashMap<String, String> = HashMap()
    )
}
