package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class NetworkDetails(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val networkName: HashMap<String,String>
) : Serializable

