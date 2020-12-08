package com.centurylink.biwf.model.subscription

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Data class for records details
 */
class Records(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("Name")
    val name: String = "",
    @SerializedName("Zuora__SubscriptionStartDate__c")
    val zuoraSubscriptionStartDate: Date? = null,
    @SerializedName("Zuora__SubscriptionEndDate__c")
    val zuoraSubscriptionEndDate: Date? = null,
    @SerializedName("Zuora__NextRenewalDate__c")
    val zuoraNextRenewalDate: Date? = null,
    @SerializedName("Zuora__NextChargeDate__c")
    val zuoraNextChargeDate: Date? = null
)
