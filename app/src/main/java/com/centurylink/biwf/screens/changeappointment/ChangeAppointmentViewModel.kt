package com.centurylink.biwf.screens.changeappointment

import android.os.Bundle
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.ChangeAppointmentCoordinatorDestinations
import com.centurylink.biwf.model.appointment.RescheduleInfo
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.EventLiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.HashMap

class ChangeAppointmentViewModel @Inject constructor(
    private val appointmentRepository: AppointmentRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    private val analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {
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

    init{
        initApis()
    }

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
            errorMessageFlow.latestValue = it
        }) {
            appointmentId = it.appointmentId
        }
    }

    private suspend fun requestAppointmentSlots(date: String) {
        val appointmentSlots = appointmentRepository
            .getAppointmentSlots(appointmentId, date)
        appointmentSlots.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            formatInputDate(it.slots)
        }
    }

    fun onAppointmentSelectedDate(date: Date) {
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

    fun onNextClicked(selectedate: String, slots: String) {
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

    fun getNextEstimatedDateForSlots(currentLastdate: String): String {
        val format = SimpleDateFormat(DateUtils.STANDARD_FORMAT)
        var myDate = format.parse(currentLastdate)
        myDate = DateUtils.addDays(myDate, 1)
        return DateUtils.toSimpleString(myDate, DateUtils.STANDARD_FORMAT)
    }

    fun checkNextSlotFallsAfter(nextDate: String, lastDate: String): Boolean {
        val format = SimpleDateFormat(DateUtils.STANDARD_FORMAT)
        var nextCallDate = format.parse(nextDate)
        var lastCallDate = format.parse(lastDate)
        return nextCallDate.before(lastCallDate)
    }

    private suspend fun rescheduleAppointmentInfo() {
        val rescheduleslots = appointmentRepository.modifyAppointmentInfo(rescheduleInfo)
        rescheduleslots.fold(ifLeft = {
            progressViewFlow.latestValue = false
            appointmenterrorEvents.emit("Error")
        }) {
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

    data class UIAppointmentModel(
        var serviceDate: String? = null,
        var availableSlotsForDate: List<String> = emptyList(),
        var startDate: Date? = null,
        var endDate: Date? = null,
        var finalSlotMap: Map<String, List<String>> = mapOf()
    )
}