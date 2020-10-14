package com.centurylink.biwf.model.sumup

import com.google.gson.annotations.SerializedName

/**
 * Data class for local server integration request details
 */
data class SumUpInput(
    @SerializedName("value3")
    val value3: Int
)
