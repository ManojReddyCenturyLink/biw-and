package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test

class NotificationCoordinatorTest :BaseRepositoryTest() {

    private lateinit var notificationCoordinator: NotificationCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        notificationCoordinator = NotificationCoordinator()
        notificationCoordinator.navigator = Navigator()
    }

    @Test
    fun navigateToNotificationDetailsSucess(){
        every {navigator.navigateToNotificationDetails()} returns Unit
        notificationCoordinator.navigateTo(NotificationCoordinatorDestinations.NOTIFICATION_DETAILS)
    }
}