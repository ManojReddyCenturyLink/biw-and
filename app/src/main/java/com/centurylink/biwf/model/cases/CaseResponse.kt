package com.centurylink.biwf.model.cases

import com.google.gson.annotations.SerializedName

/**
 * Model class for case subscription create response details
 */
class CaseResponse(
    @SerializedName("id")
    val Id: String = "",
    @SerializedName("success")
    val success: Boolean
)
