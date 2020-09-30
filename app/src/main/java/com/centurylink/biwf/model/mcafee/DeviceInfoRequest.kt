package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName

data class DeviceInfoRequest(
    @SerializedName("deviceType")
    val deviceType: String = "",
    @SerializedName("serialNumber")
    val serialNumber: String = "",
    @SerializedName("name")
    val name: String = "",
    @SerializedName("deviceId")
    val deviceId: String = ""
)