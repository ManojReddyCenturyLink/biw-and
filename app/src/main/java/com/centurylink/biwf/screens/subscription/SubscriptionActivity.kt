package com.centurylink.biwf.screens.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.SubscriptionCoordinator
import com.centurylink.biwf.databinding.ActivitySubscriptionBinding
import com.centurylink.biwf.model.account.RecordsItem
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.home.account.subscription.adapter.InvoiceClickListener
import com.centurylink.biwf.screens.home.account.subscription.adapter.PaymentInvoicesAdapter
import com.centurylink.biwf.utility.DaggerViewModelFactory
import java.util.*
import javax.inject.Inject

class SubscriptionActivity : BaseActivity(), InvoiceClickListener {

    @Inject
    lateinit var subscriptionCoordinator: SubscriptionCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    @Inject
    lateinit var navigator: Navigator
    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(SubscriptionViewModel::class.java)
    }
    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var paymentInvoicesAdapter: PaymentInvoicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.statementView,
            binding.retryOverlay.root
        )
        navigator.observe(this)
        viewModel.apply {
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            myState.observeWith(subscriptionCoordinator)
            subscriptionDetailsRecord.observe {
                binding.subscriptionInfoWidget.subscriptionInfoSubscriptionName.text = it.zuora__ProductName__c ?: ""
                binding.subscriptionInfoWidget.subscriptionInfoSubscriptionDetails.text = getString(R.string.speeds, it.internetSpeed__c?.decapitalize() ?: "")
                val value = resources.getString(R.string.your_card, it.zuora__Price__c.toString()) + resources.getString(R.string.taxes_, it.zuora__BillingPeriodStartDay__c ?: "")
                binding.subscriptionInfoWidget.tvSubscriptionDetails.text = value
            }
            paymentmethod.observe {
                binding.currentPaymentMethod.text = it
            }
        }
        prepareRecyclerView()
        initViews()
    }

    override fun onBackPressed() {
        setResult(EditPaymentDetailsActivity.REQUEST_TO_REFRESH_PAYMENT_MOVE_TO_ACCOUNTS)
        finish()
    }

    override fun onPaymentListItemClick(item: RecordsItem) {
        viewModel.launchStatement(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            EditPaymentDetailsActivity.REQUEST_TO_EDIT_PAYMENT_DETAILS -> {
                if (resultCode == EditPaymentDetailsActivity.REQUEST_TO_REFRESH_PAYMENT_MOVE_TO_ACCOUNTS) {
                    setResult(resultCode)
                    finish()
                } else if (resultCode == EditPaymentDetailsActivity.REQUEST_TO_REFRESH_PAYMENT_TO_SUBSCRIPTION) {
                    viewModel.initApis()
                }
            }
            SubscriptionStatementActivity.REQUEST_TO_STATEMENT -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT)
                    finish()
                }
            }
            CancelSubscriptionActivity.REQUEST_TO_SUBSCRIPTION -> {
                if (resultCode == Activity.RESULT_OK || resultCode == CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT) {
                    setResult(CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT)
                    finish()
                }
            }
        }
    }

    override fun retryClicked() {
        showProgress(true)
        viewModel.initApis()
    }

    private fun initViews() {
        binding.previousStatementRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val screenTitle: String = getString(R.string.subscription)
        binding.incProfileHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logDoneBtnClick()
                setResult(EditPaymentDetailsActivity.REQUEST_TO_REFRESH_PAYMENT_MOVE_TO_ACCOUNTS)
                finish()
            }
        }
        binding.manageMySubscriptionRow.setOnClickListener { viewModel.launchManageSubscription() }

        binding.editBillingContainer.setOnClickListener { viewModel.onEditBillingContainerClicked() }
    }

    private fun prepareRecyclerView() {
        viewModel.invoicesListResponse.observe { list ->
            paymentInvoicesAdapter = PaymentInvoicesAdapter(this, this, list)
            binding.previousStatementRecyclerview.adapter = paymentInvoicesAdapter
            showProgress(false)
        }
    }

    companion object {
        const val PAYMENT_CARD: String = "PaymentCard"
        const val REQUEST_TO_SUBSCRIPTION_DETAILS = 1403
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, SubscriptionActivity::class.java).putExtra(
                PAYMENT_CARD, bundle.getString(PAYMENT_CARD)
            )
        }
    }
}
