package com.centurylink.biwf.model.devices

import com.google.gson.annotations.SerializedName

data class DevicesInfo(

    @SerializedName("error")
    val error: String? = null,
    @SerializedName("code")
    val code: String? = null,
    @SerializedName("message")
    val message: String? = null,
    @SerializedName("data")
    var devicesDataList: List<DevicesData> = emptyList()
)

data class DevicesData(
    @SerializedName("stationMac")
    val stationMac: String? = null,
    @SerializedName("hostname")
    val hostName: String? = null,
    @SerializedName("vendorName")
    val vendorName: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("lastConnectionDate")
    val lastConnectionDate: String? = null,
    @SerializedName("firstConnectionTime")
    val firstConnectionTime: String? = null,
    @SerializedName("connectedInterface")
    val connectedInterface: String? = null,
    @SerializedName("ipAddress")
    val ipAddress: String? = null,

    @SerializedName("blocked")
    val blocked: Boolean = false,
    @SerializedName("rssi")
    val rssi: Int? = null,
    @SerializedName("parent")
    val parent: String? = null,
    @SerializedName("txRateKbps")
    val txRateKbps: String? = null,
    @SerializedName("rxRateKbps")
    val rxRateKbps: String? = null,
    @SerializedName("userDefinedProfile")
    val userDefinedProfile: String? = null,
    @SerializedName("location")
    val location: String? = null,
    @SerializedName("deviceId")
    val deviceId: String? = null,

    @SerializedName("maxMode")
    val maxMode: MaxMode? = null,

    @SerializedName("maxSpeed")
    val maxSpeed: MaxSpeed? = null,

    //TODO: Adding temporary variable to test analytics story, will remove once api gets integrated.
    var isPaused: Boolean = false
)

data class MaxMode(
    @SerializedName("Band2G")
    val band2G: String? = null,
    @SerializedName("Band5G")
    val band5G: String? = null
)

data class MaxSpeed(
    @SerializedName("Band2G")
    val band2G: String? = null,
    @SerializedName("Band5G")
    val band5G: String? = null
)

