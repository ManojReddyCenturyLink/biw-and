package com.centurylink.biwf.screens.home

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.biometric.BiometricManager
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.HomeCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityHomeBinding
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.deviceusagedetails.UsageDetailsActivity
import com.centurylink.biwf.screens.home.account.AccountFragment
import com.centurylink.biwf.screens.home.account.PersonalInfoActivity
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.screens.home.dashboard.adapter.HomeViewPagerAdapter
import com.centurylink.biwf.screens.home.devices.DevicesFragment
import com.centurylink.biwf.screens.networkstatus.NetworkStatusActivity
import com.centurylink.biwf.screens.subscription.EditPaymentDetailsActivity
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.PendoUtil
import com.centurylink.biwf.widgets.ChoiceDialogFragment
import com.google.android.material.tabs.TabLayout
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
    private val viewPagerAdapter by lazy { HomeViewPagerAdapter(this, this) }

    private lateinit var binding: ActivityHomeBinding

    private lateinit var onTabSelectedListener: TabLayout.OnTabSelectedListener

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
        onTabSelectedListener = object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.vpDashboard.currentItem = tab.position
                val text = tab.customView as TextView?
                if (text != null) {
                    text.typeface = Typeface.DEFAULT_BOLD
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val text = tab.customView as TextView?
                if (text != null) {
                    text.typeface = Typeface.DEFAULT
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        }
        initViews()
        initOnClicks()

        // Example: Listen to data emitted from Flow properties.
        // TODO Remove this example when we get some actual code here using this setup.
        viewModel.apply {
            testRestFlow.observe { Timber.d(it) }
            testRestErrorFlow.observe { Timber.e(it) }
            displayBioMetricPrompt.observe { biometricCheck(it) }
            refreshBioMetrics.observe { refreshAccountFragment() }
            accountDetailsInfo.observe {
                PendoUtil.initPendoSDKWithVisitor(this@HomeActivity, visitorId = it.emailAddress ?: "")
            }
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
            refreshPaymentInfoOnAccounts()
        } else if (resultCode == DashboardFragment.REFRESH_APPOINTMENT) {
            binding.vpDashboard.currentItem = 1
            refreshAppointmentsInDashBoardFragment()
        } else if (resultCode == UsageDetailsActivity.REQUEST_TO_DEVICES) {
            binding.vpDashboard.currentItem = 2
            refreshDevices()
        } else if (resultCode == NetworkStatusActivity.REQUEST_TO_HOME) {
            refreshDashboardFragment()
        } else if (resultCode == PersonalInfoActivity.REQUEST_TO_ACCOUNT_FROM_PERSONAL_INFO) {
            val phoneNumber = data?.getStringExtra(PersonalInfoActivity.PHONE_NUMBER)
            if (phoneNumber != null) {
                refreshPersonalInfo(phoneNumber)
            }
        } else if (resultCode == EditPaymentDetailsActivity.REQUEST_TO_REFRESH_PAYMENT_MOVE_TO_ACCOUNTS ||
            resultCode == EditPaymentDetailsActivity.REQUEST_TO_REFRESH_PAYMENT_TO_SUBSCRIPTION
        ) {
            refreshPaymentInfoOnAccounts()
        }
    }

    private fun refreshPersonalInfo(phoneNumber: String) {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is AccountFragment) {
                fragment.updateViews(phoneNumber)
            }
        }
    }

    private fun refreshDashboardFragment() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is DashboardFragment) {
                fragment.updateView()
            }
        }
    }

    override fun onOkBiometricResponse() {
        viewModel.onBiometricYesResponse()
    }

    override fun onCancelBiometricResponse() {
        viewModel.onBiometricNoResponse()
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
            setSupportButtonOnClick(it)
        }
        viewModel.networkStatus.observe { binding.homeOnlineStatusBar.setOnlineStatus(it) }
    }

    private fun setSupportButtonOnClick(isExistingUser: Boolean) {
        binding.supportButton.setOnClickListener { viewModel.onSupportClicked(isExistingUser) }
    }

    private fun initOnClicks() {
        // TODO right now this feature is not in active so commenting for now
        //  binding.iBtnNotificationTop.setOnClickListener { viewModel.onNotificationBellClicked() }
        //  binding.iBtnNotificationBottom.setOnClickListener { viewModel.onNotificationBellClicked() }
    }

    //isJobTypeInstallation will be used while implementing Service type installation status
    private fun setupTabsViewPager(isExistingUser: Boolean) {
        binding.iBtnNotificationBottom.visibility =
            if (isExistingUser) View.GONE else View.INVISIBLE
        binding.iBtnNotificationTop.visibility = if (isExistingUser) View.INVISIBLE else View.GONE
        binding.homeOnlineStatusBar.visibility = if (isExistingUser) View.VISIBLE else View.GONE

        binding.vpDashboard.adapter = viewPagerAdapter
        if (isExistingUser) {
            viewPagerAdapter.setTabItem(viewModel.lowerTabHeaderList)
        } else {
            viewPagerAdapter.setTabItem(viewModel.upperTabHeaderList)
        }
        TabLayoutMediator(binding.homeUpperTabs, binding.vpDashboard) { tab, position ->
            val tabTextView = TextView(this)
            tab.text = getString(viewModel.lowerTabHeaderList[position].titleRes)
            tab.customView = tabTextView
            tabTextView.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            tabTextView.text = tab.text
            tabTextView.textSize = resources.getDimension(R.dimen.text_size_6)
            tabTextView.setTextColor(getColor(R.color.white))
            tabTextView.typeface = ResourcesCompat.getFont(applicationContext, R.font.arial_mt)
            binding.vpDashboard.setCurrentItem(tab.position, true)
        }.attach()
        binding.homeUpperTabs.addOnTabSelectedListener(onTabSelectedListener)
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

    private fun refreshPaymentInfoOnAccounts() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is AccountFragment) {
                fragment.retryClicked()
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