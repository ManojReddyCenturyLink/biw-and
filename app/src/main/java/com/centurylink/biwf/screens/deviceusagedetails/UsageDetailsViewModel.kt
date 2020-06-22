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
    val usageValueMonthly : BehaviorStateFlow<String> = BehaviorStateFlow()
    val usageValueDaily : BehaviorStateFlow<String> = BehaviorStateFlow()

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

    fun onDevicesConnectedClicked() {
    }

    private suspend fun requestDailyUsageDetails() {
        val usageDetails = networkUsageRepository.getDailyUsageDetails()
        usageDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }, ifRight = {
            usageValueDaily.latestValue = it.toString().substring(0,3)
        })
    }

    private suspend fun requestMonthlyUsageDetails() {
        val usageDetails = networkUsageRepository.getMonthlyUsageDetails()
        usageDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }, ifRight = {
            progressViewFlow.latestValue = false
            usageValueMonthly.latestValue = it.toString().substring(0,4)
        })
    }
}