package com.centurylink.biwf.model.wifi
import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for update network name
 */
data class UpdateNetworkName(
    @SerializedName("newSsid")
    val newSsid: String
) : Serializable

