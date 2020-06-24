package com.centurylink.biwf.screens.deviceusagedetails

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.UsageDetailsCoordinatorDestinations
import com.centurylink.biwf.repos.NetworkUsageRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class UsageDetailsViewModel @Inject constructor(
    private val networkUsageRepository: NetworkUsageRepository
) : BaseViewModel() {

    val myState = EventFlow<UsageDetailsCoordinatorDestinations>()
    var progressViewFlow = EventFlow<Boolean>()
    val errorMessageFlow = EventFlow<String>()
    val uploadSpeedMonthly: BehaviorStateFlow<String> = BehaviorStateFlow()
    val uploadSpeedDaily: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedMonthly: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedDaily: BehaviorStateFlow<String> = BehaviorStateFlow()

    init {
        progressViewFlow.latestValue = false //TODO: assign as true after api integration
        initApis()
    }

    fun initApis() {
        viewModelScope.launch {
            requestDailyUsageDetails()
            requestMonthlyUsageDetails()
        }
    }

    fun onDevicesConnectedClicked() {}

    private suspend fun requestDailyUsageDetails() {
        val usageDetails = networkUsageRepository.getUsageDetails(true)
        usageDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }, ifRight = {
            uploadSpeedDaily.latestValue = it.uploadSpeed.toString().substring(0, 3)
            downloadSpeedDaily.latestValue = it.downloadSpeed.toString().substring(0, 3)
        })
    }

    private suspend fun requestMonthlyUsageDetails() {
        val usageDetails = networkUsageRepository.getUsageDetails(false)
        usageDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }, ifRight = {
            uploadSpeedMonthly.latestValue = formattedTraffic(it.uploadSpeed)
            downloadSpeedMonthly.latestValue = formattedTraffic(it.downloadSpeed)
            progressViewFlow.latestValue = false
        })
    }

    private fun formattedTraffic(trafficVal: Double): String {
        if (trafficVal >= 10) {
            return trafficVal.toString().substring(0, 4)
        } else {
            return trafficVal.toString().substring(0, 3)
        }
    }
}