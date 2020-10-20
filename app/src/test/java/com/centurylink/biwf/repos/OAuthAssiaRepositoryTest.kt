package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class OAuthAssiaRepositoryTest : BaseRepositoryTest() {

    private lateinit var assiaRepository: OAuthAssiaRepository

    @MockK(relaxed = true)
    private lateinit var assiaService: OAuthAssiaService

    @MockK(relaxed = true)
    private lateinit var assiaTokenService: AssiaTokenService

    @MockK
    private lateinit var assiaTokenManager: AssiaTokenManager

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var modemInfoResponse: ModemInfoResponse

    private lateinit var devicesInfo: DevicesInfo

    private lateinit var assiaToken: AssiaToken

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        devicesInfo = fromJson(readJson("devicedetails.json"))
        modemInfoResponse = fromJson(readJson("lineinfo.json"))
        assiaToken = AssiaToken("", "", "")
        assiaTokenManager = AssiaTokenManager(assiaTokenService)
        assiaRepository = OAuthAssiaRepository(mockPreferences, assiaService)
    }

    @Test
    fun testGetModemInfoSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaService.getLineInfo(any(), any()) } returns Either.Right(
                    modemInfoResponse
                )
                val userInformation = assiaRepository.getModemInfo()
                Assert.assertEquals(
                    userInformation.map { it.lineId },
                    Either.Right("C4000XG1950000308")
                )
                Assert.assertEquals(
                    userInformation.map { it.apInfoList[0].modelName },
                    Either.Right("Greenwave C4000XG")
                )
            }
        }
    }

    @Test
    fun testGetModemInfoFailure() {
        runBlocking {
            launch {
                modemInfoResponse = ModemInfoResponse(code = Constants.ERROR_CODE_1064, modemInfo = ModemInfo())
                coEvery { assiaService.getLineInfo(any(), any()) } returns Either.Right(
                    modemInfoResponse
                )
                val modemInfo = assiaRepository.getModemInfo()
                Assert.assertEquals(modemInfo.mapLeft { it }, Either.Left(""))
            }
        }
    }

    @Test
    fun testGetModemInfoForcePingSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaService.getLineInfo(any(), any()) } returns Either.Right(
                    modemInfoResponse
                )
                val modemInfo = assiaRepository.getModemInfoForcePing()
                Assert.assertEquals(
                    modemInfo.map { it.lineId },
                    Either.Right("C4000XG1950000308")
                )
                Assert.assertEquals(
                    modemInfo.map { it.apInfoList[0].modelName },
                    Either.Right("Greenwave C4000XG")
                )
            }
        }
    }

    @Test
    fun testGetModemInfoForcePingFailure() {
        runBlocking {
            launch {
                modemInfoResponse = ModemInfoResponse(code = Constants.ERROR_CODE_1064, modemInfo = ModemInfo())
                coEvery { assiaService.getLineInfo(any(), any()) } returns Either.Right(
                    modemInfoResponse
                )
                val modemInfo = assiaRepository.getModemInfoForcePing()
                Assert.assertEquals(modemInfo.mapLeft { it }, Either.Left(""))
            }
        }
    }

    @Test
    fun testGetDevicesDetailsFailure() {
        runBlocking {
            launch {
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                val devicesData= DevicesData()
                devicesInfo= DevicesInfo(code = Constants.ERROR_CODE_1064,error = "",message = "",devicesDataList = arrayListOf(devicesData) )
                coEvery { assiaService.getDevicesList(any(),any(), any()) } returns Either.Right(devicesInfo)
                val deviceInfo = assiaRepository.getDevicesDetails()
                Assert.assertEquals(deviceInfo.mapLeft { it }, Either.Left(""))
            }
        }
    }

    @Test
    fun testGetDevicesDetailsSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaService.getDevicesList(any(),any(), any()) } returns Either.Right(devicesInfo)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                val deviceInformation = assiaRepository.getDevicesDetails()
                Assert.assertEquals(
                        deviceInformation.map { it[0].vendorName },
                        Either.Right("APPLE")
                )
                Assert.assertEquals(
                        deviceInformation.map { it[0].type },
                        Either.Right("Apple")
                )
            }
        }
    }

}