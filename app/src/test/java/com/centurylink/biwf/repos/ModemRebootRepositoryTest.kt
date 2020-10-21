package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.assia.ModemRebootResponse
import com.centurylink.biwf.repos.assia.AssiaTokenManager
import com.centurylink.biwf.service.network.AssiaTokenService
import com.centurylink.biwf.service.network.OAuthAssiaService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ModemRebootRepositoryTest : BaseRepositoryTest() {

    private lateinit var modemRebootRepository: ModemRebootRepository

    @MockK(relaxed = true)
    private lateinit var assiaService: OAuthAssiaService

    @MockK(relaxed = true)
    private lateinit var assiaTokenService: AssiaTokenService

    @MockK
    private lateinit var mockPreferences: Preferences

    @MockK
    private lateinit var assiaTokenManager: AssiaTokenManager


    private lateinit var modemRebootResponse: ModemRebootResponse

    private lateinit var assiaToken: AssiaToken

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        modemRebootResponse = fromJson(readJson("blockunblock-response.json"))

        assiaToken = AssiaToken("", "", "")
        assiaTokenManager = AssiaTokenManager(assiaTokenService)
        modemRebootRepository =
            ModemRebootRepository(mockPreferences, assiaService, assiaTokenManager)
    }

    @Test
    fun testGetModemInfoSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaService.rebootModem(any()) } returns Either.Right(
                    modemRebootResponse
                )
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                val userInformation = modemRebootRepository.rebootModem()
                assertEquals(
                    userInformation.map { it.code },
                    Either.Right(Constants.ERROR_CODE_1000.toInt())
                )
                assertEquals(
                    userInformation.map { it.message },
                    Either.Right(Constants.SUCCESS)
                )
            }
        }
    }
}