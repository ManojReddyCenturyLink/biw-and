package com.centurylink.biwf.model.support

import com.centurylink.biwf.model.user.RecentItems
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ScheduleCallbackResponse(
    @SerializedName("controllerValues")
    var controllerValues: Any = {},
    @SerializedName("defaultValues")
    var defaultValue: String? = null,
    @SerializedName("eTag")
    var eTag: String = "",
    @SerializedName("url")
    var url: String = "",
    @SerializedName("values")
    var values:List<Values> = emptyList()
    ) : Serializable

data class Values(
    @SerializedName("attributes")
    var attributes: String? = null,
    @SerializedName("label")
    var label: String = "",
    @SerializedName("validFor")
    var validFor: Any? = {},
    @SerializedName("value")
    var value: String = ""
) : Serializable