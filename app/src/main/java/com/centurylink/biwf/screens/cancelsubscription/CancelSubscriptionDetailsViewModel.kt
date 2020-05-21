package com.centurylink.biwf.screens.cancelsubscription

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class CancelSubscriptionDetailsViewModel @Inject constructor(
    private val caseRepository: CaseRepository
) : BaseViewModel() {

    private var cancellationDate: Date? = null
    private var cancellationReason: String = ""
    private var cancellationReasonExplanation: String = ""
    private var ratingValue: Float? = 0F
    private var cancellationComments: String = ""
    private var caseId: String = ""

    var errorMessageFlow = EventFlow<String>()

    val cancelSubscriptionDateEvent: EventLiveData<Date> = MutableLiveData()

    val performSubmitEvent: EventLiveData<Date> = MutableLiveData()

    val changeDateEvent: EventLiveData<Unit> = MutableLiveData()

    val errorEvents: EventLiveData<String> = MutableLiveData()

    val displayReasonSelectionEvent: EventLiveData<Boolean> = MutableLiveData()

    init {
        initApis()
    }

    private fun initApis() {
        viewModelScope.launch {
            requestCaseId()
        }
    }

    fun onRatingChanged(rating: Float) {
        ratingValue = rating
    }

    fun onCancellationReason(cancellationReasonData: String) {
        cancellationReason = cancellationReasonData
        if (cancellationReasonData.equals("Other", true)) {
            displayReasonSelectionEvent.emit(true)
        } else {
            displayReasonSelectionEvent.emit(false)
        }
    }

    fun onCancellationCommentsChanged(cancellationCommentsData: String) {
        cancellationComments = cancellationCommentsData
    }

    fun onCancellationDateSelected(cancellationDateInfo: Date) {
        cancellationDate = cancellationDateInfo
        cancelSubscriptionDateEvent.emit(cancellationDate!!)
    }

    fun onOtherCancellationChanged(commentsOnOthers: String) {
        cancellationReasonExplanation = commentsOnOthers
    }

    fun onDateChange() {
        changeDateEvent.emit(Unit)
    }

    fun onSubmitCancellation() {
        if (cancellationDate == null) {
            errorEvents.emit("Error")
        } else {
            performSubmitEvent.emit(cancellationDate!!)
        }
    }

    suspend fun requestCaseId() {
        val caseDetails = caseRepository.getCaseId()
        caseDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            caseId = it.caseRecentItems[0].Id ?: ""
        }
    }

    suspend fun performCancel() {
        val caseDetails = caseRepository.createDeactivationRequest(
            cancellationDate!!,
            cancellationReason,
            cancellationReasonExplanation,
            ratingValue!!,
            cancellationComments, caseId
        )
        errorMessageFlow.latestValue = caseDetails
    }

    fun performCancellationRequest() {
        viewModelScope.launch {
            performCancel()
        }
    }
}