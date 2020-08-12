package com.centurylink.biwf.screens.deviceusagedetails

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.UsageDetailsCoordinatorDestinations
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.assia.NetworkUsageRepository
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.viewModelFactory
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.roundToInt

class UsageDetailsViewModel constructor(
    private val app: BIWFApp,
    private val networkUsageRepository: NetworkUsageRepository,
    private val assiaRepository: AssiaRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    private val analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService) {

    class Factory @Inject constructor(
        private val app: BIWFApp,
        private val networkUsageRepository: NetworkUsageRepository,
        private val asiaRepository: AssiaRepository,
        private val modemRebootMonitorService: ModemRebootMonitorService,
        private val analyticsManagerInterface: AnalyticsManager
    ) : ViewModelFactoryWithInput<String> {

        override fun withInput(input: String): ViewModelProvider.Factory {
            return viewModelFactory {
                val viewModel = UsageDetailsViewModel(app, networkUsageRepository,asiaRepository, modemRebootMonitorService, analyticsManagerInterface)
                viewModel.staMac = input
                viewModel
            }
        }
    }

    val myState = EventFlow<UsageDetailsCoordinatorDestinations>()
    val progressViewFlow = EventFlow<Boolean>()
    val errorMessageFlow = EventFlow<String>()
    val uploadSpeedMonthly: BehaviorStateFlow<String> = BehaviorStateFlow()
    val uploadSpeedDaily: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedMonthly: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedDaily: BehaviorStateFlow<String> = BehaviorStateFlow()
    val uploadSpeedMonthlyUnit: BehaviorStateFlow<String> = BehaviorStateFlow()
    val uploadSpeedDailyUnit: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedMonthlyUnit: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedDailyUnit: BehaviorStateFlow<String> = BehaviorStateFlow()
    val removeDevices: BehaviorStateFlow<Boolean> = BehaviorStateFlow()
    var staMac: String = ""

    fun initApis() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_DEVICE_DETAILS)
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestDailyUsageDetails()
            requestMonthlyUsageDetails()
        }
    }

    fun onDevicesConnectedClicked() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DEVICE_CONNECTION_STATUS_DEVICE_DETAILS)
    }

    private suspend fun requestDailyUsageDetails() {
        try {
            val result = networkUsageRepository.getUsageDetails(true, staMac)
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_USAGE_DETAILS_DAILY_SUCCESS)
            uploadSpeedDaily.latestValue =
                formattedTraffic(result.uploadTraffic, result.uploadTrafficUnit)
            downloadSpeedDaily.latestValue =
                formattedTraffic(result.downloadTraffic, result.downloadTrafficUnit)
            uploadSpeedDailyUnit.latestValue = getUnit(result.uploadTrafficUnit)
            downloadSpeedDailyUnit.latestValue = getUnit(result.downloadTrafficUnit)
        } catch (e: Exception) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_USAGE_DETAILS_DAILY_FAILURE)
            errorMessageFlow.latestValue = e.toString()
        }

    }

    private suspend fun requestMonthlyUsageDetails() {
        try {
            val result = networkUsageRepository.getUsageDetails(false, staMac)
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_USAGE_DETAILS_MONTHLY_SUCCESS)
            uploadSpeedMonthly.latestValue =
                formattedTraffic(result.uploadTraffic, result.uploadTrafficUnit)
            downloadSpeedMonthly.latestValue =
                formattedTraffic(result.downloadTraffic, result.downloadTrafficUnit)
            uploadSpeedMonthlyUnit.latestValue = getUnit(result.uploadTrafficUnit)
            downloadSpeedMonthlyUnit.latestValue = getUnit(result.downloadTrafficUnit)
            progressViewFlow.latestValue = false
        } catch (e: Exception) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_USAGE_DETAILS_MONTHLY_FAILURE)
            errorMessageFlow.latestValue = e.toString()
        }
    }

    private fun formattedTraffic(trafficVal: Double, unit: NetworkTrafficUnits): String {
        if (trafficVal.roundToInt() > 0 && (unit == NetworkTrafficUnits.MB_DOWNLOAD)) {
            return trafficVal.roundToInt().toString()
        } else if (trafficVal.roundToInt() > 0 && (unit == NetworkTrafficUnits.MB_UPLOAD)) {
            return trafficVal.roundToInt().toString()
        } else if (trafficVal.roundToInt() > 0) {
            if ((trafficVal % 1) > 0.5)
                return BigDecimal(trafficVal).setScale(1, RoundingMode.UP).toString()
            else
                return BigDecimal(trafficVal).setScale(1, RoundingMode.DOWN).toString()
        } else {
            return app.getString(R.string.empty_string)
        }
    }

    private fun getUnit(unit: NetworkTrafficUnits): String {
        return when (unit) {
            NetworkTrafficUnits.MB_DOWNLOAD -> app.getString(R.string.mb_download)
            NetworkTrafficUnits.MB_UPLOAD -> app.getString(R.string.mb_upload)
            NetworkTrafficUnits.GB_DOWNLOAD -> app.getString(R.string.gb_download)
            NetworkTrafficUnits.GB_UPLOAD -> app.getString(R.string.gb_upload)
            NetworkTrafficUnits.TB_DOWNLOAD -> app.getString(R.string.tb_download)
            NetworkTrafficUnits.TB_UPLOAD -> app.getString(R.string.tb_upload)
        }
    }
    fun removeDevices(stationMac:String) {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            invokeBlockedDevice(stationMac)
        }
    }

    private suspend fun invokeBlockedDevice(stationMac:String){
        val blockInfo = assiaRepository.blockDevices(stationMac)
        progressViewFlow.latestValue = false
        when (blockInfo) {
            is AssiaNetworkResponse.Success -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.BLOCK_DEVICE_SUCCESS)
                removeDevices.latestValue = blockInfo.body.code.equals("1000")
            }
            else -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.BLOCK_DEVICE_FAILURE)
                errorMessageFlow.latestValue = "Error DeviceInfo"
            }
        }
    }

    fun logDoneBtnClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_DEVICE_DETAILS)
    }

    fun logRemoveConnection(removeConnection: Boolean) {
        if(removeConnection){
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_REMOVE_CONFIRMATION_USAGE_DETAILS)
        }else{
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_CANCEL_CONFIRMATION_USAGE_DETAILS)
        }
    }
}