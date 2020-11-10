package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class HomeCoordinatorTest : BaseRepositoryTest() {
    private lateinit var homeCoOrdinator: HomeCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        homeCoOrdinator = HomeCoordinator()
        homeCoOrdinator.navigator = Navigator()
        HomeCoordinatorDestinations.bundle = Bundle()
    }

    /*@Test
    fun navigateToNetworkStatusActivitySuccess() {
        every { navigator.navigateToNetworkInformationScreen() } returns any()
        val det = homeCoOrdinator.navigateTo(HomeCoordinatorDestinations.NETWORK_STATUS)
        assertEquals(det, Unit)
    }*/

    @Test
    fun navigateToNotificationListSuccess() {
        every { navigator.navigateToNotificationList() } returns any()
        val det = homeCoOrdinator.navigateTo(HomeCoordinatorDestinations.NOTIFICATION_LIST)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateTonavigateToSupportSuccess() {
        every { navigator.navigateToSupport() } returns any()
        val det = homeCoOrdinator.navigateTo(HomeCoordinatorDestinations.SUPPORT)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateTonavigateToNavigationDetailsSuccess() {
        every { navigator.navigateToNotificationDetails() } returns any()
        val det = homeCoOrdinator.navigateTo(HomeCoordinatorDestinations.NOTIFICATION_DETAILS)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToSubscriptionActivitySuccess() {
        every { navigator.navigateToSubscriptionActivity() } returns any()
        val det = homeCoOrdinator.navigateTo(HomeCoordinatorDestinations.SUBSCRIPTION_ACTIVITY)
        assertEquals(det, Unit)
    }
}
