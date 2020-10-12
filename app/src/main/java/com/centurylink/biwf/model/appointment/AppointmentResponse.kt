package com.centurylink.biwf.model.appointment

import com.google.gson.annotations.SerializedName

/**
 * Model class for appointment details response
 */
data class AppointmentResponse(
    @SerializedName("serviceAppointmentId")
    val serviceAppointmentId: String? = null,
    @SerializedName("status")
    val status: String? = null
)