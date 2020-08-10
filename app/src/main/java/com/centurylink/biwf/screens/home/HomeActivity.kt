package com.centurylink.biwf.screens.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.biometric.BiometricManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.HomeCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityHomeBinding
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity
import com.centurylink.biwf.screens.home.account.AccountFragment
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.screens.home.dashboard.adapter.HomeViewPagerAdapter
import com.centurylink.biwf.screens.home.devices.DevicesFragment
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.widgets.ChoiceDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber
import javax.inject.Inject

class HomeActivity : BaseActivity(), DashboardFragment.ViewClickListener,
    ChoiceDialogFragment.BioMetricDialogCallback {

    @Inject
    lateinit var homeCoordinator: HomeCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(HomeViewModel::class.java)
    }
    private val adapter by lazy { TabsPagerRecyclerAdapter(this, this) }
    private val viewPagerAdapter by lazy { HomeViewPagerAdapter(this, this) }

    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        viewModel.myState.observeWith(homeCoordinator)
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.main,
            binding.retryOverlay.root
        )
        initViews()
        initOnClicks()

        // Example: Listen to data emitted from Flow properties.
        // TODO Remove this example when we get some actual code here using this setup.
        viewModel.apply {
            testRestFlow.observe { Timber.d(it) }
            testRestErrorFlow.observe { Timber.e(it) }
            displayBioMetricPrompt.observe { biometricCheck(it) }
            refreshBioMetrics.observe { refreshAccountFragment() }
        }
    }

    /**
     * Comparing the number of entries currently in the back stack to handle Back Press
     */
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finishAffinity()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT) {
            binding.vpDashboard.currentItem = 0
        } else if (resultCode == DashboardFragment.REFRESH_APPOINTMENT) {
            binding.vpDashboard.currentItem = 1
            refreshAppointmentsInDashBoardFragment()
        } else if(resultCode == UsageDetailsActivity.REQUEST_TO_DEVICES){
            binding.vpDashboard.currentItem = 2
            refreshDevices()
        }
    }

    override fun onOkBiometricResponse() {
        viewModel.onBiometricYesResponse()
    }

    override fun onGetStartedClick(isJobTypeInstallation: Boolean) {
        setupTabsViewPager(true)
    }

    override fun onViewDevicesClick() {
        binding.vpDashboard.currentItem = 2
    }

    override fun retryClicked() {
        showProgress(true)
        viewModel.initApis()
    }

    fun launchSubscriptionActivity(paymentMethod: String) {
        viewModel.onSubscriptionActivityClick(paymentMethod)
    }

    private fun initViews() {
        viewModel.progressViewFlow.observe {
            showProgress(it)
        }
        viewModel.errorMessageFlow.observe {
            showRetry(it.isNotEmpty())
        }
        viewModel.activeUserTabBarVisibility.observe {
            setupTabsViewPager(it)
        }
        viewModel.networkStatus.observe { binding.homeOnlineStatusBar.setOnlineStatus(it) }
    }

    private fun initOnClicks() {
        binding.iBtnNotificationTop.setOnClickListener { viewModel.onNotificationBellClicked() }
        // TODO right now this feature is not in active so commenting for now
      //  binding.iBtnNotificationBottom.setOnClickListener { viewModel.onNotificationBellClicked() }
        binding.supportButton.setOnClickListener { viewModel.onSupportClicked() }
    }

    //isJobTypeInstallation will be used while implementing Service type installation status
    private fun setupTabsViewPager(isExistingUser: Boolean) {
        // TODO right now this feature is not in active so commenting for now
      // binding.iBtnNotificationBottom.visibility = if (isExistingUser) View.GONE else View.VISIBLE
        binding.iBtnNotificationTop.visibility = if (isExistingUser) View.VISIBLE else View.GONE
        binding.homeOnlineStatusBar.visibility = if (isExistingUser) View.VISIBLE else View.GONE

        binding.vpDashboard.adapter = viewPagerAdapter
        if (isExistingUser) {
            viewPagerAdapter.setTabItem(viewModel.lowerTabHeaderList)
        } else {
            viewPagerAdapter.setTabItem(viewModel.upperTabHeaderList)
        }
        TabLayoutMediator(binding.homeUpperTabs, binding.vpDashboard) { tab, position ->
            tab.text = getString(viewModel.lowerTabHeaderList[position].titleRes)
            binding.vpDashboard.setCurrentItem(tab.position, true)
        }.attach()
        binding.vpDashboard.setCurrentItem(1, false)
    }

    private fun biometricCheck(list: ChoiceDialogMessage) {
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                openBioMetricDialog(list)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
            }
        }
    }

    private fun openBioMetricDialog(dialogMessage: ChoiceDialogMessage) {
        ChoiceDialogFragment(
            getString(dialogMessage.title),
            getString(dialogMessage.message),
            getString(dialogMessage.positiveText),
            getString(dialogMessage.negativeText)
        ).show(supportFragmentManager, null)
    }

    private fun refreshAccountFragment() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is AccountFragment) {
                fragment.refreshBioMetrics()
            }
        }
    }

    private fun refreshAppointmentsInDashBoardFragment() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is DashboardFragment) {
                fragment.retryClicked()
            }
        }
    }

    private fun refreshDevices() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is DevicesFragment) {
                fragment.retryClicked()
            }
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }
}