package com.centurylink.biwf

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.ui.viewmodel.factory.DaggerViewModelFactory
import com.centurylink.biwf.databinding.MainActivityBinding
import com.centurylink.biwf.ui.activity.BaseActivity
import com.centurylink.biwf.ui.adapter.TabsPagerRecyclerAdapter
import com.centurylink.biwf.ui.viewmodel.MainViewModel
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class MainActivity : BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(MainViewModel::class.java)
    }
    private val adapter by lazy {
        TabsPagerRecyclerAdapter(this)
    }

    private val binding by lazy {
        DataBindingUtil.setContentView<MainActivityBinding>(
            this,
            R.layout.main_activity
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupTabsViewPager()

    }

    private fun setupTabsViewPager() {

        binding.vpDashboard.adapter =adapter

/*       For future reference to load data and display on screen
        viewModel.loadAccountsData()
        viewModel.loadDevicesData()
        viewModel.loadDashboardData()*/

        adapter.submitList(viewModel.transportModulesList)

        TabLayoutMediator(binding.homeTabs, binding.vpDashboard,
            TabLayoutMediator.OnConfigureTabCallback
            { tab, position -> tab.setText(viewModel.transportModulesList[position].titleRes) }).attach()


    }
}
