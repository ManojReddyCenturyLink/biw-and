package com.centurylink.biwf.screens.changeappointment

import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService

import javax.inject.Inject

/**
 * Appointment booked view model
 *
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class AppointmentBookedViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.APPOINTMENT_CONFIRMATION_SCREEN)
    }

    /**
     * Log done button click -  It handles the done button click event
     *
     */
    fun logDoneButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_APPOINTMENT_BOOKED)
    }

    /**
     * Log view dashboard button click -  It handles the dashboard appointment booked button click
     * event
     *
     */
    fun logViewDashboardButtonClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_VIEW_DASHBOARD_APPOINTMENT_BOOKED)
    }
}
