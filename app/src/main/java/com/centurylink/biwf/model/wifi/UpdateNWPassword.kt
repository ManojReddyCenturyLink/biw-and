package com.centurylink.biwf.model.wifi

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UpdateNWPassword(
    @SerializedName("newPassword")
    val newPassword: String
) : Serializable
