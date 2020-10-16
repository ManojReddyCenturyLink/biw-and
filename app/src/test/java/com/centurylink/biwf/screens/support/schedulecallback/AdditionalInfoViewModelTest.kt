package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.coordinators.AdditionalInfoCoordinatorDestinations
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class AdditionalInfoViewModelTest : ViewModelBaseTest() {
    private lateinit var viewModel: AdditionalInfoViewModel

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        viewModel = AdditionalInfoViewModel(
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun testNavigateToContactInfoActivity() {
        runBlockingTest {
            launch {
                viewModel.launchContactInfo(true, "", "")
            }
            Assert.assertEquals(
                "Contact Info Activity was Launched",
                AdditionalInfoCoordinatorDestinations.CONTACT_INFO,
                viewModel.myState.first()
            )
        }
    }

    @Test
    fun testAnalyticsButtonClicked() {
        Assert.assertNotNull(analyticsManagerInterface)
        viewModel.logBackButtonClick()
        viewModel.logCancelButtonClick()
        viewModel.logNextButtonClick()
    }
}