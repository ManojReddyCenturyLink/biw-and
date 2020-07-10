package com.centurylink.biwf.model.assia

import com.google.gson.annotations.SerializedName

data class ModemRebootResponse(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("data")
    val data: Boolean = false,
    @SerializedName("message")
    val message: String = ""
)