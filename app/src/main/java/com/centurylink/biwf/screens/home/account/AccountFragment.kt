package com.centurylink.biwf.screens.home.account

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.AccountCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.FragmentAccountBinding
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.service.auth.AuthServiceHost
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.getViewModel
import timber.log.Timber
import javax.inject.Inject

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

    lateinit var binding: FragmentAccountBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = false
    }

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

    override fun retryClicked() {
        showProgress(true)
        viewModel.initApiCalls()
    }

    fun refreshBioMetrics() {
        viewModel.refreshBiometrics()
    }

    private fun initSwitches() {
        binding.accountBiometricSwitch.setOnCheckedChangeListener { _, boolean ->
            viewModel.onBiometricChange(boolean)
        }
        binding.accountServiceCallsSwitch.setOnCheckedChangeListener { _, boolean ->
            viewModel.onServiceCallsAndTextsChange(boolean)
        }
        binding.accountMarketingEmailsSwitch.setOnCheckedChangeListener { _, boolean ->
            viewModel.onMarketingEmailsChange(boolean)
        }
        binding.accountMarketingCallsSwitch.setOnCheckedChangeListener { _, boolean ->
            viewModel.onMarketingCallsAndTextsChange(boolean)
        }
    }

    private fun observeViews() {
        // Few API Parameters are null but tapping it needs to take to Other Screens SpHardcoding
        //Todo: Remove Harding of values once API returns
        viewModel.apply {
            progressViewFlow.observe {
                showProgress(it)
            }
            errorMessageFlow.observe {
                showRetry(it.isNotEmpty())
            }
            bioMetricFlow.observe { boolean ->
                binding.accountBiometricSwitch.isChecked = boolean
            }
            accountDetailsInfo.observe { uiAccountDetails ->
                binding.accountFullName.text = uiAccountDetails.name
                binding.accountStreetAddress.text =
                    uiAccountDetails.serviceAddress1
                binding.accountCityStateAndZip.text = uiAccountDetails.serviceAddress2
                //planInfo
                binding.accountSubscriptionCard.accountCardPlanName.text =
                    uiAccountDetails.planName ?: " "
                binding.accountSubscriptionCard.accountCardPlanDetails.text =
                    uiAccountDetails.planSpeed ?: ""
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

    private fun initClicks() {
        binding.accountSubscriptionCard.subscriptionCard.setOnClickListener {
            viewModel.onSubscriptionCardClick()
        }
        binding.accountPersonalInfoCard.personalInfoCardView.setOnClickListener {
            viewModel.onPersonalInfoCardClick()
        }
        binding.logOutButton.setOnClickListener {
            viewModel.onLogOutClick()
        }
    }
}