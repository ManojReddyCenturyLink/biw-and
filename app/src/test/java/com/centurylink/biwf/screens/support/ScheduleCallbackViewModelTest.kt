package com.centurylink.biwf.screens.support

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinatorDestinations
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

@Suppress("EXPERIMENTAL_API_USAGE")
class ScheduleCallbackViewModelTest : ViewModelBaseTest() {

    @MockK
    private lateinit var analyticsManagerInterface : AnalyticsManager

    private val dummyList = listOf(
        "I want to know more about fiber internet service",
        "I’m having trouble signing up for fiber internet service",
        "I can’t sign into my account",
        "I have questions about my account",
        "I need something not listed here"
    ).map(::TopicList)

    private lateinit var viewModel: ScheduleCallbackViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        coEvery { mockModemRebootMonitorService }
        run { analyticsManagerInterface }
        viewModel = ScheduleCallbackViewModel(mockModemRebootMonitorService,analyticsManagerInterface)
    }

    @Ignore
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

    @Ignore
    @Test
    fun onItemClicked_navigateToAdditionalInfoActivity() = runBlockingTest {
        launch {
            viewModel.navigateAdditionalInfoScreen(dummyList[3], 3)
        }

        Assert.assertEquals(
            "AdditionalInfo Activity wasn't Launched",
            ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO,
            viewModel.myState.first()
        )
    }
}
