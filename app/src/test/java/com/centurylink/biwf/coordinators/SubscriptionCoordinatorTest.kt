package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SubscriptionCoordinatorTest:BaseRepositoryTest() {

    private lateinit var subscriptionCoordinator: SubscriptionCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        subscriptionCoordinator = SubscriptionCoordinator(navigator)
    }

    @Test
    fun navigateToEditPaymentDetailsSuccess(){
        every { navigator.navigateToEditPaymentDetails()} returns any()
        val det = subscriptionCoordinator.navigateTo(SubscriptionCoordinatorDestinations.EDIT_PAYMENT)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToInvoiceDetailsSuccess(){
        every {navigator.navigateToBillStatement()} returns any()
        val det = subscriptionCoordinator.navigateTo(SubscriptionCoordinatorDestinations.STATEMENT)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToManageSubscriptionSuccess(){
        every { navigator.navigateToMangeSubscription()} returns any()
        val det = subscriptionCoordinator.navigateTo(SubscriptionCoordinatorDestinations.MANAGE_MY_SUBSCRIPTION)
        assertEquals(det, Unit)
    }
}