package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class LoginCoordinatorTest : BaseRepositoryTest() {

    private lateinit var loginCorordinator: LoginCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        loginCorordinator = LoginCoordinator(navigator)
    }

    @Test
    fun navigateToHomeScreenSuccess(){
        every {navigator.navigateToHomeScreen()} returns any()
        val det = loginCorordinator.navigateTo(LoginCoordinatorDestinations.HOME)
        assertEquals(det,Unit)
    }
}