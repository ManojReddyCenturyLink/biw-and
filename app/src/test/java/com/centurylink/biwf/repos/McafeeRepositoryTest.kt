package com.centurylink.biwf.repos

import com.centurylink.biwf.Either
import com.centurylink.biwf.model.mcafee.DevicePauseStatusResponse
import com.centurylink.biwf.model.mcafee.DeviceUpdateResponse
import com.centurylink.biwf.model.mcafee.DevicesMapping
import com.centurylink.biwf.service.network.McafeeApiService
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

class McafeeRepositoryTest  : BaseRepositoryTest() {

    private lateinit var mcafeeRepository: McafeeRepository

    @MockK(relaxed = true)
    private lateinit var mcafeeApiService: McafeeApiService

    @MockK
    private lateinit var mockPreferences: Preferences

    private lateinit var devicesMapping: DevicesMapping

    private lateinit var devicePauseStatusResponse: DevicePauseStatusResponse

    private lateinit var deviceUpdateResponse: DeviceUpdateResponse

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        every { mockPreferences.getValueByID(any()) } returns Constants.ID
        val accountString = readJson("device-mapping.json")
        devicesMapping = fromJson(accountString)

        val devicePauseString = readJson("device-pause-response.json")
        devicePauseStatusResponse = fromJson(devicePauseString)

        val deviceUpdateString = readJson("device-update-response.json")
        deviceUpdateResponse = fromJson(deviceUpdateString)
        mcafeeRepository = McafeeRepository(mockPreferences, mcafeeApiService)
    }

    @Test
    fun testGetMcafeeDeviceIdsSuccess() {
        runBlocking {
            launch {
                coEvery { mcafeeApiService.getDevicesMapping(any()) } returns Either.Right(
                    devicesMapping
                )
                val deviceList= listOf("AA.BG.FA.CC","AA.BG.FA.11")
                val accountInfo = mcafeeRepository.getMcafeeDeviceIds(deviceList)
                Assert.assertEquals(accountInfo.map { it[0].mac_address }, Either.Right("E0-C7-67-8C-BE-28"))
            }
        }
    }

    @Test
    fun testGetDevicesMappingError() {
        runBlocking {
            launch {

                devicesMapping= DevicesMapping(code = Constants.ERROR_CODE_1000)
                coEvery { mcafeeApiService.getDevicesMapping(any()) } returns Either.Right(
                    devicesMapping
                )
                val accountInfo = mcafeeRepository.getMcafeeDeviceIds(listOf())
                Assert.assertEquals(accountInfo.mapLeft { it }, Either.Left("No Mapping Devices Found "))
            }
        }
    }

    @Test
    fun testGetNetworkInfoSuccess() {
        runBlocking {
            launch {
                coEvery { mcafeeApiService.getNetworkInfo(any()) } returns Either.Right(
                    devicePauseStatusResponse
                )
                val accountInfo = mcafeeRepository.getDevicePauseResumeStatus("")
                Assert.assertEquals(accountInfo.map { it.isPaused }, Either.Right(false))
            }
        }
    }

    @Test
    fun testGetNetworkInfoError() {
        runBlocking {
            launch {

                devicePauseStatusResponse= DevicePauseStatusResponse(code = Constants.ERROR_CODE_1000)
                coEvery { mcafeeApiService.getNetworkInfo(any()) } returns Either.Right(
                    devicePauseStatusResponse
                )
                val accountInfo = mcafeeRepository.getDevicePauseResumeStatus("")
                Assert.assertEquals(accountInfo.mapLeft { it }, Either.Left("No Status  Found "))
            }
        }
    }


    @Test
    fun testUpdateNetWorkInfoSuccess() {
        runBlocking {
            launch {
                coEvery { mcafeeApiService.updateNetWorkInfo(any()) } returns Either.Right(
                    deviceUpdateResponse
                )
                val updateInfo = mcafeeRepository.updateDevicePauseResumeStatus("",true)
                Assert.assertEquals(updateInfo.map { it.isPaused }, Either.Right(true))
            }
        }
    }

    @Test
    fun testUpdateNetWorkInfoError() {
        runBlocking {
            launch {

                deviceUpdateResponse= DeviceUpdateResponse(code = Constants.ERROR_CODE_1000)
                coEvery { mcafeeApiService.updateNetWorkInfo(any()) } returns Either.Right(
                    deviceUpdateResponse
                )
                val accountInfo = mcafeeRepository.updateDevicePauseResumeStatus("",true)
                Assert.assertEquals(accountInfo.mapLeft { it }, Either.Left("No Status  Found "))
            }
        }
    }
}