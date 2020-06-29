package com.centurylink.biwf.screens.home.devices

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DevicesCoordinatorDestinations
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class DevicesViewModel @Inject constructor(
    private val devicesRepository: DevicesRepository,
    private val asiaRepository: AssiaRepository
) : BaseViewModel() {

    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    var devicesListFlow: Flow<UIDevicesTypeDetails> = BehaviorStateFlow()
    val myState = EventFlow<DevicesCoordinatorDestinations>()
    var uiDevicesTypeDetails: UIDevicesTypeDetails = UIDevicesTypeDetails()

    init {
        initApis()
    }

    fun initApis() {
        progressViewFlow.latestValue = true
        viewModelScope.interval(0, MODEM_STATUS_REFRESH_INTERVAL) {
            requestModemDetails()
        }
    }

    private suspend fun requestMockDevices() {
        val deviceDetails = devicesRepository.getDevicesDetails()
        deviceDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            progressViewFlow.latestValue = false
            sortAndDisplayDeviceInfo(it)
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

    private suspend fun requestModemDetails() {
        val modemDetails = asiaRepository.getModemInfo()
        when (modemDetails) {
            is AssiaNetworkResponse.Success -> {
                if (modemDetails.body.modemInfo.isAlive) {
                    requestDevices()
                } else {
                    onModemOffline()
                }
            }
            else -> {
                errorMessageFlow.latestValue = "Error DeviceInfo"
            }
        }
    }

    private fun sortAndDisplayDeviceInfo(deviceInfo: DevicesInfo) {
        progressViewFlow.latestValue = false
        val removedList = deviceInfo.devicesDataList.filter { it.blocked }
        val connectedList = deviceInfo.devicesDataList.filter { !it.blocked }
        val deviceMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap()
        deviceMap[DeviceStatus.CONNECTED] = connectedList
        if (!removedList.isNullOrEmpty()) {
            deviceMap[DeviceStatus.BLOCKED] = removedList
        }
        devicesListFlow.latestValue = uiDevicesTypeDetails.copy(deviceSortMap = deviceMap)
    }

    private fun onModemOffline() {
        progressViewFlow.latestValue = false
        val deviceMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap()
        deviceMap[DeviceStatus.CONNECTED] = emptyList()
        devicesListFlow.latestValue = uiDevicesTypeDetails.copy(deviceSortMap = deviceMap)
    }

    fun navigateToUsageDetails(devicesInfo: DevicesData) {
        val bundle = Bundle()
        bundle.putString(UsageDetailsActivity.HOST_NAME, devicesInfo.hostName)
        bundle.putString(UsageDetailsActivity.STA_MAC, devicesInfo.stationMac)
        DevicesCoordinatorDestinations.bundle = bundle
        myState.latestValue = DevicesCoordinatorDestinations.DEVICE_DETAILS
    }

    data class UIDevicesTypeDetails(
        val deviceSortMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap()
    )
}