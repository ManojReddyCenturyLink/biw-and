package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.speedtest.SpeedTestRes
import com.centurylink.biwf.model.speedtest.SpeedTestStatusResponse
import com.centurylink.biwf.repos.assia.SpeedTestRepository
import com.centurylink.biwf.service.network.AssiaTokenService
import com.centurylink.biwf.service.network.SpeedTestService
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SpeedTestRepositoryTest : BaseRepositoryTest() {

    @MockK(relaxed = true)
    private lateinit var speedTestService: SpeedTestService

    @MockK(relaxed = true)
    private lateinit var assiaTokenService: AssiaTokenService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var speedTestRepository: SpeedTestRepository

    private lateinit var speedTestRequestResult: SpeedTestRes

    private lateinit var speedTestResponse: SpeedTestStatusResponse

    private lateinit var assiaToken: AssiaToken

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        speedTestRequestResult = fromJson(readJson("speedtest-req.json"))
        speedTestResponse = fromJson(readJson("speedtest-response.json"))
        assiaToken = AssiaToken("", "", "")
        speedTestRepository = SpeedTestRepository(mockPreferences, speedTestService)
    }

    @Test
    fun testStartSpeedTestSuccess() {
        runBlockingTest {
            launch {
                coEvery { speedTestService.getSpeedTestDetails(any()) } returns Either.Right(
                    speedTestRequestResult
                )
                val speedTestInformation = speedTestRepository.startSpeedTest()
                Assert.assertEquals(
                    speedTestInformation.map { it.success },
                    Either.Right(true)
                )
                Assert.assertEquals(
                    speedTestInformation.map { it.code },
                    Either.Right(1000)
                )
            }
        }
    }

    @Test
    fun testStartSpeedTestFailure() {
        runBlocking {
            launch {
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                speedTestRequestResult = SpeedTestRes()
                coEvery { speedTestService.getSpeedTestDetails(any()) } returns Either.Right(
                    speedTestRequestResult
                )
                val speedTestInfo = speedTestRepository.startSpeedTest()
                Assert.assertEquals(speedTestInfo.mapLeft { it }, Either.Left("Request not found"))
            }
        }
    }

    @Test
    fun testCheckSpeedTestStatusSuccess() {
        runBlockingTest {
            launch {
                coEvery { speedTestService.getSpeedTestStatusDetails(any()) } returns Either.Right(
                    speedTestResponse
                )
                val speedTestInformation = speedTestRepository.checkSpeedTestStatus("")
                Assert.assertEquals(
                    speedTestInformation.map { it.message },
                    Either.Right("Success")
                )
            }
        }
    }

    @Test
    fun testGetSpeedTestStatusDetails() {
        runBlockingTest {
            launch {
                coEvery { speedTestService.getSpeedTestStatusDetails(any()) } returns Either.Right(
                    speedTestResponse
                )
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                val speedTestInformation = speedTestRepository.getSpeedTestResults("")
                Assert.assertEquals(
                    speedTestInformation.map { it.success },
                    Either.Right(true)
                )
            }
        }
    }
}