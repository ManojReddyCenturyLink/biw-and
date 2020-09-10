package com.centurylink.biwf.screens.subscription

import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import javax.inject.Inject

class EditPaymentDetailsViewModel @Inject constructor(
    preferences: Preferences,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val progressViewFlow = EventFlow<Boolean>()
    val errorMessageFlow = EventFlow<String>()
    val subscriptionUrlFlow = EventFlow<String>()

    private val subscriptionUrl = BASE_SUBSCRIPTION_URL + preferences.getValueByID(Preferences.USER_ID)

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_EDIT_PAYMENT_DETAILS)
        progressViewFlow.latestValue = true
        subscriptionUrlFlow.latestValue = subscriptionUrl
    }

    fun logBackPress(){
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_EDIT_PAYMENT_DETAILS)
    }

    fun logDonePress(){
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_EDIT_PAYMENT_DETAILS)
    }

    // TODO address race condition going on between this method and onWebViewError()
    fun onWebViewProgress(progress: Int) {
        if (progress == WEB_PAGE_PROGRESS_COMPLETE) {
            progressViewFlow.latestValue = false
        }
    }

    fun onWebViewError() {
        errorMessageFlow.latestValue = GENERIC_WEB_VIEW_ERROR
    }

    fun onRetryClicked() {
        subscriptionUrlFlow.latestValue = subscriptionUrl
    }

    companion object {
        const val BASE_SUBSCRIPTION_URL = "https://qa-qa101.cs16.force.com/phish/apex/vf_fiberBuyFlowPaymentMobile?userId="
        const val WEB_PAGE_PROGRESS_COMPLETE = 100
        const val GENERIC_WEB_VIEW_ERROR = "Generic Web View Error"
    }
}
