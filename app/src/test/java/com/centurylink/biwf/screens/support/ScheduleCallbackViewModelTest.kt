package com.centurylink.biwf.screens.support

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.ScheduleCallbackCoordinator
import com.centurylink.biwf.screens.support.schedulecallback.ScheduleCallbackViewModel
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations

class ScheduleCallbackViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: ScheduleCallbackViewModel

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        viewModel = ScheduleCallbackViewModel()
    }

    @Test
    fun onCallUSClicked_navigateToPhoneDiallerScreen() {
        viewModel.launchCallDialer()
        Assert.assertEquals(
            "Dialler Screen wasn't Launched",
            ScheduleCallbackCoordinator.ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT,
            viewModel.myState.value
        )
    }
}