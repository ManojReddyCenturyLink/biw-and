package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinatorDestinations
import com.centurylink.biwf.model.support.TopicList
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test
class ScheduleCallbackViewModelTest : ViewModelBaseTest() {
    private lateinit var viewModel: ScheduleCallbackViewModel

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    private val dummyList = listOf(
        "I want to know more about fiber internet service",
        "I’m having trouble signing up for fiber internet service",
        "I can’t sign into my account",
        "I have questions about my account",
        "I need something not listed here"
    ).map(::TopicList)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        viewModel = ScheduleCallbackViewModel(
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun testAnalyticsButtonClicked(){
        Assert.assertNotNull(analyticsManagerInterface)
        viewModel.logBackButtonClick()
        viewModel.logCancelButtonClick()
        viewModel.launchCallDialer()
    }

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
