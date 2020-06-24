package com.centurylink.biwf.service

import com.centurylink.biwf.BaseServiceTest
import com.centurylink.biwf.Either
import com.centurylink.biwf.model.FiberServiceResult
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.service.network.NotificationService
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class NotificationServiceTest : BaseServiceTest() {

    private lateinit var notificationService: NotificationService

    @Before
    fun setup() {
        createServer()
        notificationService = retrofit.create(NotificationService::class.java)
    }

    @Test
    fun testGetNotificationDetailsSuccess() = runBlocking {
        enqueueResponse("notifications.json")
        val posts: FiberServiceResult<NotificationSource> =
            notificationService.getNotificationDetails()
        Assert.assertEquals(
            posts.map { it.notificationlist[0].name },
            Either.Right("CenturyLink Extends Employee Benefits")
        )

    }

    @Test
    fun testGetNotificationDetailsError() = runBlocking {
        val posts: FiberServiceResult<NotificationSource> =
            notificationService.getNotificationDetails()
        Assert.assertEquals(posts.mapLeft { it.status }, Either.Left(0))
    }
}