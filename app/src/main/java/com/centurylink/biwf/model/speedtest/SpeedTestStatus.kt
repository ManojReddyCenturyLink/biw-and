package com.centurylink.biwf.model.speedtest

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SpeedTestStatus(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: SpeedTestStatusNestedResults
):Serializable

data class SpeedTestStatusNestedResults(
    @SerializedName("currentStep")
    val currentStep:String,
    @SerializedName("finished")
    val isFinished:Boolean
):Serializable