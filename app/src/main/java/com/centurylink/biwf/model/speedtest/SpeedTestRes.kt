package com.centurylink.biwf.model.speedtest

import com.google.gson.annotations.SerializedName

data class SpeedTestRes(
    @SerializedName("callBackUrl")
    val callBackUrl: String = "",
    @SerializedName("createErrorRecord")
    val createErrorRecord: Boolean = false,
    @SerializedName("requestId")
    val requestId: String = "",
    @SerializedName("success")
    val success: Boolean = false,
    @SerializedName("assiaId")
    val assiaId: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("uniqueErrorCode")
    val uniqueErrorCode: Int = 0,
    @SerializedName("status")
    val status: String = ""
)
