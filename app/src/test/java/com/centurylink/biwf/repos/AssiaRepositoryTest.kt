package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.speedtest.SpeedTestRequestResult
import com.centurylink.biwf.model.speedtest.SpeedTestResponse
import com.centurylink.biwf.model.speedtest.SpeedTestStatus
import com.centurylink.biwf.repos.assia.AssiaTokenManager
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.service.network.AssiaTokenService
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

class AssiaRepositoryTest : BaseRepositoryTest() {

    private lateinit var assiaRepository: AssiaRepository

    @MockK(relaxed = true)
    private lateinit var assiaService: AssiaService

    @MockK(relaxed = true)
    private lateinit var assiaTokenService: AssiaTokenService

    @MockK
    private lateinit var mockPreferences: Preferences

    @MockK
    private lateinit var assiaTokenManager: AssiaTokenManager

    private lateinit var modemInfoResponse: ModemInfoResponse

    private lateinit var devicesInfo: DevicesInfo

    private lateinit var speedTestRequestResult: SpeedTestRequestResult

    private lateinit var speedTestStatus: SpeedTestStatus

    private lateinit var speedTestResponse: SpeedTestResponse

    private lateinit var blockResponse: BlockResponse

    private lateinit var assiaToken: AssiaToken

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns "12345"
        modemInfoResponse = fromJson(readJson("lineinfo.json"))
        devicesInfo = fromJson(readJson("devicedetails.json"))
        speedTestRequestResult = fromJson(readJson("speedtest-req.json"))
        speedTestStatus = fromJson(readJson("speedtest-status.json"))
        speedTestResponse = fromJson(readJson("speedtest-response.json"))
        blockResponse = fromJson(readJson("blockunblock-response.json"))
        assiaToken = AssiaToken("", "", "")
        assiaTokenManager = AssiaTokenManager(assiaTokenService)
        assiaRepository = AssiaRepository(mockPreferences, assiaService, assiaTokenManager)
    }

    @Test
    fun testGetModemInfoSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaService.getModemInfo(any()) } returns Either.Right(modemInfoResponse)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
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
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                modemInfoResponse= ModemInfoResponse(code = Constants.ERROR_CODE_1064,modemInfo = ModemInfo())
                coEvery { assiaService.getModemInfo(any()) } returns Either.Right(modemInfoResponse)

                val modemInfo = assiaRepository.getModemInfo()
                Assert.assertEquals(modemInfo.mapLeft { it }, Either.Left(""))

            }
        }
    }

    @Test
    fun testGetModemInfoForcePingSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaService.getModemInfo(any()) } returns Either.Right(modemInfoResponse)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
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
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                modemInfoResponse= ModemInfoResponse(code =Constants.ERROR_CODE_1064,modemInfo = ModemInfo())
                coEvery { assiaService.getModemInfo(any()) } returns Either.Right(modemInfoResponse)

                val modemInfo = assiaRepository.getModemInfoForcePing()
                Assert.assertEquals(modemInfo.mapLeft { it }, Either.Left(""))
            }
        }
    }

    @Test
    fun testBlockDeviceSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaService.blockDevice(any(),any(),any()) } returns Either.Right(blockResponse)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                val speedTestInformation = assiaRepository.blockDevices("")
                Assert.assertEquals(
                    speedTestInformation.map { it.code},
                    Either.Right(Constants.ERROR_CODE_1000)
                )
                Assert.assertEquals(
                    speedTestInformation.map { it.data},
                    Either.Right("true")
                )
            }
        }
    }

    @Test
    fun testBlockDeviceFailure() {
        runBlocking {
            launch {
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                blockResponse= BlockResponse(code = Constants.ERROR_CODE_1064,message = "",data = "")
                coEvery { assiaService.blockDevice(any(),any(),any()) } returns Either.Right(blockResponse)
                val speedTestInfo = assiaRepository.blockDevices("")
                Assert.assertEquals(speedTestInfo.mapLeft { it }, Either.Left(""))
            }
        }
    }

    @Test
    fun testUnBlockDeviceSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaService.unBlockDevice(any(),any(),any()) } returns Either.Right(blockResponse)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                val speedTestInformation = assiaRepository.unblockDevices("")
                Assert.assertEquals(
                    speedTestInformation.map { it.code},
                    Either.Right(Constants.ERROR_CODE_1000)
                )
                Assert.assertEquals(
                    speedTestInformation.map { it.data},
                    Either.Right("true")
                )
            }
        }
    }

    @Test
    fun testUnBlockDeviceFailure() {
        runBlocking {
            launch {
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                blockResponse= BlockResponse(code = Constants.ERROR_CODE_1064,message = "",data = "")
                coEvery { assiaService.unBlockDevice(any(),any(),any()) } returns Either.Right(blockResponse)
                val speedTestInfo = assiaRepository.unblockDevices("")
                Assert.assertEquals(speedTestInfo.mapLeft { it }, Either.Left(""))
            }
        }
    }
}