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
    val marketingOptInC: String,
    @SerializedName("Email__c")
    val emailAddress: String,
    @SerializedName("BillingAddress")
    val billingAddress: BillingAddress,
    @SerializedName("Product_Plan_Name__c")
    val productPlanNameC: String = "",
    @SerializedName("Product_Name__c")
    val productNameC: String = ""
) : Serializable

data class BillingAddress(
    @SerializedName("city")
    val city: String = "",
    @SerializedName("country")
    val country: String = "",
    @SerializedName("geocodeAccuracy")
    val geocodeAccuracy: String = "",
    @SerializedName("latitude")
    val latitude: String = "",
    @SerializedName("longitude")
    val longitude: String = "",
    @SerializedName("postalCode")
    val postalCode: String = "",
    @SerializedName("state")
    val state: String,
    @SerializedName("street")
    val street: String
)