package com.centurylink.biwf.screens.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.HomeCoordinator
import com.centurylink.biwf.databinding.ActivityHomeBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class HomeActivity : BaseActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }

    @Inject
    lateinit var homeCoordinator: HomeCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val viewModel by lazy { ViewModelProvider(this, factory).get(HomeViewModel::class.java) }
    private val adapter by lazy { TabsPagerRecyclerAdapter(this) }
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
       // (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)

        viewModel.apply {
            activeUserTabBarVisibility.bindToVisibility(
                binding.homeUpperTabs,
                binding.homeLowerTabs,
                binding.homeOnlineStatusBar
            )
            networkStatus.observe { binding.homeOnlineStatusBar.setOnlineStatus(it) }
        }

        homeCoordinator.navigator.activity = this
        homeCoordinator.observeThis(viewModel.myState)

        initOnClicks()
    }

    private fun initOnClicks() {
        binding.homeOnlineStatusBar.setOnClickListener { viewModel.onOnlineToolbarClick() }
        binding.iBtnNotification.setOnClickListener { viewModel.onNotificonBellClicked() }
        binding.supportButton.setOnClickListener { viewModel.onSupportClicked() }
        binding.supportButton.setOnLongClickListener {
            viewModel.onSupportLongClick_toggleToolbars()
            true
        }
        setupTabsViewPager()
    }

    private fun setupTabsViewPager() {
        binding.vpDashboard.adapter = adapter
        //For future reference to load data and display on screen
        viewModel.loadData()
        adapter.submitList(viewModel.tabsHeaderList)
        TabLayoutMediator(binding.homeUpperTabs, binding.vpDashboard,
            TabLayoutMediator.OnConfigureTabCallback
            { tab, position -> tab.setText(viewModel.tabsHeaderList[position].titleRes) }).attach()

        TabLayoutMediator(binding.homeLowerTabs, binding.vpDashboard,
            TabLayoutMediator.OnConfigureTabCallback
            { tab, position -> tab.setText(viewModel.tabsHeaderList[position].titleRes) }).attach()
    }
}