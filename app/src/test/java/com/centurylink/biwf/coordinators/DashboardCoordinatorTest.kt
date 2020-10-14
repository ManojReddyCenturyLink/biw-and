package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DashboardCoordinatorTest : BaseRepositoryTest() {

    private lateinit var dashboardCoordinator: DashboardCoordinator

    @MockK
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        dashboardCoordinator = DashboardCoordinator()
        dashboardCoordinator.navigator = Navigator()
        DashboardCoordinatorDestinations.bundle = Bundle()
    }

    @Test
    fun navigateToChangeAppointmentSuccess(){
        every { navigator.navigateToChangeAppointment() } returns Unit
        val det = dashboardCoordinator.navigateTo(DashboardCoordinatorDestinations.CHANGE_APPOINTMENT)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToNotificationDetailsSuccess(){
        every { navigator.navigateToNotificationDetails() }returns Unit
        val det = dashboardCoordinator.navigateTo(DashboardCoordinatorDestinations.NOTIFICATION_DETAILS)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToNetworkInformationSuccess(){
        every { navigator.navigateToNetworkInformationScreen() }returns Unit
        val det = dashboardCoordinator.navigateTo(DashboardCoordinatorDestinations.NETWORK_INFORMATION)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToQRCodeScanningSuccess(){
        every { navigator.navigateToQRCodeScan() }returns Unit
        val det = dashboardCoordinator.navigateTo(DashboardCoordinatorDestinations.QR_CODE_SCANNING)
        assertEquals(det, Unit)
    }
}