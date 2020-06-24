package com.centurylink.biwf.model.usagedetails

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UsageDetailsHeader(
    @SerializedName("staMac")
    val staMac: String,
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String
) : Serializable