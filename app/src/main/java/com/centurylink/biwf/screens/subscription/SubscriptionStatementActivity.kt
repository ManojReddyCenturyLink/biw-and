package com.centurylink.biwf.screens.subscription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.LiveData
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
            updateProcessedDate(successfullyProcessed)
            paymentMethod.bindToTextView(binding.subscriptionPaymentMethodContent)
            emails.bindToTextView(binding.subscriptionStatementEmailContent)
            billingAddressData.bindToTextView(binding.subscriptionStatementBillingAddressContent)
            planName.bindToTextView(binding.subscriptionStatementPlanName)
            planCost.bindTextViewWithCosts(binding.subscriptionStatementPlanCost)
            promoCodeSubValue.bindToTextView(binding.subscriptionStatementPromoSubheader)
            salesTaxCost.bindTextViewWithCosts(binding.subscriptionStatementSalesTaxCost)
            promoCode.bindTextViewWithCosts(binding.subscriptionStatementPromoLabel)
            promoCodeCost.bindTextViewWithCosts(binding.subscriptionStatementPromoCost)
            totalCost.bindTextViewWithCosts(binding.subscriptionStatementTotalCost)
        }
    }

    private fun updateProcessedDate(successfullyProcessed: LiveData<String>) {
        successfullyProcessed.observe {
            binding.subscriptionStatementProcessedDate.text =
                getString(R.string.statement_processed_date, it)
        }
    }

    private fun LiveData<String>.bindTextViewWithCosts(textView : TextView){
      observe {
          textView.text =
                getString(R.string.cost_template, it)
        }
    }

}