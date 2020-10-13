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

class FAQCoordinatorTest : BaseRepositoryTest() {
    private lateinit var faqCoordinator: FAQCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        faqCoordinator = FAQCoordinator()
        faqCoordinator.navigator = Navigator()
        SupportCoordinatorDestinations.bundle = Bundle()
    }

    @Test
    fun navigateToScheduleCallbackSuccess(){
        every {navigator.navigateToScheduleCallbackFromFAQ()} returns any()
        val det = faqCoordinator.navigateTo(FAQCoordinatorDestinations.SCHEDULE_CALLBACK)
        assertEquals(det, Unit)
    }
}