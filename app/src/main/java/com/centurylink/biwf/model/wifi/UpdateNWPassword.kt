package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for update network password
 */
data class UpdateNWPassword(
    @SerializedName("newPassword")
    val newPassword: String
) : Serializable
