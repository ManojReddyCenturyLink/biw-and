package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class DevicesCoordinatorTest : BaseRepositoryTest() {

    private lateinit var devicesCoordinator: DevicesCoordinator

    @MockK
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        devicesCoordinator = DevicesCoordinator(navigator)
    }

    @Test
    fun navigateToUsageDetailsSuccess(){
        every { navigator.navigateToNotificationDetails() } returns Unit
        val det = devicesCoordinator.navigateTo(DevicesCoordinatorDestinations.DEVICE_DETAILS)
        assertEquals(det, Unit)
    }
}