package com.centurylink.biwf.model.contact

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UpdatedCallsandTextMarketing (

    @SerializedName("Email_Opt_In__c")
    val emailOptInC: Boolean,

    @SerializedName("Marketing_Opt_In__c")
    val marketingOptInC: Boolean
):Serializable