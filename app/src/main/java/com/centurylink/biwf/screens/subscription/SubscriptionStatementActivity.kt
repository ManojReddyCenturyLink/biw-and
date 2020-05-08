package com.centurylink.biwf.screens.subscription

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.StatementCoordinator
import com.centurylink.biwf.databinding.ActivitySubscriptionStatementBinding
import com.centurylink.biwf.screens.home.HomeActivity
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject


class SubscriptionStatementActivity : BaseActivity() {

    companion object{
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

    private fun initHeaders(){
        binding.activityHeaderView.apply{
            subheaderCenterTitle.text = getText(R.string.statment_header)
            subHeaderLeftIcon.setOnClickListener { finish() }
            subheaderRightActionTitle.text = getText(R.string.statment_done)
            subheaderRightActionTitle.setOnClickListener {
                finish()
            }
        }
    }

    private fun observeViews(){
        subscriptionStatementViewModel.apply {
            successfullyProcessed.bindToTextView(binding.subscriptionStatementProcessedDate)
            paymentMethod.bindToTextView(binding.subscriptionPaymentMethodContent)
            emails.bindToTextView(binding.subscriptionStatementEmailContent)
            billingAddress.bindToTextView(binding.subscriptionStatementBillingAddressContent)
            planName.bindToTextView(binding.subscriptionStatementPlanName)
            planCost.bindToTextView(binding.subscriptionStatementPlanCost)
            salesTaxCost.bindToTextView(binding.subscriptionStatementSalesTaxCost)
            promoCode.bindToTextView(binding.subscriptionStatementPromoLabel)
            promoCodeCost.bindToTextView(binding.subscriptionStatementPromoCost)
            promoCodeSubValue.bindToTextView(binding.subscriptionStatementPromoSubheader)
            totalCost.bindToTextView(binding.subscriptionStatementTotalCost)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CancelSubscriptionDetailsActivity.REQUEST_TO_CANCEL_SUBSCRIPTION->{
                if (resultCode == CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT) {
                    setResult(CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT)
                    finish()
                }
            }
        }
    }
}