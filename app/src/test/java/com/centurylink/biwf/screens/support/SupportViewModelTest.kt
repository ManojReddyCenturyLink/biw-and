package com.centurylink.biwf.screens.support

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.model.speedtest.*
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.SpeedTestRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.service.network.SpeedTestService
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SupportViewModelTest : ViewModelBaseTest() {
    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var modemInfoResponse: ModemInfoResponse

    private lateinit var speedTestRes: SpeedTestRes

    @MockK(relaxed = true)
    private lateinit var speedTestService: SpeedTestService

    private lateinit var speedTestRequestResult: SpeedTestRequestResult

    private lateinit var speedTestStatus: SpeedTestStatus

    private lateinit var speedTestStatusResponse: SpeedTestStatusResponse

    private lateinit var viewModel: SupportViewModel

    @MockK
    private lateinit var mockFAQRepository: FAQRepository

    @MockK
    private lateinit var mockAssiaRepository: AssiaRepository

    @MockK
    private lateinit var mocksharedPreferences: Preferences

    @MockK
    private lateinit var oAuthAssiaRepository: OAuthAssiaRepository

    @MockK
    private lateinit var speedTestRepository: SpeedTestRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    private lateinit var faq: Faq

    private lateinit var recordID: RecordId

    private lateinit var speedTestResponse: SpeedTestResponse

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns "12345"
        val jsonString = readJson("faqnosection.json")
        val recordIdString = readJson("caseid.json")
        modemInfoResponse = fromJson(readJson("lineinfo.json"))
        faq = fromJson(jsonString)
        recordID = fromJson(recordIdString)
        speedTestResponse = fromJson(readJson("speedtest-response.json"))
        speedTestRequestResult = fromJson(readJson("speedtest-req-result.json"))
        speedTestStatus = fromJson(readJson("speedtest-status.json"))
        speedTestStatusResponse = fromJson(readJson("speedtest-status-response.json"))
        speedTestRes = fromJson(readJson("speedtest-req.json"))
        coEvery { mockFAQRepository.getKnowledgeRecordTypeId() } returns Either.Right("12345")
        coEvery { mockFAQRepository.getFAQQuestionDetails(any()) } returns Either.Right(faq)
        coEvery { speedTestRepository.startSpeedTest() } returns Either.Right(speedTestRequestResult)
        coEvery { speedTestRepository.checkSpeedTestStatus(speedTestRequestResult.speedTestId) } returns Either.Right(
            speedTestStatus
        )
        coEvery { speedTestRepository.getSpeedTestResults(mocksharedPreferences.getSpeedTestId()!!) } returns Either.Right(
            speedTestStatusResponse
        )
        coEvery { speedTestRepository.checkSpeedTestStatus(any()) } returns Either.Right(
            speedTestStatus
        )
        coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(modemInfoResponse.modemInfo)
        coEvery { oAuthAssiaRepository.getModemInfoForcePing() } returns Either.Right(
            modemInfoResponse.modemInfo
        )
        speedTestRepository = SpeedTestRepository(mockPreferences, speedTestService)
        run { analyticsManagerInterface }
        viewModel = SupportViewModel(
            faqRepository = mockFAQRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            // assiaRepository = mockAssiaRepository,
            oAuthAssiaRepository = oAuthAssiaRepository,
            sharedPreferences = mocksharedPreferences,
            analyticsManagerInterface = analyticsManagerInterface,
            speedTestRepository = speedTestRepository
        )
        viewModel.initApis()
    }

    @Test
    fun OnInitApiCall() = runBlockingTest {
        val method = viewModel.javaClass.getDeclaredMethod("initApis")
        method.isAccessible = true
    }

    @Test
    fun testAnalyticsButtonClicked() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(analyticsManagerInterface)
                viewModel.logDoneButtonClick()
                viewModel.logLiveChatLaunch()
                viewModel.launchScheduleCallback(false)
                viewModel.startSpeedTest()
            }
        }
    }

    @Test
    fun testStartSpeedTestSuccess() {
        runBlockingTest {
            launch {
                coEvery { speedTestService.getSpeedTestDetails(any()) } returns Either.Right(
                    speedTestRes
                )
                viewModel.startSpeedTest()
                viewModel.startSpeedTest()
                val speedTestInformation = speedTestRepository.startSpeedTest()
                Assert.assertEquals(
                    speedTestInformation.map { it.success },
                    Either.Right(true)
                )
                Assert.assertEquals(
                    speedTestInformation.map { it.code },
                    Either.Right(1000)
                )
            }
        }
    }

    @Test
    fun testFaQSectionSuccessCase() {
        runBlockingTest {
            launch {
                coEvery { mockFAQRepository.getKnowledgeRecordTypeId() } returns Either.Right("12345")
                coEvery { mockFAQRepository.getFAQQuestionDetails(any()) } returns Either.Right(faq)
                viewModel.initApis()
            }
        }
    }

    @Test
    fun testFaQSectionErrorCase() {
        runBlockingTest {
            launch {
                coEvery { mockFAQRepository.getKnowledgeRecordTypeId() } returns Either.Right("12345")
                coEvery { mockFAQRepository.getFAQQuestionDetails(any()) } returns Either.Left("Error in FAQ")
                viewModel.initApis()
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(), "Error in FAQ"
                )
            }
        }
    }

    @Test
    fun testFaqRecordIdErrorCase() {
        runBlockingTest {
            launch {
                coEvery { mockFAQRepository.getKnowledgeRecordTypeId() } returns Either.Left("Error in RecordId")
                viewModel.initApis()
                viewModel.navigateToFAQList("")
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(), "Error in RecordId"
                )
            }
        }
    }

    @Test
    fun testHandleRebootStatus() {
        // TODO revisit this case
        runBlockingTest {
            launch {
                Assert.assertNotNull(
                    viewModel.handleRebootStatus(ModemRebootMonitorService.RebootState.ONGOING)
                )
            }
        }
    }

    @Test
    fun testGetLiveiveChatUIConfiguration() {
        Assert.assertNotNull(viewModel.getLiveChatUIConfiguration())
    }
}
