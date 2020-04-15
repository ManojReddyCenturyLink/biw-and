package com.centurylink.biwf.model

import com.google.gson.annotations.SerializedName

data class TroubleshootingModel(@SerializedName("troubleshooting") val troubleshooting: Troubleshooting)

data class Troubleshooting(@SerializedName("timeStamp") val timeStamp: String = "",
                           @SerializedName("downloadSpeed") val downloadSpeed: String = "",
                           @SerializedName("uploadSpeed") val uploadSpeed: String = "",
                           @SerializedName("webUrl") val webUrl: String = "")