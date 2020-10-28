package com.centurylink.biwf.model.account

import com.google.gson.annotations.SerializedName

/**
 * Model class for common attributes
 */
data class Attributes(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("url")
    val url: String = ""
)
