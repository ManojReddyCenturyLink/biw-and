package com.centurylink.biwf.screens.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.HomeCoordinator
import com.centurylink.biwf.databinding.ActivityHomeBinding
import com.centurylink.biwf.screens.subscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.service.network.TestRestServices
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber
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

    @Inject
    lateinit var testService: TestRestServices

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

        testService.query("SELECT Name FROM Contact LIMIT 10")
            .map { it.toString() }
            .subscribe({ Timber.d(it) }, Timber::e)
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

    override fun onResume() {
        super.onResume()
        homeCoordinator.navigator.activity = this
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
        binding.vpDashboard.adapter = adapter
        if(isExistingUser){
            adapter.submitList(viewModel.lowerTabHeaderList)
            TabLayoutMediator(binding.homeLowerTabs, binding.vpDashboard,
                TabLayoutMediator.OnConfigureTabCallback
                { tab, position -> tab.setText(viewModel.lowerTabHeaderList[position].titleRes) }).attach()
        }
        else{
            adapter.submitList(viewModel.upperTabHeaderList)
            TabLayoutMediator(binding.homeUpperTabs, binding.vpDashboard,
                TabLayoutMediator.OnConfigureTabCallback
                { tab, position -> tab.setText(viewModel.upperTabHeaderList[position].titleRes) }).attach()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == CancelSubscriptionDetailsActivity.REQUEST_TO__ACCOUNT) {
            binding.vpDashboard.currentItem = 0
        }
    }
}