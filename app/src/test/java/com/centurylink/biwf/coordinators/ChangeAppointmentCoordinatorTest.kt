package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ChangeAppointmentCoordinatorTest : BaseRepositoryTest() {

    private lateinit var changeAppointmentCoordinator: ChangeAppointmentCoordinator

    @MockK
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        changeAppointmentCoordinator = ChangeAppointmentCoordinator()
        changeAppointmentCoordinator.navigator = Navigator()
        ChangeAppointmentCoordinatorDestinations.bundle = Bundle()
    }

    @Test
    fun navigateToAppointmentConfirmationSuccess(){
        every { navigator.navigateToAppointmentConfirmation() } returns Unit
        val det = changeAppointmentCoordinator.navigateTo(ChangeAppointmentCoordinatorDestinations.APPOINTMENT_CONFIRMED)
        assertEquals(det, Unit)
    }
}