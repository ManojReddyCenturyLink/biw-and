package com.centurylink.biwf.screens.networkstatus

import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.NetworkStatusCoordinatorDestinations
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.wifi.NetWorkBand
import com.centurylink.biwf.model.wifi.UpdateNWPassword
import com.centurylink.biwf.model.wifi.UpdateNetworkName
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.repos.assia.WifiStatusRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkStatusViewModel @Inject constructor(
    private val oAuthAssiaRepository: OAuthAssiaRepository,
    private val wifiNetworkManagementRepository: WifiNetworkManagementRepository,
    private val wifiStatusRepository: WifiStatusRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {
    private var existingWifiNwName: String = ""

    private var newWifiName: String = ""
    private var existingWifiPwd: String = ""
    private var newWifiPwd: String = ""

    private var newGuestName: String = ""
    private var existingGuestName: String = ""
    private var existingGuestPwd: String = ""
    private var newGuestPwd: String = ""
    private var submitFlow: Boolean = false

    private var bssidMap: HashMap<String, String> = HashMap()
    private var ssidMap: HashMap<String, String> = HashMap()

    private var regularNetworkEnabled = false
    private var guestNetworkEnabled = false

    val modemInfoFlow: Flow<ModemInfo> = BehaviorStateFlow()
    val internetStatusFlow: Flow<OnlineStatus> = BehaviorStateFlow()
    val myState = EventFlow<NetworkStatusCoordinatorDestinations>()
    val progressViewFlow = EventFlow<Boolean>()
    private var passwordVisibility: Boolean = false
    val error = EventFlow<Errors>()
    val errorSubmitValue = EventFlow<Boolean>()
    val regularNetworkStatusFlow: Flow<UINetworkModel> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    var networkInfoComplete: Boolean = false
    val guestNetworkStatusFlow: Flow<UINetworkModel> = BehaviorStateFlow()
    private var regularNetworkInstance = UINetworkModel()
    private var guestNetworkInstance = UINetworkModel()

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_NETWORK_INFORMATION)
        progressViewFlow.latestValue = true
        initApi()
    }

    fun initApi() {
        viewModelScope.launch {
            requestModemInfo()
            fetchPasswordApi()
        }
        modemStatusRefresh()
    }

    private fun fetchPasswordApi() {
        viewModelScope.launch {
            //fetch WifiRegular Network Password
            if (ssidMap.containsKey(NetWorkBand.Band2G.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band2G)
            } else if (ssidMap.containsKey(NetWorkBand.Band5G.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band5G)
            }
            // fetch Guest Network Password
            if (ssidMap.containsKey(NetWorkBand.Band5G_Guest4.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band5G_Guest4)
            }
            if (ssidMap.containsKey(NetWorkBand.Band2G_Guest4.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band2G_Guest4)
            }
        }
    }

    private fun modemStatusRefresh() {
        viewModelScope.interval(0, MODEM_STATUS_REFRESH_INTERVAL) {
            requestModemInfo()
        }
    }

    fun wifiNetworkEnablement() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.WIFI_NETWORK_STATE_CHANGE_NETWORK_INFORMATION)
        viewModelScope.launch {
            if (internetStatusFlow.latestValue.isActive) {
                progressViewFlow.latestValue = true
                if (regularNetworkInstance.isNetworkEnabled) {
                        requestToDisableNetwork(NetWorkBand.Band2G)
                        requestToDisableNetwork(NetWorkBand.Band5G)
                } else {
                        requestToEnableNetwork(NetWorkBand.Band2G)
                        requestToEnableNetwork(NetWorkBand.Band5G)
                }
            }
        }
    }

    fun guestNetworkEnablement() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.GUEST_NETWORK_STATE_CHANGE_NETWORK_INFORMATION)
        viewModelScope.launch {
            if (internetStatusFlow.latestValue.isActive) {
                progressViewFlow.latestValue = true
                if (guestNetworkInstance.isNetworkEnabled) {
                    requestToDisableNetwork(NetWorkBand.Band2G_Guest4)
                    requestToDisableNetwork(NetWorkBand.Band5G_Guest4)
                } else {
                    requestToEnableNetwork(NetWorkBand.Band2G_Guest4)
                    requestToEnableNetwork(NetWorkBand.Band5G_Guest4)
                }
            }
        }
    }

    private suspend fun requestModemInfo() {
        val modemResponse = oAuthAssiaRepository.getModemInfo()
        progressViewFlow.latestValue = false
        modemResponse.fold(ifRight = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_WIFI_LIST_AND_CREDENTIALS_SUCCESS)
                val apiInfo = it?.apInfoList
                modemInfoFlow.latestValue = it
                if (!apiInfo.isNullOrEmpty() && apiInfo[0].isRootAp) {
                    val modemInfo = apiInfo[0]
                    bssidMap = modemInfo.bssidMap
                    ssidMap = modemInfo.ssidMap
                    regularNetworkEnabled = ModemUtils.getRegularNetworkState(modemInfo)
                    guestNetworkEnabled = ModemUtils.getGuestNetworkState(modemInfo)
                    existingWifiNwName = ModemUtils.getRegularNetworkName(modemInfo)
                    existingGuestName = ModemUtils.getGuestNetworkName(modemInfo)
                    if (modemInfo.isAlive) {
                        val onlineStatus = OnlineStatus(modemInfo.isAlive)
                        regularNetworkInstance = setRegularWifiInfo(
                            name = existingWifiNwName,
                            pwd = existingWifiPwd,
                            wifiNetworkEnabled = regularNetworkEnabled
                        )
                        guestNetworkInstance = setGuestWifiInfo(
                            name = existingGuestName,
                            pwd = existingWifiPwd,
                            guestNetworkEnabled = guestNetworkEnabled
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
            },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_WIFI_LIST_AND_CREDENTIALS_FAILURE)
                // Ignoring Error to avoid Frequent
                //errorMessageFlow.latestValue = "Modem Info Not Available"
                setOfflineNetworkInformation()
            })
    }

    private fun setOfflineNetworkInformation() {
        val onlineStatus = OnlineStatus(false)
        internetStatusFlow.latestValue = onlineStatus
        regularNetworkEnabled = false
        guestNetworkEnabled = false
        regularNetworkInstance = setRegularWifiInfo(existingWifiNwName, existingWifiPwd, regularNetworkEnabled)
        guestNetworkInstance = setGuestWifiInfo(existingGuestName, existingGuestPwd, guestNetworkEnabled)
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
                    R.drawable.ic_three_bars
                }
                false -> {
                    R.drawable.ic_wifi_off
                }
            },
            networkStatusTextColor = when (guestNetworkEnabled) {
                true -> {
                    R.color.purple
                }
                false -> {
                    R.color.med_grey
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
                    R.drawable.ic_three_bars
                }
                false -> {
                    R.drawable.ic_wifi_off
                }
            },
            networkStatusTextColor = when (wifiNetworkEnabled) {
                true -> {
                    R.color.purple
                }
                false -> {
                    R.color.med_grey
                }
            }
        )
    }

    fun onDoneClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_SAVE_CLICK_NETWORK_INFORMATION)
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
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DONE_NETWORK_INFORMATION)
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
        if (newGuestName.length > nameMaxLength) {
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
        if (newWifiName.length > nameMaxLength) {
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

    private suspend fun requestToGetNetworkPassword(netWorkBand: NetWorkBand) {
        val netWorkInfo = wifiNetworkManagementRepository.getNetworkPassword(netWorkBand)
        netWorkInfo.fold( ifRight =
             {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.REQUEST_TO_GET_NETWORK_SUCCESS)
                val password = it.networkName[netWorkBand.name]
                password?.let {
                    when (netWorkBand) {
                        NetWorkBand.Band2G, NetWorkBand.Band5G -> {
                            existingWifiPwd = password
                        }
                        NetWorkBand.Band2G_Guest4, NetWorkBand.Band5G_Guest4 -> {
                            existingGuestPwd = password
                        }
                    }
                }
                updatePasswords()
            },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.REQUEST_TO_GET_NETWORK_FAILURE)
                //TODO Currently API is returning Error -Temp Hack for displaying password
                existingWifiPwd = "test123wifi"
                existingGuestPwd = "test123Guest"
                updatePasswords()
            })
    }

    private fun updatePasswords() {
        regularNetworkInstance =
            setRegularWifiInfo(existingWifiNwName, existingWifiPwd, regularNetworkEnabled)
        guestNetworkInstance =
            setGuestWifiInfo(existingGuestName, existingGuestPwd, guestNetworkEnabled)
        regularNetworkStatusFlow.latestValue = regularNetworkInstance
        guestNetworkStatusFlow.latestValue = guestNetworkInstance
        networkInfoComplete = true
    }

    private suspend fun requestToUpdateNetWorkPassword(netWorkBand: NetWorkBand, password: String) {
        val netWorkInfo = wifiNetworkManagementRepository.updateNetworkPassword(
            netWorkBand,
            UpdateNWPassword(password)
        )
        netWorkInfo.fold(ifRight =
             {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UPDATE_NETWORK_PASSWORD_SUCCESS)

            },
           ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UPDATE_NETWORK_PASSWORD_FAILURE)
                submitFlow = true
            }
        )
    }

    private suspend fun requestToEnableNetwork(netWorkBand: NetWorkBand) {
        val netWorkInfo = wifiStatusRepository.enableNetwork(netWorkBand)
        progressViewFlow.latestValue = false
        netWorkInfo.fold(ifRight =
        {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.ENABLE_NETWORK_SUCCESS)
            updateEnableDisableNetwork(netWorkBand, true)
        },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.ENABLE_NETWORK_FAILURE)
                errorMessageFlow.latestValue = "Network Enablement Failed"
            })
    }

    private suspend fun requestToDisableNetwork(netWorkBand: NetWorkBand) {
        val netWorkInfo = wifiStatusRepository.disableNetwork(netWorkBand)
        progressViewFlow.latestValue = false
        netWorkInfo.fold(
            ifRight =  {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.DISABLE_NETWORK_SUCCESS)
                updateEnableDisableNetwork(netWorkBand, false)
            },
            ifLeft =  {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.DISABLE_NETWORK_FAILURE)
                //TODO HANDLING ERROR MOCKED FOR NOW
                errorMessageFlow.latestValue = "Network disablement Failed"

            }
        )
    }

    private fun updateEnableDisableNetwork(netWorkBand: NetWorkBand, isEnable: Boolean) {
        if (netWorkBand == NetWorkBand.Band5G || netWorkBand == NetWorkBand.Band2G) {
            regularNetworkEnabled = isEnable
            regularNetworkInstance =
                setRegularWifiInfo(existingWifiNwName, existingWifiPwd, isEnable)
            regularNetworkStatusFlow.latestValue = regularNetworkInstance
        }
        if (netWorkBand == NetWorkBand.Band2G_Guest4 || netWorkBand == NetWorkBand.Band5G_Guest4) {
            guestNetworkEnabled = isEnable
            guestNetworkInstance =
                setGuestWifiInfo(existingGuestName, existingGuestPwd, isEnable)
            guestNetworkStatusFlow.latestValue = guestNetworkInstance
        }
    }

    private suspend fun requestToUpdateWifiNetworkInfo(
        netWorkBand: NetWorkBand,
        networkName: String
    ) {
        val netWorkInfo = wifiNetworkManagementRepository.updateNetworkName(
            netWorkBand,
            UpdateNetworkName(networkName)
        )
        netWorkInfo.fold(
            ifRight =  {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UPDATE_NETWORK_NAME_SUCCESS)

            },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UPDATE_NETWORK_NAME_FAILURE)
                submitFlow = true
            }
                )
    }

    private fun submitData() {
        viewModelScope.launch {
            // Update Regular Network NAme
            if (existingWifiNwName != newWifiName) {
                if (!newWifiName.isNullOrEmpty() && regularNetworkInstance.isNetworkEnabled) {
                    if (ssidMap.containsKey(NetWorkBand.Band5G.name)) {
                        requestToUpdateWifiNetworkInfo(NetWorkBand.Band5G, newWifiName)
                    }
                    if (ssidMap.containsKey(NetWorkBand.Band2G.name)) {
                        requestToUpdateWifiNetworkInfo(NetWorkBand.Band2G, newWifiName)
                    }
                }
            }
            // Update Regular Network Password
            if (existingWifiPwd != newWifiPwd && regularNetworkInstance.isNetworkEnabled) {
                if (!newWifiPwd.isNullOrEmpty() && newWifiPwd.length > 8) {
                    if (ssidMap.containsKey(NetWorkBand.Band5G.name)) {
                        requestToUpdateNetWorkPassword(NetWorkBand.Band5G, newWifiPwd)
                    }
                    if (ssidMap.containsKey(NetWorkBand.Band2G.name)) {
                        requestToUpdateNetWorkPassword(NetWorkBand.Band2G, newWifiPwd)
                    }
                }
            }

            if (existingGuestName != newGuestName && guestNetworkInstance.isNetworkEnabled) {
                if (ssidMap.containsKey(NetWorkBand.Band2G_Guest4.name)) {
                    requestToUpdateWifiNetworkInfo(NetWorkBand.Band2G_Guest4, newGuestName)
                }
                if (ssidMap.containsKey(NetWorkBand.Band5G_Guest4.name)) {
                    requestToUpdateWifiNetworkInfo(NetWorkBand.Band5G_Guest4, newGuestName)
                }
            }

            if (existingWifiPwd != newGuestPwd && guestNetworkInstance.isNetworkEnabled) {
                if (!newGuestPwd.isNullOrEmpty() && newGuestPwd.length > 8) {
                    if (ssidMap.containsKey(NetWorkBand.Band2G_Guest4.name)) {
                        requestToUpdateNetWorkPassword(NetWorkBand.Band2G_Guest4, newGuestPwd)
                    }
                    if (ssidMap.containsKey(NetWorkBand.Band5G_Guest4.name)) {
                        requestToUpdateNetWorkPassword(NetWorkBand.Band5G_Guest4, newGuestPwd)
                    }
                }
            }
            progressViewFlow.latestValue = false
            errorSubmitValue.latestValue = submitFlow
        }
    }

    fun logDiscardChangesAndCloseClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ERROR_POPUP_NETWORK_INFORMATION)
    }

    fun logDiscardChangesClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_DISCARD_CLICK_NETWORK_INFORMATION)
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
        var networkStatusTextColor: Int = R.color.purple,
        var networkStatusSubText: Int = R.string.wifi_network_enabled,
        var statusIcon: Int = R.drawable.ic_three_bars
    )

    companion object {
        const val nameMaxLength = 32
        const val passwordMinLength = 8
        const val passwordMaxLength = 63
    }
}