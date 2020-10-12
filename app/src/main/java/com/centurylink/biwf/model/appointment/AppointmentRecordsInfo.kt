package com.centurylink.biwf.model.appointment

import org.threeten.bp.LocalDateTime

/**
 * Model class for appointment record info
 */
data class AppointmentRecordsInfo(
    val serviceAppointmentStartDate: LocalDateTime,
    val serviceAppointmentEndTime: LocalDateTime,
    val serviceEngineerName: String,
    val serviceEngineerProfilePic: String?,
    val serviceStatus: ServiceStatus?,
    val serviceLatitude: String?,
    val serviceLongitude: String?,
    val jobType: String,
    val appointmentId: String,
    val timeZone:String,
    val appointmentNumber:String
)