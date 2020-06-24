package com.centurylink.biwf.model.assia

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ModemInfoResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val modemInfo: ModemInfo
) : Serializable

data class ModemInfo(
    @SerializedName("deviceId")
    val deviceId: String = "",
    @SerializedName("lineId")
    val lineId: String = "",
    @SerializedName("modelName")
    val modelName: String = "",
    @SerializedName("isAlive")
    val isAlive: Boolean = false
) : Serializable