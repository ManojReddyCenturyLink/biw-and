package com.centurylink.biwf.model.cases

import com.google.gson.annotations.SerializedName

data class CaseCreate(
    @SerializedName("AccountId")
    val accountId: String = "",
    @SerializedName("ContactId")
    val contactId: String = "",
    @SerializedName("Case_Type__c")
    val caseType: String = "Deactivation",
    @SerializedName("Origin")
    val origin: String = "Web",
    @SerializedName("Cancellation_Reason__c")
    val cancellationReason: String = "",
    @SerializedName("CancelReason_Comments__c")
    val cancelReasonComments: String = "",
    @SerializedName("Notes__c")
    val notes: String="",
    @SerializedName("Experience__c")
    var experience: String="0",
    @SerializedName("Cancellation_Date_Holder__c")
    val cancellationDateHolder: String=""
)







