package com.centurylink.biwf.model.contact

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Model class for case contact details
 */
data class ContactDetails(
    @SerializedName("Id")
    val id: String = "",
    @SerializedName("AccountId")
    val accountId: String = "",
    @SerializedName("LastName")
    val lastName: String = "",
    @SerializedName("FirstName")
    val firstName: String = "",
    @SerializedName("Salutation")
    val salutation: String = "",
    @SerializedName("Name")
    val name: String = "",
    @SerializedName("Cell_Phone_Opt_In__c")
    val cellPhoneOptInc: Boolean,
    @SerializedName("Email_Opt_In__c")
    val emailOptInC: Boolean,
    @SerializedName("Marketing_Opt_In__c")
    val marketingOptInC: Boolean
) : Serializable
