package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName

data class MappingRequest(
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("mac_address") val deviceMacAddresses: List<String>
)

