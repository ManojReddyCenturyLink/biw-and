package com.centurylink.biwf.screens.home.account.subscription

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
import com.centurylink.biwf.screens.home.account.subscription.adapter.InvoiceClickListener
import com.centurylink.biwf.screens.home.account.subscription.adapter.PaymentInvoicesAdapter
import com.centurylink.biwf.screens.subscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.subscription.SubscriptionStatementActivity
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class SubscriptionActivity : BaseActivity(), InvoiceClickListener {

    @Inject
    lateinit var subscriptionCoordinator: SubscriptionCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator
    private val subscriptionViewModel by lazy {
        ViewModelProvider(this, factory).get(SubscriptionViewModel::class.java)
    }
    private lateinit var binding: ActivitySubscriptionBinding
    private lateinit var paymentInvoicesAdapter: PaymentInvoicesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)
        navigator.observe(this)
        subscriptionViewModel.myState.observeWith(subscriptionCoordinator)
        subscriptionViewModel.apply {
            errorMessageFlow.observe { displayToast(it) }
        }
        prepareRecyclerView()
        initViews()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onPaymentListItemClick(item: RecordsItem) {
        subscriptionViewModel.launchStatement(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CancelSubscriptionActivity.REQUEST_TO_SUBSCRIPTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
            SubscriptionStatementActivity.REQUEST_TO_STATEMENT -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
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
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        binding.manageMySubscriptionRow.setOnClickListener { subscriptionViewModel.launchManageSubscription() }
    }

    private fun prepareRecyclerView() {
        subscriptionViewModel.invoicesListResponse.observe { list ->
            paymentInvoicesAdapter = PaymentInvoicesAdapter(this, this, list)
            binding.previousStatementRecyclerview.adapter = paymentInvoicesAdapter
        }
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SubscriptionActivity::class.java)
    }
}