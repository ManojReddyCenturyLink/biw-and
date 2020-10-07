package com.centurylink.biwf.screens.support.schedulecallback

import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import javax.inject.Inject

class SelectTimeViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {


}