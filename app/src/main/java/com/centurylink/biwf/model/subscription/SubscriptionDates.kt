package com.centurylink.biwf.model.subscription

import com.google.gson.annotations.SerializedName

class SubscriptionDates(@SerializedName("totalSize")
                        val totalSize: Int = 0,
                        @SerializedName("records")
                        val records: List<Records>,
                        @SerializedName("done")
                        val done: Boolean = false)