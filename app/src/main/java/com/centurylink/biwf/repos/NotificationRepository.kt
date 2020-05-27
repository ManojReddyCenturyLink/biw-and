package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.service.network.NotificationService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val apiServices: NotificationService
) {
    suspend fun getNotificationDetails(): Either<String, NotificationSource> {
        val result: FiberServiceResult<NotificationSource> =
            apiServices.getNotificationDetails()
        return result.mapLeft { it.message?.message.toString() }
    }
}