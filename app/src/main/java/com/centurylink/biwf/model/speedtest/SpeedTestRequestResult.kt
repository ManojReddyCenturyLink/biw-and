package com.centurylink.biwf.model.speedtest

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SpeedTestRequestResult(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val speedTestId: Int = 0
) : Serializable