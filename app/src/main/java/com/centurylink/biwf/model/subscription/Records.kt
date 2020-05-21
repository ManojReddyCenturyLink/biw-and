package com.centurylink.biwf.model.subscription

import com.google.gson.annotations.SerializedName

class Records(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("Name")
    val name: String = "",
    @SerializedName("Zuora__SubscriptionStartDate__c")
    val ZuoraSubscriptionStartDate: String = "",
    @SerializedName("Zuora__SubscriptionEndDate__c")
    val ZuoraSubscriptionEndDate: String = "",
    @SerializedName("Zuora__NextRenewalDate__c")
    val ZuoraNextRenewalDate: String = "",
    @SerializedName("Zuora__NextChargeDate__c")
    val ZuoraNextChargeDate: String = ""
)