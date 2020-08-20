package com.centurylink.biwf.screens.home.dashboard

import android.os.Bundle
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.DashboardCoordinatorDestinations
import com.centurylink.biwf.coordinators.NotificationCoordinatorDestinations
import com.centurylink.biwf.model.appointment.AppointmentRecordsInfo
import com.centurylink.biwf.model.appointment.ServiceStatus
import com.centurylink.biwf.model.notification.Notification
import com.centurylink.biwf.model.notification.NotificationSource
import com.centurylink.biwf.model.wifi.NetworkType
import com.centurylink.biwf.model.wifi.WifiDetails
import com.centurylink.biwf.model.wifi.WifiInfo
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.AssiaRepository
import com.centurylink.biwf.repos.DevicesRepository
import com.centurylink.biwf.repos.NotificationRepository
import com.centurylink.biwf.repos.assia.WifiNetworkManagementRepository
import com.centurylink.biwf.screens.networkstatus.NetworkStatusActivity
import com.centurylink.biwf.screens.notification.NotificationDetailsActivity
import com.centurylink.biwf.screens.qrcode.QrScanActivity
import com.centurylink.biwf.service.impl.aasia.AssiaNetworkResponse
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.DateUtils
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DashboardViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val appointmentRepository: AppointmentRepository,
    private val sharedPreferences: Preferences,
    private val assiaRepository: AssiaRepository,
    private val devicesRepository: DevicesRepository,
    private val wifiNetworkManagementRepository: WifiNetworkManagementRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    private val analyticsManagerInterface : AnalyticsManager
) : BaseViewModel(modemRebootMonitorService,analyticsManagerInterface) {

    val dashBoardDetailsInfo: Flow<UiDashboardAppointmentInformation> = BehaviorStateFlow()
    val myState = EventFlow<DashboardCoordinatorDestinations>()
    val notificationListDetails = BehaviorStateFlow<NotificationSource>()
    val notifications: BehaviorStateFlow<MutableList<Notification>> = BehaviorStateFlow()
    val downloadSpeed: Flow<String> = BehaviorStateFlow()
    val uploadSpeed: Flow<String> = BehaviorStateFlow()
    val progressVisibility: Flow<Boolean> = BehaviorStateFlow(false)
    val latestSpeedTest: Flow<String> = BehaviorStateFlow()
    val isExistingUser = BehaviorStateFlow<Boolean>()
    val speedTestButtonState: Flow<Boolean> = BehaviorStateFlow()
    var errorMessageFlow = EventFlow<String>()
    var progressViewFlow = EventFlow<Boolean>()
    val wifiListDetails = BehaviorStateFlow<wifiScanStatus>()
    val regularNetworkInstance = WifiInfo()
    val guestNetworkInstance = WifiInfo()
    private lateinit var regularNetworkInfo: WifiInfo
    private lateinit var guestNetworkInfo: WifiInfo
    private var regularNetworkWifiPwd: String = ""
    private var guestNetworkWifiPwd: String = ""
    private lateinit var networkType: NetworkType
    private var isEnable: Boolean = true
    val wifiListDetailsUpdated = BehaviorStateFlow<wifiScanStatus>()

    private lateinit var cancelAppointmentInstance: AppointmentRecordsInfo
    private val unreadItem: Notification =
        Notification(
            DashboardFragment.KEY_UNREAD_HEADER, "",
            "", "", true, ""
        )
    private var mergedNotificationList: MutableList<Notification> = mutableListOf()
    private var rebootOngoing = false

    init {
        initApis()
        isExistingUser.value = sharedPreferences.getUserType() ?: false
    }

    fun initApis() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestToGetNetworkPassword(NetworkType.Band5G)
            requestToGetNetworkPassword(NetworkType.Band2G)
            requestWifiDetails()
            // TODO right now this feature is not in active so commenting for now
           // requestNotificationDetails()
        }
        if (sharedPreferences.getUserType() != true) {
            refreshAppointmentDetails()
        }
    }

    override suspend fun handleRebootStatus(status: ModemRebootMonitorService.RebootState) {
        super.handleRebootStatus(status)
        rebootOngoing = status == ModemRebootMonitorService.RebootState.ONGOING
    }

    fun startSpeedTest() {
        if (!progressVisibility.latestValue && !rebootOngoing) {
            getSpeedTestId()
        } else {
            speedTestButtonState.latestValue = false
        }
    }

    private fun getSpeedTestId() {
        sharedPreferences.saveSpeedTestFlag(boolean = true)
        progressVisibility.latestValue = true
        speedTestButtonState.latestValue = false
        latestSpeedTest.latestValue = EMPTY_RESPONSE
        viewModelScope.launch {
            when (val speedTestRequest = assiaRepository.startSpeedTest()) {
                is AssiaNetworkResponse.Success -> {
                    if (speedTestRequest.body.code == 1000) {
                        checkSpeedTestStatus(requestId = speedTestRequest.body.speedTestId)
                    } else {
                        displayEmptyResponse()
                    }
                }
                else -> {
                    displayEmptyResponse()
                }
            }
        }
    }

    private fun checkSpeedTestStatus(requestId: Int) {
        viewModelScope.launch {
            var keepChecking = true
            var isSuccessful = false
            while (keepChecking) {

                when (val status = assiaRepository.checkSpeedTestStatus(speedTestId = requestId)) {
                    is AssiaNetworkResponse.Success -> {
                        if (status.body.code == 1000) {
                            if (status.body.data.isFinished) {
                                isSuccessful = true
                                keepChecking = false
                            } else {
                                delay(SPEED_TEST_REFRESH_INTERVAL)
                            }
                        } else {
                            displayEmptyResponse()
                            keepChecking = false
                        }
                    }
                    else -> {
                        displayEmptyResponse()
                        keepChecking = false
                    }
                }
            }
            if (isSuccessful) getResults()
        }
    }

    private suspend fun getResults() {
        when (val upstreamData = assiaRepository.getUpstreamResults()) {
            is AssiaNetworkResponse.Success -> {
                if (upstreamData.body.data.listOfData.isNotEmpty()) {
                    val uploadMb = upstreamData.body.data.listOfData[0].speedAvg / 1000
                    uploadSpeed.latestValue = uploadMb.toString()
                    sharedPreferences.saveSpeedTestUpload(uploadSpeed = uploadSpeed.latestValue)
                } else {
                    uploadSpeed.latestValue = EMPTY_RESPONSE
                }
            }
            else -> {
                displayEmptyResponse()
            }
        }

        when (val downStreamData = assiaRepository.getDownstreamResults()) {
            is AssiaNetworkResponse.Success -> {
                if (downStreamData.body.data.listOfData.isNotEmpty()) {
                    val downloadMb = downStreamData.body.data.listOfData[0].speedAvg / 1000
                    downloadSpeed.latestValue = downloadMb.toString()
                    latestSpeedTest.latestValue =
                        formatUtcString(downStreamData.body.data.listOfData[0].timeStamp)
                    sharedPreferences.saveSpeedTestDownload(downloadSpeed = downloadSpeed.latestValue)
                    sharedPreferences.saveLastSpeedTestTime(lastRanTime = latestSpeedTest.latestValue)
                } else {
                    downloadSpeed.latestValue = EMPTY_RESPONSE
                    latestSpeedTest.latestValue = EMPTY_RESPONSE
                }
            }
            else -> {
                displayEmptyResponse()
            }
        }
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
            requestAppointmentDetails()
        }
    }

    private suspend fun requestAppointmentDetails() {
        val appointmentDetails = appointmentRepository.getAppointmentInfo()
        appointmentDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            cancelAppointmentInstance = mockInstanceforCancellation(it)
            updateAppointmentStatus(it)
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
            timeZone = it.timeZone
        )
    }

    private suspend fun requestNotificationDetails() {
        progressViewFlow.latestValue = true
        val notificationDetails = notificationRepository.getNotificationDetails()
        notificationDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            notificationListDetails.latestValue = it
            progressViewFlow.latestValue = false
        }
    }

    private suspend fun requestWifiDetails() {

        progressViewFlow.latestValue = true
        when (val modemResponse = assiaRepository.getModemInfo()) {
            is AssiaNetworkResponse.Success -> {
                val apiInfo = modemResponse.body.modemInfo.apInfoList
                if (apiInfo[0].ssidMap.containsKey(NetworkType.Band5G.name)) {
                    regularNetworkInfo = regularNetworkInstance.copy(
                        type = NetworkType.Band5G.name,
                        name = apiInfo[0].ssidMap.getValue(NetworkType.Band5G.name),
                        password = regularNetworkWifiPwd,
                        enabled = true
                    )
                }
                if (apiInfo[0].ssidMap.containsKey(NetworkType.Band2G.name)) {
                    guestNetworkInfo = guestNetworkInstance.copy(
                        NetworkType.Band2G.name,
                        name = apiInfo[0].ssidMap.getValue(NetworkType.Band2G.name),
                        password = guestNetworkWifiPwd,
                        enabled = true
                    )
                }
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
                progressViewFlow.latestValue = false
            }
            else -> {
                progressViewFlow.latestValue = false
                errorMessageFlow.latestValue = "Error WifiInfo"
            }
        }
    }

    private suspend fun requestToGetNetworkPassword(networkType: NetworkType) {
        when (val netWorkInfo = wifiNetworkManagementRepository.getNetworkPassword(networkType)) {
            is AssiaNetworkResponse.Success -> {
                val networkName = netWorkInfo.body.networkName
                if (networkName.containsKey(NetworkType.Band5G.name)) {
                    regularNetworkWifiPwd = networkName.getValue(NetworkType.Band5G.name)
                }
                if (networkName.containsKey(NetworkType.Band2G.name)) {
                    guestNetworkWifiPwd = networkName.getValue(NetworkType.Band5G.name)
                }

            }
            else -> {
                //TODO Currently API is returning Error -Temp Hack for displaying password
                regularNetworkWifiPwd = "test123wifi"
                guestNetworkWifiPwd = "test123Guest"

            }
        }
    }

    fun wifiNetworkEnablement(wifiInfo: WifiInfo) {
        viewModelScope.launch {
            if (wifiInfo.enabled!!) {
                progressViewFlow.latestValue = true
                requestToDisableNetwork(wifiInfo)
            } else {
                progressViewFlow.latestValue = true
                requestToEnableNetwork(wifiInfo)
            }
        }
    }

    private suspend fun requestToEnableNetwork(wifiInfo: WifiInfo) {
        networkType = if (wifiInfo.type?.equals(NetworkType.Band5G.name,ignoreCase = true)!!) {
            NetworkType.Band5G
        } else {
            NetworkType.Band2G
        }
        val netWorkInfo = wifiNetworkManagementRepository.enableNetwork(networkType)
        progressViewFlow.latestValue = false
        when (netWorkInfo) {
            is AssiaNetworkResponse.Success -> {
                updateEnableDisableNetwork(wifiInfo)
            }
            else -> {
                errorMessageFlow.latestValue = "Network Enablement Failed"
            }
        }
    }

    private suspend fun requestToDisableNetwork(wifiInfo: WifiInfo) {
        networkType = if (wifiInfo.type?.equals(NetworkType.Band5G.name,true)!!) {
            NetworkType.Band5G
        } else {
            NetworkType.Band2G
        }
        val netWorkInfo = wifiNetworkManagementRepository.disableNetwork(networkType)
        progressViewFlow.latestValue = false
        when (netWorkInfo) {
            is AssiaNetworkResponse.Success -> {
                updateEnableDisableNetwork(wifiInfo)
            }
            else -> {
                //TODO HANDLING ERROR MOCKED FOR NOW
                errorMessageFlow.latestValue = "Network disablement Failed"
            }
        }
    }


    private fun updateEnableDisableNetwork(wifiInfo: WifiInfo) {
        isEnable = !wifiInfo.enabled!!
        if (!wifiInfo.name.isNullOrEmpty() && wifiInfo.type.equals(NetworkType.Band5G.name)) {
            regularNetworkInfo = regularNetworkInstance.copy(
                type = NetworkType.Band5G.name,
                name = wifiInfo.name, password = regularNetworkWifiPwd, enabled = isEnable
            )
        }
        if (!wifiInfo.name.isNullOrEmpty() && wifiInfo.type.equals(NetworkType.Band2G.name)) {
            guestNetworkInfo = guestNetworkInstance.copy(
                NetworkType.Band2G.name,
                name = wifiInfo.name, password = guestNetworkWifiPwd, enabled = isEnable
            )
        }
        wifiListDetailsUpdated.latestValue = wifiScanStatus(
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


    private fun updateAppointmentStatus(
        it: AppointmentRecordsInfo
    ) {
        val timezone = it.timeZone
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
                        )
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

    fun getChangeAppointment() {
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
        val bundle = Bundle()
        bundle.putString(NetworkStatusActivity.NETWORK_NAME, networkName)
        DashboardCoordinatorDestinations.bundle = bundle
        myState.latestValue = DashboardCoordinatorDestinations.NETWORK_INFORMATION
    }

    fun navigateToQRScan(wifiInfo: WifiInfo) {
        val bundle = Bundle()
        bundle.putSerializable(QrScanActivity.WIFI_DETAILS, wifiInfo)
        DashboardCoordinatorDestinations.bundle = bundle
        myState.latestValue = DashboardCoordinatorDestinations.QR_CODE_SCANNING
    }

    fun getStartedClicked() {
        sharedPreferences.saveUserType(true)
    }

    companion object {
        const val EMPTY_RESPONSE = "- -"
        const val APPOINTMENT_DETAILS_REFRESH_INTERVAL = 30000L
    }

    fun requestAppointmentCancellation() {
        updateAppointmentStatus(cancelAppointmentInstance)
    }

    fun checkForOngoingSpeedTest() {
        speedTestButtonState.latestValue = !rebootOngoing
        val ongoingTest: Boolean = sharedPreferences.getSupportSpeedTest()
        if (ongoingTest) {
            sharedPreferences.saveSupportSpeedTest(boolean = false)
            val speedTestId = sharedPreferences.getSpeedTestId()
            if (speedTestId != null) {
                sharedPreferences.saveSpeedTestFlag(boolean = true)
                progressVisibility.latestValue = true
                latestSpeedTest.latestValue = EMPTY_RESPONSE
                checkSpeedTestStatus(requestId = speedTestId)
            }
        }
    }

    abstract class UiDashboardAppointmentInformation

    data class AppointmentScheduleState(
        val jobType: String, val status: ServiceStatus, val serviceAppointmentDate:
        String, val serviceAppointmentStartTime: String, val serviceAppointmentEndTime: String
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
}
