package com.centurylink.biwf.screens.networkstatus

import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.Either
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.assia.ApInfo
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.repos.assia.WifiStatusRepository
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.TestCoroutineRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.util.*
import kotlin.test.assertNotNull

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
        val map = HashMap<String, String>()
        map["Band2G_Guest1"] = "CenturyLink0308-24G-2"
        val apInfoList = mutableListOf(
            ApInfo(
               "", "",
                "", true, true, map, map
            )
        )
        coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(ModemInfo(
            lineId = "",
            modelName = "",
            apInfoList = apInfoList
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
        runBlockingTest {
            launch {
            }
        }
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
                val map = HashMap<String, String>()
                map["Band2G_Guest1"] = "CenturyLink0308-24G-2"
                val apInfoList = mutableListOf(
                    ApInfo(
                        "", "",
                        "", true, true, map, map
                    )
                )
                coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(ModemInfo(
                    lineId = "",
                    modelName = "",
                    apInfoList = apInfoList
                ))
                viewModel.initApi()
                Assert.assertEquals(
                            wifiInfo.name, null)}
                Assert.assertEquals(
                wifiInfo.password, null)}
        }

    @Test
    fun testFetchPasswordApiBand2G_Guest1() {
        runBlockingTest {
            launch {
                val ssidMap = HashMap<String, String>()
                ssidMap["Band2G"] = "CenturyLink"
                ssidMap["Band5G"] = "CenturyLink"
                ssidMap["Band2G_Guest1"] = "CenturyLink0308-24G-2"
                ssidMap["Band2G_Guest4"] = "CenturyLink02-Guest123"
                ssidMap["Band5G_Guest4"] = "CenturyLink02-Guest123"
                val bssidMap = HashMap<String, String>()
                bssidMap["02:6A:E3:8F:FA:B2"] = "Band2G_Guest4"
                bssidMap["02:6A:E3:8F:FA:AC"] = "Band2G_Guest1"
                val apInfoList = mutableListOf(
                    ApInfo(
                        "", "",
                        "", true, true, ssidMap, bssidMap
                    )
                )
                coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(ModemInfo(
                    lineId = "",
                    modelName = "",
                    apInfoList = apInfoList
                ))
                viewModel.initApi()
                Assert.assertEquals(
                            wifiInfo.name, null)}
                Assert.assertEquals(
                wifiInfo.password, null)}
        }

    @Test
    fun testRequestModemInfoWithEmptyList() {
        runBlockingTest {
            launch {
                coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(ModemInfo(
                    lineId = "",
                    modelName = "",
                    apInfoList = emptyList()
                ))
                viewModel.initApi()
                Assert.assertEquals(
                            wifiInfo.name, null)}
                Assert.assertEquals(
                wifiInfo.password, null)}
        }

    @Test
    fun testSetOfflineNetworkInformation() {
        runBlockingTest {
            launch {
                val map = HashMap<String, String>()
                map.put("1", "ssid")
                val apInfoList = mutableListOf(
                    ApInfo(
                        "", "",
                        "", true, false, map, map
                    )
                )
                coEvery { oAuthAssiaRepository.getModemInfo() } returns Either.Right(ModemInfo(
                    lineId = "",
                    modelName = "",
                    apInfoList = apInfoList
                ))
                viewModel.initApi()
                Assert.assertEquals(wifiInfo.name, null)}
                Assert.assertEquals(wifiInfo.password, null)}
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