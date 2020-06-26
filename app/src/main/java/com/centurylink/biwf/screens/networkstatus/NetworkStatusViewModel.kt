package com.centurylink.biwf.screens.networkstatus

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.NetworkStatusCoordinatorDestinations
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NetworkStatusViewModel @Inject constructor(
    private val assiaRepository: AssiaRepository
) : BaseViewModel() {

    val modemInfoFlow: Flow<ModemInfo> = BehaviorStateFlow()
    val internetStatusFlow: Flow<OnlineStatus> = BehaviorStateFlow()
    val myState = EventFlow<NetworkStatusCoordinatorDestinations>()
    val progressViewFlow = EventFlow<Boolean>()

    init {
        progressViewFlow.latestValue = true
        modemStatusRefresh()
    }

    private fun modemStatusRefresh() {
        viewModelScope.interval(0, MODEM_STATUS_REFRESH_INTERVAL) {
            requestModemInfo()
        }
    }

    private suspend fun requestModemInfo() {
        modemInfoFlow.latestValue = assiaRepository.getModemInfo().modemInfo
        val onlineStatus = OnlineStatus(modemInfoFlow.latestValue.isAlive)
        internetStatusFlow.latestValue = onlineStatus
        progressViewFlow.latestValue = false
    }

    fun onDoneClick() {
        myState.latestValue = NetworkStatusCoordinatorDestinations.DONE
    }

    data class OnlineStatus(
        val isActive: Boolean
    ) {
        val drawableId: Int
        val onlineStatus: Int
        val subText: Int
        val wifiNetworkButtonText: Int
        val wifiButtonSubText: Int
        val guestNetworkButtonText: Int
        val guestNetworkButtonSubText: Int

        init {
            if (isActive) {
                drawableId = R.drawable.green_circle
                onlineStatus = R.string.lowercase_online
                subText = R.string.you_are_connected_to_the_internet
                wifiNetworkButtonText = R.string.wifi_network_enabled
                wifiButtonSubText = R.string.tap_to_disable_network
                guestNetworkButtonText = R.string.guest_network_enabled
                guestNetworkButtonSubText = R.string.tap_to_disable_guest_network
            } else {
                drawableId = R.drawable.red_circle
                onlineStatus = R.string.lowercase_offline
                subText = R.string.cant_establish_internet_connection
                wifiNetworkButtonText = R.string.wifi_network_disabled
                wifiButtonSubText = R.string.tap_to_enable_network
                guestNetworkButtonText = R.string.guest_network_disabled
                guestNetworkButtonSubText = R.string.tap_to_enable_guest_network
            }
        }
    }
}