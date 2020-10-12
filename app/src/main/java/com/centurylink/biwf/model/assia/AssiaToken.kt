package com.centurylink.biwf.model.assia

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Model class for assia token info
 */
data class AssiaToken(
    @SerializedName("access_token")
    val accessToken: String = "",
    @SerializedName("token_type")
    val tokenType: String = "",
    @SerializedName("refresh_token")
    val refreshToken: String = ""
) : Serializable