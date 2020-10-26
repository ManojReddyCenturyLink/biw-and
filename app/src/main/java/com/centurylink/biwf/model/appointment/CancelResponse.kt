package com.centurylink.biwf.model.appointment

import com.google.gson.annotations.SerializedName

/**
 *  data class for cancel appointment response
 */
data class CancelResponse(
    @SerializedName("ServiceAppointmentNumber")
    val serviceAppointmentNumber: String? = null,
    @SerializedName("Status")
    val status: String? = null,
    @SerializedName("ServiceAppointmentId")
    val serviceAppointmentId: String? = null,
    @SerializedName("Message")
    val message: String? = null
)
