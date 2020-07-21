package com.centurylink.biwf.screens.changeappointment


import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService

import javax.inject.Inject

class AppointmentBookedViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService
) : BaseViewModel(modemRebootMonitorService)