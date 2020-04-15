package com.centurylink.biwf.screens.home.dashboard

import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.repos.CurrentAppointmentRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DashboardViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: DashboardViewModel
    @MockK
    private lateinit var mockCurrentAppointmentRepository: CurrentAppointmentRepository

    @Before
    fun setup() {
        every { mockCurrentAppointmentRepository.getCurrentAppointment("123") } returns true.toString()
        viewModel = DashboardViewModel(currentAppointmentRepository = mockCurrentAppointmentRepository)
    }

    @Test
    fun onChangeAppointmentClicked_navigateToChangeAppointmentScreen() {
        viewModel.getChangeAppointment()
        Assert.assertEquals(
            "Change Appointment Screen wasn't Launched",
            DashboardCoordinatorDestinations.CHANGE_APPOINTMENT,
            viewModel.myState.value
        )
    }
}