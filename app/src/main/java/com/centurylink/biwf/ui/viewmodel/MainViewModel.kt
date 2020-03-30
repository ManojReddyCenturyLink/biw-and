package com.centurylink.biwf.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.centurylink.biwf.R
import com.centurylink.biwf.ui.model.TabsBaseItem
import com.centurylink.biwf.ui.model.TabsBaseItem.Companion.ACCOUNT
import com.centurylink.biwf.ui.model.TabsBaseItem.Companion.DASHBOARD
import com.centurylink.biwf.ui.model.TabsBaseItem.Companion.DEVICES
import javax.inject.Inject

class MainViewModel @Inject constructor(): ViewModel() {

    var transportModulesList = mutableListOf<TabsBaseItem>()

    init {
        transportModulesList = initList()
    }

    private fun initList(): MutableList<TabsBaseItem> {

        val list = mutableListOf<TabsBaseItem>()

        list.add(TabsBaseItem(indextype = DEVICES, titleRes = R.string.tittle_text_devices))

        list.add(TabsBaseItem(indextype = DASHBOARD, titleRes = R.string.tittle_text_dashboard))

        list.add(TabsBaseItem(indextype = ACCOUNT, titleRes = R.string.tittle_text_account))

        return list

    }

}
