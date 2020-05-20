package com.centurylink.biwf.screens.support

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinatorDestinations
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

@Suppress("EXPERIMENTAL_API_USAGE")
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
    fun onCallUSClicked_navigateToPhoneDiallerScreen() = runBlockingTest {
        launch {
            viewModel.launchCallDialer()
        }

        Assert.assertEquals(
            "Dialler Screen wasn't Launched",
            ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT,
            viewModel.myState.first()
        )
    }

    @Test
    fun onItemClicked_navigateToAdditionalInfoActivity() = runBlockingTest {
        launch {
            viewModel.navigateAdditionalInfoScreen(dummyList[0])
        }

        Assert.assertEquals(
            "AdditionalInfo Activity wasn't Launched",
            ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO,
            viewModel.myState.first()
        )
    }
}