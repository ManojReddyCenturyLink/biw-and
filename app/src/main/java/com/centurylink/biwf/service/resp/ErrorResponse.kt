package com.centurylink.biwf.service.resp

import com.google.gson.annotations.SerializedName

data class ErrorResponse ( @SerializedName("Id")
                           val Id: String = "",
                           @SerializedName("AccountId")
                           val accountId: String = "")