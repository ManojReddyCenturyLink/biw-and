package com.centurylink.biwf.screens.support

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.SupportCoordinatorDestinations
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SupportViewModel @Inject constructor(
    private val faqRepository: FAQRepository,
    private val caseRepository: CaseRepository
) : BaseViewModel() {

    val faqSectionInfo: Flow<UiFAQQuestionsSections> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val myState = EventFlow<SupportCoordinatorDestinations>()
    var recordTypeId: String = ""

    init {
        initApis()
    }

    private fun initApis() {
        viewModelScope.launch {
            requestRecordId()
            requestFaqDetailsInfo()
        }
    }

    private suspend fun requestFaqDetailsInfo() {
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

    private fun updateFaqDetails(faq: Faq) {
        var questionMap: List<String> = faq.records.map { it.sectionC!! }.distinct()
        faqSectionInfo.latestValue = UiFAQQuestionsSections(questionMap)
    }

    fun navigateToFAQList(faqSectionSelected: String) {
        val bundle = Bundle()
        bundle.putString(FAQActivity.FAQ_TITLE, faqSectionSelected)
        SupportCoordinatorDestinations.bundle = bundle
        myState.latestValue = SupportCoordinatorDestinations.FAQ
    }

    fun runSpeedTest() {}

    fun restartModem() {}

    fun launchScheduleCallback() {
        myState.latestValue = SupportCoordinatorDestinations.SCHEDULE_CALLBACK
    }

    data class UiFAQQuestionsSections(
        val questionMap: List<String> = emptyList()
    )
}
