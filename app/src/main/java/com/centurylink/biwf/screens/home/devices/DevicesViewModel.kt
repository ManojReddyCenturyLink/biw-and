package com.centurylink.biwf.screens.home.devices

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DevicesCoordinatorDestinations
import com.centurylink.biwf.model.devices.DeviceConnectionStatus
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.mcafee.DevicePauseStatus
import com.centurylink.biwf.model.mcafee.DevicesItem
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.repos.McafeeRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.launch
import timber.log.Timber
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
    private var devicesDataList: MutableList<DevicesData> = mutableListOf()
    private var isModemAlive: Boolean = false
    var updateDevicesListFlow: EventFlow<UIDevicesTypeDetails> = EventFlow()
    val errorMsg = "Error DeviceInfo"

    init {
        initApis()
    }

    fun initApis() {
        progressViewFlow.latestValue = true
        devicesDataList.clear()
        viewModelScope.launch {
            requestModemDetails()
            requestDevices()
            val macAddresses = getMacAddressesFromDevicesInfo()
            if (!macAddresses.isNullOrEmpty()) {
                requestMcafeeDeviceMapping(macAddresses)
            }
            fetchMcDevices()
            val connectedList = devicesDataList.filter { !it.blocked }.distinct()
            if (!connectedList.isNullOrEmpty() && isModemAlive) {
                getPauseResumeState(connectedList)
            }
        }
    }

    /**
     * Get devices info from Mcafee API
     */
    private suspend fun requestMcafeeDeviceSingleMapping(
        stattionMac: String,
        deviceList: List<String>
    ) {
        val mcafeeMapping = mcafeeRepository.getMcafeeDeviceIds(deviceList)
        mcafeeMapping.fold(ifLeft = {
            updateDeviceListWithLoadingStatus(
                stattionMac!!,
                DeviceConnectionStatus.FAILURE
            )
            upDateDevicesListInUI()
        }, ifRight = { mcafeeDeviceIds ->
            devicesDataList.forEach { deviceData ->
                if (!isModemAlive) {
                    deviceData.deviceConnectionStatus = DeviceConnectionStatus.MODEM_OFF
                }
                deviceData.mcafeeDeviceId = mcafeeDeviceIds.firstOrNull {
                    deviceData.stationMac?.replace(":", "-") == it.mac_address
                }?.devices?.get(0)?.id ?: ""
                requestStateForDevices(deviceId = deviceData.mcafeeDeviceId)
            }
        })
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
        })
    }

    private suspend fun requestDevices() {
        progressViewFlow.latestValue = true
        val deviceDetails = oAuthAssiaRepository.getDevicesDetails()
        deviceDetails.fold(ifRight =
        {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_SUCCESS)
            devicesDataList = it as MutableList<DevicesData>
        }, ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_FAILURE)
            errorMessageFlow.latestValue = errorMsg
        })
    }

    private suspend fun fetchMcDevices() {
        val result = mcafeeRepository.fetchDeviceDetails()
        result.fold(ifLeft = {
            Timber.e("Mcafee Device List Error ")
            errorMessageFlow.latestValue = it
        }, ifRight = {
            updatMcAfeeDevicesInfo(it)
            displayDevicesListInUI()
        })
    }

    private fun updatMcAfeeDevicesInfo(mcAfeeList: List<DevicesItem>) {
        if (!devicesDataList.isNullOrEmpty()) {
            for (counter in devicesDataList.indices) {
                val deviceData = devicesDataList[counter]
                val devicesItem = mcAfeeList.firstOrNull { it.id == deviceData.mcafeeDeviceId }
                if (devicesItem != null) {
                    devicesDataList.removeAt(counter)
                    deviceData.mcAfeeName = devicesItem.name
                    deviceData.mcAfeeDeviceType = devicesItem.deviceType
                    devicesDataList.add(counter, deviceData)
                }
            }
        }
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
                } else {
                    // Typically the MACAFEE Device Id is needed for Making Api Calls to Apigee For some reasons the device Id is not got
                    updateEmptyDeviceIdListWithLoadingErrorStatus(item.stationMac!!)
                    upDateDevicesListInUI()
                }
            }
        }
    }

    private fun updateEmptyDeviceIdListWithLoadingErrorStatus(
        stationMac: String
    ) {
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
            upDateDevicesListInUI()
        }) {
            requestToUpdateNetworkStatus(deviceId, !it.isPaused)
        }
    }

    private suspend fun requestStateForConnectedDevices(deviceId: String) {
        val mcafeeMapping = mcafeeRepository.getDevicePauseResumeStatus(deviceId)
        mcafeeMapping.fold(ifLeft = {
            // Hack to ignore the display of Error message
            updateDeviceListWithLoadingErrorStatus(deviceId, DeviceConnectionStatus.FAILURE)
            upDateDevicesListInUI()
        }) {
            updateDeviceListWithPauseResumeStatus(it)
            upDateDevicesListInUI()
        }
    }

    private fun displayDevicesListInUI() {
        sortAndDisplayDeviceInfo()
        progressViewFlow.latestValue = false
    }

    private fun upDateDevicesListInUI() {
        sortAndUpdateDeviceInfo()
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

    private fun sortAndUpdateDeviceInfo() {
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
        updateDevicesListFlow.latestValue = uiDevicesTypeDetails
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

    private fun updateDeviceListWithLoadingStatus(
        stationMac: String,
        deviceConnectionStatus: DeviceConnectionStatus
    ) {
        if (!devicesDataList.isNullOrEmpty()) {
            for (counter in devicesDataList.indices) {
                if (devicesDataList[counter].stationMac.equals(stationMac, true)) {
                    var deviceData = devicesDataList[counter]
                    deviceData.deviceConnectionStatus = deviceConnectionStatus
                    devicesDataList.removeAt(counter)
                    devicesDataList.add(counter, deviceData)
                }
            }
        }
    }

    private fun sortAndDisplayDeviceInfo() {
        val connectedList = devicesDataList.filter { !it.blocked }.distinct()
        val deviceMap: HashMap<DeviceStatus, MutableList<DevicesData>> = HashMap()
        if (!connectedList.isNullOrEmpty()) {
            deviceMap[DeviceStatus.CONNECTED] = connectedList as MutableList<DevicesData>
        }
// TODO: Commenting code for future reference, currently remove devices api is not working.
//        if (!removedList.isNullOrEmpty()) {
//            deviceMap[DeviceStatus.BLOCKED] = removedList as MutableList<DevicesData>
//        }
        uiDevicesTypeDetails = uiDevicesTypeDetails.copy(deviceSortMap = deviceMap)
        devicesListFlow.latestValue = uiDevicesTypeDetails
    }

    private suspend fun requestModemDetails() {
        val modemDetails = oAuthAssiaRepository.getModemInfo()
        modemDetails.fold(ifRight = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_MODEM_INFO_SUCCESS)
            val apiInfo = it?.apInfoList
            if (!apiInfo.isNullOrEmpty() && apiInfo[0].isRootAp) {
                isModemAlive = apiInfo[0].isAlive
                uiDevicesTypeDetails =
                    uiDevicesTypeDetails.copy(isModemAlive = isModemAlive)
            } else {
                isModemAlive = apiInfo[0].isAlive
                uiDevicesTypeDetails =
                    uiDevicesTypeDetails.copy(isModemAlive = false)
            }
        }, ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_MODEM_INFO_FAILURE)
            errorMessageFlow.latestValue = errorMsg
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
                errorMessageFlow.latestValue = errorMsg
            })
        progressViewFlow.latestValue = false
    }

    fun unblockDevice(stationMac: String) {
        progressViewFlow.latestValue = true
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
            displayDevicesListInUI()
            upDateDevicesListInUI()
        }, ifRight = {
            updateDeviceListWithPauseResumeStatus(it)
            displayDevicesListInUI()
            upDateDevicesListInUI()
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
        viewModelScope.launch {
            analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_DEVICES)
        }
    }

    fun updatePauseResumeStatus(deviceData: DevicesData) {

        viewModelScope.launch {
            when (deviceData.deviceConnectionStatus) {
                DeviceConnectionStatus.FAILURE, DeviceConnectionStatus.DEVICE_CONNECTED, DeviceConnectionStatus.PAUSED -> {
                    var deviceId = deviceData.mcafeeDeviceId
                    var stattionMac = deviceData.stationMac

                    if (deviceId.isNotEmpty()) {
                        updateDeviceListWithLoadingErrorStatus(
                            deviceId,
                            DeviceConnectionStatus.LOADING
                        )
                        requestStateForDevices(deviceId = deviceId)
                    } else {
                        updateDeviceListWithLoadingStatus(
                            stattionMac!!,
                            DeviceConnectionStatus.LOADING
                        )
                        if (!deviceData.stationMac.isNullOrEmpty()) {
                            requestMcafeeDeviceSingleMapping(
                                deviceData.stationMac,
                                listOf(
                                    deviceData.stationMac.replace(
                                        ":",
                                        "-"
                                    )
                                )
                            )
                        }
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
