package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class UsageDetailsCoordinatorTest:BaseRepositoryTest() {

    private lateinit var usageDetailsCoordinator: UsageDetailsCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        usageDetailsCoordinator = UsageDetailsCoordinator(navigator)
    }


    @Test
    fun navigateToDeviceConnectedSuccess(){
        val det = usageDetailsCoordinator.navigateTo(UsageDetailsCoordinatorDestinations.DEVICES_CONNECTED)
        assertEquals(det, Unit)
    }
}