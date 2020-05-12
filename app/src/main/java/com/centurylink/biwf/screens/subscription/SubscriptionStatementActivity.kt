package com.centurylink.biwf.screens.subscription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.StatementCoordinator
import com.centurylink.biwf.databinding.ActivitySubscriptionStatementBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject


class SubscriptionStatementActivity : BaseActivity() {

    companion object {
        fun newIntent(context: Context) = Intent(context, SubscriptionStatementActivity::class.java)
    }

    @Inject
    lateinit var statementCoordinator: StatementCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    private val subscriptionStatementViewModel by lazy {
        ViewModelProvider(this, factory).get(SubscriptionStatementViewModel::class.java)
    }
    private lateinit var binding: ActivitySubscriptionStatementBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionStatementBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        initHeaders()
        observeViews()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        binding.activityHeaderView.apply {
            subheaderCenterTitle.text = getText(R.string.statment_header)
            subHeaderLeftIcon.setOnClickListener { finish() }
            subheaderRightActionTitle.text = getText(R.string.statment_done)
            subheaderRightActionTitle.setOnClickListener {
                finish()
            }
        }
    }

    private fun observeViews() {
        subscriptionStatementViewModel.apply {
            statementDetailsInfo.observe { uiAccountInfo ->
                binding.subscriptionStatementProcessedDate.text =
                    getString(R.string.statement_processed_date,  uiAccountInfo.successfullyProcessed)
                binding.subscriptionPaymentMethodContent.text = uiAccountInfo.paymentMethod
                binding.subscriptionStatementPlanName.text = uiAccountInfo.planName
                binding.subscriptionStatementPlanCost.text =
                    getString(R.string.cost_template, uiAccountInfo.planCost)
                binding.subscriptionStatementSalesTaxCost.text =
                    getString(R.string.cost_template, uiAccountInfo.salesTaxCost)
                binding.subscriptionStatementTotalCost.text =
                    getString(R.string.cost_template, uiAccountInfo.totalCost)
                binding.subscriptionStatementBillingAddressContent.text =
                    uiAccountInfo.billingAddress
                binding.subscriptionStatementEmailContent.text=""
            }
        }
    }
}