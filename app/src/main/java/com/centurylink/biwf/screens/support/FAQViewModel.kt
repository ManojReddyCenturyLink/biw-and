package com.centurylink.biwf.screens.support

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.FAQCoordinatorDestinations
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class FAQViewModel @Inject constructor(
    private val faqRepository: FAQRepository
) : BaseViewModel() {

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

    private fun initApis() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestRecordId()
            requestFaqDetails()
        }
    }

    private suspend fun requestRecordId() {
        val subscriptionDate = faqRepository.getKnowledgeRecordTypeId()
        subscriptionDate.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            recordTypeId = it
        }
    }

    private suspend fun requestFaqDetails() {
        val faqDetails = faqRepository.getFAQQuestionDetails(recordTypeId)
        faqDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateFaqDetails(it)
            progressViewFlow.latestValue = false
        }
    }

    private fun updateFaqDetails(faq: Faq) {
        val questionMap =
            faq.records.filter { it.sectionC!!.equals(sectionSelected, true) }
                .associateTo(HashMap(), { it.title!! to it.articleContent!! })
        faqDetailsInfo.latestValue = UiFAQQuestionsDetails(questionMap)
    }

    fun navigateToScheduleCallback() {
        myState.latestValue = FAQCoordinatorDestinations.SCHEDULE_CALLBACK
    }

    data class UiFAQQuestionsDetails(
        val questionMap: HashMap<String, String> = HashMap()
    )
}
