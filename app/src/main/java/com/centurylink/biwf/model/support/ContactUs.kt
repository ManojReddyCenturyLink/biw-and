package com.centurylink.biwf.model.support

import com.google.gson.annotations.SerializedName

data class ContactUS(@SerializedName("liveChatTimings")
                     val liveChatTimings: String = "",
                     @SerializedName("liveChatWebUrl")
                     val liveChatWebUrl: String = "",
                     @SerializedName("scheduleCallbackTimings")
                     val scheduleCallbackTimings: String = "",
                     @SerializedName("phoneNo")
                     val phoneNo: String = "")

data class ContactUsModel (@SerializedName("contactUS")
                     val contactUS: ContactUS
)

