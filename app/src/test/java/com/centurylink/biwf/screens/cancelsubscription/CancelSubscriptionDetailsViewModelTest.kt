package com.centurylink.biwf.screens.cancelsubscription

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.cases.CaseResponse
import com.centurylink.biwf.model.cases.Cases
import com.centurylink.biwf.model.cases.RecordId
import com.centurylink.biwf.repos.CaseRepository
import com.centurylink.biwf.utility.Constants
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import java.lang.reflect.Field
import java.util.*

class CancelSubscriptionDetailsViewModelTest : ViewModelBaseTest() {

    @MockK(relaxed = true)
    private lateinit var caseRepository: CaseRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: CancelSubscriptionDetailsViewModel

    private lateinit var case: Cases

    private lateinit var recordID: RecordId

    private lateinit var caseResponse: CaseResponse

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val jsonString = readJson("case.json")
        val caseRespString = readJson("cancelsubscription.json")
        val recordIdString = readJson("caseid.json")
        caseResponse = fromJson(caseRespString)
        case = fromJson(jsonString)
        recordID = fromJson(recordIdString)
        coEvery { caseRepository.getRecordTypeId() } returns Either.Right(Constants.ID)
        run { analyticsManagerInterface }
        coEvery { caseRepository.getCaseId() } returns Either.Right(case)
        viewModel = CancelSubscriptionDetailsViewModel(
            caseRepository,
            mockModemRebootMonitorService,
            analyticsManagerInterface
        )
    }


    @Test
    fun testPerformCancellationRequestSuccess() {
        runBlockingTest {
            viewModel.onCancellationDateSelected(Date())
            coEvery {
                caseRepository.createDeactivationRequest(
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
                )
            } returns Either.Right(caseResponse)
            viewModel.performCancellationRequest()
            Assert.assertEquals(
                viewModel.successDeactivation.first(), caseResponse.success
            )
        }
    }

    @Test
    fun testPerformRequestRecordIdSuccess() {
        runBlockingTest {
            val recordTypeId: Field =
                CancelSubscriptionDetailsViewModel::class.java.getDeclaredField("recordTypeId")
            recordTypeId.isAccessible = true
            viewModel.initApis()
            Assert.assertEquals(
                recordTypeId.get(viewModel), Constants.ID
            )
        }
    }

    @Test
    fun testPerformRequestRecordIdError() {
        runBlockingTest {
            coEvery { caseRepository.getRecordTypeId() } returns Either.Left("Error in RecordId")
            viewModel.initApis()
            Assert.assertEquals(
                viewModel.errorMessageFlow.first(), "Error in RecordId"
            )
        }
    }

    @Test
    fun testonCancellationReasonOther() {
        runBlockingTest {
            viewModel.onCancellationReason("other")
            var expectedvalue = viewModel.displayReasonSelectionEvent.value!!.peekContent()
            Assert.assertEquals(expectedvalue, true)
        }
    }

    @Test
    fun testonCancellationReasonexcpetOther() {
        runBlockingTest {
            viewModel.onCancellationReason("Moving")
            var expectedvalue = viewModel.displayReasonSelectionEvent.value!!.peekContent()
            Assert.assertEquals(expectedvalue, false)
        }
    }

    @Test
    fun testOnCancellationCommentsChanged() {
        viewModel.onCancellationReason("Hello")
        val cancellationComments: Field =
            CancelSubscriptionDetailsViewModel::class.java.getDeclaredField("cancellationComments")
        cancellationComments.isAccessible = true
        viewModel.onCancellationCommentsChanged("Hello")
        Assert.assertEquals(
            cancellationComments.get(viewModel), "Hello"
        )
    }

    @Test
    fun testOnOtherCancellationChangedChanged() {
        val cancellationReasonExplanation: Field =
            CancelSubscriptionDetailsViewModel::class.java.getDeclaredField("cancellationReasonExplanation")
        cancellationReasonExplanation.isAccessible = true
        viewModel.onOtherCancellationChanged("Hello")
        Assert.assertEquals(
            cancellationReasonExplanation.get(viewModel), "Hello"
        )
    }

    @Test
    fun testOnDateSelected() {
        runBlockingTest {
            viewModel.onCancellationDateSelected(Date())
            var expectedDate = viewModel.cancelSubscriptionDateEvent.value!!.peekContent()
            Assert.assertEquals(
                com.centurylink.biwf.utility.DateUtils.toSimpleString(
                    expectedDate,
                    com.centurylink.biwf.utility.DateUtils.STANDARD_FORMAT
                ), com.centurylink.biwf.utility.DateUtils.toSimpleString(
                    (Date()),
                    com.centurylink.biwf.utility.DateUtils.STANDARD_FORMAT
                )
            )
        }
    }

    @Test
    fun testOnDateChanged() {
        runBlockingTest {
            viewModel.onDateChange()
            viewModel.onRatingChanged(4.0f)
            var expectedDate = viewModel.changeDateEvent.value!!.peekContent()
            Assert.assertEquals(expectedDate, Unit)
            val cancellationDate: Field =
                CancelSubscriptionDetailsViewModel::class.java.getDeclaredField("cancellationDate")
            cancellationDate.isAccessible = true
            val emptyDate: Date? = null
            cancellationDate.set(viewModel, emptyDate);
            viewModel.onSubmitCancellation()
        }
    }

    @Test
    fun logSubmitButtonClick() {
        Assert.assertNotNull(viewModel.logSubmitButtonClick())
    }

    @Test
    fun logBackPress() {
        Assert.assertNotNull(viewModel.logBackPress())
    }

    @Test
    fun logCancelPress() {
        Assert.assertNotNull(viewModel.logCancelPress())
    }

    @Test
    fun discardCancellationRequest() {
        Assert.assertNotNull(viewModel.discardCancellationRequest())
    }

    @Test
    fun onSubmitCancellation() {
        viewModel.onCancellationDateSelected(Date())
        viewModel.onSubmitCancellation()
        Assert.assertNotNull(viewModel.performSubmitEvent)
    }

}