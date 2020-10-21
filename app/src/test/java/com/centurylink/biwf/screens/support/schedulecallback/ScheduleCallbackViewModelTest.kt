package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinatorDestinations
import com.centurylink.biwf.model.support.ScheduleCallbackResponse
import com.centurylink.biwf.model.support.TopicList
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.repos.ScheduleCallbackRepository
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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

    @MockK
    private lateinit var caseRepository: CaseRepository

    @MockK
    private lateinit var scheduleCallbackRepository: ScheduleCallbackRepository
    private lateinit var schedulecallbackinfo: ScheduleCallbackResponse

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
        schedulecallbackinfo = fromJson(readJson("schedulecallback-info.json"))
        val recordTypeId = "012f0000000l0wrAAA"
        coEvery { scheduleCallbackRepository.scheduleCallbackInfo(recordTypeId) } returns Either.Right(schedulecallbackinfo)
        run { analyticsManagerInterface }
        viewModel = ScheduleCallbackViewModel(
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface,
            scheduleCallbackRepository = scheduleCallbackRepository,
            caseRepository = caseRepository
        )
        viewModel.initApiCalls()
    }

    @Test
    fun testAnalyticsButtonClicked(){
        Assert.assertNotNull(analyticsManagerInterface)
        viewModel.logBackButtonClick()
        viewModel.logCancelButtonClick()
        viewModel.launchCallDialer()
    }

    @Test
    fun testSetIsExistingUserState() {
        runBlockingTest {
            viewModel.setIsExistingUserState(true)
            Assert.assertEquals(viewModel.isExistingUserState, true)
        }
    }

    @Test
    fun testGetRecordTypeIdSuccess() {
        runBlockingTest {
           coEvery { caseRepository.getRecordTypeId() } returns Either.Right("012f0000000l0wrAAA")
            val recordIdDetails = caseRepository.getRecordTypeId()
            Assert.assertEquals(recordIdDetails.map { it }, Either.Right("012f0000000l0wrAAA"))
        }
    }

    @Test
    fun testApiCallFailure() {
        runBlockingTest {
            coEvery { caseRepository.getRecordTypeId() } returns Either.Left("")
            coEvery { scheduleCallbackRepository.scheduleCallbackInfo(any())} returns Either.Left("")
            viewModel = ScheduleCallbackViewModel(
                modemRebootMonitorService = mockModemRebootMonitorService,
                analyticsManagerInterface = analyticsManagerInterface,
                scheduleCallbackRepository = scheduleCallbackRepository,
                caseRepository = caseRepository
            )
            viewModel.initApiCalls()
        }
    }

    @Test
    fun testUpdateScheduleCallbackPicklist() {
        runBlockingTest {
            viewModel.updateScheduleCallbackPicklist(schedulecallbackinfo)
        }
    }

    @Test
    fun onItemClicked_navigateToAdditionalInfoActivity() = runBlockingTest {
        launch {
            viewModel.navigateAdditionalInfoScreen(dummyList[3])
        }

        Assert.assertEquals(
            "AdditionalInfo Activity wasn't Launched",
            ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO,
            viewModel.myState.first()
        )
    }
}
