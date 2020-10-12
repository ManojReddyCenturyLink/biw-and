package com.centurylink.biwf.model.appointment

import com.google.gson.annotations.SerializedName

/**
 * enum class for service status details
 */
enum class ServiceStatus {
    @SerializedName("Scheduled")
    SCHEDULED,
    @SerializedName("None")
    NONE,
    @SerializedName("Dispatched")
    DISPATCHED,
    @SerializedName("Enroute")
    EN_ROUTE,
    @SerializedName("Work Begun")
    WORK_BEGUN,
    @SerializedName("Completed")
    COMPLETED,
    @SerializedName("Canceled")
    CANCELED
}