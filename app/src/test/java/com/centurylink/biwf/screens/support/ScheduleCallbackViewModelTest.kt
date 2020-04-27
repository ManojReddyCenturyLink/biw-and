package com.centurylink.biwf.screens.support

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinator
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class ScheduleCallbackViewModelTest : ViewModelBaseTest() {

    private val dummyList = listOf(
        "I want to know more about fiber internet service",
        "I’m having trouble signing up for fiber internet service",
        "I can’t sign into my account",
        "I have questions about my account",
        "I need something not listed here"
    ).map (::TopicList)

    private lateinit var viewModel: ScheduleCallbackViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        viewModel = ScheduleCallbackViewModel()
    }

    @Test
    fun onCallUSClicked_navigateToPhoneDiallerScreen() {
        viewModel.launchCallDialer()
        Assert.assertEquals(
            "Dialler Screen wasn't Launched",
            ScheduleCallbackCoordinator.ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT,
            viewModel.myState.value
        )
    }

    @Test
    fun onItemClicked_navigateToAdditionalInfoActivity() {
        viewModel.navigateAdditionalInfoScreen(dummyList.get(0))
        Assert.assertEquals(
            "AdditionalInfo Activity wasn't Launched",
            ScheduleCallbackCoordinator.ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO,
            viewModel.myState.value
        )
    }
}