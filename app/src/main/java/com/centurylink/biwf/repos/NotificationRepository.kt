package com.centurylink.biwf.repos

import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.service.network.IntegrationRestServices
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiServices: IntegrationRestServices
) {
    suspend fun getNotificationDetails(): NotificationSource {
        val result: NotificationSource =
            apiServices.getNotificationDetails("notifications")
        return result
    }
}