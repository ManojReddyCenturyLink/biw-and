package com.centurylink.biwf.screens.cancelsubscription

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinatorDestinations
import com.centurylink.biwf.repos.ZouraSubscriptionRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class CancelSubscriptionViewModel @Inject constructor(
    private val zuoraSubscriptionRepository: ZouraSubscriptionRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    private val analyticsManagerInterface :AnalyticsManager
) : BaseViewModel(modemRebootMonitorService) {

    val cancelSubscriptionDate: Flow<UiCancelSubscriptionDetails> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    val myState = EventFlow<CancelSubscriptionCoordinatorDestinations>()
    var progressViewFlow = EventFlow<Boolean>()

    init {
        initApis()
    }

    fun initApis() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_CANCEL_SUBSCRIPTION)
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestSubscriptionDate()
        }
    }

    private suspend fun requestSubscriptionDate() {
        val subscriptionDate = zuoraSubscriptionRepository.getSubscriptionDate()
        subscriptionDate.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_SUBSCRIPTION_DATE_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_SUBSCRIPTION_DATE_SUCCESS)
            cancelSubscriptionDate.latestValue = UiCancelSubscriptionDetails(
                subscriptionEndDate = DateUtils.toSimpleString(it, DateUtils.STANDARD_FORMAT)
            )
            progressViewFlow.latestValue = false
        }
    }

    fun onNavigateToCancelSubscriptionDetails() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CONTINUE_CANCELLATION)
        myState.latestValue =
            CancelSubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION
    }

    fun logCancelPress() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_CANCEL_SUBSCRIPTION)
    }

    fun logBackPress() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_CANCEL_SUBSCRIPTION)
    }

    data class UiCancelSubscriptionDetails(
        val subscriptionEndDate: String? = null
    )
}
