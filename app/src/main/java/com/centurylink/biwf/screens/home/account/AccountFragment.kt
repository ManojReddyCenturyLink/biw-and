package com.centurylink.biwf.screens.home.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.AccountCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.FragmentAccountBinding
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.screens.home.dashboard.DashboardFragment
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.NumberUtil.Companion.getOnlyDigits
import com.centurylink.biwf.utility.getViewModel
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import com.google.android.material.switchmaterial.SwitchMaterial
import timber.log.Timber
import javax.inject.Inject

/**
 * Account fragment - This class handle common methods related to account screen
 *
 * @constructor Create empty Account fragment
 */
class AccountFragment : BaseFragment(), AuthServiceHost {
    override val hostContext: Context get() = requireActivity()

    override val lifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var accountCoordinator: AccountCoordinator

    @Inject
    lateinit var viewModelFactory: AccountViewModel.Factory

    private val viewModel by lazy {
        getViewModel<AccountViewModel>(viewModelFactory.withInput(this))
    }

    private val fragManager by lazy { activity?.supportFragmentManager }

    lateinit var binding: FragmentAccountBinding

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    /**
     * On create view - The onCreateView method is called when Fragment should create its View
     *                  object hierarchy
     *
     * @param inflater - LayoutInflater: The LayoutInflater object that can be used to
     *                   inflate any views in the fragment,
     * @param container - ViewGroup: If non-null, this is the parent view that the fragment's UI
     *                    should be attached to. The fragment should not add the view itself,
     *                    but this can be used to generate the LayoutParams of the view.
     *                    This value may be null.
     * @param savedInstanceState - Bundle: If non-null, this fragment is being re-constructed
     * @return - Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAccountBinding.inflate(layoutInflater)
        setApiProgressViews(
            binding.viewGroup,
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.retryOverlay.root
        )
        observeViews()
        initSwitches()
        initClicks()
        viewModel.myState.observeWith(accountCoordinator)
        return binding.root
    }

    /**
     * On resume - Called when the fragment is visible to the user and actively running
     *
     */
    override fun onResume() {
        viewModel.logScreenLaunch()
        super.onResume()
    }

    /**
     * Retry clicked - It will handle the retry functionality
     *
     */
    override fun retryClicked() {
        showProgress(true)
        viewModel.initApiCalls()
    }

    /**
     * Refresh bio metrics
     *
     */
    fun refreshBioMetrics() {
        viewModel.refreshBiometrics()
    }

    /**
     * Init switches - It will initialize switch listeners
     *
     */
    private fun initSwitches() {
        binding.accountBiometricSwitch.setOnClickListener { view ->
            viewModel.onBiometricChange((view as SwitchMaterial).isChecked)
        }
        binding.accountMarketingEmailsSwitch.setOnClickListener { view ->
            viewModel.onMarketingEmailsChange((view as SwitchMaterial).isChecked)
        }
        binding.accountMarketingCallsSwitch.setOnClickListener { view ->
            viewModel.onMarketingCallsAndTextsChange((view as SwitchMaterial).isChecked, binding.accountPersonalInfoCard.personalInfoCellphone.text.toString())
        }
        binding.accountServiceCallsSwitch.setOnClickListener { view ->
            viewModel.onServiceCallsAndTextsChange((view as SwitchMaterial).isChecked)
        }
    }

    /**
     * Observe views - It is used to observe views
     *
     */
    private fun observeViews() {
        // Few API Parameters are null but tapping it needs to take to Other Screens SpHardcoding
        viewModel.apply {
            progressViewFlow.observe {
                showProgress(it)
            }
            errorMessageFlow.observe {
                showRetry(it.isNotEmpty())
            }
            noInternetMessage.observe {
                if (it) {
                    CustomDialogBlueTheme(
                        getString(R.string.err_no_network_connectivity_title),
                        getString(R.string.err_no_network_connectivity_message),
                        getString(R.string.ok),
                        true,
                        ::onErrorDialogCallback
                    ).show(fragManager!!, DashboardFragment::class.simpleName)
                }
            }
            bioMetricFlow.observe { boolean ->
                binding.accountBiometricSwitch.isChecked = boolean
            }
            viewModel.userPhoneNumberUpdateFlow.observe {
                viewModel.initAccountAndContactApiCalls()
            }
            accountDetailsInfo.observe { uiAccountDetails ->
                binding.accountFullName.text = uiAccountDetails.name

                // Service Address
                binding.accountServiceAddressLine1.text = uiAccountDetails.formattedServiceAddressLine1
                binding.accountServiceAddressLine2.text = uiAccountDetails.formattedServiceAddressLine2
                binding.accountServiceAddressLine1.visibility =
                    if (uiAccountDetails.formattedServiceAddressLine1.isEmpty()) View.GONE else View.VISIBLE
                binding.accountServiceAddressLine2.visibility =
                    if (uiAccountDetails.formattedServiceAddressLine2.isEmpty()) View.GONE else View.VISIBLE

                // planInfo
                binding.accountSubscriptionCard.accountCardPlanName.text = uiAccountDetails.planName ?: ""
                binding.accountSubscriptionCard.accountCardPlanDetails.text = getString(R.string.speeds,
                    uiAccountDetails.planSpeed?.decapitalize() ?: "")
                binding.accountSubscriptionCard.accountCardNextPaymentDate.text =
                    uiAccountDetails.paymentDate
                binding.accountSubscriptionCard.accountCardCardNumbers.text =
                    uiAccountDetails.paymentMethod ?: ""

                // Personal Info
                binding.accountPersonalInfoCard.personalInfoEmail.text = uiAccountDetails.email
                binding.accountPersonalInfoCard.personalInfoCellphone.text =
                    uiAccountDetails.cellPhone
                binding.accountPersonalInfoCard.personalInfoHomephone.text =
                    uiAccountDetails.homePhone
                binding.accountPersonalInfoCard.personalInfoWorkphone.text =
                    uiAccountDetails.workPhone

                // Preference
                binding.accountServiceCallsSwitch.isChecked =
                    uiAccountDetails.serviceCallsAndText
                binding.accountMarketingEmailsSwitch.isChecked =
                    uiAccountDetails.marketingEmails
                binding.accountMarketingCallsSwitch.isChecked =
                    uiAccountDetails.marketingCallsAndText
            }
            navigateToSubscriptionActivityEvent.handleEvent { paymentMethod ->
                (context as HomeActivity).launchSubscriptionActivity(paymentMethod)
            }
            errorMessageFlow.observe {
                Timber.d(it)
            }
        }
    }

    /**
     * Init clicks - It will initializes click listeners
     *
     */
    private fun initClicks() {
        binding.accountSubscriptionCard.subscriptionCard.setOnClickListener {
            viewModel.onSubscriptionCardClick()
        }
        binding.accountPersonalInfoCard.personalInfoCardView.setOnClickListener {
            viewModel.onPersonalInfoCardClick()
        }
        binding.logOutButton.setOnClickListener {
            viewModel.onLogOutClick(requireActivity())
        }
    }

    /**
     * Update views - It is used to update views according to phone number
     *
     * @param phoneNumber - returns phone number
     */
    fun updateViews(phoneNumber: String) {
        if (!getOnlyDigits(binding.accountPersonalInfoCard.personalInfoCellphone.text.toString()).equals(
                getOnlyDigits(phoneNumber)
            )
        ) {
            getOnlyDigits(phoneNumber)?.let {
                viewModel.onMarketingCallsAndTextsChange(
                    binding.accountMarketingCallsSwitch.isChecked, it

                )
            }
        }
    }

    /**
     * On error dialog callback - It will handle the error dialog callback listeners
     *
     * @param buttonType - It returns the which button type pressed
     */
    private fun onErrorDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                Timber.e("positive button pressed")
            }
        }
    }
}
