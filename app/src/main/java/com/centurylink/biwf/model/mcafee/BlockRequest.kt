package com.centurylink.biwf.model.mcafee

import com.google.gson.annotations.SerializedName

/**
 * Model class for block device request info
 */
data class BlockRequest(@SerializedName("serialNumber") val serialNumber: String,
                        @SerializedName("deviceId") val macAddress: String)