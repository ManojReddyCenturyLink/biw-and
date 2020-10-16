package com.centurylink.biwf.screens.support.schedulecallback

import android.content.Context
import android.os.Bundle
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinatorDestinations
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import javax.inject.Inject

/**
 * Schedule callback view model
 *
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class ScheduleCallbackViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val myState = EventFlow<ScheduleCallbackCoordinatorDestinations>()
    var isExistingUserState: Boolean = false

    //currently we are hard coding data, once api will be there will update its value.
    var progressViewFlow = EventFlow<Boolean>()

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_SCHEDULE_CALLBACK_SUPPORT)
    }

    /**
     * Launch call dialer - It is used to launch caller dialer logic
     *
     */
    fun launchCallDialer() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CALL_US_SCHEDULE_CALLBACK)
        myState.latestValue = ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT
    }

    /**
     * Navigate additional info screen - It will handle navigation to additional info screen logic
     *on select of item
     *
     * @param item - The item selected for navigation
     */
    fun navigateAdditionalInfoScreen(item: TopicList) {
        analyticsManagerInterface.logListItemClickEvent(AnalyticsKeys.LIST_ITEM_SCHEDULE_CALLBACK)
        ScheduleCallbackCoordinatorDestinations.bundle = Bundle().apply {
            putString(AdditionalInfoActivity.ADDITIONAL_INFO, item.topic)
            putBoolean(AdditionalInfoActivity.IS_EXISTING_USER, isExistingUserState)
        }
        myState.latestValue = ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO
    }

    /**
     * Set is existing user state
     *
     * @param isExistingUser - The boolean value to set existing user
     * Its true for existing user and false for non existing user
     */
    fun setIsExistingUserState(isExistingUser: Boolean) {
        isExistingUserState = isExistingUser
    }

    /**
     * Customer care options list - fetches list of customer care options from resources
     *
     * @param context - activiy context
     * @return - returns list of customer care options as List
     */
    fun customerCareOptionsList(context: Context): List<TopicList> {
        val customerCareOptionsList = context.resources.getStringArray(R.array.customer_care_options)
        return customerCareOptionsList.map(::TopicList)
    }

    /**
     * Log back button click - It will handle back button click event logic
     *
     */
    fun logBackButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_SCHEDULE_CALLBACK)
    }

    /**
     * Log cancel button click - It will handle cancel button click event logic
     *
     */
    fun logCancelButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_SCHEDULE_CALLBACK)
    }
}
