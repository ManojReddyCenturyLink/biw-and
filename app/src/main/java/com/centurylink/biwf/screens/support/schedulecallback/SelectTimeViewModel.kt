package com.centurylink.biwf.screens.support.schedulecallback

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventLiveData
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class SelectTimeViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {


    private var callbackDate: Date? = null
    private var callbackTime: String? = null
    val changeCallbackDateEvent: EventLiveData<Unit> = MutableLiveData()
    val changeCallbackTimeEvent: EventLiveData<Unit> = MutableLiveData()
    val callbackDateUpdateEvent: EventLiveData<Date> = MutableLiveData()
    val callbackTimeUpdateEvent: EventLiveData<String> = MutableLiveData()
    private var nextDay: Boolean = false


    fun onDateChange() {
        changeCallbackDateEvent.emit(Unit)
    }

    fun onTimeChange() {
        changeCallbackTimeEvent.emit(Unit)
    }

    fun onCallbackDateSelected(callbackDateInfo: Date) {
        callbackDate = callbackDateInfo
        callbackDateUpdateEvent.emit(callbackDate!!)
    }

    fun onCallbackTimeSelected(callbackTimeInfo: String) {
        callbackTime = callbackTimeInfo
        callbackTimeUpdateEvent.emit(callbackTime!!)
    }

    fun getDefaultTimeSlot(): String {
        val localTimeMinutes: Int = LocalTime.now().minute
        var localTimeHours: Int = LocalTime.now().hour

        var localTimeMinutesFinal: Int = localTimeMinutes
        var localTimeHoursFinal: Int = localTimeHours
        nextDay = false

        var amPm: String
        if (localTimeHours >= 12) {
            amPm = "PM"
            localTimeHours -= 12
        } else amPm = "AM"

        if (localTimeHours == 0) {
            localTimeHours = 12
        }

        if (localTimeMinutes in 0..14) {
            localTimeMinutesFinal = 15
            localTimeHoursFinal = localTimeHours
        } else if (localTimeMinutes in 15..29) {
            localTimeMinutesFinal = 30
            localTimeHoursFinal = localTimeHours
        } else if (localTimeMinutes in 30..44) {
            localTimeMinutesFinal = 45
            localTimeHoursFinal = localTimeHours
        } else if (localTimeMinutes in 45..59 && localTimeHours != 11) {
            localTimeMinutesFinal = 0
            localTimeHoursFinal = localTimeHours + 1
        } else if (localTimeMinutes in 45..59 && localTimeHours == 11 && amPm == "AM") {
            localTimeMinutesFinal = 0
            localTimeHoursFinal = localTimeHours + 1
            amPm = "PM"
        } else if (localTimeMinutes in 45..59 && localTimeHours == 11 && amPm == "PM") {
            localTimeMinutesFinal = 0
            localTimeHoursFinal = localTimeHours + 1
            amPm = "AM"
            nextDay = true
        }

        var localTimeMinutesString = localTimeMinutesFinal.toString()
        var localTimeHoursString = localTimeHoursFinal.toString()
        val zeroString = "0"

        if (localTimeMinutesString.length == 1) {
            localTimeMinutesString = zeroString.plus(localTimeMinutesString)
        }
        if (localTimeHoursString.length == 1) {
            localTimeHoursString = zeroString.plus(localTimeHoursString)
        }
        return localTimeHoursString.plus(":").plus(localTimeMinutesString).plus(amPm)
    }

    fun getDefaultDateSlot(): String {
        var localDate = LocalDate.now()
        if (nextDay) {
            localDate = localDate.plusDays(1)
        }
        val formatter =
            DateTimeFormatter.ofPattern("MM/dd/YY")
        return localDate.format(formatter)
    }
}