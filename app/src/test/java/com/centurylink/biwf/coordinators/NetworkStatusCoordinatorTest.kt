package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class NetworkStatusCoordinatorTest : BaseRepositoryTest() {

    private lateinit var networkStatusCoordinator: NetworkStatusCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        networkStatusCoordinator = NetworkStatusCoordinator(navigator)
    }

    @Test
    fun navigateToNetworkStatus() {
        val det = networkStatusCoordinator.navigateTo(NetworkStatusCoordinatorDestinations.DONE)
        assertEquals(det, Unit)
    }
}
