package com.centurylink.biwf.screens.qrcode

import android.content.res.Resources
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.viewModelFactory
import com.google.zxing.EncodeHintType
import net.glxn.qrgen.android.QRCode
import javax.inject.Inject

class QRScanViewModel constructor(
    private var wifiInfo: WifiInfo,
    modemRebootMonitorService: ModemRebootMonitorService,
    private var resources: Resources,
    private val analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    class Factory @Inject constructor(
        private val modemRebootMonitorService: ModemRebootMonitorService,
        private var resources: Resources,
        private val analyticsManagerInterface : AnalyticsManager
    ) : ViewModelFactoryWithInput<WifiInfo> {

        override fun withInput(input: WifiInfo): ViewModelProvider.Factory {
            return viewModelFactory {
                QRScanViewModel(input, modemRebootMonitorService,resources,analyticsManagerInterface)
            }
        }
    }

    val qrScanFlow: BehaviorStateFlow<QrScanInfo> = BehaviorStateFlow()

    init {
        generateQrCodeInfo()
    }

    private fun generateQrCodeInfo() {
        val qrdata = resources.getString(R.string.wifi_code, wifiInfo.name, wifiInfo.password)
        val qrCode: Bitmap =
            QRCode.from(qrdata).withColor(QrScanActivity.ON_COLOR_QR, QrScanActivity.OFF_COLOR_QR)
                .withHint(EncodeHintType.MARGIN, 0)
                .bitmap()
        qrScanFlow.latestValue = QrScanInfo(qrCode, wifiInfo.name!!)
    }

    fun QRCode.withColor(onColor: Long, offColor: Long) =
        this.withColor(onColor.toInt(), offColor.toInt())

    data class QrScanInfo(var wifiQrCode: Bitmap, var name: String)
}