package com.centurylink.biwf.coordinators

import android.os.Bundle
import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class SupportCoordinatorTest : BaseRepositoryTest() {

    private lateinit var supportCoordinator: SupportCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        supportCoordinator = SupportCoordinator()
        supportCoordinator.navigator = Navigator()
        SupportCoordinatorDestinations.bundle = Bundle()
    }

    @Test
    fun navigateToFaqSuccess() {
        every { navigator.navigateToFaq() } returns any()
        val det = supportCoordinator.navigateTo(SupportCoordinatorDestinations.FAQ)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToScheduleCallback() {
        every { navigator.navigateToScheduleCallbackFromFAQ() } returns any()
        val det = supportCoordinator.navigateTo(SupportCoordinatorDestinations.SCHEDULE_CALLBACK)
        assertEquals(det, Unit)
    }

    @Test
    fun navigateToLiveChatSuccess() {
        val det = supportCoordinator.navigateTo(SupportCoordinatorDestinations.LIVE_CHAT)
        assertEquals(det, Unit)
    }
}
