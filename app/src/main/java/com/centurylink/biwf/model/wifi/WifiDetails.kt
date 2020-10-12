package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName

/**
 * Data class for wifi details
 */
data class WifiDetails( @SerializedName("wifiInfo") var
                        wifiList: List<WifiInfo> = emptyList())