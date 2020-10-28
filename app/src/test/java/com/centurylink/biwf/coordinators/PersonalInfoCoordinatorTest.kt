package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class PersonalInfoCoordinatorTest : BaseRepositoryTest() {

    private lateinit var personalInfoCoordinator: PersonalInfoCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        personalInfoCoordinator = PersonalInfoCoordinator(navigator)
    }

    @Test
    fun navigateToPersonalInfoSuccess() {
        val det = personalInfoCoordinator.navigateTo(PersonalInfoCoordinatorDestinations.DONE)
        assertEquals(det, Unit)
    }
}
