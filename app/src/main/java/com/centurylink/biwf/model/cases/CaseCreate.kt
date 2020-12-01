package com.centurylink.biwf.model.cases

import com.google.gson.annotations.SerializedName
/**
 * Model class for case subscription create request details
 */
data class CaseCreate(
    @SerializedName("ContactId")
    val contactId: String = "",
    @SerializedName("Case_Type__c")
    val caseTypeC: String = "Deactivation",
    @SerializedName("Origin")
    val origin: String = "Web",
    @SerializedName("Cancellation_Reason__c")
    val cancellationReasonC: String = "",
    @SerializedName("CancelReason_Comments__c")
    val cancelreasonCommentsC: String = "",
    @SerializedName("Notes__c")
    val notesC: String = "",
    @SerializedName("Experience__c")
    var experienceC: String = "0",
    @SerializedName("Cancellation_Date_Holder__c")
    val cancellationDateHolderC: String = "",
    @SerializedName("RecordTypeId")
    val recordTypeId: String = ""
)
