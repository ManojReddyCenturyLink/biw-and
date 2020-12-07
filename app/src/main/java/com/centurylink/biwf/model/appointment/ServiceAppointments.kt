package com.centurylink.biwf.model.appointment

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Model class for service appointments details
 */
data class ServiceAppointments(

    @SerializedName("Id")
    val id: String? = null,
    @SerializedName("OwnerId")
    val ownerId: String? = null,
    @SerializedName("IsDeleted")
    val isDeleted: Boolean? = null,
    @SerializedName("AppointmentNumber")
    val appointmentNumber: String? = null,
    @SerializedName("CreatedDate")
    val createdDate: Date? = null,
    @SerializedName("CreatedById")
    val createdById: String? = null,
    @SerializedName("LastModifiedDate")
    val lastModifiedDate: Date? = null,
    @SerializedName("LastModifiedById")
    val lastModifiedById: String? = null,
    @SerializedName("SystemModstamp")
    val systemModstamp: String? = null,
    @SerializedName("LastViewedDate")
    val lastViewedDate: Date? = null,
    @SerializedName("CallCenterId")
    val callCenterId: String? = null,
    @SerializedName("LastReferencedDate")
    val lastReferencedDate: Date? = null,
    @SerializedName("ParentRecordId")
    val parentRecordId: String? = null,
    @SerializedName("ParentRecordType")
    val parentRecordType: String? = null,
    @SerializedName("AccountId")
    val accountId: String? = null,
    @SerializedName("WorkTypeId")
    val workTypeId: String? = null,
    @SerializedName("ContactId")
    val contactId: String? = null,
    @SerializedName("Street")
    val street: String? = null,
    @SerializedName("City")
    val city: String? = null,
    @SerializedName("State")
    val state: String? = null,
    @SerializedName("PostalCode")
    val postalCode: String? = null,
    @SerializedName("Country")
    val country: String? = null,
    @SerializedName("Latitude")
    val latitude: String? = null,
    @SerializedName("Longitude")
    val longitude: String? = null,
    @SerializedName("GeocodeAccuracy")
    val geocodeAccuracy: String? = null,
    @SerializedName("Description")
    val description: String? = null,
    @SerializedName("EarliestStartTime")
    val earliestStartTime: Date? = null,
    @SerializedName("DueDate")
    val dueDate: Date? = null,
    @SerializedName("Duration")
    val duration: Float? = null,
    @SerializedName("ArrivalWindowStartTime")
    val arrivalWindowStartTime: Date? = null,
    @SerializedName("ArrivalWindowEndTime")
    val arrivalWindowEndTime: Date? = null,
    @SerializedName("Status")
    val status: String? = null,
    @SerializedName("SchedStartTime")
    val schedStartTime: Date? = null,
    @SerializedName("SchedEndTime")
    val schedEndTime: Date? = null,
    @SerializedName("ActualStartTime")
    val actualStartTime: Date? = null,
    @SerializedName("ActualEndTime")
    val actualEndTime: Date? = null,

    @SerializedName("ActualDuration")
    val actualDuration: Float? = null,
    @SerializedName("DurationType")
    val durationType: String? = null,
    @SerializedName("DurationInMinutes")
    val durationInMinutes: Float? = null,
    @SerializedName("ServiceTerritoryId")
    val serviceTerritoryId: String? = null,
    @SerializedName("Subject")
    val subject: String? = null,
    @SerializedName("ParentRecordStatusCategory")
    val parentRecordStatusCategory: String? = null,
    @SerializedName("StatusCategory")
    val statusCategory: String? = null,
    @SerializedName("ServiceNote")
    val serviceNote: String? = null,

    @SerializedName("FSL__Appointment_Grade__c")
    val appointmentGrade: String? = null,
    @SerializedName("FSL__Auto_Schedule__c")
    val autoSchedule: Boolean? = null,
    @SerializedName("FSL__Emergency__c")
    val emergency: Boolean? = null,
    @SerializedName("FSL__Gantt_Display_Date__c")
    val ganttDisplayDate: Date? = null,
    @SerializedName("FSL__InJeopardyReason__c")
    val inJeopardyReason: String? = null,
    @SerializedName("FSL__InJeopardy__c")
    val inJeopardy: Boolean? = null,
    @SerializedName("FSL__InternalSLRGeolocation__Latitude__s")
    val internalSLRGeolocationLatitude: String? = null,
    @SerializedName("FSL__InternalSLRGeolocation__Longitude__s")
    val internalSLRGeolocationLongitude: String? = null,

    @SerializedName("FSL__IsMultiDay__c")
    val isMultiDay: Boolean? = null,
    @SerializedName("FSL__MDS_Calculated_length__c")
    val mDSCalculatedlength: String? = null,
    @SerializedName("FSL__MDT_Operational_Time__c")
    val mDTOperationalTime: String? = null,
    @SerializedName("FSL__Pinned__c")
    val pinned: Boolean? = null,

    @SerializedName("FSL__Prevent_Geocoding_For_Chatter_Actions__c")
    val fpreventGeocodingForChatterActions: Boolean? = null,
    @SerializedName("FSL__Related_Service__c")
    val relatedervice: String? = null,
    @SerializedName("FSL__Same_Day__c")
    val sameDay: Boolean? = null,
    @SerializedName("FSL__Same_Resource__c")
    val sameResource: Boolean? = null,

    @SerializedName("FSL__Schedule_Mode__c")
    val scheduleMode: String? = null,
    @SerializedName("FSL__Schedule_over_lower_priority_appointment__c")
    val scheduleOverLowerPriorityAppointment: Boolean? = null,
    @SerializedName("FSL__Scheduling_Policy_Used__c")
    val schedulingPolicyUsed: String? = null,
    @SerializedName("FSL__Time_Dependency__c")
    val timeDependency: Boolean? = null,

    @SerializedName("FSL__UpdatedByOptimization__c")
    val updatedByOptimization: Boolean? = null,
    @SerializedName("FSL__Use_Async_Logic__c")
    val useAsyncLogic: Boolean? = null,
    @SerializedName("FSL__Virtual_Service_For_Chatter_Action__c")
    val virtualServiceForChatterAction: Boolean? = null,
    @SerializedName("Expected_Completion_Time__c")
    val expectedCompletionTime: Date? = null,

    @SerializedName("WorkTypeName__c")
    val workTypeName: String? = null,
    @SerializedName("Arrival_Start_Time__c")
    val arrivalStartTime: String? = null,
    @SerializedName("Successful_Payment__c")
    val successfulPayment: Boolean? = null,
    @SerializedName("Job_Type__c")
    val jobType: String? = null,

    @SerializedName("Appointment_Reminder__c")
    val appointmentReminder: String? = null,
    @SerializedName("Appointment_Number_Text__c")
    val appointmentNumberText: String? = null,
    @SerializedName("Cannot_Complete_Date_Time__c")
    val cannotCompleteDateTime: Date? = null,
    @SerializedName("Left_In_Facilities__c")
    val leftInFacilities: String? = null,
    @SerializedName("Comment__c")
    val comment: String? = null
)
