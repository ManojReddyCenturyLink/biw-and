package com.centurylink.biwf.screens.deviceusagedetails

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.Either
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.UsageDetailsCoordinatorDestinations
import com.centurylink.biwf.model.devices.DeviceConnectionStatus
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.mcafee.DevicesItem
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.McafeeRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.NetworkUsageRepository
import com.centurylink.biwf.screens.networkstatus.ModemUtils
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.viewModelFactory
import kotlinx.coroutines.launch
import timber.log.Timber
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Usage details view model
 *
 * @property app - application class instance to get global context
 * @property networkUsageRepository - repository instance to handle network usage api calls
 * @property assiaRepository - repository instance to handle assia api calls
 * @property mcafeeRepository - repository instance t ao handle mcafee api calls
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class UsageDetailsViewModel constructor(
    private val app: BIWFApp,
    private val networkUsageRepository: NetworkUsageRepository,
    private val assiaRepository: AssiaRepository,
    private val oAuthAssiaRepository: OAuthAssiaRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager,
    private val mcafeeRepository: McafeeRepository
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    class Factory @Inject constructor(
        private val app: BIWFApp,
        private val networkUsageRepository: NetworkUsageRepository,
        private val asiaRepository: AssiaRepository,
        private val oAuthAssiaRepository: OAuthAssiaRepository,
        private val modemRebootMonitorService: ModemRebootMonitorService,
        private val analyticsManagerInterface: AnalyticsManager,
        private val mcafeeRepository: McafeeRepository
    ) : ViewModelFactoryWithInput<DevicesData> {

        override fun withInput(input: DevicesData): ViewModelProvider.Factory {
            return viewModelFactory {
                val viewModel = UsageDetailsViewModel(
                    app,
                    networkUsageRepository,
                    asiaRepository,
                    oAuthAssiaRepository,
                    modemRebootMonitorService,
                    analyticsManagerInterface,
                    mcafeeRepository
                )
                viewModel.staMac = input.stationMac!!
                viewModel.macAfeeDeviceId = input.mcafeeDeviceId
                viewModel.deviceData = input
                viewModel
            }
        }
    }

    val myState = EventFlow<UsageDetailsCoordinatorDestinations>()
    val progressViewFlow = EventFlow<Boolean>()
    val errorMessageFlow = EventFlow<String>()
    val showErrorPopup = EventFlow<Boolean>()
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
    var macAfeeDeviceId: String = ""
    var deviceData: DevicesData = DevicesData()
    var pauseUnpauseConnection = EventFlow<DevicesData>()
    var mcAfeedeviceList: List<DevicesItem> = emptyList()
    var mcAfeedeviceNames: ArrayList<String> = ArrayList()
    var retryStatus = true

    /**
     * Init apis - It will start all the api calls initialisation
     *
     */
    fun initApis() {
        // TODO: Temporarily using boolean variable to test pause/un-pause connection analytics
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_DEVICE_DETAILS)
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            fetchMcDevicesNames()
            requestDailyUsageDetails()
            requestMonthlyUsageDetails()
            requestStateForDevices()
        }
    }

    /**
     * On remove devices clicked - It handles the remove devices click event
     *
     */
    fun onRemoveDevicesClicked() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_REMOVE_DEVICES_DEVICE_DETAILS)
    }

    /**
     * On devices connected clicked - It handles the connected devices click event
     *
     */
    fun onDevicesConnectedClicked() {
        if (deviceData?.isPaused) {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_PAUSE_CONNECTION_DEVICE_DETAILS)
        } else {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_RESUME_CONNECTION_DEVICE_DETAILS)
        }
        when (deviceData.deviceConnectionStatus) {
            DeviceConnectionStatus.PAUSED,
            DeviceConnectionStatus.DEVICE_CONNECTED,
            DeviceConnectionStatus.LOADING,
            DeviceConnectionStatus.FAILURE -> {
                if (!macAfeeDeviceId.isNullOrEmpty()) {
                    updatePauseResumeStatus()
                }
            }
            DeviceConnectionStatus.MODEM_OFF -> {
                Timber.e("Cant Perform any Action")
            }
        }
    }

    /**
     * Remove devices - it will handle the remove devices logic
     *
     * @param stationMac
     */
    fun removeDevices(stationMac: String) {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            invokeBlockedDevice(stationMac)
        }
    }

    /**
     * On done btn click - It handles the done button click logic
     *
     * @param nickname
     */
    fun onDoneBtnClick(nickname: String) {
        if (nickname.isNotEmpty() && nickname != deviceData.mcAfeeName) {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_DEVICE_DETAILS)
            progressViewFlow.latestValue = true
            viewModelScope.launch {
                val distinctName =
                    ModemUtils.generateNewNickName(nickname.trim(), mcAfeedeviceNames)
                updateDeviceName(
                    deviceData.mcAfeeDeviceType,
                    distinctName,
                    deviceData.mcafeeDeviceId
                )
            }
        } else {
            showErrorPopup.latestValue = false
        }
    }

    /**
     * Log remove connection - it handles related to usage details cancellation analytics
     *
     * @param removeConnection
     */
    fun logRemoveConnection(removeConnection: Boolean) {
        if (removeConnection) {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_REMOVE_CONFIRMATION_USAGE_DETAILS)
        } else {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_CANCEL_CONFIRMATION_USAGE_DETAILS)
        }
    }

    /**
     * Validate input- it will do validation for the provided string
     * @param nickname name to validated
     * @return - it returns the formatted string
     */
    fun validateInput(nickname: String): Boolean {
        val specialCharacter: Pattern = Pattern.compile("[!@#$%&*()_+=|<>?{}\\[\\]~.]")
        val hasSpecial: Matcher = specialCharacter.matcher(nickname)
        return hasSpecial.find()
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

    private fun updatePauseResumeStatus() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            val macResponse = mcafeeRepository.updateDevicePauseResumeStatus(
                macAfeeDeviceId,
                !deviceData.isPaused
            )
            macResponse.fold(ifLeft = {
                if (retryStatus) {
                    errorMessageFlow.latestValue = it
                } else {
                    progressViewFlow.latestValue = false
                }
                deviceData.deviceConnectionStatus = DeviceConnectionStatus.FAILURE
                pauseUnpauseConnection.latestValue = deviceData
            }, ifRight = {
                deviceData.isPaused = it.isPaused
                if (it.isPaused) {
                    deviceData.deviceConnectionStatus = DeviceConnectionStatus.PAUSED
                } else {
                    deviceData.deviceConnectionStatus = DeviceConnectionStatus.DEVICE_CONNECTED
                }
                pauseUnpauseConnection.latestValue = deviceData
                progressViewFlow.latestValue = false
            })
        }
    }

    private suspend fun invokeBlockedDevice(stationMac: String) {
        progressViewFlow.latestValue = false
        val blockInfo = oAuthAssiaRepository.blockDevices(stationMac)
        blockInfo.fold(
            ifRight = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.BLOCK_DEVICE_SUCCESS)
                removeDevices.latestValue = true
            },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.BLOCK_DEVICE_FAILURE)
                errorMessageFlow.latestValue = "Error DeviceInfo"
            }
        )
    }

    private suspend fun fetchMcDevicesNames() {
        val result: Either<String, List<DevicesItem>> = mcafeeRepository.fetchDeviceDetails()
        result.fold(ifLeft = {
            Timber.e("Mcafee Device List Error ")
        }, ifRight = { devicesItemList ->
            mcAfeedeviceList = devicesItemList
            mcAfeedeviceNames = ArrayList(devicesItemList.map { it.name }.toMutableList())
        })
    }

    private suspend fun updateDeviceName(deviceType: String, nickname: String, id: String) {
        val result = mcafeeRepository.updateDeviceName(deviceType, nickname, id)
        result.fold(
            ifLeft = {
                progressViewFlow.latestValue = false
                showErrorPopup.latestValue = true
            },
            ifRight = {
                progressViewFlow.latestValue = false
                showErrorPopup.latestValue = false
            })
    }

    private suspend fun requestStateForDevices() {
        val mcafeeMapping = mcafeeRepository.getDevicePauseResumeStatus(macAfeeDeviceId)
        mcafeeMapping.fold(ifLeft = {
            // On Error we are updating the device Icon with Failure icon instead of wifi icon
            deviceData.isPaused = false
            pauseUnpauseConnection.latestValue = deviceData
            deviceData.deviceConnectionStatus = DeviceConnectionStatus.FAILURE
        }) { devicePauseStatus ->
            deviceData.isPaused = devicePauseStatus.isPaused
            if (deviceData.isPaused) {
                deviceData.deviceConnectionStatus = DeviceConnectionStatus.PAUSED
            } else {
                deviceData.deviceConnectionStatus = DeviceConnectionStatus.DEVICE_CONNECTED
            }
            pauseUnpauseConnection.latestValue = deviceData
            progressViewFlow.latestValue = false
        }
    }
}
