package com.centurylink.biwf.screens.home

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.Either
import com.centurylink.biwf.R
import com.centurylink.biwf.analytics.AnalyticsKeys
import com.centurylink.biwf.analytics.AnalyticsManager
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.model.TabsBaseItem
import com.centurylink.biwf.model.account.AccountDetails
import com.centurylink.biwf.model.sumup.SumUpInput
import com.centurylink.biwf.repos.*
import com.centurylink.biwf.screens.subscription.SubscriptionActivity
import com.centurylink.biwf.screens.support.SupportActivity
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.service.network.TestRestServices
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home view model
 *
 * @property testRestServices - service instance to handle testRest api calls
 * @property appointmentRepository - repository instance to handle appointment api calls
 * @property sharedPreferences - preference instance to handle  sharedPreferences
 * @property integrationServices - service instance to handle integration api calls
 * @property userRepository - repository instance to handle user api calls
 * @property assiaRepository - repository instance to handle assia api calls
 * @property oAuthAssiaRepository - repository instance to handle oAuth assia api calls
 * @property accountRepository - repository instance to handle account api calls
 * @constructor
 *
 * @param modemRebootMonitorService - service instance to handle  modem reboot functionality
 * @param analyticsManagerInterface - analytics instance to handle analytics events
 */
class HomeViewModel @Inject constructor(
    private val testRestServices: TestRestServices,
    private val appointmentRepository: AppointmentRepository,
    private val sharedPreferences: Preferences,
    private val integrationServices: IntegrationRestServices,
    private val userRepository: UserRepository,
    private val assiaRepository: AssiaRepository,
    private val oAuthAssiaRepository: OAuthAssiaRepository,
    private val accountRepository: AccountRepository,
    private val modemIdRepository: ModemIdRepository,
    modemRebootMonitorService: ModemRebootMonitorService,
    analyticsManagerInterface: AnalyticsManager
) : BaseViewModel(modemRebootMonitorService, analyticsManagerInterface) {

    val networkStatus: BehaviorStateFlow<Boolean> = BehaviorStateFlow()
    val myState = EventFlow<HomeCoordinatorDestinations>()
    val displayBioMetricPrompt = EventFlow<ChoiceDialogMessage>()
    val refreshBioMetrics = EventFlow<Unit>()
    var errorMessageFlow = EventFlow<String>()

    // Example: Expose data through Flow properties.
    // TODO Remove later when example is no longer needed.
    val testRestFlow: Flow<String> = BehaviorStateFlow()
    val testRestErrorFlow: Flow<String> = BehaviorStateFlow()
    val activeUserTabBarVisibility = BehaviorStateFlow<Boolean>()
    val isExistingUser = BehaviorStateFlow<Boolean>()
    var upperTabHeaderList = mutableListOf<TabsBaseItem>()
    var lowerTabHeaderList = mutableListOf<TabsBaseItem>()
    var progressViewFlow = EventFlow<Boolean>()
    val accountDetailsInfo: Flow<AccountDetails> = BehaviorStateFlow()

    private val dialogMessage = ChoiceDialogMessage(
        title = R.string.welcome_to_dashboard,
        message = R.string.biometric_dialog_message,
        positiveText = R.string.ok,
        negativeText = R.string.dont_allow
    )

    /**
     * This block is executed first, when the class is instantiated.
     */
    init {
        upperTabHeaderList = initList(true)
        lowerTabHeaderList = initList(false)
        isExistingUser.value = sharedPreferences.getUserType() ?: false
        if (!sharedPreferences.getHasSeenDialog()) {
            displayBioMetricPrompt.latestValue = dialogMessage
            sharedPreferences.saveHasSeenDialog()
        }
        initApis()
    }

    /**
     * Init Apis - It will start all the api calls initialisation
     *
     */
    fun initApis() {
        viewModelScope.launch {
            progressViewFlow.latestValue = true
            requestUserInfo()
            requestUserDetails()
            requestAccountDetails()
            requestModemId()
        }
    }

    /**
     * On support clicked - It will handle support button click logic
     *
     * @param isExistingUser - The boolean value to set existing user
     * Its true for existing user and false for non existing user
     */
    fun onSupportClicked(isExistingUser: Boolean) {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.BUTTON_SUPPORT_HOME_SCREEN)
        val bundle = Bundle()
        bundle.putBoolean(SupportActivity.IS_EXISTING_USER, isExistingUser)
        HomeCoordinatorDestinations.bundle = bundle
        myState.latestValue = HomeCoordinatorDestinations.SUPPORT
    }

    /**
     * On notification bell clicked - It will handle notification bell click logic
     *
     */
    fun onNotificationBellClicked() {
        myState.latestValue = HomeCoordinatorDestinations.NOTIFICATION_LIST
    }

    /**
     * On biometric yes response - This method is used to handle biometric positive response
     *
     */
    fun onBiometricYesResponse() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_ENABLE_BIOMETRICS_OK)
        sharedPreferences.apply {
            saveBioMetrics(value = true)
            saveHasSeenDialog()
        }
        refreshBioMetrics.latestValue = Unit
    }

    /**
     * On biometric yes response - This method is used to handle biometric negative response
     *
     */
    fun onBiometricNoResponse() {
        analyticsManagerInterface.logButtonClickEvent(AnalyticsKeys.ALERT_ENABLE_BIOMETRICS_DONT_ALLOW)
    }

    /**
     * On subscription activity click
     *
     * @param paymentMethod - The selected payment method
     */
    fun onSubscriptionActivityClick(paymentMethod: String) {
        HomeCoordinatorDestinations.bundle = Bundle().apply {
            putString(SubscriptionActivity.PAYMENT_CARD, paymentMethod)
        }
        myState.latestValue = HomeCoordinatorDestinations.SUBSCRIPTION_ACTIVITY
    }

    // Example: Use Coroutines to get data asynchronously and emit the results through Flows
    // TODO Remove later when example is no longer needed.
    private fun requestTestRestFlow() {
        viewModelScope.launch {
            val sumUpResult = integrationServices.calculateSum(12, 25, SumUpInput(10))
            val response = integrationServices.getNotificationDetails("notifications")
            testRestServices.query("SELECT Name FROM Contact LIMIT 10").also {
                when (it) {
                    is Either.Left -> testRestErrorFlow.latestValue =
                        "Encountered error ${it.error}"
                    is Either.Right -> testRestFlow.latestValue = it.value.toString()
                }
            }
        }
    }

    /**
     * Request modem id -It will  get the modem id from api
     *
     */
    private suspend fun requestModemId() {
        val modemIdInfo = modemIdRepository.getModemTypeId()
        modemIdInfo.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            sharedPreferences.saveAssiaId(it)
        }
    }

    private suspend fun requestUserDetails() {
        val userDetails = userRepository.getUserDetails()
        userDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {

        }
    }

    /**
     * Request user info - It is used to request user info through API call
     *
     */
    private suspend fun requestUserInfo() {
        val userInfo = userRepository.getUserInfo()
        userInfo.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {}
    }

    /**
     * Request account details - It is used to request account details through API call
     *
     */
    private suspend fun requestAccountDetails() {
        val accountDetails = accountRepository.getAccountDetails()
        accountDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {
            accountDetailsInfo.latestValue = it
            if (it.accountStatus.equals(pendingActivation, true) ||
                it.accountStatus.equals(abandonedActivation, true)
            ) {
                if (sharedPreferences.getInstallationStatus()) {
                    invokeStandardUserDashboard()
                } else {
                    requestAppointmentDetails()
                }
            } else {
                invokeStandardUserDashboard()
                progressViewFlow.latestValue = false
            }
        }
    }

    /**
     * Request appointment details - It is used to request appointment details through API call
     *
     */
    private suspend fun requestAppointmentDetails() {
        val appointmentDetails = appointmentRepository.getAppointmentInfo()
        appointmentDetails.fold(ifLeft = {
            if (it.equals("No Appointment Records", ignoreCase = true)) {
                invokeStandardUserDashboard()
            } else {
                errorMessageFlow.latestValue = it
            }
        }) {
            if (!it.jobType.contains(intsall)) {
                invokeStandardUserDashboard()
            } else {
                invokeNewUserDashboard()
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

    /**
     * Modem status refresh - It is used to refresh modem status depending on interval
     *
     */
    private fun modemStatusRefresh() {
        viewModelScope.launch {
            requestModemId()
        }
        viewModelScope.interval(0, MODEM_STATUS_REFRESH_INTERVAL) {
            requestModemInfo()
        }
    }

    /**
     * Init list - It is used to initialize header tabs in home screen
     *
     * @param isUpperTab - The boolean value to set header tabs
     * @return - returns list of header tabs
     */
    private fun initList(isUpperTab: Boolean): MutableList<TabsBaseItem> {
        val list = mutableListOf<TabsBaseItem>()

        list.add(
            TabsBaseItem(
                indextype = TabsBaseItem.ACCOUNT,
                titleRes = R.string.tittle_text_account
            )
        )
        list.add(
            TabsBaseItem(
                indextype = TabsBaseItem.DASHBOARD,
                titleRes = R.string.tittle_text_dashboard,
                bundle = bundleOf("NEW_USER" to isUpperTab)
            )
        )
        if (!isUpperTab)
            list.add(
                TabsBaseItem(
                    indextype = TabsBaseItem.DEVICES,
                    titleRes = R.string.tittle_text_devices
                )
            )
        return list
    }

    /**
     * Invoke new user dashboard - This is used to show two tabs in home screen
     *
     */
    // show 2 tabs
    private fun invokeNewUserDashboard() {
        activeUserTabBarVisibility.latestValue = false
        progressViewFlow.latestValue = false
    }

    /**
     * Invoke standard user dashboard - This is used to show 3 tabs in home screen
     *
     */
    // show 3 tabs
    private fun invokeStandardUserDashboard() {
        activeUserTabBarVisibility.latestValue = true
        progressViewFlow.latestValue = false
        modemStatusRefresh()
    }

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        const val pendingActivation = "Pending Activation"
        const val abandonedActivation = "Abandoned Activation"
        const val intsall = "Install"
    }
}


data class ChoiceDialogMessage(
    val title: Int,
    val message: Int,
    val positiveText: Int,
    val negativeText: Int
)