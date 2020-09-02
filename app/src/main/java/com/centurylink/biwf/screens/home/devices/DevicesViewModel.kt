package com.centurylink.biwf.screens.home.devices

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DevicesCoordinatorDestinations
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.model.mcafee.DevicePauseStatus
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.repos.McafeeRepository
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class DevicesViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository,
    private val asiaRepository: AssiaRepository,
    private val mcafeeRepository: McafeeRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    var devicesListFlow: Flow<UIDevicesTypeDetails> = BehaviorStateFlow()
    val myState = EventFlow<DevicesCoordinatorDestinations>()
    private var uiDevicesTypeDetails: UIDevicesTypeDetails = UIDevicesTypeDetails()
    private lateinit var devicesInfo: DevicesInfo

    init {
        progressViewFlow.latestValue = true
        initApis()
    }

    fun initApis() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_DEVICES)
        viewModelScope.launch {
            requestModemDetails()

        }
    }

    /**
     * Get devices info from Mcafee API
     */
    private suspend fun requestMcafeeDeviceMapping(deviceList: List<String>){
        progressViewFlow.latestValue = true
        val mcafeeMapping = mcafeeRepository.getMcafeeDeviceIds(deviceList)
        mcafeeMapping.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) { mcafeeDeviceIds ->
            devicesInfo.devicesDataList.forEach { deviceData ->
                deviceData.mcafeeDeviceId = mcafeeDeviceIds.firstOrNull {
                    deviceData.stationMac?.replace(":", "-") == it.mac_address
                }?.devices?.get(0)?.id ?: ""
            }
            sortAndDisplayDeviceInfo(devicesInfo.devicesDataList)
        }
    }

    private suspend fun requestDevices() {
        val deviceDetails = asiaRepository.getDevicesDetails()
        when (deviceDetails) {
            is AssiaNetworkResponse.Success -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_SUCCESS)
                devicesInfo =deviceDetails.body
                val macAddresses = getMacAddressesFromDevicesInfo(devicesInfo)
                sortDeviceIdFromMcAfee(macAddresses)
            }
            else -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_FAILURE)
                errorMessageFlow.latestValue = "Error DeviceInfo"
            }
        }
    }

    private fun getMacAddressesFromDevicesInfo(devicesInfo: DevicesInfo): List<String> {
        return devicesInfo.devicesDataList.map { it.stationMac!!.replace(":", "-") }
    }

    private fun sortDeviceIdFromMcAfee(deviceStationMacList: List<String>) {
        if (!deviceStationMacList.isNullOrEmpty()) {
            initMacList(deviceStationMacList)
        }
    }

    private fun initMacList(macList: List<String>) {
        viewModelScope.launch {
            requestMcafeeDeviceMapping(macList)
        }
    }

    private fun getPauseResumeState(connectedList: List<DevicesData>) {
        var concurrentList = ConcurrentLinkedQueue(connectedList)
        viewModelScope.launch {
            for (item in concurrentList) {
                if (!item.mcafeeDeviceId.isNullOrEmpty()) {
                    requestStateForConnectedDevices(item.mcafeeDeviceId)
                }
            }
            progressViewFlow.latestValue = false
        }
    }

    private suspend fun requestStateForDevices(deviceId: String) {
        val mcafeeMapping = mcafeeRepository.getDevicePauseResumeStatus(deviceId)
        mcafeeMapping.fold(ifLeft = {
            // No Op
        }) {
            requestToUpdateNetworkStatus(deviceId, !it.isPaused)
        }
    }

    private suspend fun requestStateForConnectedDevices(deviceId: String) {
        val mcafeeMapping = mcafeeRepository.getDevicePauseResumeStatus(deviceId)
        mcafeeMapping.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            updateUiWithStatus(it)
        }
    }

    private fun updateUiWithStatus(deviceStatus: DevicePauseStatus) {
        var removedList = uiDevicesTypeDetails.deviceSortMap[DeviceStatus.BLOCKED]
        var deviceList =
            uiDevicesTypeDetails.deviceSortMap[DeviceStatus.CONNECTED]!!.toMutableList()

        if (!deviceList.isNullOrEmpty()) {
            for (counter in deviceList.indices) {
                if (deviceList[counter].mcafeeDeviceId.equals(deviceStatus.deviceId, true)) {
                    var deviceData = deviceList[counter]
                    deviceData.isPaused = deviceStatus.isPaused
                    deviceList.removeAt(counter)
                    deviceList.add(counter, deviceData)
                }
            }
        }
        val deviceMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap()

        deviceMap[DeviceStatus.CONNECTED] = deviceList
        if (!removedList.isNullOrEmpty()) {
            deviceMap[DeviceStatus.BLOCKED] = removedList
        }
        uiDevicesTypeDetails = uiDevicesTypeDetails.copy(deviceSortMap = deviceMap)
        devicesListFlow.latestValue = uiDevicesTypeDetails
    }

    private fun sortAndDisplayDeviceInfo(devicesDataList: ArrayList<DevicesData>) {
        val removedList = devicesDataList.filter { it.blocked }.distinct()
        val connectedList = devicesDataList.filter { !it.blocked }.distinct()
        val deviceMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap()
        if (!connectedList.isNullOrEmpty()) {
            deviceMap[DeviceStatus.CONNECTED] = connectedList
        }
        if (!removedList.isNullOrEmpty()) {
            deviceMap[DeviceStatus.BLOCKED] = removedList
        }
        uiDevicesTypeDetails = uiDevicesTypeDetails.copy(deviceSortMap = deviceMap)
        devicesListFlow.latestValue = uiDevicesTypeDetails
        progressViewFlow.latestValue = false
        if (!connectedList.isNullOrEmpty()) {
            getPauseResumeState(connectedList = connectedList)
        }

    }

    private suspend fun requestModemDetails() {
        when (val modemDetails = asiaRepository.getModemInfo()) {
            is AssiaNetworkResponse.Success -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_MODEM_INFO_SUCCESS)
                val apiInfo = modemDetails.body.modemInfo?.apInfoList
                if (!apiInfo.isNullOrEmpty() && apiInfo[0].isRootAp) {
                    uiDevicesTypeDetails =
                        uiDevicesTypeDetails.copy(isModemAlive = apiInfo[0].isAlive)
                    requestDevices()
                } else {
                    uiDevicesTypeDetails =
                        uiDevicesTypeDetails.copy(isModemAlive = false)
                }
            }
            else -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_MODEM_INFO_FAILURE)
                errorMessageFlow.latestValue = "Error DeviceInfo"
            }
        }
    }

    private suspend fun requestBlocking(stationMac: String) {
        val blockInfo = asiaRepository.unblockDevices(stationMac)
        when (blockInfo) {
            is AssiaNetworkResponse.Success -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UNBLOCK_DEVICE_SUCCESS)
                requestModemDetails()
            }
            else -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UNBLOCK_DEVICE_FAILURE)
                errorMessageFlow.latestValue = "Error DeviceInfo"
            }
        }
    }

    fun unblockDevice(stationMac: String) {
        viewModelScope.launch {
            requestBlocking(stationMac)
        }
    }

    fun navigateToUsageDetails(deviceData: DevicesData) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.LIST_ITEM_CONNECTED_DEVICES)
        val bundle = Bundle()
        bundle.putSerializable(UsageDetailsActivity.DEVICE_INFO, deviceData)
        DevicesCoordinatorDestinations.bundle = bundle
        myState.latestValue = DevicesCoordinatorDestinations.DEVICE_DETAILS
    }

    private suspend fun requestToUpdateNetworkStatus(deviceId: String, isPaused: Boolean) {
        val macresponse = mcafeeRepository.updateDevicePauseResumeStatus(deviceId, isPaused)
        macresponse.fold(ifLeft = {
        }, ifRight = {
            updateUiWithStatus(it)
        })
    }

    fun logRestoreConnection(restoreConnection: Boolean) {
        if (restoreConnection) {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_REMOVED_DEVICES_RESTORE_ACCESS)
        } else {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_REMOVED_DEVICES_CANCEL_ACCESS)
        }
    }

    fun logConnectionStatusChanged(isPaused: Boolean) {
        if (isPaused) {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_PAUSE_CONNECTION_DEVICE_SCREEN)
        } else {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_RESUME_CONNECTION_DEVICE_SCREEN)
        }
    }

    fun logListExpandCollapse() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.EXPANDABLE_LIST_DEVICES)
    }

    fun logRemoveDevicesItemClick() {
        analyticsManagerInterface.logListItemClickEvent(AnalyticsKeys.LIST_ITEM_REMOVED_DEVICES)
    }

    fun updatePauseResumeStatus(deviceData: DevicesData) {
        viewModelScope.launch {
            var deviceId = deviceData.mcafeeDeviceId
            if (!deviceId.isNullOrEmpty()) {
                requestStateForDevices(deviceId = deviceId)
            }
        }
    }

    data class UIDevicesTypeDetails(
        var deviceSortMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap(),
        var isModemAlive: Boolean = false
    )
}