package com.centurylink.biwf.model.contact

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Model class for marketing emails update info
 */
class UpdatedMarketingEmails(
    @SerializedName("Email_Opt_In__c")
    val emailOptInC: Boolean
) : Serializable
