package com.centurylink.biwf.model.devices

import com.google.gson.annotations.SerializedName

/**
 * Model class for block device request
 */
data class BlockDeviceRequest(
    @SerializedName("assiaId")
    val assiaId: String = "",
    @SerializedName("stationMacAddress")
    val stationMacAddress: String = ""
)