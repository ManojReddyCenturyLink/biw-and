package com.centurylink.biwf.screens.deviceusagedetails

import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.mcafee.DevicePauseStatus
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.McafeeRepository
import com.centurylink.biwf.repos.assia.NetworkUsageRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
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

    private lateinit var deviceData: DevicesData

    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        run { analyticsManagerInterface }
        coEvery { mcafeeRepository.updateDevicePauseResumeStatus("",true)} returns Either.Right(
            DevicePauseStatus(
                isPaused =  true,
                deviceId = ""
            )
        )
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
            launch {
                viewModel.initApis()
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
        viewModel.onDoneBtnClick("")
        viewModel.onRemoveDevicesClicked()
       // viewModel.onDevicesConnectedClicked()
        viewModel.logRemoveConnection(true)
        viewModel.logRemoveConnection(false)
    }

    @Test
    fun validateInputTest(){
        runBlockingTest {
            launch {
            viewModel.validateInput("")
            }
            Assert.assertEquals(false , viewModel.validateInput(""))
        }
    }
}