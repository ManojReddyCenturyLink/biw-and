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
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.repos.assia.WifiStatusRepository
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.Errors
import com.centurylink.biwf.utility.EventFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Network status view model
 *
 * @property oAuthAssiaRepository - repository instance to handle oAuth assia api calls
 * @property wifiNetworkManagementRepository - repository instance to handle wifi network
 * management api calls
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class NetworkStatusViewModel @Inject constructor(
    private val oAuthAssiaRepository: OAuthAssiaRepository,
    private val wifiNetworkManagementRepository: WifiNetworkManagementRepository,
    private val wifiStatusRepository: WifiStatusRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {
    private var existingWifiNwName: String = ""

    private var newWifiName: String = ""
    private var existingWifiPassKey: String = ""
    private var newWifiPwd: String = ""

    private var newGuestName: String = ""
    private var existingGuestName: String = ""
    private var existingGuestPassKey: String = ""
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
    var dialogEnableDisableProgress = EventFlow<Boolean>()
    var dialogEnableError = EventFlow<Boolean>()
    var dialogDisableError = EventFlow<Boolean>()
    private var isEnableDisableError: Boolean = false
    var networkCurrentRunningProcess: NetworkEnableDisableEventType = NetworkEnableDisableEventType.REGULAR_WIFI_DISABLE_IN_PROGRESS
    var modemDeviceID = EventFlow<Boolean>()
    var offlineNetworkinfo = false
    var submitValue = false

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_NETWORK_INFORMATION)
        progressViewFlow.latestValue = true
        initApi()
    }

    /**
     * Init api - It will start all the api calls initialisation
     *
     */
    fun initApi() {
        viewModelScope.launch {
            requestModemInfo()
            fetchPasswordApi()
        }
        modemStatusRefresh()
    }

    /**
     * Fetch password api - It will handle password fetching logic from API
     *
     */
    private fun fetchPasswordApi() {
        viewModelScope.launch {
            // fetch WifiRegular Network Password
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

    /**
     * Modem status refresh - It is used to refresh modem status depending on delay
     *
     */
    private fun modemStatusRefresh() {
        viewModelScope.interval(0, MODEM_STATUS_REFRESH_INTERVAL) {
            refreshModemInfo()
        }
    }

    /**
     * Wifi network enablement - It will handle wifi network enable and disable logic
     *
     */
    fun wifiNetworkEnablement() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.WIFI_NETWORK_STATE_CHANGE_NETWORK_INFORMATION)
        isEnableDisableError = false
        viewModelScope.launch {
            if (internetStatusFlow.latestValue.isActive) {
                if (regularNetworkInstance.isNetworkEnabled) {
                    dialogEnableDisableProgress.latestValue = true
                    Timber.d("EnableDisableFlow : wifiNetworkEnablement $bssidMap")
                    if (bssidMap.containsValue(NetWorkBand.Band2G.name) && !isEnableDisableError) {
                        requestToDisableNetwork(NetWorkBand.Band2G)
                        delay(ENABLE_DISABLE_STATUS_REFRESH_INTERVAL)
                    }
                    if (bssidMap.containsValue(NetWorkBand.Band5G.name) && !isEnableDisableError) {
                        requestToDisableNetwork(NetWorkBand.Band5G)
                        delay(ENABLE_DISABLE_STATUS_REFRESH_INTERVAL)
                    }
                } else {
                    dialogEnableDisableProgress.latestValue = true
                    if (!bssidMap.containsValue(NetWorkBand.Band2G.name) && !isEnableDisableError) {
                        requestToEnableNetwork(NetWorkBand.Band2G)
                        delay(ENABLE_DISABLE_STATUS_REFRESH_INTERVAL)
                    }
                    if (!bssidMap.containsValue(NetWorkBand.Band5G.name) && !isEnableDisableError) {
                        requestToEnableNetwork(NetWorkBand.Band5G)
                        delay(ENABLE_DISABLE_STATUS_REFRESH_INTERVAL)
                    }
                }
                requestModemInfo()
                dialogEnableDisableProgress.latestValue = false
            }
        }
    }

    /**
     * Guest network enablement - It will handle guest network enable and disable logic
     *
     */
    fun guestNetworkEnablement() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.GUEST_NETWORK_STATE_CHANGE_NETWORK_INFORMATION)
        isEnableDisableError = false
        viewModelScope.launch {
            if (internetStatusFlow.latestValue.isActive) {
                if (guestNetworkInstance.isNetworkEnabled) {
                    dialogEnableDisableProgress.latestValue = true
                    if (bssidMap.containsValue(NetWorkBand.Band2G_Guest4.name) && !isEnableDisableError) {
                        requestToDisableNetwork(NetWorkBand.Band2G_Guest4)
                        delay(ENABLE_DISABLE_STATUS_REFRESH_INTERVAL)
                    }
                    if (bssidMap.containsValue(NetWorkBand.Band5G_Guest4.name) && !isEnableDisableError) {
                        requestToDisableNetwork(NetWorkBand.Band5G_Guest4)
                        delay(ENABLE_DISABLE_STATUS_REFRESH_INTERVAL)
                    }
                } else {

                    dialogEnableDisableProgress.latestValue = true
                    if (!bssidMap.containsValue(NetWorkBand.Band2G_Guest4.name) && !isEnableDisableError) {
                        requestToEnableNetwork(NetWorkBand.Band2G_Guest4)
                        delay(ENABLE_DISABLE_STATUS_REFRESH_INTERVAL)
                    }
                    if (!bssidMap.containsValue(NetWorkBand.Band5G_Guest4.name) && !isEnableDisableError) {
                        requestToEnableNetwork(NetWorkBand.Band5G_Guest4)
                        delay(ENABLE_DISABLE_STATUS_REFRESH_INTERVAL)
                    }
                }
                requestModemInfo()
                dialogEnableDisableProgress.latestValue = false
            }
        }
    }

    /**
     * Request modem info - It is used handle modem request info logic
     *
     */
    private suspend fun requestModemInfo() {
        val modemResponse = oAuthAssiaRepository.getModemInfo()
        modemResponse.fold(ifRight = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_WIFI_LIST_AND_CREDENTIALS_SUCCESS)
            val apiInfo = it.apInfoList
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
                        pwd = existingWifiPassKey,
                        wifiNetworkEnabled = regularNetworkEnabled
                    )
                    guestNetworkInstance = setGuestWifiInfo(
                        name = existingGuestName,
                        pwd = existingWifiPassKey,
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
                // errorMessageFlow.latestValue = "Modem Info Not Available"
                setOfflineNetworkInformation()
            })
    }

    private suspend fun refreshModemInfo() {
        val modemResponse = oAuthAssiaRepository.getModemInfo()
        modemResponse.fold(ifRight = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_WIFI_LIST_AND_CREDENTIALS_SUCCESS)
            val apiInfo = it.apInfoList
            modemInfoFlow.latestValue = it
            if (!apiInfo.isNullOrEmpty() && apiInfo[0].isRootAp) {
                val modemInfo = apiInfo[0]
                bssidMap = modemInfo.bssidMap
                ssidMap = modemInfo.ssidMap
                regularNetworkEnabled = ModemUtils.getRegularNetworkState(modemInfo)
                guestNetworkEnabled = ModemUtils.getGuestNetworkState(modemInfo)
                if (modemInfo.isAlive) {
                    val onlineStatus = OnlineStatus(modemInfo.isAlive)
                    internetStatusFlow.latestValue = onlineStatus
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
                // errorMessageFlow.latestValue = "Modem Info Not Available"
                setOfflineNetworkInformation()
            })
    }

    /**
     * Set offline network information - It will handle offline network information logic
     *
     */
    private fun setOfflineNetworkInformation() {
        val onlineStatus = OnlineStatus(false)
        internetStatusFlow.latestValue = onlineStatus
        regularNetworkEnabled = false
        guestNetworkEnabled = false
        regularNetworkInstance =
            setRegularWifiInfo(existingWifiNwName, existingWifiPassKey, regularNetworkEnabled)
        guestNetworkInstance =
            setGuestWifiInfo(existingGuestName, existingGuestPassKey, guestNetworkEnabled)
        regularNetworkStatusFlow.latestValue = regularNetworkInstance
        guestNetworkStatusFlow.latestValue = guestNetworkInstance
        modemDeviceID.latestValue = true
        progressViewFlow.latestValue = false
        offlineNetworkinfo = true
    }

    /**
     * Set guest wifi info - It is used set guest wifi network info
     *
     * @param name - The guest wifi network name to be set
     * @param pwd - The guest wifi network password to be set
     * @param guestNetworkEnabled - The boolean value to set guest wifi network enable and disable
     * @return - This will return updated guest network wifi view
     */
    private fun setGuestWifiInfo(
        name: String,
        pwd: String,
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

    /**
     * Set regular wifi info - It is used set regular wifi network info
     *
     * @param name - The regular wifi network name to be set
     * @param pwd - The regular wifi network password to be set
     * @param wifiNetworkEnabled - The boolean value to set regular wifi network enable and disable
     * @return -  This will return updated regular wifi network view
     */
    private fun setRegularWifiInfo(
        name: String,
        pwd: String,
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

    /**
     * On done click - It will handle done button click event logic
     *
     */
    fun onDoneClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_SAVE_CLICK_NETWORK_INFORMATION)
        progressViewFlow.latestValue = true
        submitData()
    }

    /**
     * Toggle password visibility - It will handle toggling password visibility logic
     *
     * @return - negates the visibility
     */
    fun togglePasswordVisibility(): Boolean {
        passwordVisibility = !passwordVisibility
        return passwordVisibility
    }

    /**
     * On guest password value changed - It used to handle change in guest network passwork
     *
     * @param passwordValue - This is the guest network password to be updated
     */
    fun onGuestPasswordValueChanged(passwordValue: String) {
        this.newGuestPwd = passwordValue
    }

    /**
     * On guest name value changed - It used to handle change in guest network name
     *
     * @param nameValue - This is the guest network name to be updated
     */
    fun onGuestNameValueChanged(nameValue: String) {
        this.newGuestName = nameValue
    }

    /**
     * On wifi name value changed - It used to handle change in network name
     *
     * @param wifiNameValue - his is the network name to be updated
     */
    fun onWifiNameValueChanged(wifiNameValue: String) {
        this.newWifiName = wifiNameValue
    }

    /**
     * On wifi password value changed - It used to handle change in network password
     *
     * @param wifiPasswordValue - his is the network password to be updated
     */
    fun onWifiPasswordValueChanged(wifiPasswordValue: String) {
        this.newWifiPwd = wifiPasswordValue
    }

    /**
     * Validate input - It will validate inputs for networks screen
     *
     * @return - return errors based network screen inputs
     */
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

    /**
     * Request to get network password
     *
     * @param netWorkBand - Network Band to update network details through API call
     */
    private suspend fun requestToGetNetworkPassword(netWorkBand: NetWorkBand) {
        val netWorkInfo = wifiNetworkManagementRepository.getNetworkPassword(netWorkBand)
        netWorkInfo.fold(ifRight =
        {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.REQUEST_TO_GET_NETWORK_SUCCESS)
            val password = it.networkName[netWorkBand.name]
            password?.let {
                when (netWorkBand) {
                    NetWorkBand.Band2G, NetWorkBand.Band5G -> {
                        existingWifiPassKey = password
                    }
                    NetWorkBand.Band2G_Guest4, NetWorkBand.Band5G_Guest4 -> {
                        existingGuestPassKey = password
                    }
                }
            }
            updatePasswords()
        },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.REQUEST_TO_GET_NETWORK_FAILURE)
                // TODO Currently API is returning Error -Temp Hack for displaying password
                existingWifiPassKey = "test123wifi"
                existingGuestPassKey = "test123Guest"
            })
    }

    /**
     * Update passwords - It used to update regular and guest wifi network passwords
     *
     */
    private fun updatePasswords() {
        regularNetworkInstance =
            setRegularWifiInfo(existingWifiNwName, existingWifiPassKey, regularNetworkEnabled)
        guestNetworkInstance =
            setGuestWifiInfo(existingGuestName, existingGuestPassKey, guestNetworkEnabled)
        regularNetworkStatusFlow.latestValue = regularNetworkInstance
        guestNetworkStatusFlow.latestValue = guestNetworkInstance
        progressViewFlow.latestValue = false
        networkInfoComplete = true
    }

    /**
     * Request to update net work password
     *
     * @param netWorkBand - Network Band types of the server.
     * @param password - The password to be updated
     */
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

    /**
     * Request to enable network
     *
     * @param netWorkBand - Network Band types of the server to request network enablement
     */
    private suspend fun requestToEnableNetwork(netWorkBand: NetWorkBand) {
        networkCurrentRunningProcess = when (netWorkBand) {
            NetWorkBand.Band2G,
            NetWorkBand.Band5G ->
                NetworkEnableDisableEventType.REGULAR_WIFI_ENABLE_IN_PROGRESS
            NetWorkBand.Band2G_Guest4,
            NetWorkBand.Band5G_Guest4 -> NetworkEnableDisableEventType.GUEST_WIFI_ENABLE_IN_PROGRESS
        }
        val netWorkInfo = wifiStatusRepository.enableNetwork(netWorkBand)
        netWorkInfo.fold(ifRight =
        {
            Timber.d("EnableDisableFlow : Success - requestToEnableNetwork ${netWorkBand.name}")
            analyticsManagerInterface.logApiCall(AnalyticsKeys.ENABLE_NETWORK_SUCCESS)
        },
            ifLeft = {
                delay(MODEM_STATUS_REFRESH_LINEINFO_INTERVAL)
                Timber.d("EnableDisableFlow : failure - requestToEnableNetwork ${netWorkBand.name}")
                isEnableDisableError = true
                analyticsManagerInterface.logApiCall(AnalyticsKeys.ENABLE_NETWORK_FAILURE)
                dialogEnableError.latestValue = true
            })
    }

    /**
     * Request to disable network
     *
     * @param netWorkBand -  Network Band types of the server to request  network disablement
     */
    private suspend fun requestToDisableNetwork(netWorkBand: NetWorkBand) {
        networkCurrentRunningProcess = when (netWorkBand) {
            NetWorkBand.Band2G,
            NetWorkBand.Band5G ->
                NetworkEnableDisableEventType.REGULAR_WIFI_DISABLE_IN_PROGRESS
            NetWorkBand.Band2G_Guest4,
            NetWorkBand.Band5G_Guest4 -> NetworkEnableDisableEventType.GUEST_WIFI_DISABLE_IN_PROGRESS
        }
        val netWorkInfo = wifiStatusRepository.disableNetwork(netWorkBand)
        progressViewFlow.latestValue = false
        netWorkInfo.fold(
            ifRight = {
                Timber.d("EnableDisableFlow : Success - requestToDisableNetwork ${netWorkBand.name}")
                analyticsManagerInterface.logApiCall(AnalyticsKeys.DISABLE_NETWORK_SUCCESS)
            },
            ifLeft = {
                Timber.d("EnableDisableFlow : Failure - requestToDisableNetwork ${netWorkBand.name}")
                analyticsManagerInterface.logApiCall(AnalyticsKeys.DISABLE_NETWORK_FAILURE)
                delay(MODEM_STATUS_REFRESH_LINEINFO_INTERVAL)

                isEnableDisableError = true
                dialogDisableError.latestValue = true
            }
        )
    }

    /**
     * Request to update wifi network info
     *
     * @param netWorkBand - Network Band types of the server to update wifi network information
     * @param networkName - Network name to be updated through network band
     */
    private suspend fun requestToUpdateWifiNetworkInfo(
        netWorkBand: String,
        networkName: String
    ) {
        val netWorkInfo = wifiNetworkManagementRepository.updateNetworkName(
            netWorkBand,
            networkName
        )
        netWorkInfo.fold(
            ifRight = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UPDATE_NETWORK_NAME_SUCCESS)
            },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.UPDATE_NETWORK_NAME_FAILURE)
                submitFlow = true
            }
        )
    }

    /**
     * Submit data - It handles Updating Regular Network Name and Regular Network password
     *
     */
    private fun submitData() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            // Update Regular Network NAme
            if (existingWifiNwName != newWifiName) {
                if (!newWifiName.isNullOrEmpty() && regularNetworkInstance.isNetworkEnabled) {
                    if (ssidMap.containsKey(NetWorkBand.Band5G.name)) {
                        requestToUpdateWifiNetworkInfo(NetWorkBand.Band5G.toString(), newWifiName)
                    }
                    if (ssidMap.containsKey(NetWorkBand.Band2G.name)) {
                        requestToUpdateWifiNetworkInfo(NetWorkBand.Band2G.toString(), newWifiName)
                    }
                }
            }
            // Update Regular Network Password
            if (existingWifiPassKey != newWifiPwd && regularNetworkInstance.isNetworkEnabled) {
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
                    requestToUpdateWifiNetworkInfo(
                        NetWorkBand.Band2G_Guest4.toString(),
                        newGuestName
                    )
                }
                if (ssidMap.containsKey(NetWorkBand.Band5G_Guest4.name)) {
                    requestToUpdateWifiNetworkInfo(
                        NetWorkBand.Band5G_Guest4.toString(),
                        newGuestName
                    )
                }
            }

            if (existingWifiPassKey != newGuestPwd && guestNetworkInstance.isNetworkEnabled) {
                if (!newGuestPwd.isNullOrEmpty() && newGuestPwd.length > 8) {
                    if (ssidMap.containsKey(NetWorkBand.Band2G_Guest4.name)) {
                        requestToUpdateNetWorkPassword(NetWorkBand.Band2G_Guest4, newGuestPwd)
                    }
                    if (ssidMap.containsKey(NetWorkBand.Band5G_Guest4.name)) {
                        requestToUpdateNetWorkPassword(NetWorkBand.Band5G_Guest4, newGuestPwd)
                    }
                }
            }
            errorSubmitValue.latestValue = submitFlow
            if (submitValue) {
                progressViewFlow.latestValue = false
            }
        }
    }

    /**
     * Log discard changes and close click - It will handle discard button click event logic for
     * error dialog
     *
     */
    fun logDiscardChangesAndCloseClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ERROR_POPUP_NETWORK_INFORMATION)
    }

    /**
     * Log discard changes click - It will handle discard button click event logic for alert dialog
     *
     */
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

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {

        enum class NetworkEnableDisableEventType {
            REGULAR_WIFI_ENABLE_IN_PROGRESS, REGULAR_WIFI_DISABLE_IN_PROGRESS, GUEST_WIFI_ENABLE_IN_PROGRESS, GUEST_WIFI_DISABLE_IN_PROGRESS
        }
        const val nameMaxLength = 32
        const val passwordMinLength = 8
        const val passwordMaxLength = 63
    }
}
