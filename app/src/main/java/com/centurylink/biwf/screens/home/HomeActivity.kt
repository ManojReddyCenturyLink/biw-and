package com.centurylink.biwf.screens.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.biometric.BiometricManager
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.HomeCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityHomeBinding
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.home.account.AccountFragment
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.widgets.ChoiceDialogFragment
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber
import javax.inject.Inject

class HomeActivity : BaseActivity(), DashboardFragment.GetStartedEventClickListener,
    ChoiceDialogFragment.BioMetricDialogCallback {

    @Inject
    lateinit var homeCoordinator: HomeCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    @Inject
    lateinit var navigator: Navigator

    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(HomeViewModel::class.java)
    }
    private val adapter by lazy { TabsPagerRecyclerAdapter(this, this) }
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        viewModel.myState.observeWith(homeCoordinator)

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
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT) {
            binding.vpDashboard.currentItem = 0
        }
    }

    override fun onOkBiometricResponse() {
        viewModel.onBiometricYesResponse()
    }

    override fun onGetStartedClick(isJobTypeInstallation: Boolean) {
        setupTabsViewPager(isJobTypeInstallation, true)
    }

    fun launchSubscriptionActivity() {
        viewModel.onSubscriptionActivityClick()
    }

    private fun initViews() {
        viewModel.activeUserTabBarVisibility.observe {
            setupTabsViewPager(it, viewModel.isExistingUser.value)
        }
        viewModel.networkStatus.observe { binding.homeOnlineStatusBar.setOnlineStatus(it) }
    }

    private fun initOnClicks() {
        binding.homeOnlineStatusBar.setOnClickListener { viewModel.onOnlineToolbarClick() }
        binding.iBtnNotificationTop.setOnClickListener { viewModel.onNotificationBellClicked() }
        binding.iBtnNotificationBottom.setOnClickListener { viewModel.onNotificationBellClicked() }
        binding.supportButton.setOnClickListener { viewModel.onSupportClicked() }
    }

    //isJobTypeInstallation will be used while implementing Service type installation status
    private fun setupTabsViewPager(isJobTypeInstallation: Boolean, isExistingUser: Boolean) {
        binding.iBtnNotificationBottom.visibility = if (isExistingUser) View.GONE else View.VISIBLE
        binding.iBtnNotificationTop.visibility = if (isExistingUser) View.VISIBLE else View.GONE
        binding.homeOnlineStatusBar.visibility = if (isExistingUser) View.VISIBLE else View.GONE

        binding.vpDashboard.adapter = adapter
        if(isExistingUser){
            adapter.submitList(viewModel.lowerTabHeaderList)
        }else{
            adapter.submitList(viewModel.upperTabHeaderList)
        }
        TabLayoutMediator(binding.homeUpperTabs, binding.vpDashboard,
            TabLayoutMediator.OnConfigureTabCallback
            { tab, position -> tab.setText(viewModel.lowerTabHeaderList[position].titleRes) }).attach()
        binding.vpDashboard.currentItem = 0
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
        val accountFrag =
            supportFragmentManager.findFragmentById(R.id.account_container) as AccountFragment?
        accountFrag?.refreshBioMetrics()
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }
}