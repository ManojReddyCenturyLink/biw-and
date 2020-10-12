package com.centurylink.biwf.model.notification

import com.google.gson.annotations.SerializedName
import java.io.Serializable

/**
 * Data class for notification source details
 */
data class NotificationSource(
    @SerializedName("notifications") var
    notificationlist: List<Notification> = emptyList()
) : Serializable