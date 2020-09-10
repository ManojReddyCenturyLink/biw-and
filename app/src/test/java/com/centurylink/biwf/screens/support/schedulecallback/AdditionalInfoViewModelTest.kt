package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
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
    fun testAnalyticsButtonClicked(){
        Assert.assertNotNull(analyticsManagerInterface)
        viewModel.logBackButtonClick()
        viewModel.logCancelButtonClick()
        viewModel.logNextButtonClick()

    }

}