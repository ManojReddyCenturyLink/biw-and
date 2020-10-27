package com.centurylink.biwf.model.appointment

import com.google.gson.annotations.SerializedName

/**
 *  data class for cancel appointment request
 */
data class CancelAppointmentInfo(
    @SerializedName("ServiceAppointmentNumber")
    val serviceAppointmentNumber: String? = null,
    @SerializedName("Status")
    val status: ServiceStatus? = null
)
