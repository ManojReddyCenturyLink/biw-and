package com.centurylink.biwf.model.notification

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for notification details
 */
data class Notification(
    @SerializedName("id") var id: String? = null,
    @SerializedName("name") var name: String? = null,
    @SerializedName("description") var description: String? = null,
    @SerializedName("imageUrl") var imageUrl: String? = null,
    @SerializedName("isUnRead") var isUnRead: Boolean,
    @SerializedName("detialUrl") var detialUrl: String? = null
) : Serializable