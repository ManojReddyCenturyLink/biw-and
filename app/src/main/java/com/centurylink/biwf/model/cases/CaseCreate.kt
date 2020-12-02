package com.centurylink.biwf.model.cases

import com.google.gson.annotations.SerializedName
/**
 * Model class for case subscription create request details
 */
data class CaseCreate(
    @SerializedName("ContactId")
    val contactId: String = "",
    @SerializedName("Case_Type__c")
    val case_Type__c: String = "Deactivation",
    @SerializedName("Origin")
    val origin: String = "Web",
    @SerializedName("Cancellation_Reason__c")
    val cancellation_Reason__c: String = "",
    @SerializedName("CancelReason_Comments__c")
    val cancelReason_Comments__c: String = "",
    @SerializedName("Notes__c")
    val notes__c: String = "",
    @SerializedName("Experience__c")
    var experience__c: String = "0",
    @SerializedName("Cancellation_Date_Holder__c")
    val cancellation_Date_Holder__c: String = "",
    @SerializedName("RecordTypeId")
    val recordTypeId: String = ""
)
