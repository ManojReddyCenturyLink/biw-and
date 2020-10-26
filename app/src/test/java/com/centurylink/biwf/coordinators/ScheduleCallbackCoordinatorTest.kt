package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ScheduleCallbackCoordinatorTest : BaseRepositoryTest() {
    private lateinit var scheduleCallbackCoordinator: ScheduleCallbackCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        scheduleCallbackCoordinator = ScheduleCallbackCoordinator(navigator)
    }

    @Test
    fun navigateToAdditionalInfoSuccess() {
        every { navigator.navigateToAdditionalInfo() } returns any()
        val det = scheduleCallbackCoordinator.navigateTo(ScheduleCallbackCoordinatorDestinations.ADDITIONAL_INFO)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToPhoneDiallerSuccess() {
        every { navigator.navigateToPhoneDialler() } returns any()
        val det = scheduleCallbackCoordinator.navigateTo(ScheduleCallbackCoordinatorDestinations.CALL_SUPPORT)
        assertEquals(det, Unit)
    }
}
