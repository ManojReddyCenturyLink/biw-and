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
    private val faqRepository: FAQRepository,
    private val caseRepository: CaseRepository
) : BaseViewModel() {

    val faqDetailsInfo: Flow<UiFAQQuestionsDetails> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val myState = EventFlow<FAQCoordinatorDestinations>()
    var sectionSelected: String = ""
    var recordTypeId: String = ""

    init {
        initApis()
    }

    fun setFilteredSelection(selectedSection: String) {
        sectionSelected = selectedSection
    }

    fun initApis() {
        viewModelScope.launch {
            requestRecordId()
            requestFaqDetails()
        }
    }

    private suspend fun requestFaqDetails() {
        val faqDetails = faqRepository.getFAQQuestionDetails(recordTypeId)
        faqDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateFaqDetails(it)
        }
    }

    private suspend fun requestRecordId() {
        val subscriptionDate = caseRepository.getRecordTypeId()
        subscriptionDate.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            recordTypeId = it
        }
    }

    fun updateFaqDetails(faq: Faq) {
        val questionMap =
            faq.records.filter { it.sectionC!!.equals(sectionSelected, true) }.toList()
                .associateTo(HashMap(), { it.title!! to it.articleContent!! })
        val uifaqQuestionDetails = UiFAQQuestionsDetails(questionMap)
        faqDetailsInfo.latestValue = uifaqQuestionDetails
    }

    fun navigateToScheduleCallback() {
        myState.latestValue = FAQCoordinatorDestinations.SCHEDULE_CALLBACK
    }

    data class UiFAQQuestionsDetails(
        val questionMap: HashMap<String, String> = HashMap<String, String>()
    )
}
