package com.centurylink.biwf.screens.support.schedulecallback

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
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
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

/**
 * Select time view model - class to handle select time related business logic and data classes
 *
 * @property preferences - preferences instance to get stored user id
 * @property supportRepository - repository instance to handle support service related API calls
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to modem reboot actions
 * @param analyticsManagerInterface - -analytics instance to track events in firebase
 */
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

    init {
        isScheduleCallbackSuccessful.latestValue = false
        errorFlow.latestValue = false
        scheduleCallbackFlow.latestValue = false
    }

    /**
     * On date change - updates event flow when date is changed
     *
     */
    fun onDateChange() {
        changeCallbackDateEvent.emit(Unit)
    }

    /**
     * On time change - updates event flow when time is changed
     *
     */
    fun onTimeChange() {
        changeCallbackTimeEvent.emit(Unit)
    }

    /**
     * On callback date selected - updates new selected date
     *
     * @param callbackDateInfo - new date selected by user
     */
    fun onCallbackDateSelected(callbackDateInfo: Date) {
        callbackDate = callbackDateInfo
        callbackDateUpdateEvent.emit(callbackDate!!)
    }

    /**
     * On callback time selected - updates new selected time
     *
     * @param callbackTimeInfo - new time selected by user
     */
    fun onCallbackTimeSelected(callbackTimeInfo: String) {
        callbackTime = callbackTimeInfo
        callbackTimeUpdateEvent.emit(callbackTime!!)
    }

    /**
     * Support service - initiates call to schedule callback request
     *
     * @param phoneNumber - phone number input
     * @param ASAP - boolean value corresponding to next available time slot
     * @param customerCareOption - customer care option
     * @param fullDateAndTime - date and time slot
     * @param additionalInfo - additional info related to callback
     */
    fun supportService(
        phoneNumber: String,
        ASAP: String,
        customerCareOption: String,
        fullDateAndTime: String,
        additionalInfo: String
    ) {
        scheduleCallbackFlow.latestValue = true
        viewModelScope.launch {
            supportServiceInfo(SupportServicesReq(
                preferences.getValueByID(Preferences.USER_ID),
                phoneNumber,
                ASAP,
                customerCareOption,
                ASAP,
                fullDateAndTime,
                additionalInfo
            )
            )
        }
    }

    /**
     * Support service info - API call to schedule callback
     *
     * @param data - support service request data
     */
    private suspend fun supportServiceInfo(data: SupportServicesReq) {
        val deviceDetails = supportRepository.supportServiceInfo(data)
        deviceDetails.fold(ifRight =
        {
            scheduleCallbackFlow.latestValue = false
            if (it.status == "SUCCESS") {
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

    /**
     * Get default time slot - returns default callback time slot
     *
     * @param currentMin - current minute value at time of selection
     * @param currentHour - current hour value at time of selection
     * @return - returns default callback time slot as string value
     */
    fun getDefaultTimeSlot(currentMin: Int, currentHour: Int): String {
        val localTimeMinutes: Int = currentMin
        var localTimeHours: Int = currentHour

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
        } else if (localTimeMinutes in 45..59 && localTimeHours == 12) {
            localTimeMinutesFinal = 0
            localTimeHoursFinal = 1
        } else if (localTimeMinutes in 45..59 && localTimeHours != 11 && localTimeHours != 12) {
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

    /**
     * Format date and time - formats date and time selected in proper format
     *
     * @param date - date selected
     * @param time - time selected
     * @return - returns formatted date and time as string
     */
    fun formatDateAndTime(date: CharSequence, time: CharSequence): String {
        val selectedMonth = date.substring(0, 2)
        val selectedDate = date.substring(3, 5)
        var selectedYear = date.substring(6)

        var selectedHour = Integer.parseInt(time.substring(0, 2))
        val selectedMin = time.substring(3, 5)
        val selectedAMPM = time.substring(5)
        if (selectedAMPM == "PM" && selectedHour != 12) {
            selectedHour += 12
        } else if (selectedAMPM == "AM" && selectedHour == 12) {
            selectedHour = 0
        }

        var selectedHourString = selectedHour.toString()
        selectedYear = ("20").plus(selectedYear)
        if (selectedHourString.length == 1) {
            selectedHourString = ("0").plus(selectedHourString)
        }

        return selectedYear.plus("-").plus(selectedMonth).plus("-").plus(selectedDate)
            .plus(" ").plus(selectedHourString).plus(":").plus(selectedMin).plus(":").plus("00")
    }

    /**
     * Get default date slot - returns default callback date slot
     *
     * @return - returns the default date slot as string
     */
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
