package com.centurylink.biwf.model.appointment

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDateTime

/**
 * Model class for account details screen
 */
data class Appointments(
    @SerializedName("totalSize")
    val totalSize: Int = 0,
    @SerializedName("records")
    val records: List<AppointmentRecords>,
    @SerializedName("done")
    val done: Boolean = false
)

data class AppointmentRecords(
    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("ArrivalWindowEndTime")
    val arrivalWindowEndTime: LocalDateTime? = null,
    @SerializedName("ArrivalWindowStartTime")
    val arrivalWindowStarTime: LocalDateTime? = null,
    @SerializedName("Status")
    val appointmentStatus: ServiceStatus? = null,
    @SerializedName("Job_Type__c")
    val jobType: String? = null,
    @SerializedName("Latitude")
    val latitude: String? = null,
    @SerializedName("Longitude")
    val longitude: String? = null,
    @SerializedName("WorkTypeId")
    val workTypeId: String? = null,
    @SerializedName("Appointment_Number_Text__c")
    val appointmentNumber: String? = null,
    @SerializedName("ServiceResources")
    val serviceResources: ServiceResources? = null,
    @SerializedName("ServiceTerritory")
    val serviceTerritory: ServiceTerritory? = null
)

data class ServiceTerritory(
    @SerializedName("OperatingHours")
    val operatingHours: OperatingHours? = null

)

data class OperatingHours(
    @SerializedName("TimeZone")
    val timeZone: String? = null
)

data class ServiceResources(
    @SerializedName("records")
    val records: List<ServiceRecords> = emptyList(),
    @SerializedName("totalSize")
    val totalSize: Int = 0
)

data class ServiceRecords(
    @SerializedName("ServiceResource")
    val serviceResource: ServiceResource? = null
)

data class ServiceResource(
    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("Name")
    val name: String? = null
)
