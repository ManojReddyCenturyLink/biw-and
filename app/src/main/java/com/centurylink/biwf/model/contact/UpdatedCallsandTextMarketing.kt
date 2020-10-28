package com.centurylink.biwf.model.contact

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Model class for marketing calls and text update info
 */
data class UpdatedCallsandTextMarketing(
    @SerializedName("Marketing_Opt_In__c")
    val marketingOptInC: Boolean,
    @SerializedName("MobilePhone")
    val phoneNumber: String
) : Serializable
