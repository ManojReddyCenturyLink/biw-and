package com.centurylink.biwf.model.speedtest

import com.google.gson.annotations.SerializedName

data class SpeedTestStatusRequest(
    @SerializedName("callBackUrl")
    val callBackUrl: String = "",
    @SerializedName("requestId")
    val requestId: String = "",
    @SerializedName("assiaId")
    val assiaId: String = ""
)
