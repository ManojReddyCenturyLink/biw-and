package com.centurylink.biwf.model.account

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AccountDetails(
    @SerializedName("Id")
    val Id: String = "",
    @SerializedName("Name")
    val name: String = "",
    @SerializedName("LastName__c")
    val lastName: String = "",
    @SerializedName("FirstName__c")
    val firstName: String = "",
    @SerializedName("Is_Billing_Address_Updated__c")
    val isBillingAddressUpdated: Boolean,
    @SerializedName("Email_Opt_In__c")
    val emailOptInC: Boolean,
    @SerializedName("Marketing_Opt_In__c")
    val marketingOptInC: Boolean
):Serializable