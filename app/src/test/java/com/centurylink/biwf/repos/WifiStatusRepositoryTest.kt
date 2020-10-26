package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.AssiaErrorMessage
import com.centurylink.biwf.model.AssiaHttpError
import com.centurylink.biwf.model.wifi.NetWorkBand
import com.centurylink.biwf.model.wifi.UpdateNetworkResponse
import com.centurylink.biwf.repos.assia.WifiStatusRepository
import com.centurylink.biwf.service.network.WifiStatusService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class WifiStatusRepositoryTest : BaseRepositoryTest() {

    private lateinit var wifiStatusRepository: WifiStatusRepository

    @MockK(relaxed = true)
    private lateinit var wifiStatusService: WifiStatusService

    private lateinit var updateNetworkResponse: UpdateNetworkResponse

    @MockK
    private lateinit var mockPreferences: Preferences

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        val updateresponseString = readJson("apigee_enable_response.json")
        updateNetworkResponse = fromJson(updateresponseString)
        wifiStatusRepository = WifiStatusRepository(mockPreferences, wifiStatusService)
    }

    @Test
    fun testWifiEnableNetwork() {
        runBlocking {
            launch {
                coEvery { wifiStatusService.enableNetwork(any()) } returns Either.Right(
                    updateNetworkResponse
                )
                val wifiStatusInfo = wifiStatusRepository.enableNetwork(NetWorkBand.Band2G)
                Assert.assertEquals(wifiStatusInfo.map { it.code }, Either.Right("1000"))
                Assert.assertEquals(wifiStatusInfo.map { it.message }, Either.Right("Success"))
            }
        }
    }

    @Test
    fun testWifiDisnableNetwork() {
        runBlocking {
            launch {
                coEvery { wifiStatusService.disableNetwork(any()) } returns Either.Right(
                    updateNetworkResponse
                )
                val wifiStatusInfo = wifiStatusRepository.disableNetwork(NetWorkBand.Band2G)
                Assert.assertEquals(wifiStatusInfo.map { it.code }, Either.Right("1000"))
                Assert.assertEquals(wifiStatusInfo.map { it.message }, Either.Right("Success"))
            }
        }
    }

    @Test
    fun testdisableNetworkError() {
        runBlocking {
            launch {
                val asiaError: AssiaHttpError = AssiaHttpError(
                    Constants.STATUS_CODE,
                    AssiaErrorMessage(error = "1001", message = Constants.ERROR)
                )
                coEvery { wifiStatusService.disableNetwork(any()) } returns Either.Left(
                    asiaError
                )
                val statusInfo = wifiStatusRepository.disableNetwork(NetWorkBand.Band2G)
                Assert.assertEquals(statusInfo.mapLeft { it }, Either.Left(Constants.ERROR))
            }
        }
    }

    @Test
    fun testEnableNetworkError() {
        runBlocking {
            launch {
                val asiaError: AssiaHttpError = AssiaHttpError(
                    Constants.STATUS_CODE,
                    AssiaErrorMessage(error = "1001", message = Constants.ERROR)
                )
                coEvery { wifiStatusService.enableNetwork(any()) } returns Either.Left(
                    asiaError
                )
                val statusInfo = wifiStatusRepository.enableNetwork(NetWorkBand.Band2G)
                Assert.assertEquals(statusInfo.mapLeft { it }, Either.Left(Constants.ERROR))
            }
        }
    }
}
