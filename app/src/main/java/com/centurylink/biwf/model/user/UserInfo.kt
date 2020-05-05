package com.centurylink.biwf.model.user

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("recentItems")
    @Expose
    val recentItems: List<RecentItems> = emptyList()
)

data class RecentItems(
    @SerializedName("Id")
    @Expose
    val Id: String? = null,

    @SerializedName("Name")
    @Expose
    val name: String? = null
)