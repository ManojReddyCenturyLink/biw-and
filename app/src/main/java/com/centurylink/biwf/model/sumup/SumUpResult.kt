package com.centurylink.biwf.model.sumup

import com.google.gson.annotations.SerializedName

/**
 * Data class for local server integration response details
 */
data class SumUpResult(
    @SerializedName("value")
    val value: Int
)
