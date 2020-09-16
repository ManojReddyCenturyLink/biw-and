package com.centurylink.biwf.screens.qrcode

import android.content.res.Resources
import com.centurylink.biwf.ViewModelBaseTest
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.model.wifi.WifiInfo
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test


//class QRScanViewModelTest  : ViewModelBaseTest() {
//
//    private lateinit var viewModel: QRScanViewModel
//
//    @MockK
//    private lateinit var analyticsManagerInterface: AnalyticsManager
//
//    private lateinit var WifiInfo: WifiInfo
//
//    @MockK
//    private lateinit var Resources: Resources
//
//    @Before
//    fun setUp() {
//        MockKAnnotations.init(this, relaxed = true)
//       WifiInfo = fromJson(readJson("WifiInfo.json"))
//        run { analyticsManagerInterface }
//        viewModel = QRScanViewModel(
//            wifiInfo = WifiInfo,
//            modemRebootMonitorService = mockModemRebootMonitorService,
//            resources = Resources,
//            analyticsManagerInterface = analyticsManagerInterface
//        )
//    }
//
//}