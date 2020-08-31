package com.centurylink.biwf.screens.home.devices

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DevicesCoordinatorDestinations
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
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
import javax.inject.Inject

class DevicesViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository,
    private val asiaRepository: AssiaRepository,
    private val mcafeeRepository: McafeeRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    var devicesListFlow: Flow<UIDevicesTypeDetails> = BehaviorStateFlow()
    val myState = EventFlow<DevicesCoordinatorDestinations>()
    var uiDevicesTypeDetails: UIDevicesTypeDetails = UIDevicesTypeDetails()

    init {
        progressViewFlow.latestValue = true
        initApis()
    }

    fun initApis() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_DEVICES)
        viewModelScope.launch {
            requestModemDetails()
            //requestMcafeeDeviceMapping()
        }
    }

    /**
     * Get devices info from Mcafee API
     */
    private suspend fun requestMcafeeDeviceMapping() {
        progressViewFlow.latestValue = true
        val deviceList = ArrayList<String>()
        // TODO - Don't hardcode, but pass in list of MAC Addresses from CloudCheck
        //  Note - We may need to replace the ':' characters in CloudCheck MAC Addresses with '-'
        deviceList.add("CC-FA-00-C6-F5-A6")
        val mcafeeMapping = mcafeeRepository.getDeviceInfo(deviceList)
        mcafeeMapping.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            progressViewFlow.latestValue = false
        }
    }

    private suspend fun requestDevices() {
        val deviceDetails = asiaRepository.getDevicesDetails()
        when (deviceDetails) {
            is AssiaNetworkResponse.Success -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_SUCCESS)
                sortAndDisplayDeviceInfo(deviceDetails.body)
            }
            else -> {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_FAILURE)
                errorMessageFlow.latestValue = "Error DeviceInfo"
            }
        }
    }

    private fun sortAndDisplayDeviceInfo(deviceInfo: DevicesInfo) {
        val removedList = deviceInfo.devicesDataList.filter { it.blocked }.distinct()
        val connectedList = deviceInfo.devicesDataList.filter { !it.blocked }.distinct()
        val deviceMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap()
        deviceMap[DeviceStatus.CONNECTED] = connectedList
        if (!removedList.isNullOrEmpty()) {
            deviceMap[DeviceStatus.BLOCKED] = removedList
        }
        uiDevicesTypeDetails = uiDevicesTypeDetails.copy(deviceSortMap = deviceMap)
        devicesListFlow.latestValue = uiDevicesTypeDetails
        progressViewFlow.latestValue = false
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

    fun navigateToUsageDetails(devicesInfo: DevicesData) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.LIST_ITEM_CONNECTED_DEVICES)
        val bundle = Bundle()
        bundle.putString(UsageDetailsActivity.HOST_NAME, devicesInfo.hostName)
        bundle.putString(UsageDetailsActivity.STA_MAC, devicesInfo.stationMac)
        bundle.putString(
            UsageDetailsActivity.VENDOR_NAME,
            devicesInfo.vendorName?.toLowerCase()?.capitalize()
        )
        DevicesCoordinatorDestinations.bundle = bundle
        myState.latestValue = DevicesCoordinatorDestinations.DEVICE_DETAILS
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

    data class UIDevicesTypeDetails(
        var deviceSortMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap(),
        var isModemAlive: Boolean = false
    )
}