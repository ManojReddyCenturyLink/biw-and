package com.centurylink.biwf.model.subscriptionDetails

import com.google.gson.annotations.SerializedName

data class SubscriptionDetails(
    @SerializedName("totalSize") val totalSize: Int,
    @SerializedName("done") val done: Boolean,
    @SerializedName("records") val records: List<Records>
)
