package com.centurylink.biwf.model.user

import com.google.gson.annotations.SerializedName

class UpdatedPassword(
    @SerializedName("NewPassword")
    val password: String? = null
)