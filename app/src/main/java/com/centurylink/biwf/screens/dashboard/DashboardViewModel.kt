package com.centurylink.biwf.screens.dashboard

import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.repos.CurrentAppointmentRepository
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val currentAppointmentRepository: CurrentAppointmentRepository
) : BaseViewModel() {

    val myState = ObservableData(DashboardCoordinatorDestinations.APPOINTMENT_SCHEDULED)
    private var accountID: String? = null

    fun getChangeAppointment(){
        myState.value = DashboardCoordinatorDestinations.CHANGE_APPOINTMENT
    }

    /**
     * param: accountID as input
     * Function to fetch Current Appointment details from api
    */
    fun getCurrentStatus(){
        currentAppointmentRepository.getCurrentAppointment(accountId = "")
        myState.value = DashboardCoordinatorDestinations.APPOINTMENT_SCHEDULED
    }
}