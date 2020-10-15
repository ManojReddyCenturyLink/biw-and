package com.centurylink.biwf.coordinators


import android.os.Bundle
import com.centurylink.biwf.repos.BaseRepositoryTest
import io.mockk.MockKAnnotations
import io.mockk.MockKStaticScope
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.amshove.kluent.any
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class AdditionalInfoCoordinatorTest : BaseRepositoryTest() {

    private lateinit var additionalInfoCoordinator: AdditionalInfoCoordinator


    @MockK
    private lateinit var navigator: Navigator

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        AdditionalInfoCoordinatorDestinations.bundle= Bundle()
        additionalInfoCoordinator = AdditionalInfoCoordinator()
        additionalInfoCoordinator.navigator = Navigator()
    }

    @Test
    fun navigateToContactInfoSuccess(){
        every { navigator.navigateToContactInfo() } returns Unit
        val det = additionalInfoCoordinator.navigateTo(AdditionalInfoCoordinatorDestinations.CONTACT_INFO)
        assertEquals(det, Unit)
    }
}