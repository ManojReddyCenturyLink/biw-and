package com.centurylink.biwf.screens.support.schedulecallback

import android.os.Bundle
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.AdditionalInfoCoordinatorDestinations
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import javax.inject.Inject

class AdditionalInfoViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val myState = EventFlow<AdditionalInfoCoordinatorDestinations>()

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

    fun launchContactInfo(isExistingUser: Boolean, customerCareOption: String, additionalInfo: String) {
        val bundle = Bundle()
        bundle.putBoolean(ContactInfoActivity.IS_EXISTING_USER, isExistingUser)
        bundle.putString(ContactInfoActivity.CUSTOMER_CARE_OPTION, customerCareOption)
        bundle.putString(ContactInfoActivity.ADDITIONAL_INFO, additionalInfo)
        AdditionalInfoCoordinatorDestinations.bundle = bundle
        myState.latestValue = AdditionalInfoCoordinatorDestinations.CONTACT_INFO
    }
}