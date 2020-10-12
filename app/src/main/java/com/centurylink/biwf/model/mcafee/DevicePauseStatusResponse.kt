package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName

/**
 * Model class for block devices pause status response info
 */
data class DevicePauseStatusResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("blocked")
    val blocked: Boolean = false
)

class DevicePauseStatus(val isPaused: Boolean,val deviceId: String)

data class DevicePauseStatusRequest(
    @SerializedName("deviceId")
    val deviceId: String = "",
    @SerializedName("serialNumber")
    val serialNumber: String = "",
    @SerializedName("blocked")
    val blocked: Boolean = false
)

data class DeviceUpdateResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = ""
)

