package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class CancelSubscriptionCoordinatorTest : BaseRepositoryTest() {

    private lateinit var cancelSubscriptionCoordinator: CancelSubscriptionCoordinator

    @MockK
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        cancelSubscriptionCoordinator = CancelSubscriptionCoordinator()
        cancelSubscriptionCoordinator.navigator = Navigator()
    }

    @Test
    fun navigateToCancelSubscriptionSuccess() {
        every { navigator.navigateToCancelSubscriptionDetails() } returns Unit
        val det = cancelSubscriptionCoordinator.navigateTo(CancelSubscriptionCoordinatorDestinations.CANCEL_SELECT_DATE_SUBSCRIPTION)
        assertEquals(det, Unit)
    }
}
