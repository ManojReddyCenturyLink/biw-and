package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName

/**
 * Data class for Support Services Info
 */
data class SupportServicesReq(
    @SerializedName("UserId") var userId: String? = "",
    @SerializedName("Phone") var phone: String? = "",
    @SerializedName("ASAP") var aSAP: String? = "",
    @SerializedName("CustomerCareOption") var customerCareOption: String? = "",
    @SerializedName("HandleOption") var handleOption: String? = "",
    @SerializedName("CallbackTime") var callbackTime: String? = "",
    @SerializedName("CallbackReason") var callbackReason: String? = ""
) : java.io.Serializable
