package com.centurylink.biwf.model.contact

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UpdatedMarketingEmails (

    @SerializedName("Email_Opt_In__c")
    val emailOptInC: Boolean,

    @SerializedName("Marketing_Opt_In__c")
    val marketingOptInC: Boolean
):Serializable