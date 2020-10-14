package com.centurylink.biwf.model.account

import com.google.gson.annotations.SerializedName

/**
 * Model class for record details
 */
data class RecordsItem(
    @SerializedName("CreatedDate")
    val createdDate: String = "",
    @SerializedName("Zuora__Invoice__c")
    val zuoraInvoiceC: String = "",
    @SerializedName("attributes")
    val attributes: Attributes,
    @SerializedName("Id")
    val id: String = ""
)