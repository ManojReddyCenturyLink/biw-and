package com.centurylink.biwf.screens.qrcode

import android.content.res.Resources
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsKeys
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

/**
 * Q r scan view model
 *
 * @property wifiInfo - wifiInfo instance to handle wifi information
 * @property resources - resource instance to handle resources information
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class QRScanViewModel constructor(
    private var wifiInfo: WifiInfo,
    modemRebootMonitorService: ModemRebootMonitorService,
    private var resources: Resources,
    analyticsManagerInterface : AnalyticsManager
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

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        generateQrCodeInfo()
    }

    /**
     * Generate qr code info - It used to generate Qr code information
     *
     */
    private fun generateQrCodeInfo() {
        val qrdata = resources.getString(R.string.wifi_code, wifiInfo.name, wifiInfo.password)
        val qrCode: Bitmap =
            QRCode.from(qrdata).withColor(QrScanActivity.ON_COLOR_QR, QrScanActivity.OFF_COLOR_QR)
                .withHint(EncodeHintType.MARGIN, 0)
                .bitmap()
        qrScanFlow.latestValue = QrScanInfo(qrCode, wifiInfo.name!!)
    }

    /**
     * Log done button click - It will handle done button click events
     *
     */
    fun logDoneButtonClick(){
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_QR_CODE)
    }

    /**
     * With color - customizes qr code color
     *
     * @param onColor - returns on color
     * @param offColor - returns off color
     */
    fun QRCode.withColor(onColor: Long, offColor: Long) =
        this.withColor(onColor.toInt(), offColor.toInt())

    data class QrScanInfo(var wifiQrCode: Bitmap, var name: String)
}