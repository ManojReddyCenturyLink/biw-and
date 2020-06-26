package com.centurylink.biwf.screens.deviceusagedetails

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.UsageDetailsCoordinatorDestinations
import com.centurylink.biwf.repos.assia.NetworkUsageRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.ViewModelFactoryWithInput
import com.centurylink.biwf.utility.viewModelFactory
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import kotlin.math.roundToInt

class UsageDetailsViewModel constructor(
    private val app: BIWFApp,
    private val networkUsageRepository: NetworkUsageRepository
) : BaseViewModel() {

    class Factory @Inject constructor(
        private val app: BIWFApp,
        private val networkUsageRepository: NetworkUsageRepository
    ) : ViewModelFactoryWithInput<String> {

        override fun withInput(input: String): ViewModelProvider.Factory {
            return viewModelFactory {
                val viewModel = UsageDetailsViewModel(app, networkUsageRepository)
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
    val uploadSpeedMonthlyUnit: BehaviorStateFlow<String> = BehaviorStateFlow()
    val uploadSpeedDailyUnit: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedMonthlyUnit: BehaviorStateFlow<String> = BehaviorStateFlow()
    val downloadSpeedDailyUnit: BehaviorStateFlow<String> = BehaviorStateFlow()
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
        uploadSpeedDaily.latestValue =
            formattedTraffic(result.uploadTraffic, result.uploadTrafficUnit)
        downloadSpeedDaily.latestValue =
            formattedTraffic(result.downloadTraffic, result.downloadTrafficUnit)
        uploadSpeedDailyUnit.latestValue = result.uploadTrafficUnit
        downloadSpeedDailyUnit.latestValue = result.downloadTrafficUnit
    }

    private suspend fun requestMonthlyUsageDetails() {
        val result = networkUsageRepository.getUsageDetails(false, staMac)
        uploadSpeedMonthly.latestValue =
            formattedTraffic(result.uploadTraffic, result.uploadTrafficUnit)
        downloadSpeedMonthly.latestValue =
            formattedTraffic(result.downloadTraffic, result.downloadTrafficUnit)
        uploadSpeedMonthlyUnit.latestValue = result.uploadTrafficUnit
        downloadSpeedMonthlyUnit.latestValue = result.downloadTrafficUnit
        progressViewFlow.latestValue = false
    }

    private fun formattedTraffic(trafficVal: Double, unit: String): String {
        if (trafficVal.roundToInt() > 0 && (unit == app.getString(R.string.mb_download))) {
            return trafficVal.roundToInt().toString()
        } else if (trafficVal.roundToInt() > 0 && (unit == app.getString(R.string.mb_upload))) {
            return trafficVal.roundToInt().toString()
        } else if (trafficVal.roundToInt() > 0) {
            return BigDecimal(trafficVal).setScale(1, RoundingMode.UP).toString()
        } else {
            return app.getString(R.string.empty_string)
        }
    }
}