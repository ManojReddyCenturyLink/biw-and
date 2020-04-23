package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinator
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class ScheduleCallbackViewModel @Inject constructor(
) : BaseViewModel() {

    val myState = ObservableData(ScheduleCallbackCoordinator.ScheduleCallbackCoordinatorDestinations.SCHEDULE_CALLBACK)
    val topicList: MutableList<TopicList> = dummyList()

    fun launchCallDialer() {
        myState.value = ScheduleCallbackCoordinator.ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT
    }

    private fun dummyList(): MutableList<TopicList> {
        return mutableListOf(
            TopicList( "I want to know more about fiber internet service"),
            TopicList( "I’m having trouble signing up for fiber internet service"),
            TopicList( "I can’t sign into my account"),
            TopicList( "I have questions about my account"),
            TopicList( "I need something not listed here")
        )
    }
}