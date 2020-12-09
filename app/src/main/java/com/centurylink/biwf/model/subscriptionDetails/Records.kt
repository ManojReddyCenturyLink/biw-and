package com.centurylink.biwf.model.subscriptionDetails

import com.google.gson.annotations.SerializedName

data class Records(
    @SerializedName("attributes") val attributes: Attributes? = null,
    @SerializedName("Id") val id: String? = null,
    @SerializedName("Zuora__ProductName__c") val zuora__ProductName__c: String? = null,
    @SerializedName("InternetSpeed__c") val internetSpeed__c: String? = null,
    @SerializedName("Zuora__Price__c") val zuora__Price__c: Double? = null,
    @SerializedName("Zuora__ExtendedAmount__c") val zuora__ExtendedAmount__c: Double? = null,
    @SerializedName("Zuora__BillingPeriodStartDay__c") val zuora__BillingPeriodStartDay__c: String? = null
)
