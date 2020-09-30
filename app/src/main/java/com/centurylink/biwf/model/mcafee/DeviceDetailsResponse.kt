package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName
import com.typesafe.config.ConfigException

data class DeviceDetailsResponse(
    @SerializedName("code")
    val code: String = "",
    @SerializedName("devices")
    val devices: List<DevicesItem>,
    @SerializedName("message")
    val message: String = ""
)

data class DevicesItem(
    @SerializedName("os")
    val os: String? = null,
    @SerializedName("os_version")
    val osVersion: String? = null,
    @SerializedName("name")
    val name: String = "",
    @SerializedName("csp_client_id")
    val cspClientId: ConfigException? = null,
    @SerializedName("device_type")
    val deviceType: String = "",
    @SerializedName("enforcement_type")
    val enforcementType: List<Integer>?,
    @SerializedName("id")
    val id: String = "",
    @SerializedName("manufacturer")
    val manufacturer: String = ""
)