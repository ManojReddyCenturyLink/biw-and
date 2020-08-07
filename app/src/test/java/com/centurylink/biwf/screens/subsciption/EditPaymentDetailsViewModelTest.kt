package com.centurylink.biwf.screens.subsciption

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.screens.subscription.EditPaymentDetailsViewModel
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toCollection
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Ignore
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

    @Ignore("TODO fix this test")
    @Test
    fun `dismiss progress view when web page finishes loading`() = runBlockingTest {
        viewModel.onWebViewProgress(EditPaymentDetailsViewModel.WEB_PAGE_PROGRESS_COMPLETE)

        launch {
            val flowVals = mutableListOf<Boolean>()
            viewModel.progressViewFlow.toCollection(flowVals)

            assertThat(flowVals.last(), `is`(false))
        }
    }

    @Ignore("TODO fix this test")
    @Test
    fun `do not dismiss progress view when web page is not finished loading`() = runBlockingTest {
        viewModel.onWebViewProgress(50)

        launch {
            val flowVals = mutableListOf<Boolean>()
            viewModel.progressViewFlow.toCollection(flowVals)

            assertThat(flowVals.last(), `is`(true))
        }
    }

    @Test
    fun `emit error when web view experiences a loading error`() = runBlockingTest {
        viewModel.onWebViewError()

        assertThat(
            viewModel.errorMessageFlow.first(),
            `is`(EditPaymentDetailsViewModel.GENERIC_WEB_VIEW_ERROR)
        )
    }

    @Ignore("TODO fix this test")
    @Test
    fun `emit URL string event again when retry clicked`() = runBlockingTest {
        viewModel.onRetryClicked()

        launch {
            assertThat(
                viewModel.subscriptionUrlFlow.count(),
                `is`(2)
            )
        }

    }

    companion object {
        private const val MOCK_ACCOUNT_ID = "MockAccountId"
    }
}