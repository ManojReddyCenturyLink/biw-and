package com.centurylink.biwf.model.wifi
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UpdateNetworkName(
    @SerializedName("newSsid")
    val newSsid: String
) : Serializable

