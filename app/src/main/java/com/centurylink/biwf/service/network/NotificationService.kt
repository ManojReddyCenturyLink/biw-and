package com.centurylink.biwf.service.network

import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.notification.NotificationSource
import retrofit2.http.GET

interface NotificationService {

    @GET("notifications.json")
    fun getNotificationDetails(): FiberServiceResult<NotificationSource>
}