package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for wifi info
 */
data class WifiInfo(@SerializedName("category")var category: NetWorkCategory?=null,
                    @SerializedName("type") var type: String? = null,
                    @SerializedName("name") var name: String? = null,
                    @SerializedName("password") var password: String? = null,
                    @SerializedName("enabled") var enabled: Boolean? = null) : Serializable