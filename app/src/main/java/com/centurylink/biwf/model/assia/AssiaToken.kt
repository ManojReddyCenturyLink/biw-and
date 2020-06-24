package com.centurylink.biwf.model.assia

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AssiaToken(
    @SerializedName("access_token")
    val accessToken: String = "",
    @SerializedName("token_type")
    val tokenType: String = "",
    @SerializedName("refresh_token")
    val refreshToken: String = ""
) : Serializable