package com.centurylink.biwf.screens.home.devices

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DevicesCoordinatorDestinations
import com.centurylink.biwf.model.devices.DeviceConnectionStatus
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.mcafee.DevicePauseStatus
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.repos.McafeeRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject

class DevicesViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository,
    private val asiaRepository: AssiaRepository,
    private val oAuthAssiaRepository: OAuthAssiaRepository,
    private val mcafeeRepository: McafeeRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    var devicesListFlow: EventFlow<UIDevicesTypeDetails> = EventFlow()
    val myState = EventFlow<DevicesCoordinatorDestinations>()
    private var uiDevicesTypeDetails: UIDevicesTypeDetails = UIDevicesTypeDetails()
    private lateinit var devicesDataList: MutableList<DevicesData>
    private var isModemAlive: Boolean = false

    init {
        initApis()
    }

    fun initApis() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestModemDetails()
            requestDevices()
            val macAddresses = getMacAddressesFromDevicesInfo()
            if (!macAddresses.isNullOrEmpty()) {
                requestMcafeeDeviceMapping(macAddresses)
            }
            val connectedList = devicesDataList.filter { !it.blocked }.distinct()

            if (!connectedList.isNullOrEmpty() && isModemAlive) {
                getPauseResumeState(connectedList)
            }

            //TODO: Remove later
            val res = mcafeeRepository.updateDeviceName("Tablet","Tablet","00-24-9B-1C149E1B5C613E615643D83783622040F97F4089B0507B451CDD097322BA48EF")
            Log.d("lazy 1"," $res")
            val res1 = mcafeeRepository.fetchDeviceDetails()
            Log.d("lazy 2"," $res1")
        }
    }

    /**
     * Get devices info from Mcafee API
     */
    private suspend fun requestMcafeeDeviceMapping(deviceList: List<String>) {
        val mcafeeMapping = mcafeeRepository.getMcafeeDeviceIds(deviceList)
        mcafeeMapping.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }, ifRight = { mcafeeDeviceIds ->
            devicesDataList.forEach { deviceData ->
                if (!isModemAlive) {
                    deviceData.deviceConnectionStatus = DeviceConnectionStatus.MODEM_OFF
                }
                deviceData.mcafeeDeviceId = mcafeeDeviceIds.firstOrNull {
                    deviceData.stationMac?.replace(":", "-") == it.mac_address
                }?.devices?.get(0)?.id ?: ""
            }
            diplayDevicesListInUI()
        })
    }

    private suspend fun requestDevices() {
        val deviceDetails = asiaRepository.getDevicesDetails()
        deviceDetails.fold(ifRight =
        {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_SUCCESS)
            devicesDataList = it as MutableList<DevicesData>
            diplayDevicesListInUI()
        }, ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_FAILURE)
            errorMessageFlow.latestValue = "Error DeviceInfo"
        })
    }

    private fun getMacAddressesFromDevicesInfo(): List<String> {
        return devicesDataList.map { it.stationMac!!.replace(":", "-") }
    }

    private fun getPauseResumeState(connectedList: List<DevicesData>) {
        val concurrentList = ConcurrentLinkedQueue(connectedList)
        viewModelScope.launch {
            for (item in concurrentList) {
                if (!item.mcafeeDeviceId.isNullOrEmpty()) {
                    requestStateForConnectedDevices(item.mcafeeDeviceId)
                } else{
                    // Typically the MACAFEE Device Id is needed for Making Api Calls to Apigee For some reasons the device Id is not got
                    updateEmptyDeviceIdListWithLoadingErrorStatus(item.stationMac!!)
                    diplayDevicesListInUI()
                }
            }
        }
    }

    private fun updateEmptyDeviceIdListWithLoadingErrorStatus(
        stationMac: String) {
        if (!devicesDataList.isNullOrEmpty()) {
            for (counter in devicesDataList.indices) {
                if (devicesDataList[counter].stationMac.equals(stationMac, true)) {
                    val deviceData = devicesDataList[counter]
                    deviceData.deviceConnectionStatus = DeviceConnectionStatus.FAILURE
                    devicesDataList.removeAt(counter)
                    devicesDataList.add(counter, deviceData)
                }
            }
        }
    }

    private suspend fun requestStateForDevices(deviceId: String) {
        val mcafeeMapping = mcafeeRepository.getDevicePauseResumeStatus(deviceId)
        mcafeeMapping.fold(ifLeft = {
            updateDeviceListWithLoadingErrorStatus(deviceId, DeviceConnectionStatus.FAILURE)
            diplayDevicesListInUI()
        }) {
            requestToUpdateNetworkStatus(deviceId, !it.isPaused)
        }
    }

    private suspend fun requestStateForConnectedDevices(deviceId: String) {
        val mcafeeMapping = mcafeeRepository.getDevicePauseResumeStatus(deviceId)
        mcafeeMapping.fold(ifLeft = {
            // Hack to ignore the display of Error message
            updateDeviceListWithLoadingErrorStatus(deviceId, DeviceConnectionStatus.FAILURE)
            diplayDevicesListInUI()
        }) {
            updateDeviceListWithPauseResumeStatus(it)
            diplayDevicesListInUI()
        }
    }

    private fun diplayDevicesListInUI() {
        sortAndDisplayDeviceInfo()
        progressViewFlow.latestValue = false
    }

    private fun updateDeviceListWithPauseResumeStatus(deviceStatus: DevicePauseStatus) {
        if (!devicesDataList.isNullOrEmpty()) {
            for (counter in devicesDataList.indices) {
                if (devicesDataList[counter].mcafeeDeviceId.equals(deviceStatus.deviceId, true)) {
                    val deviceData = devicesDataList[counter]
                    deviceData.isPaused = deviceStatus.isPaused
                    if (deviceStatus.isPaused) {
                        deviceData.deviceConnectionStatus = DeviceConnectionStatus.PAUSED
                    } else {
                        deviceData.deviceConnectionStatus = DeviceConnectionStatus.DEVICE_CONNECTED
                    }
                    devicesDataList.removeAt(counter)
                    devicesDataList.add(counter, deviceData)
                }
            }
        }
    }

    private fun updateDeviceListWithLoadingErrorStatus(
        errDeviceId: String,
        deviceConnectionStatus: DeviceConnectionStatus
    ) {
        if (!devicesDataList.isNullOrEmpty()) {
            for (counter in devicesDataList.indices) {
                if (devicesDataList[counter].mcafeeDeviceId.equals(errDeviceId, true)) {
                    var deviceData = devicesDataList[counter]
                    deviceData.deviceConnectionStatus = deviceConnectionStatus
                    devicesDataList.removeAt(counter)
                    devicesDataList.add(counter, deviceData)
                }
            }
        }
    }

    private fun sortAndDisplayDeviceInfo() {
        val removedList = devicesDataList.filter { it.blocked }.distinct()
        val connectedList = devicesDataList.filter { !it.blocked }.distinct()
        val deviceMap: HashMap<DeviceStatus, MutableList<DevicesData>> = HashMap()
        if (!connectedList.isNullOrEmpty()) {
            deviceMap[DeviceStatus.CONNECTED] = connectedList as MutableList<DevicesData>
        }
        if (!removedList.isNullOrEmpty()) {
            deviceMap[DeviceStatus.BLOCKED] = removedList as MutableList<DevicesData>
        }
        uiDevicesTypeDetails = uiDevicesTypeDetails.copy(deviceSortMap = deviceMap)
        devicesListFlow.latestValue = uiDevicesTypeDetails
    }

    private suspend fun requestModemDetails() {
      val modemDetails = oAuthAssiaRepository.getModemInfo()
                modemDetails.fold(ifRight =  {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_MODEM_INFO_SUCCESS)
                val apiInfo = it?.apInfoList
                if (!apiInfo.isNullOrEmpty() && apiInfo[0].isRootAp) {
                    isModemAlive =  apiInfo[0].isAlive
                    uiDevicesTypeDetails =
                        uiDevicesTypeDetails.copy(isModemAlive = isModemAlive)
                } else {
                    isModemAlive =  apiInfo[0].isAlive
                    uiDevicesTypeDetails =
                        uiDevicesTypeDetails.copy(isModemAlive = false)
                }
            },ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_MODEM_INFO_FAILURE)
            errorMessageFlow.latestValue = "Error DeviceInfo"
        })
    }

    private suspend fun requestBlocking(stationMac: String) {
        val blockInfo = asiaRepository.unblockDevices(stationMac)
        blockInfo.fold(ifRight =
        {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.UNBLOCK_DEVICE_SUCCESS)
            initApis()
        },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UNBLOCK_DEVICE_FAILURE)
                errorMessageFlow.latestValue = "Error DeviceInfo"
            })
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
        bundle.putBoolean(UsageDetailsActivity.MODEM_STATUS, isModemAlive)
        DevicesCoordinatorDestinations.bundle = bundle
        myState.latestValue = DevicesCoordinatorDestinations.DEVICE_DETAILS
    }

    private suspend fun requestToUpdateNetworkStatus(deviceId: String, isPaused: Boolean) {
        val macresponse = mcafeeRepository.updateDevicePauseResumeStatus(deviceId, isPaused)
        macresponse.fold(ifLeft = {
            updateDeviceListWithLoadingErrorStatus(deviceId, DeviceConnectionStatus.FAILURE)
            diplayDevicesListInUI()
        }, ifRight = {
            updateDeviceListWithPauseResumeStatus(it)
            diplayDevicesListInUI()
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

    fun logScreenLaunch() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_DEVICES)
    }

    fun updatePauseResumeStatus(deviceData: DevicesData) {
        viewModelScope.launch {
            when (deviceData.deviceConnectionStatus) {
                DeviceConnectionStatus.FAILURE,
                DeviceConnectionStatus.DEVICE_CONNECTED,
                DeviceConnectionStatus.PAUSED -> {
                    var deviceId = deviceData.mcafeeDeviceId
                    updateDeviceListWithLoadingErrorStatus(deviceId, DeviceConnectionStatus.LOADING)
                    if (!deviceId.isNullOrEmpty()) {
                        requestStateForDevices(deviceId = deviceId)
                    }
                }
            }
        }
    }

    data class UIDevicesTypeDetails(
        var deviceSortMap: HashMap<DeviceStatus, MutableList<DevicesData>> = HashMap(),
        var isModemAlive: Boolean = false
    )
}