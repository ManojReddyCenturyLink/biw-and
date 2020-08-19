package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName

enum class NetworkType {
    @SerializedName("Band5G")
    Band5G,

    @SerializedName("Band2G")
    Band2G
}