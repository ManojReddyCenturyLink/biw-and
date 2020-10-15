package com.centurylink.biwf.coordinators


import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AccountCoordinatorTest{

    private lateinit var accountCoordinator: AccountCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        accountCoordinator = AccountCoordinator(navigator)
        every { navigator.navigateToLoginScreen() } returns any()
    }

    @Test
    fun navigateToLogInActivitySuccess(){
        every { navigator.navigateToLoginScreen() } returns any()
        val det = accountCoordinator.navigateTo(AccountCoordinatorDestinations.LOG_IN)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToPersonalInfoActivitySuccess(){
        every { navigator.navigateToLoginScreen() } returns any()
        val det = accountCoordinator.navigateTo(AccountCoordinatorDestinations.PROFILE_INFO)
        assertEquals(det, Unit)
    }
}