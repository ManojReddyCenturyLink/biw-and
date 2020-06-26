package com.centurylink.biwf.screens.home.dashboard.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.centurylink.biwf.model.TabsBaseItem
import com.centurylink.biwf.screens.home.account.AccountFragment
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.screens.home.devices.DevicesFragment

class HomeViewPagerAdapter(private val getStartedEventClickListener: DashboardFragment.GetStartedEventClickListener,
                           fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    var tabHeaderList = mutableListOf<TabsBaseItem>()

    private lateinit var dashBoardFragment: DashboardFragment

    private lateinit var accountFragment: AccountFragment

    private lateinit var devicesFragment: DevicesFragment

    override fun getItemCount(): Int {
        return tabHeaderList.size
    }

    fun setTabItem(list: MutableList<TabsBaseItem>) {
        tabHeaderList = list
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> setupAccountFragment()
            1 -> setupDashBoardFragment()
            2 -> setupDeviceFragment()
            else -> throw IllegalArgumentException("Undefined Pager Fragment")
        }
    }

    private fun setupDashBoardFragment(): DashboardFragment {
        val newUser = tabHeaderList[TabsBaseItem.DASHBOARD].bundle.getBoolean("NEW_USER", false)
        dashBoardFragment = DashboardFragment(newUser)
        dashBoardFragment.setListener(getStartedEventClickListener)
        return dashBoardFragment
    }

    private fun setupAccountFragment(): AccountFragment {
        accountFragment = AccountFragment()
        return accountFragment
    }

    private fun setupDeviceFragment(): DevicesFragment {
        devicesFragment = DevicesFragment()
        return devicesFragment
    }

}