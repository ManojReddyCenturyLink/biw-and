package com.centurylink.biwf.screens.home

import androidx.core.os.bundleOf
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.Either
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.model.TabsBaseItem
import com.centurylink.biwf.model.sumup.SumUpInput
import com.centurylink.biwf.repos.AppointmentRepository
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.service.network.TestRestServices
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import com.centurylink.biwf.widgets.OnlineStatusData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val testRestServices: TestRestServices,
    private val appointmentRepository: AppointmentRepository,
    private val sharedPreferences: Preferences,
    private val integrationServices: IntegrationRestServices,
    private val userRepository: UserRepository
) : BaseViewModel() {

    val networkStatus: BehaviorStateFlow<OnlineStatusData> = BehaviorStateFlow(OnlineStatusData())
    val myState = EventFlow<HomeCoordinatorDestinations>()
    val displayBioMetricPrompt = EventFlow<ChoiceDialogMessage>()
    val refreshBioMetrics = EventFlow<Unit>()
    var errorMessageFlow = EventFlow<String>()
    // Example: Expose data through Flow properties.
    // TODO Remove later when example is no longer needed.
    val testRestFlow: Flow<String> = BehaviorStateFlow()
    val testRestErrorFlow: Flow<String> = BehaviorStateFlow()
    val activeUserTabBarVisibility = BehaviorStateFlow<Boolean>()
    var upperTabHeaderList = mutableListOf<TabsBaseItem>()
    var lowerTabHeaderList = mutableListOf<TabsBaseItem>()
    val isExistingUser = BehaviorStateFlow<Boolean>()

    // dummy variable that helps toggle between online states. Will remove when implementing real online status
    var dummyOnline = false

    private val dialogMessage = ChoiceDialogMessage(
        title = R.string.welcome_to_dashboard,
        message = R.string.biometric_dialog_message,
        positiveText = R.string.ok,
        negativeText = R.string.dont_allow
    )

    init {
        upperTabHeaderList = initList(true)
        lowerTabHeaderList = initList(false)
        isExistingUser.value = sharedPreferences.getUserType() ?: false

        //requestTestRestFlow()
        if (!sharedPreferences.getHasSeenDialog()) {
            displayBioMetricPrompt.latestValue = dialogMessage
            sharedPreferences.saveHasSeenDialog()
        }
        initApis()
    }

    private fun initApis() {
        viewModelScope.launch {
            requestUserInfo()
            requestUserDetails()
            requestAppointmentDetails()
        }
    }

    fun handleTabBarVisibility(isExistingUser: Boolean) {
        //just a dummy function to test showing different toolbars
        activeUserTabBarVisibility.latestValue = isExistingUser
    }

    fun onSupportClicked() {
        myState.latestValue = HomeCoordinatorDestinations.SUPPORT
    }

    fun onNotificationBellClicked() {
        myState.latestValue = HomeCoordinatorDestinations.NOTIFICATION_LIST
    }

    fun onBiometricYesResponse() {
        sharedPreferences.apply {
            saveBioMetrics(value = true)
            saveHasSeenDialog()
        }
        refreshBioMetrics.latestValue = Unit
    }

    fun onOnlineToolbarClick() {
        // dummy function to show the different states of the user Online / Offline
        if (dummyOnline) {
            networkStatus.latestValue = OnlineStatusData()
        } else {
            val onlineStatusData = OnlineStatusData(isOnline = true, networkName = "Fake Network")
            networkStatus.latestValue = onlineStatusData
        }
        dummyOnline = !dummyOnline
    }

    fun onSubscriptionActivityClick() {
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

    private fun requestAppointmentDetails() {
        viewModelScope.launch {
            val appointmentDetails = appointmentRepository.getAppointmentInfo()
            appointmentDetails.fold(ifLeft = {
            }) {
                activeUserTabBarVisibility.latestValue =
                    (it.jobType == "Fiber Install - For Installations")
            }
        }
    }

    private suspend fun requestUserDetails() {
        val userDetails = userRepository.getUserDetails()
        userDetails.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {

        }
    }

    private suspend fun requestUserInfo() {
        val userInfo = userRepository.getUserInfo()
        userInfo.fold(ifLeft = {
            errorMessageFlow.latestValue = it
        }) {}
    }

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
}

data class ChoiceDialogMessage(
    val title: Int,
    val message: Int,
    val positiveText: Int,
    val negativeText: Int
)