package com.centurylink.biwf.screens.support.schedulecallback

import android.os.Bundle
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinatorDestinations
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import javax.inject.Inject

class ScheduleCallbackViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val myState = EventFlow<ScheduleCallbackCoordinatorDestinations>()
    val topicList: List<TopicList> = dummyList()

    //currently we are hard coding data, once api will be there will update its value.
    var progressViewFlow = EventFlow<Boolean>()

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_SCHEDULE_CALLBACK_SUPPORT)
    }

    fun launchCallDialer() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CALL_US_SCHEDULE_CALLBACK)
        myState.latestValue = ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT
    }

    fun navigateAdditionalInfoScreen(item: TopicList) {
        analyticsManagerInterface.logListItemClickEvent(AnalyticsKeys.LIST_ITEM_SCHEDULE_CALLBACK)
        ScheduleCallbackCoordinatorDestinations.bundle = Bundle().apply {
            putString(AdditionalInfoActivity.ADDITIONAL_INFO, item.topic)
        }
        myState.latestValue = ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO
    }

    private fun dummyList(): List<TopicList> {
        return listOf(
            "I want to know more about fiber internet service",
            "I’m having trouble signing up for fiber internet service",
            "I can’t sign into my account",
            "I have questions about my account",
            "I need something not listed here"
        ).map(::TopicList)
    }

    fun logBackButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_SCHEDULE_CALLBACK)
    }

    fun logCancelButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_SCHEDULE_CALLBACK)
    }
}
