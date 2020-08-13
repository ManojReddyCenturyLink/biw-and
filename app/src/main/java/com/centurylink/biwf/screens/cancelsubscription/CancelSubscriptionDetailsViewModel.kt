package com.centurylink.biwf.screens.cancelsubscription

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class CancelSubscriptionDetailsViewModel @Inject constructor(
    private val caseRepository: CaseRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    private val analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService) {

    private var cancellationDate: Date? = null
    private var cancellationReason: String = ""
    private var cancellationReasonExplanation: String = ""
    private var ratingValue: Float? = 0F
    private var cancellationComments: String = ""
    private var recordTypeId: String = ""
    var errorMessageFlow = EventFlow<String>()
    var successDeactivation = EventFlow<Boolean>()
    val cancelSubscriptionDateEvent: EventLiveData<Date> = MutableLiveData()
    val performSubmitEvent: EventLiveData<Date> = MutableLiveData()
    val changeDateEvent: EventLiveData<Unit> = MutableLiveData()
    val errorEvents: EventLiveData<String> = MutableLiveData()
    val displayReasonSelectionEvent: EventLiveData<Boolean> = MutableLiveData()
    var progressViewFlow = EventFlow<Boolean>()

    fun onRatingChanged(rating: Float) {
        ratingValue = rating
    }

    init {
        initApis()
    }

    fun initApis() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_CANCEL_SUBSCRIPTION_DETAILS)
        viewModelScope.launch {
            requestRecordId()
        }
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

    private suspend fun performCancel() {
        progressViewFlow.latestValue = true
        val caseDetails = caseRepository.createDeactivationRequest(
            cancellationDate!!,
            cancellationReason,
            cancellationReasonExplanation,
            ratingValue!!,
            cancellationComments,
            recordTypeId
        )
        caseDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.RECORD_TYPE_ID_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.RECORD_TYPE_ID_SUCCESS)
            progressViewFlow.latestValue = false
            successDeactivation.latestValue = it.success
        }
    }

    private suspend fun requestRecordId() {
        val subscriptionDate = caseRepository.getRecordTypeId()
        subscriptionDate.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.POST_CASE_FOR_SUBSCRIPTION_SUCCESS)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.POST_CASE_FOR_SUBSCRIPTION_FAILURE)
            recordTypeId = it
        }
    }

    fun performCancellationRequest() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_CANCEL_SUBSCRIPTION_CANCEL_SERVICE)
        viewModelScope.launch {
            performCancel()
        }
    }

    fun discardCancellationRequest() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_CANCEL_SUBSCRIPTION_KEEP_SERVICE)
    }

    fun logSubmitButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_SUBMIT_CANCEL_SUBSCRIPTION_CONFIRMATION)
    }

    fun logBackPress() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_CANCEL_SUBSCRIPTION_CONFIRMATION)
    }

    fun logCancelPress() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_CANCEL_SUBSCRIPTION_CONFIRMATION)
    }
}