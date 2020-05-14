package com.centurylink.biwf.model.account

import com.google.gson.annotations.SerializedName

data class Attributes(
    @SerializedName("type")
    val type: String = "",
    @SerializedName("url")
    val url: String = ""
)