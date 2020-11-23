package com.centurylink.biwf.screens.support

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.model.faq.Faq
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.repos.FAQRepository
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

class FAQViewModelTest : ViewModelBaseTest() {

    @MockK(relaxed = true)
    lateinit var faqRepository: FAQRepository

    @MockK(relaxed = true)
    lateinit var caseRepository: CaseRepository

    @MockK(relaxed = true)
    private lateinit var mockPreferences: Preferences

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    private lateinit var viewModel: FAQViewModel

    private lateinit var faq: Faq

    private lateinit var recordID: RecordId

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        every { mockPreferences.getValueByID(any()) } returns "12345"
        val jsonString = readJson("faqnosection.json")
        val recordIdString = readJson("caseid.json")
        faq = fromJson(jsonString)
        recordID = fromJson(recordIdString)
        coEvery { faqRepository.getKnowledgeRecordTypeId() } returns Either.Right("12345")
        coEvery { faqRepository.getFAQQuestionDetails(any()) } returns Either.Right(faq)

        viewModel = FAQViewModel(faqRepository, mockPreferences, mockModemRebootMonitorService, analyticsManagerInterface)
        viewModel.setFilteredSelection("Manage my account")
    }

    @Test
    fun testFaQSection() {
        runBlockingTest {
            launch {
                coEvery { caseRepository.getRecordTypeId() } returns Either.Right("12345")
                coEvery { faqRepository.getFAQQuestionDetails(any()) } returns Either.Right(faq)
                viewModel.initApis()
                var faqQuestionDetails = viewModel.faqDetailsInfo.latestValue.questionMap
            }
        }
    }

    @Test
    fun testFaQSectionError() {
        runBlockingTest {
            launch {
                coEvery { faqRepository.getKnowledgeRecordTypeId() } returns Either.Right("12345")
                coEvery { faqRepository.getFAQQuestionDetails(any()) } returns Either.Left("Error in FAQ")
                viewModel.initApis()
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(), "Error in FAQ"
                )
            }
        }
    }

    @Test
    fun testFaqRecordIdError() {
        runBlockingTest {
            launch {
                coEvery { faqRepository.getKnowledgeRecordTypeId() } returns Either.Left("Error in RecordId")
                viewModel.initApis()
                viewModel.navigateToScheduleCallback(isExistingUser = true)
                Assert.assertEquals(
                    viewModel.errorMessageFlow.first(), "Error in RecordId"
                )
            }
        }
    }

    @Test
    fun testLogBackButtonClick() {
        Assert.assertNotNull(viewModel.logBackButtonClick())
    }

    @Test
    fun testLogDoneButtonClick() {
        Assert.assertNotNull(viewModel.logDoneButtonClick())
    }

    @Test
    fun testLogLiveChatLaunch() {
        Assert.assertNotNull(viewModel.logLiveChatLaunch())
    }

    @Test
    fun testLogItemExpanded() {
        Assert.assertNotNull(viewModel.logItemExpanded())
    }

    @Test
    fun testLogItemCollapsed() {
        Assert.assertNotNull(viewModel.logItemCollapsed())
    }

    @Test
    fun testGetLiveiveChatUIConfiguration() {
        Assert.assertNotNull(viewModel.getLiveChatUIConfiguration())
    }
}
