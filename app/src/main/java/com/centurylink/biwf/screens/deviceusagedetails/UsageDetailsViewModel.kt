package com.centurylink.biwf.screens.deviceusagedetails

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.UsageDetailsCoordinatorDestinations
import com.centurylink.biwf.repos.assia.AssiaNetworkUsageRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class UsageDetailsViewModel @Inject constructor(
    private val assiaNetworkUsageRepository: AssiaNetworkUsageRepository
) : BaseViewModel() {

    val myState = EventFlow<UsageDetailsCoordinatorDestinations>()
    var progressViewFlow = EventFlow<Boolean>()
    val errorMessageFlow = EventFlow<String>()
    val staMacFlow = BehaviorStateFlow<String>()
    val uploadSpeedMonthly: BehaviorStateFlow<String> = BehaviorStateFlow()
    val uploadSpeedDaily: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedMonthly: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedDaily: BehaviorStateFlow<String> = BehaviorStateFlow()

    fun initApis() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestDailyUsageDetails()
            requestMonthlyUsageDetails()
        }
    }

    fun updateStaMacValue(staMac: String) {
        staMacFlow.latestValue = staMac
    }

    fun onDevicesConnectedClicked() {}

    private suspend fun requestDailyUsageDetails() {
        val result = assiaNetworkUsageRepository.getUsageDetails(true, staMacFlow.value)
        uploadSpeedDaily.latestValue = result.uploadSpeed.toString().substring(0, 3)
        downloadSpeedDaily.latestValue = result.downloadSpeed.toString().substring(0, 3)
    }

    private suspend fun requestMonthlyUsageDetails() {
        val result = assiaNetworkUsageRepository.getUsageDetails(false, staMacFlow.value)
        uploadSpeedMonthly.latestValue = formattedTraffic(result.uploadSpeed)
        downloadSpeedMonthly.latestValue = formattedTraffic(result.downloadSpeed)
        progressViewFlow.latestValue = false
    }

    private fun formattedTraffic(trafficVal: Double): String {
        return if (trafficVal >= 10) {
            trafficVal.toString().substring(0, 4)
        } else {
            trafficVal.toString().substring(0, 3)
        }
    }
}