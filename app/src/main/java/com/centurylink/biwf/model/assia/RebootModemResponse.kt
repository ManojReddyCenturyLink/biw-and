package com.centurylink.biwf.model.assia

import com.google.gson.annotations.SerializedName

/**
 * Model class for reboot modem info
 */
data class ModemRebootResponse(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("data")
    val data: Boolean = false,
    @SerializedName("message")
    val message: String = ""
)