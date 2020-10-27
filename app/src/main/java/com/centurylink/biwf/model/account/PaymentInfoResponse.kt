package com.centurylink.biwf.model.account

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Model class for payment details
 */
data class PaymentInfoResponse(
    @SerializedName("totalSize")
    val totalSize: Int,
    @SerializedName("done")
    val isDone: Boolean,
    @SerializedName("records")
    val list: List<PaymentInfo>
) : Serializable

data class PaymentInfo(
    @SerializedName("Credit_Card_Summary__c")
    val creditCardSummary: String,
    @SerializedName("Name")
    val name: String,
    @SerializedName("Next_Renewal_Date__c")
    val nextRenewalDate: String,
    @SerializedName("Zuora__BillCycleDay__c")
    val billCycleDay: String
) : Serializable
