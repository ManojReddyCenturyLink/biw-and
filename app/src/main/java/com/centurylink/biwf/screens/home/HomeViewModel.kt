package com.centurylink.biwf.screens.home

import android.util.Log
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.coordinators.HomeCoordinatorDestinations
import com.centurylink.biwf.model.TabsBaseItem
import com.centurylink.biwf.utility.ObservableData
import javax.inject.Inject

class HomeViewModel @Inject constructor(
) : BaseViewModel() {

    val myState = ObservableData(HomeCoordinatorDestinations.HOME)
    var tabsHeaderList = mutableListOf<TabsBaseItem>()

    init {
        tabsHeaderList = initList()
    }

    private fun initList(): MutableList<TabsBaseItem> {

        val list = mutableListOf<TabsBaseItem>()

        list.add(TabsBaseItem(indextype = TabsBaseItem.DEVICES, titleRes = R.string.tittle_text_devices))

        list.add(TabsBaseItem(indextype = TabsBaseItem.DASHBOARD, titleRes = R.string.tittle_text_dashboard))

        list.add(TabsBaseItem(indextype = TabsBaseItem.ACCOUNT, titleRes = R.string.tittle_text_account))

        return list
    }

    fun onSupportClicked() {
        myState.value = HomeCoordinatorDestinations.SUPPORT
    }

    fun onNotificonBellClicked() {
        Log.i("Pravin","onNotificonBellClicked");
        myState.value = HomeCoordinatorDestinations.NOTIFICATION_LIST
    }

    fun loadData() {
        loadAccountsData()
        loadDevicesData()
        loadDashboardData()
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

