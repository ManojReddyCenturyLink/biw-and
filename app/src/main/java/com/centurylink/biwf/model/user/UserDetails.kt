package com.centurylink.biwf.model.user

import com.google.gson.annotations.SerializedName

/**
 * Data class for user details
 */
data class UserDetails(
    @SerializedName("Id")
    val Id: String? = null,

    @SerializedName("Username")
    val username: String? = null,

    @SerializedName("LastName")
    val lastName: String? = null,

    @SerializedName("FirstName")
    val firstName: String? = null,

    @SerializedName("MiddleName")
    val middleName: String? = null,

    @SerializedName("Suffix")
    val suffix: String? = null,

    @SerializedName("Name")
    val name: String? = null,

    @SerializedName("CompanyName")
    val companyName: String? = null,

    @SerializedName("ContactId")
    val contactId: String? = null,

    @SerializedName("AccountId")
    val accountId: String? = null,

    @SerializedName("CallCenterId")
    val callCenterId: String? = null,

    @SerializedName("Email")
    val email: String? = null
)
