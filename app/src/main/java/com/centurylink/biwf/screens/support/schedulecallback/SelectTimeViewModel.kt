package com.centurylink.biwf.screens.support.schedulecallback

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.support.SupportServicesReq
import com.centurylink.biwf.repos.SupportRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

class SelectTimeViewModel @Inject constructor(
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager,
    private val preferences: Preferences,
    private val supportRepository: SupportRepository
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {


    private var callbackDate: Date? = null
    private var callbackTime: String? = null
    var scheduleCallbackFlow = EventFlow<Boolean>()
    var errorFlow = EventFlow<Boolean>()
    val changeCallbackDateEvent: EventLiveData<Unit> = MutableLiveData()
    val changeCallbackTimeEvent: EventLiveData<Unit> = MutableLiveData()
    val callbackDateUpdateEvent: EventLiveData<Date> = MutableLiveData()
    val callbackTimeUpdateEvent: EventLiveData<String> = MutableLiveData()
    private var nextDay: Boolean = false
    var isScheduleCallbackSuccessful = EventFlow<Boolean>()
    private lateinit var customerCare: String

    init {
        isScheduleCallbackSuccessful.latestValue = false
        errorFlow.latestValue = false
        scheduleCallbackFlow.latestValue = false
    }

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


    fun supportService(context: Context,
                       phoneNumber: String,
                       ASAP: String,
                       customerCareOption: String,
                       fullDateAndTime: String,
                       additionalInfo: String
    ) {
        scheduleCallbackFlow.latestValue = true
        val custom = context.resources.getStringArray(R.array.customer_care_options)
        customerCare = when (customerCareOption) {
            "0" -> custom[0]
            "1" -> custom[1]
            "2" -> custom[2]
            "3" -> custom[3]
            else -> custom[4]

        }
        viewModelScope.launch {
            supportServiceInfo(SupportServicesReq(
                preferences.getValueByID(Preferences.USER_ID),
                phoneNumber,
                ASAP,
                customerCare,
                ASAP,
                fullDateAndTime,
                additionalInfo
            )
            )
        }
    }

    private suspend fun supportServiceInfo(data: SupportServicesReq) {
        val deviceDetails = supportRepository.supportServiceInfo(data)
        deviceDetails.fold(ifRight =
        {
            scheduleCallbackFlow.latestValue = false
            if(it.status == "SUCCESS") {
                scheduleCallbackFlow.latestValue = false
                isScheduleCallbackSuccessful.latestValue = true
                errorFlow.latestValue = false
            } else {
                scheduleCallbackFlow.latestValue = false
                isScheduleCallbackSuccessful.latestValue = false
                errorFlow.latestValue = true
            }
        }, ifLeft = {
            scheduleCallbackFlow.latestValue = false
            isScheduleCallbackSuccessful.latestValue = false
            errorFlow.latestValue = true
        })
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
        } else if(localTimeMinutes in 45..59 && localTimeHours == 12) {
            localTimeMinutesFinal = 0
            localTimeHoursFinal = 1
        } else if (localTimeMinutes in 45..59 && localTimeHours != 11 && localTimeHours !=12) {
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

    fun formatDateAndTime(date: CharSequence, time: CharSequence): String {
        val selectedMonth = date.substring(0, 2)
        val selectedDate = date.substring(3, 5)
        var selectedYear = date.substring(6)

        var selectedHour = Integer.parseInt(time.substring(0, 2))
        val selectedMin = time.substring(3, 5)
        val selectedAMPM = time.substring(5)
        if (selectedAMPM == "PM" && selectedHour != 12) {
            selectedHour += 12
        } else if(selectedAMPM == "AM" && selectedHour == 12) {
            selectedHour = 0
        }

        var selectedHourString = selectedHour.toString()
        selectedYear = ("20").plus(selectedYear)
        if(selectedHourString.length == 1) {
            selectedHourString = ("0").plus(selectedHourString)
        }

        return selectedYear.plus("-").plus(selectedMonth).plus("-").plus(selectedDate)
            .plus(" ").plus(selectedHourString).plus(":").plus(selectedMin).plus(":").plus("00")
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