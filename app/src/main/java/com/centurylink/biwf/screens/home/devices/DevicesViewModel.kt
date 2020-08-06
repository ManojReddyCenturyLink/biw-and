package com.centurylink.biwf.screens.home.devices

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DevicesCoordinatorDestinations
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
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
    modemRebootMonitorService: ModemRebootMonitorService
) : BaseViewModel(modemRebootMonitorService) {

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
        viewModelScope.launch {
            requestModemDetails()
        }
    }

    private suspend fun requestDevices() {
        val deviceDetails = asiaRepository.getDevicesDetails()
        when (deviceDetails) {
            is AssiaNetworkResponse.Success -> {
                sortAndDisplayDeviceInfo(deviceDetails.body)
            }
            else -> {
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
                errorMessageFlow.latestValue = "Error DeviceInfo"
            }
        }
    }

    private suspend fun requestBlocking(stationMac: String) {
        val blockInfo = asiaRepository.unblockDevices(stationMac)
        when (blockInfo) {
            is AssiaNetworkResponse.Success -> {
                requestModemDetails()
            }
            else -> {
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

    data class UIDevicesTypeDetails(
        var deviceSortMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap(),
        var isModemAlive: Boolean = false
    )
}