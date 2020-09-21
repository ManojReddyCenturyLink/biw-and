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
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.screens.home.HomeViewModel
import com.centurylink.biwf.screens.networkstatus.ModemUtils
import com.centurylink.biwf.screens.networkstatus.NetworkStatusActivity
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
 *  ViewModel class to handle dashboard related business logic and data classes.
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
    private var rebootOngoing = false
    var installationStatus: Boolean

    init {
        analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_DASHBOARD)
        installationStatus = sharedPreferences.getInstallationStatus()
        progressViewFlow.latestValue = true
        initAccountDetails()
        initModemStatusRefresh()
    }

    fun initDevicesApis() {
        viewModelScope.launch {
            requestWifiDetails()
            fetchPasswordApi()
            requestDevices()
        }
    }

    private fun initModemStatusRefresh() {
        viewModelScope.launch {
            requestModemInfo()
        }
      //  networkStatus.latestValue = false
    }

    fun initAccountDetails() {
        viewModelScope.launch {
            requestAccountDetails()
        }
    }

    private fun fetchPasswordApi() {
        viewModelScope.launch {
            //Fetching Password for Regular Network
            if (ssidMap.containsKey(NetWorkBand.Band2G.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band2G)
            } else if (ssidMap.containsKey(NetWorkBand.Band5G.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band5G)
            }
            //Fetching Password for Guest Network
            if (ssidMap.containsKey(NetWorkBand.Band5G_Guest4.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band5G_Guest4)
            } else if (ssidMap.containsKey(NetWorkBand.Band2G_Guest4.name)) {
                requestToGetNetworkPassword(NetWorkBand.Band2G_Guest4)
            }
        }
    }

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
                //errorMessageFlow.latestValue = modemInfo.toString()
            }
        )
    }

    override suspend fun handleRebootStatus(status: ModemRebootMonitorService.RebootState) {
        super.handleRebootStatus(status)
        rebootOngoing = status == ModemRebootMonitorService.RebootState.ONGOING
    }

    fun startSpeedTest() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_RUN_SPEED_TEST_DASHBOARD)
        if (!progressVisibility.latestValue && !rebootOngoing) {
            getSpeedTestId()
        }
    }

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

    private fun getSpeedTestId() {
        progressVisibility.latestValue = true
        speedTestButtonState.latestValue = false
        sharedPreferences.saveSpeedTestFlag(boolean = true)
        latestSpeedTest.latestValue = DashboardViewModel.EMPTY_RESPONSE
        viewModelScope.launch {
            val speedTestRequest = assiaRepository.startSpeedTest()
            speedTestRequest.fold(
                ifRight = {
                    analyticsManagerInterface.logApiCall(AnalyticsKeys.START_SPEED_TEST_SUCCESS)
                    sharedPreferences.saveSupportSpeedTest(boolean = false)
                    sharedPreferences.saveSpeedTestId(speedTestId = it.speedTestId)
                    checkSpeedTestStatus(requestId = it.speedTestId)

                },
                ifLeft = {
                    analyticsManagerInterface.logApiCall(AnalyticsKeys.START_SPEED_TEST_FAILURE)
                    displayEmptyResponse()
                }
            )
        }
    }

    private fun checkSpeedTestStatus(requestId: Int) {
        viewModelScope.launch {
            var keepChecking = true
            var isSuccessful = false
            while (keepChecking) {
                val status = assiaRepository.checkSpeedTestStatus(speedTestId = requestId)
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
                        displayEmptyResponse()
                        keepChecking = false
                        sharedPreferences.saveSupportSpeedTest(false)
                    }
                )
            }
            if (isSuccessful) getResults()
        }
    }

    private suspend fun getResults() {
        val upstreamData = assiaRepository.getUpstreamResults()
        upstreamData.fold(ifLeft = { displayEmptyResponse() }, ifRight = {
            if (it.data.listOfData.isNotEmpty()) {
                val uploadMb = it.data.listOfData[0].speedAvg / 1000
                uploadSpeed.latestValue = uploadMb.toString()
                sharedPreferences.saveSpeedTestUpload(uploadSpeed = uploadSpeed.latestValue)
            } else {
                uploadSpeed.latestValue = EMPTY_RESPONSE
            }
        })
        val downStreamData = assiaRepository.getDownstreamResults()
        downStreamData.fold(ifLeft = {
            displayEmptyResponse()
        }, ifRight = {
            if (it.data.listOfData.isNotEmpty()) {
                val downloadMb = it.data.listOfData[0].speedAvg / 1000
                downloadSpeed.latestValue = downloadMb.toString()
                latestSpeedTest.latestValue =
                    formatUtcString(it.data.listOfData[0].timeStamp)
                sharedPreferences.saveSpeedTestDownload(downloadSpeed = downloadSpeed.latestValue)
                sharedPreferences.saveLastSpeedTestTime(lastRanTime = latestSpeedTest.latestValue)
            } else {
                downloadSpeed.latestValue = EMPTY_RESPONSE
                latestSpeedTest.latestValue = EMPTY_RESPONSE
            }
        })

        progressVisibility.latestValue = false
        speedTestButtonState.latestValue = true
        sharedPreferences.saveSpeedTestFlag(boolean = false)
    }

    private fun displayEmptyResponse() {
        downloadSpeed.latestValue = EMPTY_RESPONSE
        uploadSpeed.latestValue = EMPTY_RESPONSE
        latestSpeedTest.latestValue = EMPTY_RESPONSE
        progressVisibility.latestValue = false
        speedTestButtonState.latestValue = true
        sharedPreferences.saveSpeedTestFlag(boolean = false)
    }

    private fun refreshAppointmentDetails() {
        viewModelScope.interval(0, APPOINTMENT_DETAILS_REFRESH_INTERVAL) {
            recurringAppointmentCall()
        }
    }


    private suspend fun requestAppointmentDetails() {
        val appointmentDetails = appointmentRepository.getAppointmentInfo()
        appointmentDetails.fold(ifLeft = {
            progressViewFlow.latestValue = false
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_APPOINTMENT_INFO_FAILURE)
            if (it.equals("No Appointment Records", ignoreCase = true)) {
                refresh = false
                isAccountStatus.latestValue =true
                initDevicesApis()
            }

        }) {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_APPOINTMENT_INFO_SUCCESS)
            progressViewFlow.latestValue = false
            cancellationDetails = mockInstanceforCancellation(it)
            refresh = !(it.serviceStatus?.name.equals(ServiceStatus.CANCELED.name) ||
                    it.serviceStatus?.name.equals(ServiceStatus.COMPLETED.name))
            if (!it.jobType.contains(HomeViewModel.intsall) && it.serviceStatus?.name.equals(
                    ServiceStatus.CANCELED.name)
            ) {
                isAccountStatus.latestValue = true
                initDevicesApis()
            }
            else {
                if (!installationStatus) {
                    updateAppointmentStatus(it)
                }else{
                    isAccountStatus.latestValue =true
                    initDevicesApis()
                }
            }
        }
        if (refresh) {
            refreshAppointmentDetails()
        }
    }

    private suspend fun recurringAppointmentCall() {
        val appointmentDetails = appointmentRepository.getAppointmentInfo()
        appointmentDetails.fold(ifLeft = {
            Timber.i("Error in Appointments")
        }) {
            progressViewFlow.latestValue = false
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

    private suspend fun requestWifiDetails() {
        progressViewFlow.latestValue = true
        val modemResponse = oAuthAssiaRepository.getModemInfo()
        modemResponse.fold(ifRight =
        {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_WIFI_LIST_AND_CREDENTIALS_SUCCESS)
            val apiInfo = it?.apInfoList
            if (!apiInfo.isNullOrEmpty()) {
                val modemInfo = apiInfo[0]
                var regularNetworkName = ""
                var guestNetworkName = ""
                ssidMap = modemInfo.ssidMap
                bssidMap = modemInfo.bssidMap
                regularNetworkName = ModemUtils.getRegularNetworkName(modemInfo)
                guestNetworkName = ModemUtils.getGuestNetworkName(modemInfo)
                val guestNetworkEnabled = ModemUtils.getGuestNetworkState(modemInfo)
                val wifiNetworkEnabled = ModemUtils.getRegularNetworkState(modemInfo)
                regularNetworkInfo = regularNetworkInstance.copy(
                    type = NetWorkBand.Band5G.name,
                    name = regularNetworkName,
                    password = regularNetworkWifiPwd,
                    enabled = wifiNetworkEnabled
                )
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
            progressViewFlow.latestValue = false
        }, ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_WIFI_LIST_AND_CREDENTIALS_FAILURE)
            errorMessageFlow.latestValue = "Error WifiInfo"
        })
    }

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
                //TODO Currently API is returning Error -Temp Hack for displaying password
                analyticsManagerInterface.logApiCall(AnalyticsKeys.REQUEST_TO_GET_NETWORK_FAILURE)
                regularNetworkWifiPwd = "test123wifi"
                guestNetworkWifiPwd = "test123Guest"
            })
    }

    private suspend fun requestDevices() {
        val deviceDetails = assiaRepository.getDevicesDetails()
        deviceDetails.fold(ifRight = { deviceList ->
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_SUCCESS)
            val connectedList = deviceList.filter { !it.blocked }.distinct()
            connectedDevicesNumber.latestValue = connectedList.size.toString()
        }, ifLeft = {
            analyticsManagerInterface.logApiCall(AnalyticsKeys.GET_DEVICES_DETAILS_FAILURE)
            errorMessageFlow.latestValue = "Error DeviceInfo"
        })
    }

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

    private suspend fun requestToEnableNetwork(
        netWorkBand: NetWorkBand, wifiInfo: WifiInfo
    ) {
        val netWorkInfo = wifiNetworkManagementRepository.enableNetwork(netWorkBand)
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

    private suspend fun requestToDisableNetwork(
        netWorkBand: NetWorkBand,
        wifiInfo: WifiInfo
    ) {
        val netWorkInfo = wifiNetworkManagementRepository.disableNetwork(netWorkBand)
        netWorkInfo.fold(
            ifRight = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.DISABLE_NETWORK_SUCCESS)
                updateEnableDisableNetwork(wifiInfo)
            },
            ifLeft = {
                analyticsManagerInterface.logApiCall(AnalyticsKeys.DISABLE_NETWORK_FAILURE)
                //TODO HANDLING ERROR MOCKED FOR NOW
                errorMessageFlow.latestValue = "Network disablement Failed"
            })
        progressViewFlow.latestValue = false
    }

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

    private fun updateAppointmentStatus(
        it: AppointmentRecordsInfo
    ) {
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
                    status = it.serviceStatus
                )
                dashBoardDetailsInfo.latestValue = appointmentComplete
            }
            ServiceStatus.CANCELED -> {
                val appointmentCanceled = AppointmentCanceled(
                    serviceAppointmentTime = "",
                    status = ServiceStatus.CANCELED
                )
                dashBoardDetailsInfo.latestValue = appointmentCanceled
            }
            else -> {
                errorMessageFlow.latestValue = "Status not found"
            }
        }
    }

    fun logCancelAppointmentClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CANCEL_APPOINTMENT_DASHBOARD)
    }

    fun logCancelAppointmentAlertClick(positive: Boolean) {
        if (positive) {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_KEEP_CANCEL_APPOINTMENT_CONFIRMATION)
        } else {
            analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_CANCEL_CANCEL_APPOINTMENT_CONFIRMATION)
        }
    }

    fun getChangeAppointment() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CHANGE_APPOINTMENT_DASHBOARD)
        myState.latestValue = DashboardCoordinatorDestinations.CHANGE_APPOINTMENT
    }

    fun displaySortedNotifications(notificationList: List<Notification>) {
        val unreadNotificationList: MutableList<Notification> = notificationList.asSequence()
            .filter { it.isUnRead }
            .toMutableList()
        mergedNotificationList.addAll(unreadNotificationList)
        notifications.value = unreadNotificationList
    }

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

    fun navigateToNotificationDetails(notificationItem: Notification) {
        val bundle = Bundle()
        bundle.putString(NotificationDetailsActivity.URL_TO_LAUNCH, notificationItem.detialUrl)
        bundle.putBoolean(NotificationDetailsActivity.LAUNCH_FROM_HOME, true)
        NotificationCoordinatorDestinations.bundle = bundle
        myState.latestValue = DashboardCoordinatorDestinations.NOTIFICATION_DETAILS
    }

    fun navigateToNetworkInformation(networkName: String) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.CARD_NETWORK_INFO)
        val bundle = Bundle()
        bundle.putString(NetworkStatusActivity.NETWORK_NAME, networkName)
        DashboardCoordinatorDestinations.bundle = bundle
        myState.latestValue = DashboardCoordinatorDestinations.NETWORK_INFORMATION
    }

    fun navigateToQRScan(wifiInfo: WifiInfo) {
        analyticsManagerInterface.logCardClickEvent(AnalyticsKeys.QR_IMAGE)
        val bundle = Bundle()
        bundle.putSerializable(QrScanActivity.WIFI_DETAILS, wifiInfo)
        DashboardCoordinatorDestinations.bundle = bundle
        myState.latestValue = DashboardCoordinatorDestinations.QR_CODE_SCANNING
    }

    fun getStartedClicked() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_GET_STARTED_DASHBOARD)
        sharedPreferences.setInstallationStatus(true)
        isAccountActive = true
        isAccountStatus.latestValue = isAccountActive
    }

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
            analyticsManagerInterface.logApiCall(AnalyticsKeys.CANCEL_APPOINTMENT_SUCCESS)
            progressViewFlow.latestValue = false
            if (it.status != null) {
                updateAppointmentStatus(cancellationDetails)
            }
        }
    }

    fun checkForOngoingSpeedTest() {
        val ongoingTest: Boolean = sharedPreferences.getSupportSpeedTest()
        if (ongoingTest) {
            speedTestButtonState.latestValue = false
            sharedPreferences.saveSupportSpeedTest(boolean = false)
            val speedTestId = sharedPreferences.getSpeedTestId()
            if (speedTestId != null) {
                sharedPreferences.saveSpeedTestFlag(boolean = true)
                progressVisibility.latestValue = true
                latestSpeedTest.latestValue = EMPTY_RESPONSE
                checkSpeedTestStatus(requestId = speedTestId)
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

    fun logAppointmentStatusState(state: Int) {
        when (state) {
            1 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_SCHEDULE_APPOINTMENT)
            2 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_ENROUTE_APPOINTMENT)
            3 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_IN_PROGRESS_APPOINTMENT)
            4 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_COMPLETED_APPOINTMENT)
            5 -> analyticsManagerInterface.logScreenEvent(AnalyticsKeys.SCREEN_CANCELLED_APPOINTMENT)
        }
    }

    fun logViewDevicesClick() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_CONNECTED_DEVICES_DASHBOARD)
    }

    fun logDismissNotification() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_DISMISS_NOTIFICATION)
    }

    abstract class UiDashboardAppointmentInformation

    data class AppointmentScheduleState(
        val jobType: String,
        val status: ServiceStatus,
        val serviceAppointmentDate:
        String,
        val serviceAppointmentStartTime: String,
        val serviceAppointmentEndTime: String,
        val appointmentNumber: String
    ) : UiDashboardAppointmentInformation()

    data class AppointmentEngineerStatus(
        val jobType: String, val status: ServiceStatus,
        val serviceLatitude: String,
        val serviceLongitude: String,
        val serviceAppointmentStartTime: String,
        val serviceAppointmentEndTime: String,
        val serviceAppointmentTime: String,
        val serviceEngineerName: String,
        val serviceEngineerProfilePic: String
    ) : UiDashboardAppointmentInformation()

    data class AppointmentEngineerWIP(
        val jobType: String,
        val status: ServiceStatus,
        val serviceLatitude: String,
        val serviceLongitude: String,
        val serviceEngineerName: String,
        val serviceEngineerProfilePic: String
    ) : UiDashboardAppointmentInformation()

    data class AppointmentComplete(
        val jobType: String,
        val status: ServiceStatus
    ) : UiDashboardAppointmentInformation()

    data class AppointmentCanceled(
        val serviceAppointmentTime: String,
        val status: ServiceStatus
    ) : UiDashboardAppointmentInformation()

    data class wifiScanStatus(
        var wifiListDetails: ArrayList<WifiInfo> = arrayListOf()
    )

    companion object {
        const val EMPTY_RESPONSE = "- -"
        const val APPOINTMENT_DETAILS_REFRESH_INTERVAL = 30000L
    }
}
