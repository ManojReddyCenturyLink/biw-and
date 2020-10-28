package com.centurylink.biwf.coordinators

import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class StatementCoordinatorTest : BaseRepositoryTest() {

    private lateinit var statementCoordinator: StatementCoordinator

    @MockK(relaxed = true)
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        statementCoordinator = StatementCoordinator()
    }

    @Test
    fun navigateToFAQSuccess() {
        val det = statementCoordinator.navigateTo(StatementCoordinatorDestinations.FAQ)
        assertEquals(det, Unit)
    }
}
