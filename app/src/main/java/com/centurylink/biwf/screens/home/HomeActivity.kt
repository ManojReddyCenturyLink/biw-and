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
import com.centurylink.biwf.utility.EventFlow
import com.centurylink.biwf.utility.PendoUtil
import com.centurylink.biwf.widgets.ChoiceDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import timber.log.Timber
import javax.inject.Inject

/**
 * Home activity - This class handles common methods related to Home screen
 *
 * @constructor Create empty Home activity
 */
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

    var isOnlineStatus = EventFlow<Boolean>()

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
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
                if (tab.position == 1) {
                    refreshDashboardFragment()
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

    /**
     * On activity result - Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned and any additional data from it.
     *
     * @param requestCode - It is originally supplied to startActivityForResult(), allowing
     * to identify result code came from.
     * @param resultCode - It is returned by the child activity through its setResult().
     * @param data - It will return result data to the caller activity.
     */
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

    /**
     * Refresh personal info
     *
     * @param phoneNumber - returns phone number
     */
    private fun refreshPersonalInfo(phoneNumber: String) {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is AccountFragment) {
                fragment.updateViews(phoneNumber)
            }
        }
    }

    /**
     * Refresh dashboard fragment
     *
     */
    private fun refreshDashboardFragment() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is DashboardFragment) {
                fragment.updateView()
            }
        }
    }

    /**
     * On ok biometric response  - This will handle biometric positive response
     *
     */
    override fun onOkBiometricResponse() {
        viewModel.onBiometricYesResponse()
    }

    /**
     * On cancel biometric response - This will handle biometric negative response
     *
     */
    override fun onCancelBiometricResponse() {
        viewModel.onBiometricNoResponse()
    }

    /**
     * On get started click - This will handle on get started button click listeners
     *
     * @param isJobTypeInstallation
     */
    override fun onGetStartedClick(isJobTypeInstallation: Boolean) {
        setupTabsViewPager(true)
    }

    /**
     * On view devices click - This will handle on view devices click listeners
     *
     */
    override fun onViewDevicesClick() {
        binding.vpDashboard.currentItem = 2
    }

    /**
     * Retry clicked - This will handle retry click listeners
     *
     */
    override fun retryClicked() {
        showProgress(true)
        viewModel.initApis()
    }

    /**
     * Launch subscription activity
     *
     * @param paymentMethod - returns payment method
     */
    fun launchSubscriptionActivity(paymentMethod: String) {
        viewModel.onSubscriptionActivityClick(paymentMethod)
    }

    /**
     * Init views - It will initialises the views
     *
     */
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
        viewModel.networkStatus.observe { binding.homeOnlineStatusBar.setOnlineStatus(it)
            isOnlineStatus.postValue(it)
        }
    }

    /**
     * Set support button on click
     *
     * @param isExistingUser - returns true value for existing user
     * returns false value for non existing user
     */
    private fun setSupportButtonOnClick(isExistingUser: Boolean) {
        binding.supportButton.setOnClickListener { viewModel.onSupportClicked(isExistingUser) }
    }

    /**
     * Init on clicks - It will initialize the onclick listeners
     *
     */
    private fun initOnClicks() {
        // TODO right now this feature is not in active so commenting for now
        //  binding.iBtnNotificationTop.setOnClickListener { viewModel.onNotificationBellClicked() }
        //  binding.iBtnNotificationBottom.setOnClickListener { viewModel.onNotificationBellClicked() }
    }

    /**
     * Setup tabs view pager
     *
     * @param isExistingUser - returns true value for existing user
     * returns false value for non existing user
     */
    // isJobTypeInstallation will be used while implementing Service type installation status
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
            tabTextView.textSize = resources.getDimension(R.dimen.tab_text_size)
            tabTextView.setTextColor(getColor(R.color.white))
            tabTextView.typeface = ResourcesCompat.getFont(applicationContext, R.font.arial_mt)
            binding.vpDashboard.setCurrentItem(tab.position, true)
        }.attach()
        binding.homeUpperTabs.addOnTabSelectedListener(onTabSelectedListener)
        binding.vpDashboard.setCurrentItem(1, false)
    }

    /**
     * Biometric check - It will check for hardware related issues to activate biometrics
     *
     * @param list - returns list to be handled
     */
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

    /**
     * Refresh payment info on accounts
     *
     */
    private fun refreshPaymentInfoOnAccounts() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is AccountFragment) {
                fragment.retryClicked()
            }
        }
    }

    /**
     * Open bio metric dialog - It will show biometric alert dialog
     *
     * @param dialogMessage - The alert message to be displayed
     */
    private fun openBioMetricDialog(dialogMessage: ChoiceDialogMessage) {
        ChoiceDialogFragment(
            getString(dialogMessage.title),
            getString(dialogMessage.message),
            getString(dialogMessage.positiveText),
            getString(dialogMessage.negativeText)
        ).show(supportFragmentManager, null)
    }

    /**
     * Refresh account fragment
     *
     */
    private fun refreshAccountFragment() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is AccountFragment) {
                fragment.refreshBioMetrics()
            }
        }
    }

    /**
     * Refresh appointments in dash board fragment
     *
     */
    private fun refreshAppointmentsInDashBoardFragment() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is DashboardFragment) {
                fragment.retryClicked()
            }
        }
    }

    /**
     * Refresh devices
     *
     */
    private fun refreshDevices() {
        val allFragments: List<Fragment> =
            supportFragmentManager.fragments
        for (fragment in allFragments) {
            if (fragment is DevicesFragment) {
                fragment.retryClicked()
            }
        }
    }

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, HomeActivity::class.java)
        }
    }
}
