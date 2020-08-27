package com.centurylink.biwf.screens.changeappointment

import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService

import javax.inject.Inject

class AppointmentBookedViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.APPOINTMENT_CONFIRMATION_SCREEN)
    }

    fun logDoneButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_APPOINTMENT_BOOKED)
    }

    fun logViewDashboardButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_VIEW_DASHBOARD_APPOINTMENT_BOOKED)
    }
}