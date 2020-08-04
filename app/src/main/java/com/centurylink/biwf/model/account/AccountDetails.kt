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
    @SerializedName("AccountStatus__c")
    var accountStatus: String = "",
    @SerializedName("DTN__c")
    var lineId: String? = null,
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
    val billingAddress: BillingAddress? = null,
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
    val lastViewedDate: String? = null,
    @SerializedName("ShippingAddress")
    val shippingAddress: BillingAddress? = null
) : Serializable

data class BillingAddress(
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("country")
    val country: String? = null,
    @SerializedName("geocodeAccuracy")
    val geocodeAccuracy: String? = null,
    @SerializedName("latitude")
    val latitude: String? = null,
    @SerializedName("longitude")
    val longitude: String? = null,
    @SerializedName("postalCode")
    val postalCode: String? = null,
    @SerializedName("state")
    val state: String? = null,
    @SerializedName("street")
    val street: String? = null
) : Serializable