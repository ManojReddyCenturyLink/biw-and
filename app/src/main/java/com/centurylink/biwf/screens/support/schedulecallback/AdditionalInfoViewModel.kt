package com.centurylink.biwf.screens.support.schedulecallback

import android.os.Bundle
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.AdditionalInfoCoordinatorDestinations
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import javax.inject.Inject

/**
 * Additional info view model
 *
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class AdditionalInfoViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val myState = EventFlow<AdditionalInfoCoordinatorDestinations>()

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_ADDITIONAL_INFO)
    }

    /**
     * Log back button click - It will handle back button click event logic
     *
     */
    fun logBackButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_ADDITIONAL_INFO)
    }

    /**
     * Log cancel button click - It will handle cancel button click event logic
     *
     */
    fun logCancelButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_ADDITIONAL_INFO)
    }

    /**
     * Log next button click - It will handle next button click event logic
     *
     */
    fun logNextButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_NEXT_ADDITIONAL_INFO)
    }

    /**
     * Launch contact info - It will launch contact info screen from additional screen by passing
     * bundle
     *
     * @param isExistingUser - The boolean value to handle contact info screen for existing and
     * non existing users
     * @param customerCareOption - The customer care option
     * @param additionalInfo - The additional information to be added
     */
    fun launchContactInfo(isExistingUser: Boolean, customerCareOption: String, additionalInfo: String) {
        val bundle = Bundle()
        bundle.putBoolean(ContactInfoActivity.IS_EXISTING_USER, isExistingUser)
        bundle.putString(ContactInfoActivity.CUSTOMER_CARE_OPTION, customerCareOption)
        bundle.putString(ContactInfoActivity.ADDITIONAL_INFO, additionalInfo)
        AdditionalInfoCoordinatorDestinations.bundle = bundle
        myState.latestValue = AdditionalInfoCoordinatorDestinations.CONTACT_INFO
    }
}