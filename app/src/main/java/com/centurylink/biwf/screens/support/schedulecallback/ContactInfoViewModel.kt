package com.centurylink.biwf.screens.support.schedulecallback

import android.os.Bundle
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ContactInfoCoordinatorDestinations
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import javax.inject.Inject

class ContactInfoViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val myState = EventFlow<ContactInfoCoordinatorDestinations>()

    fun launchSelectTime() {
        val bundle = Bundle()
        bundle.putString(SelectTimeActivity.SELECT_TIME, "Select time")
        ContactInfoCoordinatorDestinations.bundle = bundle
        myState.latestValue = ContactInfoCoordinatorDestinations.SELECT_TIME
    }
}