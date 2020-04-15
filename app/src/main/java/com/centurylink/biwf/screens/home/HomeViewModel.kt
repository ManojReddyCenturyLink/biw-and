package com.centurylink.biwf.screens.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.model.TabsBaseItem
import com.centurylink.biwf.utility.ObservableData
import com.centurylink.biwf.widgets.OnlineStatusData
import javax.inject.Inject

class HomeViewModel @Inject constructor(
) : BaseViewModel() {

    val activeUserTabBarVisibility: LiveData<Boolean> = MutableLiveData(false)
    val networkStatus: LiveData<OnlineStatusData> = MutableLiveData(OnlineStatusData())
    val myState = ObservableData(HomeCoordinatorDestinations.HOME)
    var tabsHeaderList = mutableListOf<TabsBaseItem>()

    // dummy variable that helps toggle between online states. Will remove when implementing real online status
    var dummyOnline = false

    init {
        tabsHeaderList = initList()
    }

    fun onSupportClicked() {
        myState.value = HomeCoordinatorDestinations.SUPPORT
    }

    fun onNotificonBellClicked() {
        myState.value = HomeCoordinatorDestinations.NOTIFICATION_LIST
    }

	fun onNotificationClicked(){
        myState.value = HomeCoordinatorDestinations.NOTIFICATION_DETAILS
    }
    fun loadData() {
        loadAccountsData()
        loadDevicesData()
        loadDashboardData()
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

    fun onSupportLongClick_toggleToolbars() {
        //just a dummy function to test showing different toolbars
        activeUserTabBarVisibility.latestValue = !(activeUserTabBarVisibility.value)!!
    }

    fun onProfileClickEvent(){
        myState.value = HomeCoordinatorDestinations.PROFILE
    }

    private fun initList(): MutableList<TabsBaseItem> {
        val list = mutableListOf<TabsBaseItem>()

        list.add(TabsBaseItem(indextype = TabsBaseItem.ACCOUNT, titleRes = R.string.tittle_text_account))
        list.add(TabsBaseItem(indextype = TabsBaseItem.DASHBOARD, titleRes = R.string.tittle_text_dashboard))
        list.add(TabsBaseItem(indextype = TabsBaseItem.DEVICES, titleRes = R.string.tittle_text_devices))

        return list
    }

    private fun loadDashboardData() {
        //Load data here
    }

    private fun loadDevicesData() {
        //Load data here
    }

    private fun loadAccountsData() {
        //Load data here
    }
}

