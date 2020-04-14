package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Videofaq(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("thumbnail") var thumbNailUrl: String? = null,
    @SerializedName("duration") var duration: String? = null,
    @SerializedName("videourl") var videoUrl: String? = null
) : Serializable