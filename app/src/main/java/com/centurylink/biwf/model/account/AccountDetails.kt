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
    val emailAddress: String? = null,
    @SerializedName("BillingAddress")
    val billingAddress: BillingAddress,
    @SerializedName("Product_Plan_Name__c")
    val productPlanNameC: String? = null,
    @SerializedName("Product_Name__c")
    val productNameC: String? = null,
    @SerializedName("Service_Address__c")
    val serviceCompleteAddress: String? = null,
    @SerializedName("Service_City__c")
    val serviceCity: String? = null,
    @SerializedName("Service_Country__c")
    val serviceCountry: String? = null,
    @SerializedName("Service_State_Province__c")
    val serviceStateProvince: String? = null,
    @SerializedName("Service_Street__c")
    val serviceStreet: String? = null,
    @SerializedName("Service_Zip_Postal_Code__c")
    val servicePostalCode: String? = null,
    @SerializedName("Phone")
    val phone: String? = null,
    @SerializedName("Secondary_Phone__c")
    val secondaryPhone: String? = null,
    @SerializedName("Cell_Phone_Opt_In__c")
    val cellPhoneOptInC: Boolean,
    @SerializedName("Payment_Method_Name__c")
    val paymentMethodName: String? = null,
    @SerializedName("LastViewedDate")
    val lastViewedDate: String? = null

) : Serializable

data class BillingAddress(
    @SerializedName("city")
    val city: String? = "",
    @SerializedName("country")
    val country: String? = "",
    @SerializedName("geocodeAccuracy")
    val geocodeAccuracy: String? = "",
    @SerializedName("latitude")
    val latitude: String? = "",
    @SerializedName("longitude")
    val longitude: String? = "",
    @SerializedName("postalCode")
    val postalCode: String? = "",
    @SerializedName("state")
    val state: String? = "",
    @SerializedName("street")
    val street: String? = ""
) : Serializable