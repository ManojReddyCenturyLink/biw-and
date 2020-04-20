package com.centurylink.biwf.screens.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
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
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, HomeActivity::class.java)
                .putExtra("EXISTING_USER", bundle.getBoolean("EXISTING_USER"))
        }
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
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)
        homeCoordinator.navigator.activity = this
        homeCoordinator.observeThis(viewModel.myState)
        initViews()
        initOnClicks()
    }

    /**
     * Comparing the number of entries currently in the back stack to handle Back Press
     */
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun initViews(){
        viewModel.handleTabBarVisibility(intent.getBooleanExtra("EXISTING_USER",false));
        viewModel.apply {
            activeUserTabBarVisibility.bindToVisibility(
                binding.homeUpperTabs,
                binding.homeLowerTabs,
                binding.homeOnlineStatusBar
            )
            networkStatus.observe { binding.homeOnlineStatusBar.setOnlineStatus(it) }
        }
        setupTabsViewPager(intent.getBooleanExtra("EXISTING_USER",false))
    }

    fun onProfileClickEvent(){
        viewModel.onProfileClickEvent()
    }

    private fun initOnClicks() {
        binding.homeOnlineStatusBar.setOnClickListener { viewModel.onOnlineToolbarClick() }
        binding.iBtnNotification.setOnClickListener { viewModel.onNotificationBellClicked() }
        binding.supportButton.setOnClickListener { viewModel.onSupportClicked() }
    }

    private fun setupTabsViewPager(isExistingUser : Boolean) {
        //For future reference to load data and display on screen
        viewModel.loadData()
        if(isExistingUser){
            adapter.submitList(viewModel.tabsHeaderList)
            binding.vpDashboard.adapter = adapter

            TabLayoutMediator(binding.homeLowerTabs, binding.vpDashboard,
                TabLayoutMediator.OnConfigureTabCallback
                { tab, position -> tab.setText(viewModel.tabsHeaderList[position].titleRes) }).attach()
        }
        else{
            adapter.submitList(viewModel.tabUpperHeaderList)
            binding.vpDashboard.adapter = adapter

            TabLayoutMediator(binding.homeUpperTabs, binding.vpDashboard,
                TabLayoutMediator.OnConfigureTabCallback
                { tab, position -> tab.setText(viewModel.tabUpperHeaderList[position].titleRes) }).attach()
        }
    }
}