package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for network details
 */
data class NetworkDetails(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("message")
    val message: String = "",
    @SerializedName("data")
    val networkName: HashMap<String, String>
) : Serializable
