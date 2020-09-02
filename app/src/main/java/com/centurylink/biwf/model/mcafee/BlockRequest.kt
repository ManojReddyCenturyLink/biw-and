package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName

data class BlockRequest(@SerializedName("serialNumber") val serialNumber: String,
                        @SerializedName("deviceId") val macAddress: String)