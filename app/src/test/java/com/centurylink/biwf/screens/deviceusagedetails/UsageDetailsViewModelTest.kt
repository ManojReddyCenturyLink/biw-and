package com.centurylink.biwf.screens.deviceusagedetails

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.mcafee.DevicePauseStatus
import com.centurylink.biwf.model.mcafee.DevicesItem
import com.centurylink.biwf.model.usagedetails.UsageDetails
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.McafeeRepository
import com.centurylink.biwf.repos.assia.NetworkUsageRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.Constants
import com.centurylink.biwf.utility.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UsageDetailsViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: UsageDetailsViewModel

    @MockK
    private lateinit var networkUsageRepository: NetworkUsageRepository

    @MockK
    private lateinit var assiaRepository: AssiaRepository

    @MockK
    private lateinit var mcafeeRepository: McafeeRepository

    @MockK
    private lateinit var modemRebootMonitorService: ModemRebootMonitorService

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @MockK
    private lateinit var deviceData: DevicesData

    @MockK
    private lateinit var devicesInfo: DevicesInfo

    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        devicesInfo = fromJson(readJson("devicedetails.json"))
        deviceData = fromJson(readJson("devicedata.json"))
        val devicesItem = DevicesItem(
            os = "11",osVersion = "11",name = "abc",cspClientId = "123",deviceType = "Andr",enforcementType = emptyList(),id = "",manufacturer = "onePlus"
        )
        val usageDetailsRes =  UsageDetails(
            downloadTraffic = 100.00,
            downloadTrafficUnit = NetworkTrafficUnits.MB_DOWNLOAD,
            uploadTraffic = 100.00,
            uploadTrafficUnit = NetworkTrafficUnits.MB_UPLOAD
        )
        coEvery { networkUsageRepository.getUsageDetails(true, deviceData.stationMac!!) } returns usageDetailsRes
        coEvery { mcafeeRepository.fetchDeviceDetails() } returns Either.Right(listOf(devicesItem))
        coEvery { mcafeeRepository.updateDeviceName(deviceData.mcAfeeDeviceType, "vini1234", deviceData.mcafeeDeviceId) } returns Either.Right("")
        coEvery { assiaRepository.blockDevices("") } returns Either.Right(
            BlockResponse(
                code = Constants.ERROR_CODE_1064,
                message = "",
                data = ""
            )
        )
        coEvery { mcafeeRepository.updateDevicePauseResumeStatus(deviceData.deviceId!!, !deviceData.isPaused) } returns Either.Right(DevicePauseStatus(isPaused = deviceData.isPaused, deviceId = deviceData.deviceId!!))
        coEvery { mcafeeRepository.getDevicePauseResumeStatus(deviceData.deviceId!!) } returns Either.Right(
            DevicePauseStatus(isPaused = deviceData.isPaused, deviceId = deviceData.deviceId!!))
        coEvery { assiaRepository.getDevicesDetails() } returns Either.Right(devicesInfo.devicesDataList)
        run { analyticsManagerInterface }

        viewModel = UsageDetailsViewModel(
            app = BIWFApp(),
            networkUsageRepository = networkUsageRepository,
            assiaRepository = assiaRepository,
            modemRebootMonitorService = modemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface,
            mcafeeRepository = mcafeeRepository
        )
        viewModel.deviceData = fromJson(readJson("devicedata.json"))
        viewModel.macAfeeDeviceId = deviceData.mcafeeDeviceId
        viewModel.staMac = deviceData.stationMac!!
    }

    @Test
    fun requestDailyUsageDetailsApiCall() =
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.initApis())
            }
        }

    @Test
    fun testMonthlyUsageDetailsApiCall() {
        runBlockingTest {
            launch {
                val usageDetailsRes =  UsageDetails(
                        downloadTraffic = 1000.00,
                        downloadTrafficUnit = NetworkTrafficUnits.GB_DOWNLOAD,
                        uploadTraffic = 1000.00,
                        uploadTrafficUnit = NetworkTrafficUnits.GB_UPLOAD
                )
                coEvery { networkUsageRepository.getUsageDetails(false, deviceData.stationMac!!) } returns usageDetailsRes
                Assert.assertNotNull(viewModel.initApis())
            }
        }
    }

    @Test
    fun testRequestStateForDevicesSuccess() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.initApis())
            }
        }
    }

    @Test
    fun testInitApisFailure() {
        runBlockingTest {
            launch {
                coEvery { mcafeeRepository.fetchDeviceDetails() } returns Either.Left("")
                coEvery { assiaRepository.blockDevices("") } returns Either.Left("")
                coEvery { mcafeeRepository.getDevicePauseResumeStatus(deviceData.deviceId!!) } returns Either.Left("")
                Assert.assertNotNull(viewModel.initApis())
            }
        }
    }

    @Test
    fun testUpdatePauseResumeStatusFailure() {
        deviceData = fromJson(readJson("devicedata.json"))
        runBlockingTest {
            launch {
                coEvery { mcafeeRepository.updateDevicePauseResumeStatus(deviceData.mcafeeDeviceId!!, !deviceData.isPaused) } returns Either.Left("")
                Assert.assertNotNull(viewModel.onDevicesConnectedClicked())
            }
        }
    }

    @Test
    fun testOnDevicesConnectedClicked() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.onDevicesConnectedClicked())
            }
        }
    }

    @Test
    fun testOnDevicesConnectedPausedStatus() {
        deviceData = fromJson(readJson("device-data.json"))
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.onDevicesConnectedClicked())
            }
        }
    }

    @Test
    fun testOnDevicesConnectedModemOffStatus() {
        viewModel.deviceData =  fromJson(readJson("devicedata-modemoff.json"))
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.onDevicesConnectedClicked())
            }
        }
    }

    @Test
    fun testValidateInput() {
        Assert.assertNotNull(viewModel.validateInput("Vini123"))
    }

    @Test
    fun testInvokeBlockedDevicesSuccess() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.removeDevices("stationMac"))
            }
        }
    }

    @Test
    fun testInvokeBlockedDevicesFailure() {
        runBlockingTest {
            launch {
                coEvery { assiaRepository.blockDevices(any()) } returns Either.Left("Error DeviceInfo")
                Assert.assertNotNull(viewModel.removeDevices("stationMac"))
            }
        }
    }

    @Test
    fun testUpdatePauseResumeStatusSuccess() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.initApis())
            }
        }
    }

    @Test
    fun `on Remove Devices Clicked`() =
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.removeDevices(""))
            }
        }

    @Test
    fun `on Done Clicked`() =
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.onDoneBtnClick("vini1234"))
            }
        }

    @Test
    fun `on updateDeviceName Failure`() =
        runBlockingTest {
            coEvery { mcafeeRepository.updateDeviceName(deviceData.mcAfeeDeviceType, "vini1234", deviceData.mcafeeDeviceId) } returns Either.Left("")
            launch {
                Assert.assertNotNull(viewModel.onDoneBtnClick("vini1234"))
            }
        }

    @Test
    fun logAnalytics() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.onDoneBtnClick(""))
                Assert.assertNotNull(viewModel.onRemoveDevicesClicked())
                Assert.assertNotNull(viewModel.onDevicesConnectedClicked())
                Assert.assertNotNull(viewModel.logRemoveConnection(true))
                Assert.assertNotNull(viewModel.logRemoveConnection(false))
            }
        }
    }

    @Test
    fun validateInputTest(){
        runBlockingTest {
            launch {
                Assert.assertEquals(false, viewModel.validateInput("nickname"))
            }
        }
    }
}