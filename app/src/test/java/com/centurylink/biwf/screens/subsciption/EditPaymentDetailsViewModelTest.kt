package com.centurylink.biwf.screens.subsciption

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.screens.subscription.EditPaymentDetailsViewModel
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class EditPaymentDetailsViewModelTest : ViewModelBaseTest() {

    @MockK
    private lateinit var preferences: Preferences

    private lateinit var viewModel: EditPaymentDetailsViewModel

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        every { preferences.getValueByID(Preferences.USER_ID) } returns MOCK_ACCOUNT_ID

        viewModel = EditPaymentDetailsViewModel(
            preferences = preferences,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun `emit error when web view experiences a loading error`() = runBlockingTest {
        viewModel.onWebViewError()
        assertThat(
            viewModel.errorMessageFlow.first(),
            `is`(EditPaymentDetailsViewModel.GENERIC_WEB_VIEW_ERROR)
        )
    }

    @Test
    fun testLogBackPress() {
        Assert.assertNotNull(viewModel.logBackPress())
    }

    @Test
    fun testLogDonePress() {
        Assert.assertNotNull(viewModel.logDonePress())
    }

    @Test
    fun testOnWebViewProgress() {
        Assert.assertNotNull(viewModel.onWebViewProgress(100))
    }

    @Test
    fun `emit URL string event again when retry clicked`() = runBlockingTest {
        viewModel.onRetryClicked()
        Assert.assertEquals(
            viewModel.subscriptionUrlFlow.first(),
            "https://qa-qa101.cs16.force.com/fiber/apex/vf_fiberBuyFlowPaymentMobile?userId=MockAccountId"
        )
    }

    companion object {
        private const val MOCK_ACCOUNT_ID = "MockAccountId"
    }
}