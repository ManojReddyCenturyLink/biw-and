package com.centurylink.biwf.screens.changeappointment

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertNotNull

class AppointmentBookedViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: AppointmentBookedViewModel

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @MockK
    private lateinit var mockPreferences: Preferences

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)

        viewModel = AppointmentBookedViewModel(
            mockPreferences,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun testLogDoneButtonClick() {
        assertNotNull(viewModel.logDoneButtonClick())
    }

    @Test
    fun testLogViewDashboardButtonClick() {
        assertNotNull(viewModel.logViewDashboardButtonClick())
    }
}
