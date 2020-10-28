package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName

/**
 * Data class for network brand types details
 */
enum class NetWorkBand {
    @SerializedName("Band5G")
    Band5G,

    @SerializedName("Band2G")
    Band2G,

    @SerializedName("Band2G_Guest4")
    Band2G_Guest4,

    @SerializedName("Band5G_Guest4")
    Band5G_Guest4
}

enum class NetWorkCategory {
    REGULAR,
    GUEST
}
