package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName

/**
 * Data class for Support Services Info
 */
data class SupportServicesReq(@SerializedName("UserId") var UserId: String? = "",
                              @SerializedName("Phone") var Phone: String? = "",
                              @SerializedName("ASAP") var ASAP: String? = "",
                              @SerializedName("CustomerCareOption") var CustomerCareOption: String? = "",
                              @SerializedName("HandleOption") var HandleOption: String? = "",
                              @SerializedName("CallbackTime") var CallbackTime: String? = "",
                              @SerializedName("CallbackReason") var CallbackReason: String? = ""
): java.io.Serializable