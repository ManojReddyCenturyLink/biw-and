package com.centurylink.biwf.model.devices

import com.google.gson.annotations.SerializedName

/**
 * Model class for block device info
 */
data class BlockResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val data: String
)