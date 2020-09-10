package com.centurylink.biwf.screens.support

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.FAQRepository
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SupportViewModelTest : ViewModelBaseTest() {
    private lateinit var viewModel: SupportViewModel

    @MockK
    private lateinit var mockFAQRepository: FAQRepository

    @MockK
    private lateinit var mockAssiaRepository: AssiaRepository

    @MockK
    private lateinit var mocksharedPreferences: Preferences

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    private lateinit var faq: Faq

    private lateinit var recordID: RecordId


    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        viewModel = SupportViewModel(
            faqRepository = mockFAQRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            assiaRepository = mockAssiaRepository,
            sharedPreferences = mocksharedPreferences,
            analyticsManagerInterface = analyticsManagerInterface
        )
        val jsonString = readJson("faqnosection.json")
        val recordIdString = readJson("caseid.json")
        faq = fromJson(jsonString)
        recordID = fromJson(recordIdString)
        coEvery { mockFAQRepository.getKnowledgeRecordTypeId() } returns Either.Right("12345")
        coEvery { mockFAQRepository.getFAQQuestionDetails(any()) } returns Either.Right(faq)
    }

    @Test
    fun OnInitApiCall() = runBlockingTest {
        val method = viewModel.javaClass.getDeclaredMethod("initApis")
        method.isAccessible = true
    }

    @Test
    fun testAnalyticsButtonClicked(){
        Assert.assertNotNull(analyticsManagerInterface)
        viewModel.logDoneButtonClick()
        viewModel.logLiveChatLaunch()
        viewModel.launchScheduleCallback()
        viewModel.startSpeedTest()
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
}