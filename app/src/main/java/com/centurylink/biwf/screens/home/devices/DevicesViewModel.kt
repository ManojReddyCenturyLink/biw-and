package com.centurylink.biwf.screens.home.devices

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.model.devices.DevicesInfo
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DevicesViewModel @Inject constructor(
    private val sharedPreferences: Preferences,
    private val devicesRepository: DevicesRepository
) : BaseViewModel() {

    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    var devicesListFlow: Flow<UIDevicesTypeDetails> = BehaviorStateFlow()

    init {
        initApis()
    }

    fun initApis() {
        viewModelScope.launch {
            requestDevices()
        }
    }

    private suspend fun requestDevices() {
        val deviceDetails = devicesRepository.getDevicesDetails()
        deviceDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            sortAndDisplayDeviceInfo(it)
        }
    }

    private fun sortAndDisplayDeviceInfo(deviceInfo: DevicesInfo) {
        val removedList = deviceInfo.devicesDataList.filter { it.blocked }
        val connectedList = deviceInfo.devicesDataList.filter { !it.blocked }
        val deviceMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap()
        deviceMap[DeviceStatus.CONNECTED_DEVICES] = connectedList
        deviceMap[DeviceStatus.BLOCKED_DEVICES] = removedList
        devicesListFlow.latestValue = UIDevicesTypeDetails(deviceMap)
    }

    data class UIDevicesTypeDetails(
        val deviceSortMap: HashMap<DeviceStatus, List<DevicesData>> = HashMap()
    )
}