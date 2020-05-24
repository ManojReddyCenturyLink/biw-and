package com.centurylink.biwf.screens.home

import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.centurylink.biwf.Either
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.model.TabsBaseItem
import com.centurylink.biwf.model.sumup.SumUpInput
import com.centurylink.biwf.repos.UserRepository
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.service.network.TestRestServices
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.preferences.Preferences
import com.centurylink.biwf.widgets.OnlineStatusData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val testRestServices: TestRestServices,
    private val userRepository: UserRepository,
    private val sharedPreferences: Preferences,
    private val integrationServices: IntegrationRestServices
) : BaseViewModel() {

    val activeUserTabBarVisibility: LiveData<Boolean> = MutableLiveData(false)
    val networkStatus: LiveData<OnlineStatusData> = MutableLiveData(OnlineStatusData())
    val myState = EventFlow<HomeCoordinatorDestinations>()
    var upperTabHeaderList = mutableListOf<TabsBaseItem>()
    var lowerTabHeaderList = mutableListOf<TabsBaseItem>()

    val displayBioMetricPrompt = EventFlow<List<Int>>()
    val refreshBioMetrics = EventFlow<Unit>()

    // Example: Expose data through Flow properties.
    // TODO Remove later when example is no longer needed.
    val testRestFlow: Flow<String> = BehaviorStateFlow()
    val testRestErrorFlow: Flow<String> = BehaviorStateFlow()

    // dummy variable that helps toggle between online states. Will remove when implementing real online status
    var dummyOnline = false

    private val list = listOf(
        R.string.welcome_to_dashboard,
        R.string.biometric_dialog_message,
        R.string.ok,
        R.string.dont_allow
    )

    init {
        upperTabHeaderList = initList(true)
        lowerTabHeaderList = initList(false)
        if (!sharedPreferences.getHasSeenDialog()) {
            displayBioMetricPrompt.latestValue = list
            sharedPreferences.saveHasSeenDialog()
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

    fun onNotificationClicked() {
        myState.latestValue = HomeCoordinatorDestinations.NOTIFICATION_DETAILS
    }

    fun loadData() {
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
            Timber.d("IntegrationService test: sumUp returned $sumUpResult")

            testRestServices.query("SELECT Name FROM Contact LIMIT 10").also {
                when (it) {
                    is Either.Left -> testRestErrorFlow.latestValue = "Encountered error ${it.error}"
                    is Either.Right -> testRestFlow.latestValue = it.value.toString()
                }
            }
        }
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
