package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName

/**
 * Data class for get devices mapping request info
 */
data class MappingRequest(
    @SerializedName("serialNumber") val serialNumber: String,
    @SerializedName("mac_address") val deviceMacAddresses: List<String>
)
