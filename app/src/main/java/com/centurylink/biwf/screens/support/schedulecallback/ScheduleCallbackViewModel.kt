package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinator
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class ScheduleCallbackViewModel @Inject constructor(
) : BaseViewModel() {

    val myState = ObservableData(ScheduleCallbackCoordinator.ScheduleCallbackCoordinatorDestinations.SCHEDULE_CALLBACK)
    val topicList: List<TopicList> = dummyList()

    fun launchCallDialer() {
        myState.value = ScheduleCallbackCoordinator.ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT
    }

    private fun dummyList(): List<TopicList> {
        return listOf(
            "I want to know more about fiber internet service",
            "I’m having trouble signing up for fiber internet service",
            "I can’t sign into my account",
            "I have questions about my account",
            "I need something not listed here"
        ).map (::TopicList)
    }
}