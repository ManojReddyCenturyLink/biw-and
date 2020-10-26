package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.mcafee.DevicesMapping
import com.centurylink.biwf.model.usagedetails.Data
import com.centurylink.biwf.model.usagedetails.NetworkListItem
import com.centurylink.biwf.model.usagedetails.TrafficPattern
import com.centurylink.biwf.model.usagedetails.TrafficUsageResponse
import com.centurylink.biwf.repos.BaseRepositoryTest
import com.centurylink.biwf.service.network.AssiaTokenService
import com.centurylink.biwf.service.network.AssiaTrafficUsageService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test

class NetworkUsageRepositoryTest : BaseRepositoryTest() {

    private lateinit var networkUsageRepository: NetworkUsageRepository

    @MockK(relaxed = true)
    private lateinit var assiaTrafficUsageService: AssiaTrafficUsageService

    @MockK(relaxed = true)
    private lateinit var integrationRestServices: IntegrationRestServices

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var devicesMapping: DevicesMapping

    @MockK
    private lateinit var assiaTokenManager: AssiaTokenManager

    @MockK(relaxed = true)
    private lateinit var assiaTokenService: AssiaTokenService

    private lateinit var assiaToken: AssiaToken

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        devicesMapping = fromJson(readJson("device-mapping.json"))
        assiaToken = AssiaToken("", "", "")
        assiaTokenManager = AssiaTokenManager(assiaTokenService)
        networkUsageRepository = NetworkUsageRepository(
            assiaTokenManager,
            assiaTrafficUsageService,
            integrationRestServices,
            mockPreferences
        )
    }

    @Test
    fun testGetUsageDetailsSuccess() {
        runBlocking {
            launch {
                val networkListItem = NetworkListItem(downLinkTraffic = 999.9, upLinkTraffic = 999000.0, upLinkPackets = 1, trafficPattern = TrafficPattern(), downLinkPackets =
                1, intf = "", stationMac = "", downLinkPacketsFailed = 1)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery {
                    assiaTrafficUsageService.getUsageDetails(
                        any(),
                        any(),
                        any(),
                        any()
                    )
                } returns Either.Right(
                    TrafficUsageResponse(
                        code = 0,
                        data = Data(name = "", list = listOf(networkListItem)),
                        message = "Success",
                        uniqueErrorCode = 1,
                        createErrorRecord = false
                    )
                )

                val usagedetails = networkUsageRepository.getUsageDetails(false, "")
                assertEquals(usagedetails.downloadTraffic, 0.9999)
            }
        }
    }

    @Test
    fun testGetUsageDetailsSuccessHighUploadTraffic() {
        runBlocking {
            launch {
                val networkListItem = NetworkListItem(downLinkTraffic = 999000.9, upLinkTraffic = 999000.88, upLinkPackets = 1, trafficPattern = TrafficPattern(), downLinkPackets =
                1, intf = "", stationMac = "", downLinkPacketsFailed = 1)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery {
                    assiaTrafficUsageService.getUsageDetails(
                        any(),
                        any(),
                        any(),
                        any()
                    )
                } returns Either.Right(
                    TrafficUsageResponse(
                        code = 0,
                        data = Data(name = "", list = listOf(networkListItem)),
                        message = "Success",
                        uniqueErrorCode = 1,
                        createErrorRecord = false
                    )
                )

                val usagedetails = networkUsageRepository.getUsageDetails(false, "")
                assertEquals(usagedetails.downloadTraffic, 0.9990009)
            }
        }
    }

    @Test
    fun testGetUsageDetailsSuccessWithDailyData() {
        runBlocking {
            launch {
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery {
                    assiaTrafficUsageService.getUsageDetails(
                        any(),
                        any(),
                        any(),
                        any()
                    )
                } returns Either.Right(
                    TrafficUsageResponse(
                        code = 0,
                        data = Data(name = "", list = listOf()),
                        message = "Success",
                        uniqueErrorCode = 1,
                        createErrorRecord = false
                    )
                )

                val usagedetails = networkUsageRepository.getUsageDetails(true, "")
                assertEquals(usagedetails.downloadTraffic, 0.0)
            }
        }
    }
}
