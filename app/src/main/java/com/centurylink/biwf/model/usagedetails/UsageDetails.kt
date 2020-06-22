package com.centurylink.biwf.model.usagedetails

import com.google.gson.annotations.SerializedName

data class UsageDetails(
    @SerializedName("code")
    val code: Int = 0,
    @SerializedName("data")
    val data: Data,
    @SerializedName("message")
    val message: String = ""
)

data class Data(
    @SerializedName("name")
    val name: String = "",
    @SerializedName("list")
    val list: List<NetworkListItem>?
)

data class NetworkListItem(
    @SerializedName("downLinkTraffic")
    val downLinkTraffic: Double = 0.0,
    @SerializedName("trafficPattern")
    val trafficPattern: TrafficPattern,
    @SerializedName("upLinkPackets")
    val upLinkPackets: Int = 0,
    @SerializedName("upLinkTraffic")
    val upLinkTraffic: Double = 0.0,
    @SerializedName("intf")
    val intf: String = "",
    @SerializedName("stationMac")
    val stationMac: String = "",
    @SerializedName("downLinkPackets")
    val downLinkPackets: Int = 0,
    @SerializedName("downLinkPacketsFailed")
    val downLinkPacketsFailed: Int = 0,
    @SerializedName("timestamp")
    val timestamp: String = ""
)

data class TrafficPattern(
    @SerializedName("stdTxPacketsPerSecond")
    val stdTxPacketsPerSecond: Double = 0.0,
    @SerializedName("stdRxPacketsPerSecond")
    val stdRxPacketsPerSecond: Double = 0.0,
    @SerializedName("stdTxBytesPerSecond")
    val stdTxBytesPerSecond: Double = 0.0,
    @SerializedName("avgRxBytesPerPacket")
    val avgRxBytesPerPacket: Double = 0.0,
    @SerializedName("avgTxBytesPerPacket")
    val avgTxBytesPerPacket: Double = 0.0,
    @SerializedName("stdRxBytesPerSecond")
    val stdRxBytesPerSecond: Double = 0.0,
    @SerializedName("numSamplesWithTraffic")
    val numSamplesWithTraffic: Int = 0,
    @SerializedName("stdRxBytesPerPacket")
    val stdRxBytesPerPacket: Double = 0.0,
    @SerializedName("avgTxPacketsPerSecond")
    val avgTxPacketsPerSecond: Double = 0.0,
    @SerializedName("avgTxBytesPerSecond")
    val avgTxBytesPerSecond: Double = 0.0,
    @SerializedName("stdTxBytesPerPacket")
    val stdTxBytesPerPacket: Double = 0.0,
    @SerializedName("avgRxPacketsPerSecond")
    val avgRxPacketsPerSecond: Double = 0.0,
    @SerializedName("avgRxBytesPerSecond")
    val avgRxBytesPerSecond: Double = 0.0,
    @SerializedName("numSamples")
    val numSamples: Int = 0
)

