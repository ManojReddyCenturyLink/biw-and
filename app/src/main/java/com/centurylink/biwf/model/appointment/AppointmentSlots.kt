package com.centurylink.biwf.model.appointment

import com.google.gson.annotations.SerializedName

data class AppointmentSlots(
    @SerializedName("opHoursTimeZone")
    val totalSize: String? = null,
    @SerializedName("slotsValue")
    val slots: HashMap<String, List<String>>,
    @SerializedName("ServiceAppointmentId")
    val serviceAppointmentId: String? = null
)