package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName

/**
 * Model class for block devices response info
 */
data class DeviceInfoResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = ""
)
