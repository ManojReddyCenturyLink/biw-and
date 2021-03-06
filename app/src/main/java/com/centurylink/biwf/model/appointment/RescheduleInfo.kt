package com.centurylink.biwf.model.appointment

import com.google.gson.annotations.SerializedName

/**
 * Model class for reschedule info
 */
data class RescheduleInfo(
    @SerializedName("ServiceAppointmentId")
    val serviceAppointmentId: String? = null,
    @SerializedName("ArrivalWindowStartTime")
    val arrivalWindowStartTime: String? = null,
    @SerializedName("ArrivalWindowEndTime")
    val arrivalWindowEndTime: String? = null
)
