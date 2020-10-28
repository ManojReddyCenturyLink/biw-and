package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.support.SupportServicesResponse
import com.centurylink.biwf.service.network.SupportService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.any
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SupportRepositoryTest: BaseRepositoryTest() {
    private lateinit var supportRepository: SupportRepository

    @MockK(relaxed = true)
    private lateinit var supportService: SupportService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var supportServiceResult: SupportServicesResponse

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        supportServiceResult = fromJson(readJson("supportservice-response.json"))
        supportRepository = SupportRepository(mockPreferences, supportService)
    }

    @Test
    fun testSupportServiceSuccess() {
        runBlockingTest {
            coEvery {
                supportService.supportServiceInfo(any())
            } returns Either.Right(supportServiceResult)
            val supportServicesResponse=supportRepository.supportServiceInfo(any())

            Assert.assertEquals(supportServicesResponse.map { it.message }, Either.Right("Call back request has been succuessfully created! "))

        }
    }
}