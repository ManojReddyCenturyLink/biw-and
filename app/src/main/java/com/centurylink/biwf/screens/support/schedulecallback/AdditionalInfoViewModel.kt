package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import javax.inject.Inject

class AdditionalInfoViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    private val analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_ADDITIONAL_INFO)
    }

    fun logBackButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_ADDITIONAL_INFO)
    }

    fun logCancelButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_ADDITIONAL_INFO)
    }

    fun logNextButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_NEXT_ADDITIONAL_INFO)
    }
}