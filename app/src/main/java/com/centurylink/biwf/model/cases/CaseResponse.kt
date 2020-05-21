package com.centurylink.biwf.model.cases

import com.google.gson.annotations.SerializedName

class CaseResponse(
    @SerializedName("id")
    val Id: String = "",
    @SerializedName("success")
    val success: Boolean
)