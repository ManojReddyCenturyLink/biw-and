package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ContactInfoCoordinatorTest : BaseRepositoryTest() {

    private lateinit var changeAppointmentCoordinator: ContactInfoCoordinator

    @MockK
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        changeAppointmentCoordinator = ContactInfoCoordinator()
        changeAppointmentCoordinator.navigator = Navigator()
        ContactInfoCoordinatorDestinations.bundle = Bundle()
    }

    @Test
    fun navigateToSelectTimeSuccess(){
        every { navigator.navigateToSelectTime() } returns Unit
        val det = changeAppointmentCoordinator.navigateTo(ContactInfoCoordinatorDestinations.SELECT_TIME)
        assertEquals(det, Unit)
    }
}