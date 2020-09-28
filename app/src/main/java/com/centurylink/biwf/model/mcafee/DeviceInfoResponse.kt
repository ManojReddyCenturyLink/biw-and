package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName

data class DeviceInfoResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = ""
)