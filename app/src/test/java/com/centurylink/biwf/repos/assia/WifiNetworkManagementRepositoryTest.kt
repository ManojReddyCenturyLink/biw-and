package com.centurylink.biwf.repos.assia

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.repos.BaseRepositoryTest
import com.centurylink.biwf.service.network.AssiaTokenService
import com.centurylink.biwf.service.network.WifiNetworkApiService
import com.centurylink.biwf.service.network.WifiStatusService
import com.centurylink.biwf.utility.preferences.Preferences
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class WifiNetworkManagementRepositoryTest : BaseRepositoryTest() {

    private lateinit var wifiNetworkManagementRepository: WifiNetworkManagementRepository

    @MockK(relaxed = true)
    private lateinit var wifiNetworkApiService: WifiNetworkApiService

    @MockK(relaxed = true)
    private lateinit var wifiStatusService: WifiStatusService

    @MockK
    private lateinit var mockPreferences: Preferences

    @MockK
    private lateinit var assiaTokenManager: AssiaTokenManager

    @MockK(relaxed = true)
    private lateinit var assiaTokenService: AssiaTokenService

    private lateinit var assiaToken: AssiaToken

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        assiaToken = AssiaToken("", "", "")
        assiaTokenManager = AssiaTokenManager(assiaTokenService)
        wifiNetworkManagementRepository = WifiNetworkManagementRepository(
            preferences = mockPreferences,
            wifiNetworkApiService = wifiNetworkApiService,
            assiaTokenManager = assiaTokenManager,
            wifiStatusService = wifiStatusService
        )
    }

    @Test
    fun testGetNetworkNameSuccess() {
        runBlocking {
            launch {

                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery {
                    wifiNetworkApiService.getNetworkName(any(), any(), any())
                } returns Either.Right(
                    NetworkDetails(
                        code = "0",
                        message = "Success",
                        networkName = hashMapOf()
                    )
                )

                val networkDetails =
                    wifiNetworkManagementRepository.getNetworkName(NetWorkBand.Band2G)
                assertEquals(networkDetails.map { it.message }, Either.Right("Success"))
            }
        }
    }

    @Test
    fun testUpdateNetworkNameSuccess() {
        runBlocking {
            launch {

                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery {
                    wifiStatusService.updateNetworkName(any())
                } returns Either.Right(
                    UpdateNetworkResponse(
                        code = "0",
                        message = "Success",
                        data = true,
                        createErrorRecord = true,
                        errorCode = "0"
                    )
                )

                val networkDetails = wifiNetworkManagementRepository.updateNetworkName(
                    " NetWorkBand.Band2G",
                    ""
                )
                assertEquals(networkDetails.map { it.message }, Either.Right("Success"))
            }
        }
    }

    @Test
    fun testGetNetworkPasswordSuccess() {
        runBlocking {
            launch {

                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery {
                    wifiStatusService.getNetworkPassword(any(), any())
                } returns Either.Right(
                    NetworkDetails(
                        code = "0",
                        message = "Success",
                        networkName = hashMapOf()
                    )
                )

                val networkDetails =
                    wifiNetworkManagementRepository.getNetworkPassword(NetWorkBand.Band2G)
                assertEquals(networkDetails.map { it.message }, Either.Right("Success"))
            }
        }
    }

    @Test
    fun testUpdateNetworkPasswordSuccess() {
        runBlocking {
            launch {

                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery {
                    wifiStatusService.updateNetworkPassword(any(), any(), any())
                } returns Either.Right(
                    UpdateNetworkResponse(
                        code = "0",
                        message = "Success",
                        data = true,
                        createErrorRecord = true,
                        errorCode = "0"
                    )
                )

                val networkDetails = wifiNetworkManagementRepository.updateNetworkPassword(
                    NetWorkBand.Band2G,
                    UpdateNWPassword(newPassword = "")
                )
                assertEquals(networkDetails.map { it.message }, Either.Right("Success"))
            }
        }
    }
}
