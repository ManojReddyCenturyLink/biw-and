package com.centurylink.biwf.model.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class UpdatedPassword (@SerializedName("NewPassword")
                       @Expose
                       val password: String? = null)