package com.centurylink.biwf.model.speedtest

import com.google.gson.annotations.SerializedName

/**
 * Data class for speed test response details
 */
data class SpeedTestResponse(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: SpeedTestNestedResults
)
