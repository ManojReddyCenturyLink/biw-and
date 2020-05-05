package com.centurylink.biwf.screens.home.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseFragment
import com.centurylink.biwf.databinding.FragmentAccountBinding
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class AccountFragment : BaseFragment() {

    override val lifecycleOwner: LifecycleOwner = this

    @Inject
    lateinit var factory: DaggerViewModelFactory

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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_account, container, false)
        viewModel.apply {
            accountName.bindToTextView(binding.accountFullName)
            streetAddress.bindToTextView(binding.accountStreetAddress)
            combinedSecondHalfOfAddress.bindToTextView(binding.accountCityStateAndZip)
            emailAddress.bindToTextView(binding.accountPersonalInfoCard.personalInfoEmail)
            cellNumber.bindToTextView(binding.accountPersonalInfoCard.personalInfoCellphone)
            homeNumber.bindToTextView(binding.accountPersonalInfoCard.personalInfoHomephone)
            workNumber.bindToTextView(binding.accountPersonalInfoCard.personalInfoWorkphone)
            biometricStatus.bindToSwitch(binding.accountBiometricSwitch)
            subscriptionName.bindToTextView(binding.accountSubscriptionCard.accountCardPlanName)
            subscriptionDescription.bindToTextView(binding.accountSubscriptionCard.accountCardPlanDetails)
            subscriptionDate.bindToTextView(binding.accountSubscriptionCard.accountCardNextPaymentDate)
            subscriptionCardDisplayedText.bindToTextView(binding.accountSubscriptionCard.accountCardCardNumbers)

            serviceCallsAndTextStatus.bindToSwitch(binding.accountServiceCallsSwitch)
            marketingEmailStatus.bindToSwitch(binding.accountMarketingEmailsSwitch)
            marketingCallsAndTextStatus.bindToSwitch(binding.accountMarketingCallsSwitch)

            navigateToSubscriptionActivityEvent.handleEvent { (context as HomeActivity).onProfileClickEvent() }
        }

        initSwitches()
        initClicks()
        return binding.root
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

    private fun initClicks() {
        binding.accountSubscriptionCard.subscriptionCard.setOnClickListener {
            viewModel.onSubscriptionCardClick()
        }
    }
}