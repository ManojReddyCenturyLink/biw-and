package com.centurylink.biwf.screens.networkstatus

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.NetworkStatusCoordinatorDestinations
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.wifi.NetworkType
import com.centurylink.biwf.model.wifi.UpdateNWPassword
import com.centurylink.biwf.model.wifi.UpdateNetworkName
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkStatusViewModel @Inject constructor(
    private val assiaRepository: AssiaRepository,
    private val wifiNetworkManagementRepository: WifiNetworkManagementRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {
    private var existingWifiNwName: String = ""

    //TODO REMOVE HARDCODING WHEN TEXT WATCHER IS IMPLEMENTED
    private var newWifiName: String = "ctlwifi1"
    private var existingWifiPwd: String = ""
    private var newWifiPwd: String = ""

    //TODO REMOVE HARDCODING WHEN TEXT WATCHER IS IMPLEMENTED
    private var newGuestName: String = "ctlguest1"
    private var existingGuestName: String = ""
    private var existingGuestPwd: String = ""
    var newGuestPwd: String = ""
    var submitFlow: Boolean = false

    val modemInfoFlow: Flow<ModemInfo> = BehaviorStateFlow()
    val internetStatusFlow: Flow<OnlineStatus> = BehaviorStateFlow()
    val myState = EventFlow<NetworkStatusCoordinatorDestinations>()
    val progressViewFlow = EventFlow<Boolean>()
    private var passwordVisibility: Boolean = false
    val error = EventFlow<Errors>()
    val errorSubmitValue = EventFlow<Boolean>()
    val regularNetworkStatusFlow: Flow<UINetworkModel> = BehaviorStateFlow()

    var errorMessageFlow = EventFlow<String>()

    val guestNetworkStatusFlow: Flow<UINetworkModel> = BehaviorStateFlow()
    private var regularNetworkInstance = UINetworkModel()
    private var guestNetworkInstance = UINetworkModel()

    init {
        progressViewFlow.latestValue = true
        fetchPasswordApi()
    }

    private fun fetchPasswordApi() {
        viewModelScope.launch {
            requestToGetNetworkPassword(NetworkType.Band5G)
            requestToGetNetworkPassword(NetworkType.Band2G)
            requestModemInfo()
        }
    }

    private fun modemStatusRefresh() {
        viewModelScope.interval(0, MODEM_STATUS_REFRESH_INTERVAL) {
            requestModemInfo()
        }
    }

    fun wifiNetworkEnablement() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            if (regularNetworkInstance.isNetworkEnabled) {
                requestToDisableNetwork(NetworkType.Band5G)
            } else {
                requestToEnableNetwork(NetworkType.Band5G)
            }
        }
    }

    fun guestNetworkEnablement() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            if (guestNetworkInstance.isNetworkEnabled) {
                requestToDisableNetwork(NetworkType.Band2G)
            } else {
                requestToEnableNetwork(NetworkType.Band2G)
            }
        }
    }

    private suspend fun requestModemInfo() {
        val modemResponse = assiaRepository.getModemInfo()
        progressViewFlow.latestValue = false
        when (modemResponse) {
            is AssiaNetworkResponse.Success -> {
                val apiInfo = modemResponse.body.modemInfo.apInfoList
                modemInfoFlow.latestValue = modemResponse.body.modemInfo
                if (!apiInfo.isNullOrEmpty() && apiInfo[0].isRootAp) {
                    val modemInfo = apiInfo[0]
                    val onlineStatus = OnlineStatus(apiInfo[0].isAlive)
                    val wifiNetworkEnabled =
                        modemInfo.bssidMap.containsValue(NetworkType.Band5G.name)
                    val guestNetworkEnabled =
                        modemInfo.bssidMap.containsValue(NetworkType.Band2G.name)
                    internetStatusFlow.latestValue = onlineStatus
                    if (modemInfo.ssidMap.containsKey(NetworkType.Band5G.name)) {
                        existingWifiNwName = modemInfo.ssidMap.getValue(NetworkType.Band5G.name)
                    }
                    if (modemInfo.ssidMap.containsKey(NetworkType.Band2G.name)) {
                        existingGuestName = modemInfo.ssidMap.getValue(NetworkType.Band2G.name)
                    }
                    if (modemInfo.isAlive) {
                        val onlineStatus = OnlineStatus(modemInfo.isAlive)
                        regularNetworkInstance = setRegularWifiInfo(
                            existingWifiNwName,
                            existingWifiPwd,
                            wifiNetworkEnabled
                        )
                        guestNetworkInstance = setGuestWifiInfo(
                            existingGuestName,
                            existingWifiPwd,
                            true
                        )
                        internetStatusFlow.latestValue = onlineStatus
                        regularNetworkStatusFlow.latestValue = regularNetworkInstance
                        guestNetworkStatusFlow.latestValue = guestNetworkInstance
                    } else {
                        setOfflineNetworkInformation()
                    }
                } else {
                    setOfflineNetworkInformation()
                }
            }
            else -> {
                // Ignoring Error to avoid Frequent
                //errorMessageFlow.latestValue = "Modem Info Not Available"
                setOfflineNetworkInformation()
            }
        }
    }

    private fun setOfflineNetworkInformation() {
        val onlineStatus = OnlineStatus(false)
        internetStatusFlow.latestValue = onlineStatus
        regularNetworkInstance = setRegularWifiInfo(existingWifiNwName, existingWifiPwd, false)
        guestNetworkInstance = setGuestWifiInfo(existingGuestName, existingGuestPwd, false)
        regularNetworkStatusFlow.latestValue = regularNetworkInstance
        guestNetworkStatusFlow.latestValue = guestNetworkInstance
    }

    private fun setGuestWifiInfo(
        name: String, pwd: String,
        guestNetworkEnabled: Boolean
    ): UINetworkModel {
        return guestNetworkInstance.copy(
            netWorkName = name,
            networkPassword = pwd,
            isNetworkEnabled = guestNetworkEnabled,
            networkStatusText = when (guestNetworkEnabled) {
                true -> {
                    R.string.guest_network_enabled
                }
                false -> {
                    R.string.guest_network_disabled
                }
            },
            networkStatusSubText = when (guestNetworkEnabled) {
                true -> {
                    R.string.tap_to_disable_guest_network
                }
                false -> {
                    R.string.tap_to_enable_guest_network
                }
            },
            statusIcon = when (guestNetworkEnabled) {
                true -> {
                    R.drawable.ic_strong_signal
                }
                false -> {
                    R.drawable.ic_off
                }
            }
        )
    }

    private fun setRegularWifiInfo(
        name: String, pwd: String,
        wifiNetworkEnabled: Boolean
    ): UINetworkModel {
        return regularNetworkInstance.copy(
            netWorkName = name,
            networkPassword = pwd,
            isNetworkEnabled = wifiNetworkEnabled,
            networkStatusText = when (wifiNetworkEnabled) {
                true -> {
                    R.string.wifi_network_enabled
                }
                false -> {
                    R.string.wifi_network_disabled
                }
            },
            networkStatusSubText = when (wifiNetworkEnabled) {
                true -> {
                    R.string.tap_to_disable_network
                }
                false -> {
                    R.string.tap_to_enable_network
                }
            },
            statusIcon = when (wifiNetworkEnabled) {
                true -> {
                    R.drawable.ic_strong_signal
                }
                false -> {
                    R.drawable.ic_off
                }
            }
        )
    }

    fun onDoneClick() {
        progressViewFlow.latestValue = true
        submitData()
    }

    fun togglePasswordVisibility(): Boolean {
        passwordVisibility = !passwordVisibility
        return passwordVisibility
    }

    fun onGuestPasswordValueChanged(passwordValue: String) {
        this.newGuestPwd = passwordValue
    }

    fun onGuestNameValueChanged(nameValue: String) {
        this.newGuestName = nameValue
    }

    fun onWifiNameValueChanged(wifiNameValue: String) {
        this.newWifiName = wifiNameValue
    }

    fun onWifiPasswordValueChanged(wifiPasswordValue: String) {
        this.newWifiPwd = wifiPasswordValue
    }

    fun validateInput(): Errors {
        val errors = Errors()
        //  Guest Network State Management
        if (newGuestName.isEmpty()) {
            errors["guestNameError"] = "guestNameError"
            errors["guestNameFieldMandatory"] = "guestNameFieldMandatory"
        }
        if (newGuestPwd.isEmpty()) {
            errors["guestPasswordError"] = "guestPasswordError"
            errors["guestPasswordFieldMandatory"] = "guestPasswordFieldMandatory"
        }
        if (newGuestName.length == nameMinLength || newGuestName.length > nameMaxLength) {
            errors["guestNameError"] = "guestNameError"
            errors["guestNameFieldLength"] = "guestNameFieldLength"
        }
        if (newGuestPwd.length < passwordMinLength || newGuestPwd.length > passwordMaxLength) {
            errors["guestPasswordError"] = "guestPasswordError"
            errors["guestPasswordFieldLength"] = "guestPasswordFieldLength"
        }
        //  Wifi Network State Management
        if (newWifiName.isEmpty()) {
            errors["wifiNameError"] = "wifiNameError"
            errors["wifiNameFieldMandatory"] = "wifiNameFieldMandatory"
        }
        if (newWifiName.length == nameMinLength || newWifiName.length > nameMaxLength) {
            errors["wifiNameError"] = "wifiNameError"
            errors["wifiNameFieldLength"] = "wifiNameFieldLength"
        }
        if (newWifiPwd.isEmpty()) {
            errors["wifiPasswordError"] = "wifiPasswordError"
            errors["wifiPasswordFieldMandatory"] = "wifiPasswordFieldMandatory"
        }

        if (newWifiPwd.length < passwordMinLength || newWifiPwd.length > passwordMaxLength) {
            errors["wifiPasswordError"] = "wifiPasswordError"
            errors["wifiPasswordFieldLength"] = "wifiPasswordFieldLength"
        }
        this.error.latestValue = errors
        return errors
    }

    private suspend fun requestToGetNetworkPassword(networkType: NetworkType) {
        val netWorkInfo = wifiNetworkManagementRepository.getNetworkPassword(networkType)
        when (netWorkInfo) {
            is AssiaNetworkResponse.Success -> {
                val networkName = netWorkInfo.body.networkName
                if (networkName.containsKey(NetworkType.Band5G.name)) {
                    existingWifiPwd = networkName.getValue(NetworkType.Band5G.name)
                }
                if (networkName.containsKey(NetworkType.Band2G.name)) {
                    existingGuestPwd = networkName.getValue(NetworkType.Band5G.name)
                }
                updatePasswords()
            }
            else -> {
                //TODO Currently API is returning Error -Temp Hack for displaying password
                existingWifiPwd = "test123wifi"
                existingGuestPwd = "test123Guest"
                updatePasswords()
            }
        }
    }

    private fun updatePasswords() {
        regularNetworkInstance = setRegularWifiInfo(existingWifiNwName, existingWifiPwd, false)
        guestNetworkInstance = setGuestWifiInfo(existingGuestName, existingGuestPwd, false)
        regularNetworkStatusFlow.latestValue = regularNetworkInstance
        guestNetworkStatusFlow.latestValue = guestNetworkInstance
    }

    private suspend fun requestToUpdateNetWorkPassword(networkType: NetworkType, password: String) {
        val netWorkInfo = wifiNetworkManagementRepository.updateNetworkPassword(
            networkType,
            UpdateNWPassword(password)
        )
        when (netWorkInfo) {
            is AssiaNetworkResponse.Success -> {
                if (netWorkInfo.body.code != "1000") {
                    submitFlow = true
                }
            }
            else -> {
                submitFlow = true
            }
        }
    }

    private suspend fun requestToEnableNetwork(networkType: NetworkType) {
        val netWorkInfo = wifiNetworkManagementRepository.enableNetwork(networkType)
        progressViewFlow.latestValue = false
        when (netWorkInfo) {
            is AssiaNetworkResponse.Success -> {
                updateEnableDisableNetwork(networkType, true)
            }
            else -> {
                errorMessageFlow.latestValue = "Network Enablement Failed"
            }
        }
    }

    private suspend fun requestToDisableNetwork(networkType: NetworkType) {
        val netWorkInfo = wifiNetworkManagementRepository.disableNetwork(networkType)
        progressViewFlow.latestValue = false
        when (netWorkInfo) {
            is AssiaNetworkResponse.Success -> {
                updateEnableDisableNetwork(networkType, false)
            }
            else -> {
                //TODO HANDLING ERROR MOCKED FOR NOW
                errorMessageFlow.latestValue = "Network disablement Failed"
            }
        }
    }

    private fun updateEnableDisableNetwork(networkType: NetworkType, isEnable: Boolean) {
        if (networkType == NetworkType.Band5G) {
            regularNetworkInstance =
                setRegularWifiInfo(existingWifiNwName, existingWifiPwd, isEnable)
            regularNetworkStatusFlow.latestValue = regularNetworkInstance
        }
        if (networkType == NetworkType.Band2G) {
            guestNetworkInstance =
                setGuestWifiInfo(existingGuestName, existingGuestPwd, isEnable)
            guestNetworkStatusFlow.latestValue = guestNetworkInstance
        }
    }


    private suspend fun requestToUpdateWifiNetworkInfo(
        networkType: NetworkType,
        networkName: String
    ) {
        val netWorkInfo = wifiNetworkManagementRepository.updateNetworkName(
            networkType,
            UpdateNetworkName(networkName)
        )
        when (netWorkInfo) {
            is AssiaNetworkResponse.Success -> {
                if (netWorkInfo.body.code != "1000") {
                    submitFlow = true
                }
            }
            else -> {
                submitFlow = true
            }
        }
    }

    private fun submitData() {
        viewModelScope.launch {
            if (existingWifiNwName != newWifiName) {
                if (!newWifiName.isNullOrEmpty() && regularNetworkInstance.isNetworkEnabled) {
                    if (!newWifiName.isNullOrEmpty()) {
                        requestToUpdateWifiNetworkInfo(NetworkType.Band5G, newWifiName)
                    }
                }
            }
            if (existingGuestName != newGuestName && guestNetworkInstance.isNetworkEnabled) {
                if (!newGuestName.isNullOrEmpty()) {
                    requestToUpdateWifiNetworkInfo(NetworkType.Band2G, newGuestName)
                }
            }
            if (existingWifiPwd != newWifiPwd && regularNetworkInstance.isNetworkEnabled) {
                if (!newWifiPwd.isNullOrEmpty() && newWifiPwd.length > 8) {
                    //requestToUpdateNetWorkPassword(NetworkType.Band5G, newWifiPwd)
                }
            }
            if (existingWifiPwd != newGuestPwd && guestNetworkInstance.isNetworkEnabled) {
                if (!newGuestPwd.isNullOrEmpty() && newGuestPwd.length > 8) {
                   // requestToUpdateNetWorkPassword(NetworkType.Band2G, newGuestPwd)
                }
            }
            progressViewFlow.latestValue = false
            errorSubmitValue.latestValue = submitFlow
        }
    }


    data class OnlineStatus(
        val isActive: Boolean
    ) {
        val drawableId: Int
        val onlineStatus: Int
        val subText: Int

        init {
            if (isActive) {
                drawableId = R.drawable.green_circle
                onlineStatus = R.string.lowercase_online
                subText = R.string.you_are_connected_to_the_internet
            } else {
                drawableId = R.drawable.red_circle
                onlineStatus = R.string.lowercase_offline
                subText = R.string.cant_establish_internet_connection
            }
        }
    }

    data class UINetworkModel(
        var netWorkName: String = "",
        var networkPassword: String = "",
        var isNetworkEnabled: Boolean = false,
        var networkStatusText: Int = R.string.wifi_network_enabled,
        var networkStatusSubText: Int = R.string.wifi_network_enabled,
        var statusIcon: Int = R.drawable.ic_strong_signal
    )

    companion object {
        const val nameMinLength = 1
        const val nameMaxLength = 32
        const val passwordMinLength = 8
        const val passwordMaxLength = 63
    }
}