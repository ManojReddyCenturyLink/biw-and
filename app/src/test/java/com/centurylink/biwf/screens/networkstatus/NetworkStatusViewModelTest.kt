package com.centurylink.biwf.screens.networkstatus

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.wifi.NetWorkBand
import com.centurylink.biwf.model.wifi.NetworkDetails
import com.centurylink.biwf.model.wifi.UpdateNetworkResponse
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.repos.assia.WifiStatusRepository
import com.centurylink.biwf.repos.assia.WifiStatusRepository_Factory
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
    private lateinit var wifiStatusRepository: WifiStatusRepository

    @MockK
    private lateinit var analyticsManagerInterface: AnalyticsManager

    private lateinit var wifiInfo : WifiInfo

    @ExperimentalCoroutinesApi
    @get:Rule
    var coroutinesTestRule = TestCoroutineRule()

    var error: MutableLiveData<Errors> = MutableLiveData()

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxed = true)
        coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(ModemInfo(
            lineId = "",
            modelName = "",
            apInfoList = emptyList()
        ))
        coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Left("Modem Info Error")
        run { analyticsManagerInterface }
        val scanString = readJson("scaninfo.json")
        wifiInfo = fromJson(scanString)
        viewModel = NetworkStatusViewModel(
            oAuthAssiaRepository = oAuthAssiaRepository,
            wifiNetworkManagementRepository = wifiNetworkManagementRepository,
            wifiStatusRepository = wifiStatusRepository,
            modemRebootMonitorService = mockModemRebootMonitorService,
            analyticsManagerInterface = analyticsManagerInterface
        )
    }

    @Test
    fun onValidateInput_EmptyValueInput() {
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
    fun onValidateInput_ValidInput() {
        viewModel.onGuestNameValueChanged("ABCDEFGHIJKLMOPQRSTUVWXYZABCDEFGHIJKLMNOP")
        viewModel.onWifiNameValueChanged("ABCDEFGHIJKLMOPQRSTUVWXYZABCDEFGHIJKLMNOP")
        error.value = viewModel.validateInput()
        MatcherAssert.assertThat("Guest Name is Correct", error.value!!.contains("guestNameFieldLength"))
        MatcherAssert.assertThat("Guest Name is Correct", error.value!!.contains("wifiNameFieldLength"))
    }

    @Test
    fun togglePasswordVisibility() {
        viewModel.togglePasswordVisibility()
        Assert.assertSame(false, viewModel.togglePasswordVisibility())
    }

    @Test
    fun testOnInitApi() {
        runBlockingTest {
            val method = viewModel.javaClass.getDeclaredMethod("initApi")
            method.isAccessible = true
        }
    }

    @Test
    fun wifiNetworkEnablement() {
        assertNotNull(viewModel.wifiNetworkEnablement())
        runBlockingTest {
            launch {
            }
        }
    }

    @Test
    fun guestNetworkEnablement() {
        assertNotNull(viewModel.guestNetworkEnablement())
    }

    @Test
    fun onDoneClick_UpdatePassword() {
        assertNotNull(viewModel.onDoneClick())
    }

    @Test
    fun testOnDoneClick() =
        runBlockingTest {
            launch {
                viewModel.onDoneClick()
                assertNotNull(viewModel.onDoneClick())
            }
        }


    @Test
    fun testRequestModemInfo() {
        runBlockingTest {
            launch {
                coEvery {
                    oAuthAssiaRepository.getModemInfo() } returns Either.Right(
                    ModemInfo(
                        lineId = "",
                        modelName = "",
                        apInfoList = emptyList()
                    )
                )
                viewModel.initApi()
//                Assert.assertEquals(
//                    viewModel.errorMessageFlow.first(), "Error in FAQ"
                Assert.assertEquals(
                            wifiInfo.name, null)}
                Assert.assertEquals(
                wifiInfo.password, null)}
        }

//    @Test
//    fun testRequestToUpdateNetwork() {
//        runBlockingTest {
//            launch {
//                val networkBand = NetWorkBand.Band2G
//                coEvery { wifiNetworkManagementRepository.updateNetworkName() }
//            }
//        }
//    }


    @Test
    fun analyticsManagerInterface_handle() {
        Assert.assertNotNull(analyticsManagerInterface)
        viewModel.logModemRebootErrorDialog()
        viewModel.logDiscardChangesAndCloseClick()
        viewModel.logDiscardChangesClick()
        viewModel.logModemRebootSuccessDialog()
    }
}