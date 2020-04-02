package com.centurylink.biwf.screens.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.coordinators.HomeCoordinator
import com.centurylink.biwf.databinding.ActivityHomeBinding
import com.centurylink.biwf.di.viewModelFactory.DaggerViewModelFactory
import com.google.android.material.tabs.TabLayoutMediator
import javax.inject.Inject

class HomeActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, HomeActivity::class.java)
    }

    @Inject
    lateinit var homeCoordinator: HomeCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(HomeViewModel::class.java)
    }
    private val adapter by lazy {
        TabsPagerRecyclerAdapter(this)
    }
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)
        viewModel.apply {
        }

        homeCoordinator.navigator.activity = this
        homeCoordinator.observeThis(viewModel.myState)

        initOnClicks()
    }

    private fun initOnClicks() {
        binding.supportButton.setOnClickListener { viewModel.onSupportClicked() }
        homeCoordinator.navigator.activity = this
        homeCoordinator.observeThis(viewModel.myState)
        setupTabsViewPager()
    }

    private fun setupTabsViewPager() {

        binding.vpDashboard.adapter = adapter
        //For future reference to load data and display on screen
        viewModel.loadData()
        adapter.submitList(viewModel.tabsHeaderList)
        TabLayoutMediator(binding.homeTabs, binding.vpDashboard,
            TabLayoutMediator.OnConfigureTabCallback
            { tab, position -> tab.setText(viewModel.tabsHeaderList[position].titleRes) }).attach()
    }
}