package com.centurylink.biwf.model.user

import com.google.gson.annotations.SerializedName

/**
 * Data class for update password details
 */
class UpdatedPassword(
    @SerializedName("NewPassword")
    val password: String? = null
)
