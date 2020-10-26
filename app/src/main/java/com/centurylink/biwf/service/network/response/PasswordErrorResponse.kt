package com.centurylink.biwf.service.network.response

import com.google.gson.annotations.SerializedName

data class PasswordErrorResponse(
    @SerializedName("errorCode")
    val errorCode: String = "",
    @SerializedName("message")
    val message: String = ""
)
