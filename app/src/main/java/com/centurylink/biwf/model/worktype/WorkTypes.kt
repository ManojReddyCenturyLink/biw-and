package com.centurylink.biwf.model.worktype

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Data class for work types
 */
data class WorkTypes(
    @SerializedName("Id") var id: String? = null,
    @SerializedName("OwnerId") var ownerId: String? = null,
    @SerializedName("IsDeleted") var IsDeleted: Boolean? = null,
    @SerializedName("Name") var name: String? = null,
    @SerializedName("CreatedDate") var createdDate: Date? = null,
    @SerializedName("CreatedById") var createdById: String? = null,
    @SerializedName("LastModifiedDate") var lastModifiedDate: Date? = null,
    @SerializedName("LastModifiedById") var lastModifiedById: String? = null,
    @SerializedName("SystemModstamp") var systemModstamp: String? = null,
    @SerializedName("LastViewedDate") var lastViewedDate: Date? = null,
    @SerializedName("Description") var description: String? = null,
    @SerializedName("EstimatedDuration") var estimatedDuration: Float? = null,
    @SerializedName("DurationType") var durationType: Boolean? = null,
    @SerializedName("DurationInMinutes") var durationInMinutes: Float? = null,
    @SerializedName("ShouldAutoCreateSvcAppt") var shouldAutoCreateSvcAppt: Boolean? = null,
    @SerializedName("ServiceReportTemplateId") var serviceReportTemplateId: String? = null,
    @SerializedName("MinimumCrewSize") var minimumCrewSize: String? = null,
    @SerializedName("RecommendedCrewSize") var recommendedCrewSize: String? = null,
    @SerializedName("FSL__Due_Date_Offset__c") var fslDueDateOffset: Date? = null,
    @SerializedName("FSL__Exact_Appointments__c") var fslExactAppointments: Boolean? = null
)
