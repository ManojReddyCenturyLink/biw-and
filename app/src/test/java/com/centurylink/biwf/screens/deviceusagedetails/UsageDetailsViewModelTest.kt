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
    }

    @Test
    fun requestDailyUsageDetailsApiCall() =
        runBlockingTest {
            var devicesItem = DevicesItem(
                os = null,osVersion = "",name = "",cspClientId = "",deviceType = "",enforcementType = emptyList(),id = "",manufacturer = ""
            )
            launch {
                coEvery { mcafeeRepository.fetchDeviceDetails() } returns Either.Right(listOf(devicesItem))
                viewModel.initApis()
            }
        }

    @Test
    fun testDailyUsageDetailsApiCall() {
        runBlockingTest {
            val usageDetailsRes =  UsageDetails(
                downloadTraffic = 100.00,
                downloadTrafficUnit = NetworkTrafficUnits.MB_DOWNLOAD,
                uploadTraffic = 100.00,
                uploadTrafficUnit = NetworkTrafficUnits.MB_UPLOAD
            )
            launch {
                try {
                    coEvery { networkUsageRepository.getUsageDetails(true, "") } returns usageDetailsRes
                }catch (e: Exception){}
                viewModel.initApis()
            }
        }
    }

    @Test
    fun testRequestStateForDevicesSuccess() {
        runBlockingTest {
            launch {
                coEvery { mcafeeRepository.getDevicePauseResumeStatus("") } returns Either.Right(
                    DevicePauseStatus(
                        isPaused = false,
                        deviceId = "00-24-9B-1C149E1B5C613E615643D83783622040F97F4089B0507B451CDD097322BA48EF"
                    )
                )
                viewModel.initApis()
            }
        }
    }

    @Test
    fun testRequestStateForDevicesFailure() {
        runBlockingTest {
            launch {
                coEvery { assiaRepository.blockDevices("") } returns Either.Left("")
                viewModel.initApis()
            }
        }
    }

    @Test
    fun testOnDevicesConnectedClicked() {
        deviceData = fromJson(readJson("devicedata.json"))
        viewModel.onDevicesConnectedClicked()
    }

    @Test
    fun testOnDevicesConnectedPausedStatus() {
        deviceData = fromJson(readJson("device-data.json"))
        viewModel.onDevicesConnectedClicked()
    }

    @Test
    fun testValidateInput() {
        viewModel.validateInput("Vini123")
    }

    @Test
    fun testInvokeBlockedDevicesSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaRepository.blockDevices("") } returns Either.Right(
                    BlockResponse(
                        code = Constants.ERROR_CODE_1064,
                        message = "",
                        data = ""
                    )
                )
                viewModel.removeDevices("stationMac")
            }
        }
    }

    @Test
    fun testInvokeBlockedDevicesFailure() {
        runBlockingTest {
            launch {
                coEvery { assiaRepository.blockDevices(any()) } returns Either.Left("Error DeviceInfo")
                viewModel.removeDevices("stationMac")
            }
        }
    }

    @Test
    fun testUpdatePauseResumeStatusSuccess() {
        deviceData = fromJson(readJson("devicedata.json"))
        runBlockingTest {
            launch {
                coEvery {
                    mcafeeRepository.updateDevicePauseResumeStatus(
                        "",
                        !deviceData.isPaused
                    )
                } returns Either.Right(
                    DevicePauseStatus(
                        isPaused = true,
                        deviceId = ""
                    )
                )
                viewModel.initApis()
            }
        }
    }

    @Test
    fun testUpdatePauseResumeStatusFailure() {
        deviceData = fromJson(readJson("devicedata.json"))
        runBlockingTest {
            launch {
                coEvery {
                    mcafeeRepository.updateDevicePauseResumeStatus(
                        "",
                        !deviceData.isPaused
                    )
                } returns Either.Left("")
                viewModel.initApis()
            }
        }
    }

    @Test
    fun `on Remove Devices Clicked`() =
        runBlockingTest {
            launch {
                viewModel.removeDevices("")
            }
        }

    @Test
    fun logAnalytics() {
        viewModel.onDoneBtnClick("vini1234")
        viewModel.onRemoveDevicesClicked()
        viewModel.logRemoveConnection(true)
        viewModel.logRemoveConnection(false)
    }
}