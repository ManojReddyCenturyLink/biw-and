package com.centurylink.biwf.screens.home.dashboard

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.coordinators.NotificationCoordinatorDestinations
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.CancelAppointmentInfo
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.model.assia.ModemInfo
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.model.wifi.NetWorkBand
import com.centurylink.biwf.model.wifi.NetWorkCategory
import com.centurylink.biwf.model.wifi.WifiDetails
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.repos.AccountRepository
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.repos.OAuthAssiaRepository
import com.centurylink.biwf.repos.assia.SpeedTestRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.repos.assia.WifiStatusRepository
import com.centurylink.biwf.screens.home.HomeViewModel
import com.centurylink.biwf.screens.networkstatus.ModemUtils
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
import com.centurylink.biwf.screens.qrcode.QrScanActivity
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * Dashboard view model-ViewModel class to handle dashboard related business logic and data classes.
 *
 * @property notificationRepository-repository instance to handle notification api calls
 * @property appointmentRepository-repository instance to handle appointment api calls
 * @property sharedPreferences-sharedPreferences instance toto store and retrive data with in our app
 * @property assiaRepository-repository instance to handle assia services related api calls
 * @property oAuthAssiaRepository-repository instance to handle authentication assia services
 *                                  related api calls
 * @property devicesRepository-repository instance to handle devices related api calls
 * @property accountRepository-repository instance to handle account related api calls
 * @property wifiNetworkManagementRepository-repository instance to handle wifi network related api calls
 * @constructor
 *
 * @param modemRebootMonitorService-service instance to modem reboot actions
 * @param analyticsManagerInterface-analytics instance to track events in firebase
 */
class DashboardViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val appointmentRepository: AppointmentRepository,
    private val sharedPreferences: Preferences,
    private val assiaRepository: AssiaRepository,
    private val oAuthAssiaRepository: OAuthAssiaRepository,
    private val devicesRepository: DevicesRepository,
    private val accountRepository: AccountRepository,
    private val wifiNetworkManagementRepository: WifiNetworkManagementRepository,
    private val wifiStatusRepository: WifiStatusRepository,
    private val speedTestRepository: SpeedTestRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    val dashBoardDetailsInfo: Flow<UiDashboardAppointmentInformation> = BehaviorStateFlow()
    val myState = EventFlow<DashboardCoordinatorDestinations>()
    val notificationListDetails = BehaviorStateFlow<NotificationSource>()
    val notifications: BehaviorStateFlow<MutableList<Notification>> = BehaviorStateFlow()
    val downloadSpeed: Flow<String> = BehaviorStateFlow()
    val uploadSpeed: Flow<String> = BehaviorStateFlow()
    val progressVisibility: Flow<Boolean> = BehaviorStateFlow(false)
    val latestSpeedTest: Flow<String> = BehaviorStateFlow()
    val connectedDevicesNumber: Flow<String> = BehaviorStateFlow()
    val speedTestButtonState: Flow<Boolean> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    var cancelAppointmentError = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    var isAccountStatus = EventFlow<Boolean>()
    val wifiListDetails = BehaviorStateFlow<wifiScanStatus>()
    val networkStatus: BehaviorStateFlow<Boolean> = BehaviorStateFlow()
    val regularNetworkInstance = WifiInfo()
    val guestNetworkInstance = WifiInfo()
    var modemInfoReceived: ModemInfo = ModemInfo()
    private lateinit var regularNetworkInfo: WifiInfo
    private lateinit var guestNetworkInfo: WifiInfo
    private var regularNetworkWifiPwd: String = ""
    private var guestNetworkWifiPwd: String = ""
    private var isEnable: Boolean = true
    private var isAccountActive: Boolean = true
    val wifiListDetailsUpdated = BehaviorStateFlow<wifiScanStatus>()
    private var ssidMap: HashMap<String, String> = HashMap()
    private var bssidMap: HashMap<String, String> = HashMap()
    private lateinit var cancellationDetails: AppointmentRecordsInfo
    private lateinit var appointmentDetails: AppointmentRecordsInfo
    private var refresh: Boolean = false
    private val unreadItem: Notification =
        Notification(
            DashboardFragment.KEY_UNREAD_HEADER, "",
            "", "", true, ""
        )
    private var mergedNotificationList: MutableList<Notification> = mutableListOf()
    var speedTestError = EventFlow<Boolean>()
    private var rebootOngoing = false
    var installationStatus: Boolean = sharedPreferences.getAppointmentNumber()?.let {
        sharedPreferences.getInstallationStatus(
            it
        )
    }!!

    init {
        progressViewFlow.latestValue = true
        initAccountDetails()
        initModemStatusRefresh()
    }

    /**
     * Init devices apis
     */
    fun initDevicesApis() {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            requestWifiDetails()
            fetchPasswordApi()
            requestDevices()
        }
        progressViewFlow.latestValue = false
    }

    /**
     * Init modem status refresh
     */
    private fun initModemStatusRefresh() {
        viewModelScope.interval(0, MODEM_STATUS_REFRESH_INTERVAL) {
            requestModemInfo()
        }
    }

    /**
     * Init account details
     */
    private fun initAccountDetails() {
        viewModelScope.launch {
            requestAccountDetails()
        }
    }

    /**
     * Fetch password api
     */
    private fun fetchPasswordApi() {
        viewModelScope.launch {
            // Fetching Password for Regular Network
            if (ssidMap.containsKey(NetWorkBand.Band2G.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band2G)
            } else if (ssidMap.containsKey(NetWorkBand.Band5G.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band5G)
            }
            // Fetching Password for Guest Network
            if (ssidMap.containsKey(NetWorkBand.Band5G_Guest4.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band5G_Guest4)
            } else if (ssidMap.containsKey(NetWorkBand.Band2G_Guest4.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band2G_Guest4)
            }
        }
    }

    /**
     * Request modem info
     */
    private suspend fun requestModemInfo() {
        val modemInfo = oAuthAssiaRepository.getModemInfo()
        modemInfo.fold(ifRight = {
            val apiInfo = it?.apInfoList
            if (!apiInfo.isNullOrEmpty() && apiInfo[0].isRootAp) {
                networkStatus.latestValue = apiInfo[0].isAlive
            } else {
                networkStatus.latestValue = false
            }
        },
            ifLeft = {
                // Ignoring Error API called every 30 seconds
                // errorMessageFlow.latestValue = modemInfo.toString()
            }
        )
    }

    /**
     * Handle reboot status
     *
     * @param status - ModemRebootMonitorService status to check in which the device reboot process
     */
    override suspend fun handleRebootStatus(status: ModemRebootMonitorService.RebootState) {
        super.handleRebootStatus(status)
        rebootOngoing = status == ModemRebootMonitorService.RebootState.ONGOING
    }

    fun startSpeedTest(displayPopUp: Boolean) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_RUN_SPEED_TEST_DASHBOARD)
        if (!progressVisibility.latestValue && !rebootOngoing) {
            getSpeedTestId(displayPopUp)
        }
    }

    /**
     * Request account details
     */
    private suspend fun requestAccountDetails() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_FAILURE)
            errorMessageFlow.latestValue = it
        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_ACCOUNT_DETAILS_SUCCESS)
            if (it.accountStatus.equals(HomeViewModel.pendingActivation, true) ||
                it.accountStatus.equals(HomeViewModel.abandonedActivation, true)
            ) {
                isAccountActive = false
                isAccountStatus.latestValue = isAccountActive
                requestAppointmentDetails()
                progressViewFlow.latestValue = false
                if (installationStatus) {
                    initDevicesApis()
                }
            } else {
                isAccountActive = true
                requestAppointmentDetails()
                progressViewFlow.latestValue = false
            }
        }
    }

    /**
     * Get speed test id
     *
     * @param displayPopUp - this will confirm to show the  speed test popup or not
     */
    private fun getSpeedTestId(displayPopUp: Boolean) {
        progressVisibility.latestValue = true
        speedTestButtonState.latestValue = false
        sharedPreferences.saveSpeedTestFlag(boolean = true)
        latestSpeedTest.latestValue = DashboardViewModel.EMPTY_RESPONSE
        viewModelScope.launch {
            val speedTestRequest = speedTestRepository.startSpeedTest()
            speedTestRequest.fold(
                ifRight = {
                    analyticsManagerInterface.logApiCall(AnalyticsKeys.START_SPEED_TEST_SUCCESS)
                    sharedPreferences.saveSupportSpeedTest(boolean = false)
                    sharedPreferences.saveSpeedTestId(speedTestId = it.speedTestId)
                    checkSpeedTestStatus(
                        requestId = it.speedTestId,
                        displayErrorPopUp = displayPopUp
                    )
                },
                ifLeft = {
                    analyticsManagerInterface.logApiCall(AnalyticsKeys.START_SPEED_TEST_FAILURE)
                    if (displayPopUp) {
                        speedTestError.latestValue = true
                    }
                    displayEmptyResponse()
                }
            )
        }
    }

    /**
     * Check speed test status
     *
     * @param requestId - speed test request Id
     * @param displayErrorPopUp - this will confirm to show the  speed test error popup or not
     */
    private fun checkSpeedTestStatus(requestId: String, displayErrorPopUp: Boolean) {
        viewModelScope.launch {
            var keepChecking = true
            var isSuccessful = false
            while (keepChecking) {
                val status = speedTestRepository.checkSpeedTestStatus(speedTestId = requestId)
                status.fold(ifRight =
                {
                    if (it.data.isFinished) {
                        analyticsManagerInterface.logApiCall(AnalyticsKeys.CHECK_SPEED_TEST_SUCCESS)
                        isSuccessful = true
                        keepChecking = false
                    } else {
                        delay(SPEED_TEST_REFRESH_INTERVAL)
                    }
                },
                    ifLeft = {
                        analyticsManagerInterface.logApiCall(AnalyticsKeys.CHECK_SPEED_TEST_FAILURE)
                        if (displayErrorPopUp) {
                            speedTestError.latestValue = true
                        }
                        displayEmptyResponse()
                        keepChecking = false
                        sharedPreferences.saveSupportSpeedTest(false)
                    }
                )
            }
            if (isSuccessful) getResults()
        }
    }

    /**
     * Get results
     */
    private suspend fun getResults() {
        var uploadSpeedError = false
        var downloadSpeedError = false
        val result = speedTestRepository.getSpeedTestResults(sharedPreferences.getSpeedTestId()!!)
        result.fold(ifLeft = {
            displayEmptyResponse()
            uploadSpeedError = true
            downloadSpeedError = true
        }, ifRight = {
            val uploadStreamData = it.uploadSpeedSummary.speedTestNestedResults
            val downloadStreamData = it.downloadSpeedSummary.speedTestNestedResults

            if (uploadStreamData.list!!.isNotEmpty() && !uploadStreamData.list.equals(EMPTY_RESPONSE)) {
                val uploadMb = uploadStreamData.list[0].average / 1000
                uploadSpeed.latestValue = uploadMb.toString()
                sharedPreferences.saveSpeedTestUpload(uploadSpeed = uploadSpeed.latestValue)
            } else {
                uploadSpeed.latestValue = EMPTY_RESPONSE
                uploadSpeedError = true
            }

            if (downloadStreamData.list!!.isNotEmpty() && !downloadStreamData.equals(EMPTY_RESPONSE)) {
                val downloadMb = downloadStreamData.list[0].average / 1000
                downloadSpeed.latestValue = downloadMb.toString()
                latestSpeedTest.latestValue =
                    formatUtcString(downloadStreamData.list[0].timestamp)
                sharedPreferences.saveSpeedTestDownload(downloadSpeed = downloadSpeed.latestValue)
                sharedPreferences.saveLastSpeedTestTime(lastRanTime = latestSpeedTest.latestValue)
            } else {
                downloadSpeed.latestValue = EMPTY_RESPONSE
                latestSpeedTest.latestValue = EMPTY_RESPONSE
                downloadSpeedError = true
            }
        })
        if (uploadSpeedError && downloadSpeedError) {
            speedTestError.latestValue = true
        }
        progressVisibility.latestValue = false
        speedTestButtonState.latestValue = true
        sharedPreferences.saveSpeedTestFlag(boolean = false)
    }

    /**
     * Display empty response
     */
    private fun displayEmptyResponse() {
        downloadSpeed.latestValue = EMPTY_RESPONSE
        uploadSpeed.latestValue = EMPTY_RESPONSE
        latestSpeedTest.latestValue = EMPTY_RESPONSE
        progressVisibility.latestValue = false
        speedTestButtonState.latestValue = true
        sharedPreferences.saveSpeedTestFlag(boolean = false)
    }

    /**
     * Refresh appointment details
     */
    private fun refreshAppointmentDetails() {
        viewModelScope.interval(0, APPOINTMENT_DETAILS_REFRESH_INTERVAL) {
            if (::appointmentDetails.isInitialized) {
                recurringAppointmentCall()
            }
        }
    }

    /**
     * Request appointment details
     */
    private suspend fun requestAppointmentDetails() {
        val appointmentDetails = appointmentRepository.getAppointmentInfo()
        appointmentDetails.fold(ifLeft = {
            progressViewFlow.latestValue = false
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_APPOINTMENT_INFO_FAILURE)
            if (it.equals("No Appointment Records", ignoreCase = true)) {
                refresh = false
                isAccountStatus.latestValue = true
                initDevicesApis()
            }
        }) {
            sharedPreferences.saveAppointmentNumber(it.appointmentNumber)
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_APPOINTMENT_INFO_SUCCESS)
            progressViewFlow.latestValue = false
            cancellationDetails = mockInstanceforCancellation(it)
            resetAppointment()
            refresh = !(it.serviceStatus?.name.equals(ServiceStatus.CANCELED.name) ||
                    it.serviceStatus?.name.equals(ServiceStatus.COMPLETED.name))
            if (!it.jobType.contains(HomeViewModel.intsall) && it.serviceStatus?.name.equals(
                    ServiceStatus.CANCELED.name
                )
            ) {
                isAccountStatus.latestValue = true
                initDevicesApis()
            } else {
                if (!installationStatus) {
                    updateAppointmentStatus(it)
                    initDevicesApis()
                } else {
                    isAccountStatus.latestValue = true
                    initDevicesApis()
                }
            }
        }
//        if (refresh) {
        resetAppointment()
        refreshAppointmentDetails()
        //  }
    }

    /**
     * reset the appointment number
     */
    private fun resetAppointment() {
        if (::appointmentDetails.isInitialized) {
            val appointmentNumber = appointmentDetails.appointmentNumber
            if (!appointmentDetails.serviceStatus?.name.equals(ServiceStatus.CANCELED.name) &&
                !appointmentDetails.serviceStatus?.name.equals(ServiceStatus.COMPLETED.name)
            ) {
                if (appointmentNumber != null) {
                    sharedPreferences.setInstallationStatus(false, appointmentNumber)
                    installationStatus = sharedPreferences.getInstallationStatus(appointmentNumber)
                }
            }
        }
    }

    /**
     * Recurring appointment call
     */
    private suspend fun recurringAppointmentCall() {
        val appointmentDetails = appointmentRepository.getAppointmentInfo()
        appointmentDetails.fold(ifLeft = {
            Timber.i("Error in Appointments")
        }) {
            progressViewFlow.latestValue = false
            sharedPreferences.saveAppointmentNumber(it.appointmentNumber)
            cancellationDetails = mockInstanceforCancellation(it)
            refresh = !(it.serviceStatus?.name.equals(ServiceStatus.CANCELED.name) ||
                    it.serviceStatus?.name.equals(ServiceStatus.COMPLETED.name))
            if (!it.jobType.contains(HomeViewModel.intsall) && it.serviceStatus?.name.equals(
                    ServiceStatus.CANCELED.name
                )
            ) {
                isAccountStatus.latestValue = true
                initDevicesApis()
            } else {
                if (!installationStatus) {
                    updateAppointmentStatus(it)
                } else {
                    isAccountStatus.latestValue = true
                    initDevicesApis()
                }
            }
        }
    }

    /**
     * Mock instance for cancellation
     *
     * @param it- AppointmentRecordsInfo instance
     * @return - it will return updated AppointmentRecordsInfo instance
     */
    private fun mockInstanceforCancellation(it: AppointmentRecordsInfo): AppointmentRecordsInfo {
        return AppointmentRecordsInfo(
            serviceAppointmentStartDate = it.serviceAppointmentStartDate,
            serviceAppointmentEndTime = it.serviceAppointmentEndTime,
            serviceEngineerName = it.serviceEngineerName,
            serviceStatus = ServiceStatus.CANCELED,
            serviceEngineerProfilePic = "",
            jobType = it.jobType,
            serviceLatitude = it.serviceLatitude,
            serviceLongitude = it.serviceLongitude,
            appointmentId = it.appointmentId,
            timeZone = it.timeZone,
            appointmentNumber = it.appointmentNumber
        )
    }

    /**
     * Request wifi details
     */
    private suspend fun requestWifiDetails() {
        progressViewFlow.latestValue = true
        val modemResponse = oAuthAssiaRepository.getModemInfo()
        modemResponse.fold(ifRight =
        {
            modemInfoReceived = it
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_WIFI_LIST_AND_CREDENTIALS_SUCCESS)
            val apiInfo = it?.apInfoList
            if (!apiInfo.isNullOrEmpty()) {
                val modemInfo = apiInfo[0]
                ssidMap = modemInfo.ssidMap
                bssidMap = modemInfo.bssidMap
            }
            progressViewFlow.latestValue = false
        }, ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_WIFI_LIST_AND_CREDENTIALS_FAILURE)
            errorMessageFlow.latestValue = "Error WifiInfo"
        })
    }

    /**
     * Request to get network password
     *
     * @param netWorkBand - it will tell which type network brand it is
     */
    private suspend fun requestToGetNetworkPassword(netWorkBand: NetWorkBand) {
        val netWorkInfo =
            wifiNetworkManagementRepository.getNetworkPassword(netWorkBand)
        netWorkInfo.fold(ifRight = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.REQUEST_TO_GET_NETWORK_SUCCESS)
            val password = it.networkName[netWorkBand.name]
            password.let {
                when (netWorkBand) {
                    NetWorkBand.Band2G, NetWorkBand.Band5G -> {
                        regularNetworkWifiPwd = password!!
                    }
                    NetWorkBand.Band2G_Guest4, NetWorkBand.Band5G_Guest4 -> {
                        guestNetworkWifiPwd = password!!
                    }
                }
            }
        },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.REQUEST_TO_GET_NETWORK_FAILURE)
                errorMessageFlow.latestValue = it
            })
        val wifiNetworkEnabled = ModemUtils.getRegularNetworkState(modemInfoReceived?.apInfoList[0])
        val regularNetworkName = ModemUtils.getRegularNetworkName(modemInfoReceived?.apInfoList[0])
        regularNetworkInfo = regularNetworkInstance.copy(
            category = NetWorkCategory.REGULAR,
            type = NetWorkBand.Band5G.name,
            name = regularNetworkName,
            password = regularNetworkWifiPwd,
            enabled = wifiNetworkEnabled
        )
        val guestNetworkName = ModemUtils.getGuestNetworkName(modemInfoReceived?.apInfoList[0])
        val guestNetworkEnabled = ModemUtils.getGuestNetworkState(modemInfoReceived?.apInfoList[0])
        guestNetworkInfo = guestNetworkInstance.copy(
            category = NetWorkCategory.GUEST,
            type = NetWorkBand.Band2G_Guest4.name,
            name = guestNetworkName,
            password = guestNetworkWifiPwd,
            enabled = guestNetworkEnabled
        )
        wifiListDetails.latestValue = wifiScanStatus(
            ArrayList(
                (WifiDetails(
                    listOf(
                        regularNetworkInfo,
                        guestNetworkInfo
                    )
                )).wifiList
            )
        )
    }

    /**
     * Request devices
     */
    private suspend fun requestDevices() {
        val deviceDetails = oAuthAssiaRepository.getDevicesDetails()
        deviceDetails.fold(ifRight = { deviceList ->
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_SUCCESS)
            val connectedList = deviceList.filter { !it.blocked }.distinct()
            connectedDevicesNumber.latestValue = connectedList.size.toString()
        }, ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_FAILURE)
            errorMessageFlow.latestValue = "Error DeviceInfo"
        })
    }

    /**
     * Log screen launch- track the analytics for dashboard launch
     */
    fun logScreenLaunch() {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_DASHBOARD)
    }

    /**
     * Wifi network enablement
     *
     * @param wifiInfo
     */
    fun wifiNetworkEnablement(wifiInfo: WifiInfo) {
        progressViewFlow.latestValue = true
        viewModelScope.launch {
            when (wifiInfo.category) {
                NetWorkCategory.GUEST ->
                    if (wifiInfo.enabled!!) {
                        requestToDisableNetwork(NetWorkBand.Band5G_Guest4, wifiInfo)
                        requestToDisableNetwork(NetWorkBand.Band2G_Guest4, wifiInfo)
                    } else {
                        requestToEnableNetwork(NetWorkBand.Band2G_Guest4, wifiInfo)
                        requestToEnableNetwork(NetWorkBand.Band2G_Guest4, wifiInfo)
                    }
                NetWorkCategory.REGULAR ->
                    if (wifiInfo.enabled!!) {
                        requestToDisableNetwork(NetWorkBand.Band5G, wifiInfo)
                        requestToDisableNetwork(NetWorkBand.Band2G, wifiInfo)
                    } else {
                        requestToEnableNetwork(NetWorkBand.Band2G, wifiInfo)
                        requestToEnableNetwork(NetWorkBand.Band5G, wifiInfo)
                    }
            }
        }
    }

    /**
     * Request to enable network
     *
     * @param netWorkBand -it will tell which type network brand it is
     * @param wifiInfo -wifiInfo instance to read wifi details
     */
    private suspend fun requestToEnableNetwork(
        netWorkBand: NetWorkBand,
        wifiInfo: WifiInfo
    ) {
        val netWorkInfo = wifiStatusRepository.enableNetwork(netWorkBand)
        netWorkInfo.fold(ifRight =
        {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.ENABLE_NETWORK_SUCCESS)
            updateEnableDisableNetwork(wifiInfo)
        },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.ENABLE_NETWORK_FAILURE)
                errorMessageFlow.latestValue = "Network Enablement Failed"
            })
        progressViewFlow.latestValue = false
    }

    /**
     * Request to disable network
     *
     * @param netWorkBand -it will tell which type network brand it is
     * @param wifiInfo -wifiInfo instance to read wifi details
     */
    private suspend fun requestToDisableNetwork(
        netWorkBand: NetWorkBand,
        wifiInfo: WifiInfo
    ) {
        val netWorkInfo = wifiStatusRepository.disableNetwork(netWorkBand)
        netWorkInfo.fold(
            ifRight = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.DISABLE_NETWORK_SUCCESS)
                updateEnableDisableNetwork(wifiInfo)
            },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.DISABLE_NETWORK_FAILURE)
                // TODO HANDLING ERROR MOCKED FOR NOW
                errorMessageFlow.latestValue = "Network disablement Failed"
            })
        progressViewFlow.latestValue = false
    }

    /**
     * Update enable disable network
     *
     * @param wifiInfo -wifiInfo instance to read wifi details
     */
    private fun updateEnableDisableNetwork(wifiInfo: WifiInfo) {
        isEnable = !wifiInfo.enabled!!
        when (wifiInfo.type) {
            NetWorkBand.Band2G.name, NetWorkBand.Band5G.name ->
                regularNetworkInfo = regularNetworkInstance.copy(
                    category = NetWorkCategory.REGULAR,
                    type = NetWorkBand.Band5G.name,
                    name = wifiInfo.name, password = regularNetworkWifiPwd, enabled = isEnable
                )
            NetWorkBand.Band2G_Guest4.name, NetWorkBand.Band5G_Guest4.name ->
                guestNetworkInfo = guestNetworkInstance.copy(
                    category = NetWorkCategory.GUEST,
                    type = NetWorkBand.Band2G.name,
                    name = wifiInfo.name, password = guestNetworkWifiPwd, enabled = isEnable
                )
        }
        wifiListDetailsUpdated.latestValue = wifiScanStatus(
            ArrayList((WifiDetails(listOf(regularNetworkInfo, guestNetworkInfo))).wifiList)
        )
    }

    /**
     * Update appointment status
     *
     * @param it
     */
    private fun updateAppointmentStatus(
        it: AppointmentRecordsInfo
    ) {
        sharedPreferences.saveAppointmentType(it.jobType)
        val timezone = it.timeZone
        appointmentDetails = it
        when (it.serviceStatus) {
            ServiceStatus.SCHEDULED, ServiceStatus.DISPATCHED, ServiceStatus.NONE -> {
                val appointmentState =
                    AppointmentScheduleState(
                        jobType = it.jobType,
                        status = it.serviceStatus,
                        serviceAppointmentDate = DateUtils.formatAppointmentDate(it.serviceAppointmentStartDate.toString()),
                        serviceAppointmentStartTime = DateUtils.formatAppointmentTimeValuesWithTimeZone(
                            it.serviceAppointmentStartDate.toString(),
                            timezone
                        ),
                        serviceAppointmentEndTime = DateUtils.formatAppointmentTimeValuesWithTimeZone(
                            it.serviceAppointmentEndTime.toString(),
                            timezone
                        ),
                        appointmentNumber = it.appointmentNumber
                    )
                dashBoardDetailsInfo.latestValue = appointmentState
            }
            ServiceStatus.EN_ROUTE -> {
                val appointmentEngineerStatus = AppointmentEngineerStatus(
                    jobType = it.jobType,
                    status = it.serviceStatus,
                    serviceLongitude = it.serviceLongitude!!,
                    serviceLatitude = it.serviceLatitude!!,
                    serviceEngineerName = it.serviceEngineerName,
                    serviceEngineerProfilePic = it.serviceEngineerProfilePic!!,
                    serviceAppointmentStartTime = DateUtils.formatAppointmentTimeValuesWithTimeZone(
                        it.serviceAppointmentStartDate.toString(),
                        timezone
                    ),
                    serviceAppointmentEndTime = DateUtils.formatAppointmentTimeValuesWithTimeZone(
                        it.serviceAppointmentEndTime.toString(),
                        timezone
                    ),
                    serviceAppointmentTime = DateUtils.formatAppointmentTimeValuesWithTimeZone(
                        it.serviceAppointmentStartDate.toString(),
                        timezone
                    ) + "-" + DateUtils.formatAppointmentTimeValuesWithTimeZone(
                        it.serviceAppointmentEndTime.toString(),
                        timezone
                    )
                )
                dashBoardDetailsInfo.latestValue = appointmentEngineerStatus
            }
            ServiceStatus.WORK_BEGUN -> {
                val appointmentEngineerWIP = AppointmentEngineerWIP(
                    jobType = it.jobType,
                    status = it.serviceStatus,
                    serviceLongitude = it.serviceLongitude!!,
                    serviceLatitude = it.serviceLatitude!!,
                    serviceEngineerName = it.serviceEngineerName,
                    serviceEngineerProfilePic = it.serviceEngineerProfilePic!!
                )
                dashBoardDetailsInfo.latestValue = appointmentEngineerWIP
            }
            ServiceStatus.COMPLETED -> {
                val appointmentComplete = AppointmentComplete(
                    jobType = it.jobType,
                    status = it.serviceStatus, appointmentNumber = it.appointmentNumber
                )
                dashBoardDetailsInfo.latestValue = appointmentComplete
            }
            ServiceStatus.CANCELED -> {
                val appointmentCanceled = AppointmentCanceled(
                    serviceAppointmentTime = "",
                    status = ServiceStatus.CANCELED,
                    jobType = it.jobType
                )
                dashBoardDetailsInfo.latestValue = appointmentCanceled
            }
            else -> {
                errorMessageFlow.latestValue = "Status not found"
            }
        }
    }

    /**
     * Log cancel appointment click-track the analytics for cancel appointment button click
     */
    fun logCancelAppointmentClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_APPOINTMENT_DASHBOARD)
    }

    /**
     * Log cancel appointment alert click-track the analytics for cancel appointment alert dialog click
     *
     * @param positive- it will tell which alert dialog button is clicked
     */
    fun logCancelAppointmentAlertClick(positive: Boolean) {
        if (positive) {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_KEEP_CANCEL_APPOINTMENT_CONFIRMATION)
        } else {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_CANCEL_CANCEL_APPOINTMENT_CONFIRMATION)
        }
    }

    /**
     * Get change appointment-track the analytics for change appointment button click
     */
    fun getChangeAppointment() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CHANGE_APPOINTMENT_DASHBOARD)
        myState.latestValue = DashboardCoordinatorDestinations.CHANGE_APPOINTMENT
    }

    /**
     * Display sorted notifications
     *
     * @param notificationList- list nof notifications
     */
    fun displaySortedNotifications(notificationList: List<Notification>) {
        val unreadNotificationList: MutableList<Notification> = notificationList.asSequence()
            .filter { it.isUnRead }
            .toMutableList()
        mergedNotificationList.addAll(unreadNotificationList)
        notifications.value = unreadNotificationList
    }

    /**
     * Mark notification as read
     *
     * @param notificationItem - read notification instance
     */
    fun markNotificationAsRead(notificationItem: Notification) {
        if (notificationItem.isUnRead) {
            mergedNotificationList.remove(notificationItem)
            notificationItem.isUnRead = false
            mergedNotificationList.add(mergedNotificationList.size, notificationItem)
            val unreadNotificationList =
                mergedNotificationList.asSequence().filter { it.isUnRead }.toMutableList()
            if (unreadNotificationList.size == 1) {
                mergedNotificationList.remove(unreadItem)
            }
            notifications.value = unreadNotificationList
        }
    }

    /**
     * Navigate to notification details screen
     *
     * @param notificationItem - selected notification instance for navigation
     */
    fun navigateToNotificationDetails(notificationItem: Notification) {
        val bundle = Bundle()
        bundle.putString(NotificationDetailsActivity.URL_TO_LAUNCH, notificationItem.detialUrl)
        bundle.putBoolean(NotificationDetailsActivity.LAUNCH_FROM_HOME, true)
        NotificationCoordinatorDestinations.bundle = bundle
        myState.latestValue = DashboardCoordinatorDestinations.NOTIFICATION_DETAILS
    }

    /**
     * Navigate to network information screen
     */
    fun navigateToNetworkInformation() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.CARD_NETWORK_INFO)
        myState.latestValue = DashboardCoordinatorDestinations.NETWORK_INFORMATION
    }

    /**
     * Navigate to q r scan sc reen
     *
     * @param wifiInfo -wifiInfo instance to read wifi details
     */
    fun navigateToQRScan(wifiInfo: WifiInfo) {
        analyticsManagerInterface.logCardClickEvent(AnalyticsKeys.QR_IMAGE)
        val bundle = Bundle()
        bundle.putSerializable(QrScanActivity.WIFI_DETAILS, wifiInfo)
        DashboardCoordinatorDestinations.bundle = bundle
        myState.latestValue = DashboardCoordinatorDestinations.QR_CODE_SCANNING
    }

    /**
     * Get started clicked-track the analytics for get started button click
     */
    fun getStartedClicked(appointmentNumber: String) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_GET_STARTED_DASHBOARD)
        sharedPreferences.setInstallationStatus(true, appointmentNumber)
    }

    /**
     * Request appointment cancellation- it will invoke api call for appointment cancellation
     *
     */
    fun requestAppointmentCancellation() {
        viewModelScope.launch {
            cancelAppointment()
        }
    }

    /**
     * This method will call Cancellation Appointment API and update the ui
     */
    private suspend fun cancelAppointment() {
        progressViewFlow.latestValue = true
        val cancelAppointmentDetails = appointmentRepository.cancelAppointment(
            CancelAppointmentInfo(
                serviceAppointmentNumber = appointmentDetails.appointmentNumber,
                status = appointmentDetails.serviceStatus
            )
        )
        cancelAppointmentDetails.fold(ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.CANCEL_APPOINTMENT_FAILURE)
            progressViewFlow.latestValue = false
            if (it != null) {
                cancelAppointmentError.latestValue = it
            }
        }) {
            saveAppointmentTCancellation()
            analyticsManagerInterface.logApiCall(AnalyticsKeys.CANCEL_APPOINTMENT_SUCCESS)
            progressViewFlow.latestValue = false
            if (it.status != null) {
                updateAppointmentStatus(cancellationDetails)
            }
        }
    }

    /**
     * Check for ongoing speed test status
     */
    fun checkForOngoingSpeedTest() {
        val ongoingTest: Boolean = sharedPreferences.getSpeedTestFlag()
        if (ongoingTest) {
            progressVisibility.latestValue = sharedPreferences.getSpeedTestFlag()
            speedTestButtonState.latestValue = !sharedPreferences.getSpeedTestFlag()
            sharedPreferences.saveSupportSpeedTest(boolean = false)
            val speedTestId = sharedPreferences.getSpeedTestId()
            if (speedTestId != null) {
                sharedPreferences.saveSpeedTestFlag(boolean = true)
                progressVisibility.latestValue = true
                latestSpeedTest.latestValue = EMPTY_RESPONSE
                checkSpeedTestStatus(requestId = speedTestId, displayErrorPopUp = true)
            }
        }
        val oldDownLoad = sharedPreferences.getSpeedTestDownload()
        val oldUpload = sharedPreferences.getSpeedTestUpload()
        val oldTime = sharedPreferences.getLastSpeedTestTime()
        if (oldDownLoad?.isNotEmpty()!! && oldUpload?.isNotEmpty()!! && oldTime?.isNotEmpty()!!) {
            downloadSpeed.latestValue = oldDownLoad
            uploadSpeed.latestValue = oldUpload
            latestSpeedTest.latestValue = oldTime
        } else {
            displayEmptyResponse()
        }
    }

    /**
     * Log appointment status state - track the analytics based on status
     *
     * @param state-appointment status value
     */
    fun logAppointmentStatusState(state: Int) {
        when (state) {
            1 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_SCHEDULE_APPOINTMENT)
            2 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_ENROUTE_APPOINTMENT)
            3 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_IN_PROGRESS_APPOINTMENT)
            4 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_COMPLETED_APPOINTMENT)
            5 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_CANCELLED_APPOINTMENT)
        }
    }

    /**
     * Log view devices click -track the analytics connected devices click
     */
    fun logViewDevicesClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CONNECTED_DEVICES_DASHBOARD)
    }

    /**
     * Log dismiss notification- track the analytics when dismiss notification
     */
    fun logDismissNotification(state: String) {
        sharedPreferences.saveAppointmentNotificationStatus(
            true,
            sharedPreferences.getAppointmentNumber().plus("_").plus(state)
        )
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DISMISS_NOTIFICATION)
    }

    /**
     * It will read notification read status from preferences
     */
    fun readNotificationStatus(state: String): Boolean {
        return sharedPreferences.getAppointmentNotificationStatus(
            sharedPreferences.getAppointmentNumber().plus("_").plus(state)
        )
    }

    /**
     * It will read cancel appointment read status from preferences
     */
    fun readCancellationAppointmentStatus(): Boolean {

        if (sharedPreferences.getAppointmentCancellationStatus(
                sharedPreferences.getAppointmentNumber().plus("_").plus("Cancelled")
            ) && !readAppointmentType()?.contains(HomeViewModel.intsall)
        ) {
            isAccountStatus.latestValue = true
        }
        return sharedPreferences.getAppointmentCancellationStatus(
            sharedPreferences.getAppointmentNumber().plus("_").plus("Cancelled")
        )
    }

    /**
     * It will read appointment type from preferences
     */
    fun readAppointmentType(): String {
        return sharedPreferences.getAppointmentType()
    }

    /**
     * It will read appointment type from preferences
     */
    fun saveAppointmentTCancellation() {
        sharedPreferences.saveAppointmentCancellationStatus(
            true,
            sharedPreferences.getAppointmentNumber().plus("_").plus("Cancelled")
        )
    }

    /**
     * It will read appointment read status from preferences
     */
    fun clearAppointmentCancellationStatus() {
        sharedPreferences.removeAppointmentCancellationStatus()
    }

    /**
     * It will clear notification read status from preferences
     */
    fun clearNotificationStatus(state: String) {
        if (state.equals(ServiceStatus.WORK_BEGUN.name)) {
            sharedPreferences.removeScheduleNotificationReadStatus()
            sharedPreferences.removeEnrouteNotificationReadStatus()
        } else if (state.equals(ServiceStatus.EN_ROUTE.name)) {
            sharedPreferences.removeScheduleNotificationReadStatus()
            sharedPreferences.removeWorkBegunNotificationReadStatus()
        } else if (state.equals(ServiceStatus.SCHEDULED.name) || state.equals(ServiceStatus.DISPATCHED.name)) {
            sharedPreferences.removeEnrouteNotificationReadStatus()
            sharedPreferences.removeWorkBegunNotificationReadStatus()
        } else if (state.equals(ServiceStatus.COMPLETED.name)) {
            sharedPreferences.removeEnrouteNotificationReadStatus()
            sharedPreferences.removeWorkBegunNotificationReadStatus()
            sharedPreferences.removeScheduleNotificationReadStatus()
        }
    }

    abstract class UiDashboardAppointmentInformation

    /**
     * model calss for appointment schedule states
     */
    data class AppointmentScheduleState(
        val jobType: String,
        val status: ServiceStatus,
        val serviceAppointmentDate:
        String,
        val serviceAppointmentStartTime: String,
        val serviceAppointmentEndTime: String,
        val appointmentNumber: String
    ) : UiDashboardAppointmentInformation()

    /**
     * model calss for appointment engineer status
     */
    data class AppointmentEngineerStatus(
        val jobType: String,
        val status: ServiceStatus,
        val serviceLatitude: String,
        val serviceLongitude: String,
        val serviceAppointmentStartTime: String,
        val serviceAppointmentEndTime: String,
        val serviceAppointmentTime: String,
        val serviceEngineerName: String,
        val serviceEngineerProfilePic: String
    ) : UiDashboardAppointmentInformation()

    /**
     * model calss for appointment engineer wip status
     */
    data class AppointmentEngineerWIP(
        val jobType: String,
        val status: ServiceStatus,
        val serviceLatitude: String,
        val serviceLongitude: String,
        val serviceEngineerName: String,
        val serviceEngineerProfilePic: String
    ) : UiDashboardAppointmentInformation()

    /**
     * model calss for appointment complete
     */
    data class AppointmentComplete(
        val jobType: String,
        val status: ServiceStatus,
        val appointmentNumber: String
    ) : UiDashboardAppointmentInformation()

    /**
     * model calss for appointment canceled
     */
    data class AppointmentCanceled(
        val jobType: String,
        val serviceAppointmentTime: String,
        val status: ServiceStatus
    ) : UiDashboardAppointmentInformation()

    /**
     * model calss for wifi scan status
     */
    data class wifiScanStatus(
        var wifiListDetails: ArrayList<WifiInfo> = arrayListOf()
    )

    companion object {
        const val EMPTY_RESPONSE = "- -"
        const val APPOINTMENT_DETAILS_REFRESH_INTERVAL = 30000L
    }
}
