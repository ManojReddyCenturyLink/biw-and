package com.centurylink.biwf.screens.changeappointment

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ChangeAppointmentCoordinatorDestinations
import com.centurylink.biwf.model.appointment.RescheduleInfo
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

/**
 * Change appointment view model
 *
 * @property appointmentRepository -  repository instance to handle appointment api calls
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class ChangeAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {
    var errorMessageFlow = EventFlow<String>()
    val myState = EventFlow<ChangeAppointmentCoordinatorDestinations>()
    var slotForAppointments = HashMap<String, List<String>>()
    var uiAppointmentModel: UIAppointmentModel = UIAppointmentModel()
    val appointmentSlotsInfo: Flow<UIAppointmentModel> = BehaviorStateFlow()
    var progressViewFlow = EventFlow<Boolean>()
    val sloterrorEvents: EventLiveData<String> = MutableLiveData()
    val appointmenterrorEvents: EventLiveData<String> = MutableLiveData()
    lateinit var appointmentId: String
    lateinit var appointmentSlots: String
    lateinit var appointmentDate: String

    private lateinit var rescheduleInfo: RescheduleInfo

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_MODIFY_APPOINTMENT)
        initApis()
    }

    /**
     * Init apis - It will start all the api calls initialisation
     *
     */
    fun initApis() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestAppointmentDetails()
            var todaysDate = DateUtils.toSimpleString(Date(), DateUtils.STANDARD_FORMAT)
            getFirstRequestSlots(todaysDate)
        }
    }

    private fun initApisWithDates(date: String) {
        viewModelScope.launch {
            getFirstRequestSlots(date)
        }
    }

    private suspend fun getFirstRequestSlots(date: String) {
        requestAppointmentSlots(date)
    }

    private suspend fun requestAppointmentDetails() {
        val appointmentDetails = appointmentRepository.getAppointmentInfo()
        appointmentDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_APPOINTMENT_INFO_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_APPOINTMENT_INFO_SUCCESS)
            appointmentId = it.appointmentId
        }
    }

    private suspend fun requestAppointmentSlots(date: String) {
        val appointmentSlots = appointmentRepository
            .getAppointmentSlots(appointmentId, date)
        appointmentSlots.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_APPOINTMENT_SLOTS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_APPOINTMENT_SLOTS_SUCCESS)
            formatInputDate(it.slots)
        }
    }

    /**
     * On appointment selected date - It will handle appointment logic for selected date
     *
     * @param date - returns selected date for appointment
     */
    fun onAppointmentSelectedDate(date: Date) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.DATE_TAP_CHANGE_APPOINTMENT)
        appointmentDate = DateUtils.toSimpleString(date, DateUtils.STANDARD_FORMAT)
        var appointmentList = slotForAppointments[appointmentDate]
        if (appointmentList.isNullOrEmpty()) {
            appointmentList = emptyList()
        }
        uiAppointmentModel = uiAppointmentModel.copy(
            serviceDate = appointmentDate,
            availableSlotsForDate = appointmentList
        )
        appointmentSlotsInfo.latestValue = uiAppointmentModel
    }

    /**
     * Navigate to appointment confirmed - It will navigate to appointment booked screen on
     * confirmation of appointment
     *
     */
    fun navigateToAppointmentConfirmed() {
        val bundle = Bundle()
        bundle.putString(
            AppointmentBookedActivity.APPOINTMENT_STATEMENT_DATE,
            appointmentDate
        )
        bundle.putString(
            AppointmentBookedActivity.APPOINTMENT_STATEMENT_SLOTS,
            appointmentSlots.toLowerCase()
        )
        bundle.putString(
            AppointmentBookedActivity.APPOINTMENT_STATEMENT_ID,
            appointmentId
        )
        ChangeAppointmentCoordinatorDestinations.bundle = bundle
        myState.latestValue =
            ChangeAppointmentCoordinatorDestinations.APPOINTMENT_CONFIRMED
    }

    /**
     * On next clicked - It will handle next click event logic
     *
     * @param selectedate - returns selected appointment date
     * @param slots - returns appointment slots
     */
    fun onNextClicked(selectedate: String, slots: String) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_NEXT_CHANGE_APPOINTMENT)
        if (slots.isNullOrEmpty()) {
            sloterrorEvents.emit("Error")
            return
        }
        appointmentSlots = slots
        appointmentDate = selectedate
        splitSlots()
    }

    private fun formatInputDate(slots: HashMap<String, List<String>>) {
        val sdf: DateFormat = SimpleDateFormat(DateUtils.STANDARD_FORMAT)
        slots.forEach { (key, value) ->
            val date: Date = sdf.parse(key)
            val formattedKey = DateUtils.toSimpleString(date, DateUtils.STANDARD_FORMAT)
            slotForAppointments[formattedKey] = value
        }
        val sortedMaps = slotForAppointments.toSortedMap(compareByDescending { it })
        val nextDateForSlots = getNextEstimatedDateForSlots(sortedMaps.firstKey())
        var lastdayDate = DateUtils.toSimpleString(
            DateUtils.getLastDateoftheMonthAfter(),
            DateUtils.STANDARD_FORMAT
        )
        if (checkNextSlotFallsAfter(nextDateForSlots, lastdayDate)) {
            initApisWithDates(nextDateForSlots)
        } else {
            var appointmentList = slotForAppointments[sortedMaps.lastKey()]
            if (appointmentList.isNullOrEmpty()) {
                appointmentList = emptyList()
            }
            uiAppointmentModel = uiAppointmentModel.copy(
                serviceDate = sortedMaps.lastKey(),
                availableSlotsForDate = appointmentList,
                startDate = DateUtils.fromStringtoDate(sortedMaps.lastKey()),
                endDate = DateUtils.fromStringtoDate(sortedMaps.firstKey()),
                finalSlotMap = sortedMaps
            )
            appointmentSlotsInfo.latestValue = uiAppointmentModel
            progressViewFlow.latestValue = false
        }
    }

    /**
     * Get next estimated date for slots - It is used to get estimated date for next available solts
     *
     * @param currentLastdate - returns next current end date
     * @return - returns date with standard format from date utilities
     * Error - returns null or empty
     */
    fun getNextEstimatedDateForSlots(currentLastdate: String): String {
        val format = SimpleDateFormat(DateUtils.STANDARD_FORMAT)
        var myDate = format.parse(currentLastdate)
        myDate = DateUtils.addDays(myDate, 1)
        return DateUtils.toSimpleString(myDate, DateUtils.STANDARD_FORMAT)
    }

    /**
     * Check next slot falls after - It will handle next available slots logic before last date
     *
     * @param nextDate - returns next available date
     * @param lastDate - returns end date
     * @return - returns next slot available date before last date
     * Error - returns null or Empty value
     */
    fun checkNextSlotFallsAfter(nextDate: String, lastDate: String): Boolean {
        val format = SimpleDateFormat(DateUtils.STANDARD_FORMAT)
        var nextCallDate = format.parse(nextDate)
        var lastCallDate = format.parse(lastDate)
        return nextCallDate.before(lastCallDate)
    }

    private suspend fun rescheduleAppointmentInfo() {
        val rescheduleslots = appointmentRepository.modifyAppointmentInfo(rescheduleInfo)
        rescheduleslots.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.MODIFY_APPOINTMENT_INFO_FAILURE)
            progressViewFlow.latestValue = false
            appointmenterrorEvents.emit("Error")
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.MODIFY_APPOINTMENT_INFO_SUCCESS)
            progressViewFlow.latestValue = false
            navigateToAppointmentConfirmed()
        }
    }

    private fun splitSlots() {
        val separatedSlots = appointmentSlots.split("-".toRegex()).map { it.trim() }
        val date12Format = SimpleDateFormat("hh:mm a")
        val date24Format = SimpleDateFormat("HH:mm:ss")
        val arrivalStartTime =
            appointmentDate + " " + date24Format.format(date12Format.parse(separatedSlots[0]))
        val arrivalEndTime =
            appointmentDate + " " + date24Format.format(date12Format.parse(separatedSlots[1]))
        rescheduleInfo = RescheduleInfo(appointmentId, arrivalStartTime, arrivalEndTime)
        submitAppointment()
    }

    private fun submitAppointment() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            rescheduleAppointmentInfo()
        }
    }

    /**
     * Log back click - It handles the back button click event
     *
     */
    fun logBackClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_BACK_CHANGE_APPOINTMENT)
    }

    /**
     * Log appointment selected - It handles the selected slots click event for appointment
     *
     */
    fun logAppointmentSelected() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.SLOT_TAP_CHANGE_APPOINTMENT)
    }

    data class UIAppointmentModel(
        var serviceDate: String? = null,
        var availableSlotsForDate: List<String> = emptyList(),
        var startDate: Date? = null,
        var endDate: Date? = null,
        var finalSlotMap: Map<String, List<String>> = mapOf()
    )
}