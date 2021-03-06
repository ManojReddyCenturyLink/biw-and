package com.centurylink.biwf.screens.home.devices

import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.assia.AssiaToken
import com.centurylink.biwf.model.assia.ModemInfoResponse
import com.centurylink.biwf.model.devices.BlockResponse
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.mcafee.DevicePauseStatus
import com.centurylink.biwf.model.mcafee.DevicesMapping
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.repos.McafeeRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.service.network.AssiaService
import com.centurylink.biwf.service.network.AssiaTokenService
import com.centurylink.biwf.service.network.OAuthAssiaService
import com.centurylink.biwf.utility.Constants
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class DevicesViewModelTest : ViewModelBaseTest() {

    private lateinit var viewModel: DevicesViewModel

    @MockK
    private lateinit var devicesRepository: DevicesRepository

    @MockK
    private lateinit var assiaRepository: AssiaRepository

    @MockK
    private lateinit var oAuthAssiaRepository: OAuthAssiaRepository

    @MockK
    private lateinit var mcafeeRepository: McafeeRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @MockK(relaxed = true)
    private lateinit var OAuthAssiaService: OAuthAssiaService

    private lateinit var modemInfoResponse: ModemInfoResponse

    private lateinit var assiaToken: AssiaToken

    @MockK(relaxed = true)
    private lateinit var assiaService: AssiaService

    @MockK(relaxed = true)
    private lateinit var assiaTokenService: AssiaTokenService

    private lateinit var devicesInfo: DevicesInfo

    private lateinit var devicesMapping: DevicesMapping

    private lateinit var deviceData: DevicesData

    private lateinit var deviceDataEmpty: DevicesData

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        devicesInfo = fromJson(readJson("devicedetails.json"))
        modemInfoResponse = fromJson(readJson("lineinfo.json"))
        devicesMapping = fromJson(readJson("device-mapping.json"))
        deviceData = fromJson(readJson("devicedata.json"))
        deviceDataEmpty = fromJson(readJson("device-data.json"))
        assiaToken = AssiaToken("", "", "")
        coEvery { OAuthAssiaService.getLineInfo(any(), any()) } returns Either.Right(
            modemInfoResponse
        )
        coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(modemInfoResponse.modemInfo)
        coEvery { OAuthAssiaService.getDevicesList(any()) } returns Either.Right(devicesInfo)
        coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
        coEvery { oAuthAssiaRepository.getDevicesDetails() } returns Either.Right(devicesInfo.devicesDataList)
        coEvery { mcafeeRepository.getMcafeeDeviceIds(any()) } returns Either.Right(devicesMapping.macDeviceList)
        coEvery { mcafeeRepository.fetchDeviceDetails() } returns Either.Right(listOf())
        coEvery { mcafeeRepository.getDevicePauseResumeStatus(any()) } returns Either.Right(
            DevicePauseStatus(isPaused = true, deviceId = "1234")
        )
        coEvery { mcafeeRepository.updateDevicePauseResumeStatus("1234", true) } returns Either.Right(DevicePauseStatus(isPaused = true, deviceId = "1234"))
        viewModel = DevicesViewModel(
            devicesRepository = devicesRepository,
            asiaRepository = assiaRepository,
            oAuthAssiaRepository = oAuthAssiaRepository,
            mcafeeRepository = mcafeeRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun testDevicesSectionSuccess() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(
                        viewModel.initApis())
            }
        }
    }

    @Test
    fun testDevicesSectionFailure() {
        runBlockingTest {
            launch {
                coEvery { OAuthAssiaService.getLineInfo(any(), any()) } returns Either.Right(
                    modemInfoResponse
                )
                coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Left(Constants.ERROR)
                coEvery { OAuthAssiaService.getDevicesList(any()) } returns Either.Right(devicesInfo)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery { oAuthAssiaRepository.getDevicesDetails() } returns Either.Left(Constants.ERROR)
                coEvery { mcafeeRepository.getMcafeeDeviceIds(any()) } returns Either.Left(Constants.ERROR)
                coEvery { mcafeeRepository.getDevicePauseResumeStatus(any()) } returns Either.Left(
                    Constants.ERROR
                )
                coEvery { mcafeeRepository.fetchDeviceDetails() } returns Either.Left("")
                Assert.assertNotNull(
                        viewModel.initApis())
            }
        }
    }

    @Test
    fun testLogRestoreConnectionSuccess() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logRestoreConnection(true))
            }
        }
    }

    @Test
    fun testLogRestoreConnectionFail() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logRestoreConnection(false))
            }
        }
    }

    @Test
    fun testLogConnectionStatusChangedSuccess() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logConnectionStatusChanged(true))
            }
        }
    }

    @Test
    fun testLogConnectionStatusChangedFail() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logConnectionStatusChanged(false))
            }
        }
    }

    @Test
    fun testLogRemoveDevicesItemClick() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logRemoveDevicesItemClick())
            }
        }
    }

    @Test
    fun testLogListExpandCollapse() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logListExpandCollapse())
            }
        }
    }

    @Test
    fun testLogScreenLaunch() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.logScreenLaunch())
            }
        }
    }

    @Test
    fun testUpdatePauseResumeStatusDeviceIdIsNotNull() {
        coEvery { mcafeeRepository.updateDevicePauseResumeStatus("00-24-9B-1C149E1B5C613E615643D83783622040F97F4089B0507B451CDD097322BA48EF", false) } returns Either.Right(DevicePauseStatus(isPaused = true, deviceId = ""))
        Assert.assertNotNull(viewModel.updatePauseResumeStatus(deviceData))
    }

    @Test
    fun testUpdatePauseResumeStatusDeviceIdIsNull() {
        coEvery { mcafeeRepository.updateDevicePauseResumeStatus("00-24-9B-1C149E1B5C613E615643D83783622040F97F4089B0507B451CDD097322BA48EF", true) } returns Either.Left("Error")
        coEvery { mcafeeRepository.getDevicePauseResumeStatus(any()) } returns Either.Left(
                Constants.ERROR
        )
        Assert.assertNotNull(viewModel.updatePauseResumeStatus(deviceDataEmpty))
    }

    @Test
    fun testNavigateToUsageDetails() {
        runBlockingTest {
            launch {
                Assert.assertNotNull(viewModel.navigateToUsageDetails(devicesInfo.devicesDataList[1]))
            }
        }
    }

    @Test
    fun testUnblockDeviceSuccess() {
        runBlockingTest {
            launch {
                coEvery { assiaRepository.unblockDevices(any()) } returns Either.Right(
                    BlockResponse(code = Constants.ERROR_CODE_1064, message = "", data = "")
                )
                coEvery { OAuthAssiaService.getLineInfo(any(), any()) } returns Either.Right(
                    modemInfoResponse
                )
                coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Left(Constants.ERROR)
                coEvery { OAuthAssiaService.getDevicesList(any()) } returns Either.Right(devicesInfo)
                coEvery { assiaTokenService.getAssiaToken() } returns Either.Right(assiaToken)
                coEvery { oAuthAssiaRepository.getDevicesDetails() } returns Either.Left(Constants.ERROR)
                coEvery { mcafeeRepository.getMcafeeDeviceIds(any()) } returns Either.Left(Constants.ERROR)
                coEvery { mcafeeRepository.getDevicePauseResumeStatus(any()) } returns Either.Left(
                    Constants.ERROR
                )
                coEvery { mcafeeRepository.fetchDeviceDetails() } returns Either.Left("")
                Assert.assertNotNull(viewModel.unblockDevice("stationMac"))
            }
        }
    }

    @Test
    fun testUnblockDeviceFailure() {
        runBlockingTest {
            launch {
                coEvery { assiaRepository.unblockDevices(any()) } returns Either.Left(
                    "Error DeviceInfo"
                )
                Assert.assertNotNull(viewModel.unblockDevice("stationMac"))
            }
        }
    }

    @Test
    fun testRequestMcafeeDeviceMapping() {
        runBlockingTest {
            launch {
                coEvery { OAuthAssiaService.getLineInfo(any(), any()) } returns Either.Right(
                    modemInfoResponse
                )
                Assert.assertNotNull(viewModel.initApis())
            }
        }
    }
}
