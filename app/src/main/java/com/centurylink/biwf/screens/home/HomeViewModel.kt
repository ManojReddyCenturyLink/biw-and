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
import com.centurylink.biwf.service.network.IntegrationRestServices
import com.centurylink.biwf.service.network.TestRestServices
import com.centurylink.biwf.utility.BehaviorStateFlow
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.widgets.OnlineStatusData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val testRestServices: TestRestServices,
    private val appointmentRepository: AppointmentRepository,
    private val integrationServices: IntegrationRestServices
) : BaseViewModel() {

    val networkStatus: BehaviorStateFlow<OnlineStatusData> = BehaviorStateFlow(OnlineStatusData())
    val myState = EventFlow<HomeCoordinatorDestinations>()
    // Example: Expose data through Flow properties.
    // TODO Remove later when example is no longer needed.
    val testRestFlow: Flow<String> = BehaviorStateFlow()
    val testRestErrorFlow: Flow<String> = BehaviorStateFlow()
    val activeUserTabBarVisibility = BehaviorStateFlow<Boolean>()
    var upperTabHeaderList = mutableListOf<TabsBaseItem>()
    var lowerTabHeaderList = mutableListOf<TabsBaseItem>()
    // dummy variable that helps toggle between online states. Will remove when implementing real online status
    var dummyOnline = false

    init {
        upperTabHeaderList = initList(true)
        lowerTabHeaderList = initList(false)
        requestAppointmentDetails()
        //requestTestRestFlow()
    }

    fun onSupportClicked() {
        myState.latestValue = HomeCoordinatorDestinations.SUPPORT
    }

    fun onNotificationBellClicked() {
        myState.latestValue = HomeCoordinatorDestinations.NOTIFICATION_LIST
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
                    (it.jobType.equals("Fiber Install - For Installations"))
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
