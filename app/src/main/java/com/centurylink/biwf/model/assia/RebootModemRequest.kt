package com.centurylink.biwf.model.assia

import com.google.gson.annotations.SerializedName

/**
 * Model class for reboot modem request
 */
data class RebootModemRequest(
    @SerializedName("assiaId")
    val assiaId: String = ""
)
