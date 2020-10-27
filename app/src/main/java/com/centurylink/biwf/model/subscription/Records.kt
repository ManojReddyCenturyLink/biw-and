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
    val ZuoraSubscriptionStartDate: Date? = null,
    @SerializedName("Zuora__SubscriptionEndDate__c")
    val ZuoraSubscriptionEndDate: Date? = null,
    @SerializedName("Zuora__NextRenewalDate__c")
    val ZuoraNextRenewalDate: Date? = null,
    @SerializedName("Zuora__NextChargeDate__c")
    val ZuoraNextChargeDate: Date? = null
)
