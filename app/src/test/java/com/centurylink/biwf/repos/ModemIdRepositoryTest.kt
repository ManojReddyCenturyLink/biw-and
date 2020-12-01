package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.modem.Attributes
import com.centurylink.biwf.model.modem.ModemIdResponse
import com.centurylink.biwf.model.modem.Records
import com.centurylink.biwf.service.network.response.ModemIdService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ModemIdRepositoryTest : BaseRepositoryTest() {

    private lateinit var modemIdRepository: ModemIdRepository

    @MockK(relaxed = true)
    private lateinit var modemIdService: ModemIdService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var modemIdResponse: ModemIdResponse
    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val jsonString = readJson("modem-id_response.json")
        modemIdResponse = fromJson(jsonString)
        modemIdRepository = ModemIdRepository(modemIdService, mockPreferences)
    }

    @Test
    fun testGetModemIdSuccess() {
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        runBlocking {
            launch {
                coEvery { modemIdService.getModemId(any()) } returns Either.Right(modemIdResponse)
                val modemIdDetails = modemIdRepository.getModemTypeId()
                Assert.assertEquals(
                    modemIdDetails.map { it },
                    Either.Right("C4000XG2002005365")
                )
            }
        }
    }
    @Test
    fun testGetModemIdFailure() {
        runBlocking {
            launch {
                coEvery { modemIdService.getModemId(any()) } returns Either.Right(modemIdResponse)
                val modemIdDetails = modemIdRepository.getModemTypeId()
                Assert.assertEquals(
                    modemIdDetails.map { it },
                    Either.Left("Account ID is not available")
                )
            }
        }
    }

    @Test
    fun testGetEmptyModemTypeIdError() {
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        runBlocking {
            launch {

                val attributes = Attributes(type = "", url = "")
                val records = Records(attributes, modemNumberC = "")
                modemIdResponse = ModemIdResponse(totalSize = 1, done = true, records = listOf())
                coEvery { modemIdService.getModemId(any()) } returns Either.Right(modemIdResponse)

                val modemIdDetails = modemIdRepository.getModemTypeId()
                Assert.assertEquals(
                    modemIdDetails.mapLeft { it },
                    Either.Left("Modem Id is Empty")
                )
            }
        }
    }
}
