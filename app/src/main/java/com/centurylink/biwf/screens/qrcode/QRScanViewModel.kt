package com.centurylink.biwf.screens.qrcode

import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.viewModelFactory
import net.glxn.qrgen.android.QRCode
import net.glxn.qrgen.core.scheme.Wifi
import javax.inject.Inject


class QRScanViewModel constructor(
    private var wifiInfo: WifiInfo,
    modemRebootMonitorService: ModemRebootMonitorService
) : BaseViewModel(modemRebootMonitorService) {

    class Factory @Inject constructor(
        private val modemRebootMonitorService: ModemRebootMonitorService
    ) : ViewModelFactoryWithInput<WifiInfo> {

        override fun withInput(input: WifiInfo): ViewModelProvider.Factory {
            return viewModelFactory {
                QRScanViewModel(input, modemRebootMonitorService)

            }
        }
    }

    val qrScanFlow: BehaviorStateFlow<QrScanInfo> = BehaviorStateFlow()

    init {
        generateQrCodeInfo()
    }

    private fun generateQrCodeInfo() {
        val wifi = Wifi()
        wifi.ssid = wifiInfo.name
        wifi.psk = wifiInfo.password
        val qrCode: Bitmap =
            QRCode.from(wifi).withColor(QrScanActivity.ON_COLOR_QR, QrScanActivity.OFF_COLOR_QR)
                .bitmap()
        qrScanFlow.latestValue = QrScanInfo(qrCode, wifiInfo.name!!)
    }

    fun QRCode.withColor(onColor: Long, offColor: Long) =
        this.withColor(onColor.toInt(), offColor.toInt())

    data class QrScanInfo(var wifiQrCode: Bitmap, var name: String)
}