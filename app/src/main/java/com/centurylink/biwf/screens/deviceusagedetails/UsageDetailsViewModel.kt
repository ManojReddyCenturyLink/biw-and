package com.centurylink.biwf.screens.deviceusagedetails

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.UsageDetailsCoordinatorDestinations
import com.centurylink.biwf.repos.assia.NetworkUsageRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.viewModelFactory
import kotlinx.coroutines.launch
import javax.inject.Inject

class UsageDetailsViewModel @Inject constructor(
    private val networkUsageRepository: NetworkUsageRepository
) : BaseViewModel() {

    class Factory @Inject constructor(
        private val networkUsageRepository: NetworkUsageRepository
    ) : ViewModelFactoryWithInput<String> {

        override fun withInput(input: String): ViewModelProvider.Factory {
            return viewModelFactory {
                val viewModel = UsageDetailsViewModel(networkUsageRepository)
                viewModel.staMac = input
                viewModel
            }
        }
    }

    val myState = EventFlow<UsageDetailsCoordinatorDestinations>()
    val progressViewFlow = EventFlow<Boolean>()
    val errorMessageFlow = EventFlow<String>()
    val uploadSpeedMonthly: BehaviorStateFlow<String> = BehaviorStateFlow()
    val uploadSpeedDaily: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedMonthly: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedDaily: BehaviorStateFlow<String> = BehaviorStateFlow()
    var staMac: String = ""

    fun initApis() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestDailyUsageDetails()
            requestMonthlyUsageDetails()
        }
    }

    fun onDevicesConnectedClicked() {}

    private suspend fun requestDailyUsageDetails() {
        val result = networkUsageRepository.getUsageDetails(true, staMac)
        uploadSpeedDaily.latestValue = result.uploadSpeed.toString().substring(0, 3)
        downloadSpeedDaily.latestValue = result.downloadSpeed.toString().substring(0, 3)
    }

    private suspend fun requestMonthlyUsageDetails() {
        val result = networkUsageRepository.getUsageDetails(false, staMac)
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