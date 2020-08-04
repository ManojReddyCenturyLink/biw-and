package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for get device info from mcafee server
 */
data class DevicesMapping(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("mac_address")
    val macAddress: String = "",
    @SerializedName("mac_device_list")
    val macDeviceList: List<MacDeviceList> = emptyList()
) : Serializable

data class MacDeviceList(
    @SerializedName("devices") val devices: List<Devices>,
    @SerializedName("mac_address") val mac_address: String
) : Serializable

data class Devices(
    @SerializedName("id") val id: String,
    @SerializedName("member_id") val memberId: String
) : Serializable