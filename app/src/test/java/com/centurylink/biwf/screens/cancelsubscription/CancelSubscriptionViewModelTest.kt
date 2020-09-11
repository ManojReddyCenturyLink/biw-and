package com.centurylink.biwf.screens.cancelsubscription

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinatorDestinations
import com.centurylink.biwf.repos.ZouraSubscriptionRepository
import com.centurylink.biwf.utility.DateUtils
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*

class CancelSubscriptionViewModelTest : ViewModelBaseTest() {

    @MockK(relaxed = true)
    private lateinit var zuoraSubscriptionRepo: ZouraSubscriptionRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var viewModel: CancelSubscriptionViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        var date = Date()
        coEvery { zuoraSubscriptionRepo.getSubscriptionDate() } returns Either.Right(date)
        kotlin.run { analyticsManagerInterface }
        viewModel = CancelSubscriptionViewModel(
            zuoraSubscriptionRepo,
            mockModemRebootMonitorService,
            analyticsManagerInterface
        )
    }

    @Test
    fun testRequestSubscriptionSuccess() {
        runBlockingTest {
            launch {
                viewModel.initApis()
                var finalDate = DateUtils.toSimpleString(Date(), DateUtils.CANCEL_APPOINTMENT_DATE_FORMAT)
                Assert.assertEquals(
                    viewModel.cancelSubscriptionDate.latestValue.subscriptionEndDate,
                    finalDate
                )
            }
        }
    }

    @Test
    fun testRequestSubscriptionError() {
        runBlockingTest {
            coEvery { zuoraSubscriptionRepo.getSubscriptionDate() } returns Either.Left("Date is not available")
            viewModel.initApis()
            Assert.assertEquals(viewModel.errorMessageFlow.first(), "Date is not available")
        }
    }

    @Test
    fun testOnNavigateToCancelSubscriptionDetails() {
        runBlockingTest {
            viewModel.onNavigateToCancelSubscriptionDetails()
            Assert.assertEquals(
                viewModel.myState.first(),
                CancelSubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION
            )
        }
    }

    @Test
    fun logCancelPress() {
        Assert.assertNotNull(viewModel.logCancelPress())
    }

    @Test
    fun discardCancellationRequest() {
        Assert.assertNotNull(viewModel.logBackPress())
    }
}
