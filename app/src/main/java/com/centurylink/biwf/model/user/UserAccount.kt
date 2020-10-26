package com.centurylink.biwf.model.user

import com.google.gson.annotations.SerializedName

/**
 * Data class for user account details
 */
data class UserAccount(
    @SerializedName("BillingStreet")
    val billingStreetAddress: String? = null,

    @SerializedName("BillingCity")
    val billingCity: String? = null,

    @SerializedName("BillingState")
    val billingState: String? = null,

    @SerializedName("BillingPostalCode")
    val billingZipCode: String? = null,

    @SerializedName("ShippingStreet")
    val serviceAddress: String? = null,

    @SerializedName("ShippingCity")
    val serviceCity: String? = null,

    @SerializedName("ShippingState")
    val serviceState: String? = null,

    @SerializedName("ShippingPostalCode")
    val serviceZipcode: String? = null,

    @SerializedName("Phone")
    val phoneNumber: String? = null,

    @SerializedName("Account_Activation_Date__c")
    val accountActivationDate: String? = null,

    @SerializedName("Cell_Phone_Opt_In__c")
    val cellphoneOptIn: Boolean? = null,

    @SerializedName("Email_Opt_In__c")
    val emailOptIn: Boolean? = null,

    @SerializedName("Email__c")
    val accountEmail: String? = null,

    @SerializedName("FirstName__c")
    val accountFirstName: String? = null,

    @SerializedName("LastName__c")
    val accountLastName: String? = null,

    @SerializedName("Marketing_Opt_In__c")
    val marketingOptIn: Boolean? = null,

    @SerializedName("Product_Name__c")
    val productName: String? = null,

    @SerializedName("Product_Plan_Name__c")
    val productSecondName: String? = null
)
