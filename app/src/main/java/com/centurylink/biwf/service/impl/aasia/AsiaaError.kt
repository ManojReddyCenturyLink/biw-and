package com.centurylink.biwf.service.impl.aasia

import com.google.gson.annotations.SerializedName

data class AsiaaError (

    @SerializedName("error")
    val error: String,

    @SerializedName("error_description")
    val message: String
)