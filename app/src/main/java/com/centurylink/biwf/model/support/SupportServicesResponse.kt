package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName

/**
 * Data class for Support Services info
 */
data class SupportServicesResponse(
    @SerializedName("status") var status: String? = "",
    @SerializedName("message") var message: String? = ""
) : java.io.Serializable
