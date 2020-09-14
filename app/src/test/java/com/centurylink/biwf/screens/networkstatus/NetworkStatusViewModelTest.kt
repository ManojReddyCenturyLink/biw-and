package com.centurylink.biwf.screens.networkstatus

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.wifi.*
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.service.network.WifiNetworkApiService
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class NetworkStatusViewModelTest : ViewModelBaseTest() {

    @MockK
    private lateinit var viewModel: NetworkStatusViewModel

    @MockK
    private lateinit var oAuthAssiaRepository: OAuthAssiaRepository

    @MockK
    private lateinit var  wifiNetworkManagementRepository : WifiNetworkManagementRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    @MockK
    private lateinit var modemInfo : ModemInfo

    @MockK
    private lateinit var updateNetworkResponse : UpdateNetworkResponse


    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    var error: MutableLiveData<Errors> = MutableLiveData()

    val networkBand = NetWorkBand.Band2G

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        modemInfo = fromJson(readJson("modeminfo.json"))
        updateNetworkResponse = fromJson(readJson("updatenetworkresponse.json"))
        coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(modemInfo)
        coEvery {
            wifiNetworkManagementRepository.enableNetwork(networkBand) } returns Either.Right(updateNetworkResponse)
        coEvery {
            wifiNetworkManagementRepository.disableNetwork(networkBand) } returns Either.Right(updateNetworkResponse)
        coEvery {
            wifiNetworkManagementRepository.updateNetworkName(networkBand, UpdateNetworkName("")) } returns Either.Right(updateNetworkResponse)
        coEvery {
            wifiNetworkManagementRepository.updateNetworkPassword(networkBand, UpdateNWPassword("")) } returns Either.Right(updateNetworkResponse)
        coEvery {
            wifiNetworkManagementRepository.getNetworkName(networkBand) } returns Either.Right(
            NetworkDetails(code = "", message =  "", networkName = hashMapOf<String, String>()))
        coEvery {
            wifiNetworkManagementRepository.getNetworkPassword(networkBand) } returns Either.Right(
            NetworkDetails(code = "", message =  "", networkName = hashMapOf<String, String>()))
        run { analyticsManagerInterface }
        viewModel = NetworkStatusViewModel(
            oAuthAssiaRepository = oAuthAssiaRepository,
            wifiNetworkManagementRepository = wifiNetworkManagementRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun testInitApiCallsSuccess() {
        runBlockingTest {
            launch {
                viewModel.initApi()
            }
        }
    }

    @Test
    fun testInitApiCallsFailure() {
        coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Left("Modem Info Error")
        coEvery {
            val networkBand = NetWorkBand.Band2G
            wifiNetworkManagementRepository.enableNetwork(networkBand) } returns Either.Left("Network Enablement Failed")
        coEvery {
            val networkBand = NetWorkBand.Band2G
            wifiNetworkManagementRepository.disableNetwork(networkBand) } returns Either.Left( "Network disablement Failed")
        coEvery {
            val networkBand = NetWorkBand.Band2G
            wifiNetworkManagementRepository.updateNetworkName(networkBand, UpdateNetworkName("")) } returns Either.Left("")
        coEvery {
            wifiNetworkManagementRepository.updateNetworkPassword(networkBand, UpdateNWPassword("")) } returns Either.Left("")
        coEvery {
            wifiNetworkManagementRepository.getNetworkName(networkBand) } returns Either.Left("")
        coEvery {
            wifiNetworkManagementRepository.getNetworkPassword(networkBand) } returns Either.Left("")
    }

    @Test
    fun testBlankInputs() {
        viewModel.onGuestNameValueChanged("")
        viewModel.onGuestPasswordValueChanged("")
        viewModel.onWifiNameValueChanged("")
        viewModel.onWifiPasswordValueChanged("")
        error.value = viewModel.validateInput()
        MatcherAssert.assertThat("Guest Name is Blank", error.value!!.contains("guestNameFieldMandatory"))
        MatcherAssert.assertThat("Guest Password is Blank", error.value!!.contains("guestPasswordFieldMandatory"))
        MatcherAssert.assertThat("Wifi Name is Blank", error.value!!.contains("guestNameFieldMandatory"))
        MatcherAssert.assertThat("Wifi Password is Blank", error.value!!.contains("guestPasswordFieldMandatory"))
    }

    @Test
    fun testInvalidInputs() {
        viewModel.onGuestNameValueChanged("ABCDEFGHIJKLMOPQRSTUVWXYZABCDEFGHIJKLMNOP")
        viewModel.onWifiNameValueChanged("ABCDEFGHIJKLMOPQRSTUVWXYZABCDEFGHIJKLMNOP")
        error.value = viewModel.validateInput()
        MatcherAssert.assertThat("Guest Name is Correct", error.value!!.contains("guestNameFieldLength"))
        MatcherAssert.assertThat("Guest Name is Correct", error.value!!.contains("wifiNameFieldLength"))
    }

    @Test
    fun testTogglePasswordVisibility() {
        viewModel.togglePasswordVisibility()
        Assert.assertSame(false, viewModel.togglePasswordVisibility())
    }


    @Test
    fun testWifiNetworkEnablement() {
        runBlockingTest {
            launch {
                viewModel.wifiNetworkEnablement()
            }
        }
    }

    @Test
    fun testGuestNetworkEnablement() {
        runBlockingTest {
            launch {
                viewModel.guestNetworkEnablement()
            }
        }
    }


    @Test
    fun testOnDoneClick() {
        runBlockingTest {
            launch {
                viewModel.onDoneClick()
                assertNotNull(viewModel.onDoneClick())
            }
        }
        }

    @Test
    fun testRequestModemInfoSuccess() {
        runBlockingTest {
            launch {
                coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(modemInfo)
                viewModel.initApi()
            }
        }}

    @Test
    fun testRequestModemInfoFailure() {
        runBlockingTest {
            launch {
                coEvery {
                    oAuthAssiaRepository.getModemInfo() } returns Either.Left("")
                viewModel.initApi()
            }
        }}

    @Test
    fun testLogAnalytics() {
        Assert.assertNotNull(analyticsManagerInterface)
        viewModel.logModemRebootErrorDialog()
        viewModel.logDiscardChangesAndCloseClick()
        viewModel.logDiscardChangesClick()
        viewModel.logModemRebootSuccessDialog()
    }
}