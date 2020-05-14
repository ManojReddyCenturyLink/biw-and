package com.centurylink.biwf.screens.home.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.coordinators.AccountCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.FragmentAccountBinding
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class AccountFragment : BaseFragment() {

    override val lifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var accountCoordinator: AccountCoordinator
    private val viewModel by lazy {
        ViewModelProvider(this, factory).get(AccountViewModel::class.java)
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
        observeViews()
        initSwitches()
        initClicks()
        viewModel.myState.observeWith(accountCoordinator)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
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
            accountDetailsInfo.observe { uiAccountDetails ->
                binding.accountFullName.text = uiAccountDetails.name
                binding.accountStreetAddress.text =
                    uiAccountDetails.serviceAddress ?: "3004 Parkington Place SE\n" +
                            "Port Orchard, WA 98366"
                //planInfo
                binding.accountSubscriptionCard.accountCardPlanName.text =
                    uiAccountDetails.planName ?: "Best in world Fiber "
                binding.accountSubscriptionCard.accountCardPlanDetails.text =
                    uiAccountDetails.planSpeed ?: "Speeds Upto 940 Mbps"
                binding.accountSubscriptionCard.accountCardNextPaymentDate.text =
                    uiAccountDetails.paymentDate
                binding.accountSubscriptionCard.accountCardCardNumbers.text =
                    "Visa ******* 2453"
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
            navigateToSubscriptionActivityEvent.handleEvent { (context as HomeActivity).launchSubscriptionActivity() }
            errorMessageFlow.observe {
                displayToast(message = it)
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
    }
}