package com.centurylink.biwf.screens.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.afterTextChanged
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
        subscriptionCoordinator.observeThis(subscriptionViewModel.myState)
        subscriptionViewModel.apply {
            checkboxState.observe { binding.billingInfoWidget.billingInfoCheckbox.isActivated = it }
            paymentFirstName.observe { binding.paymentInfoWidget.paymentInfoFirstNameInput.setText(it) }
            paymentLastName.observe { binding.paymentInfoWidget.paymentInfoLastNameInput.setText(it) }
            billingFirstName.observe { binding.billingInfoWidget.billingInfoFirstNameInput.setText(it) }
            billingLastName.observe { binding.billingInfoWidget.billingInfoLastNameInput.setText(it) }
            billingStreetAddress.observe { binding.billingInfoWidget.billingInfoStreetAddressInput.setText(it) }
            billingCity.observe { binding.billingInfoWidget.billingInfoCityInput.setText(it) }
            billingState.observe { binding.billingInfoWidget.billingInfoStateInput.setText(it) }
            billingZipCode.observe { binding.billingInfoWidget.billingInfoZipcodeInput.setText(it) }
            planName.observe { binding.subscriptionInfoWidget.subscriptionInfoSubscriptionName.text = it }
            planDetails.observe { binding.subscriptionInfoWidget.subscriptionInfoSubscriptionDetails.text = it }
        }
        prepareRecyclerView()
        initViews()
        onTextChangeListeners()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onPaymentListItemClick(item: RecordsItem) {
        subscriptionViewModel.launchStatement(item)
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
        binding.billingInfoWidget.billingInfoCheckbox.setOnClickListener { subscriptionViewModel.sameAsServiceAddressedClicked() }
    }

    private fun prepareRecyclerView() {
        subscriptionViewModel.invoicesListResponse.observe { list ->
            paymentInvoicesAdapter = PaymentInvoicesAdapter(this, this, list)
            binding.previousStatementRecyclerview.adapter = paymentInvoicesAdapter
        }
    }

    private fun onTextChangeListeners() {
        val billingFirstName = binding.billingInfoWidget.billingInfoFirstNameInput
        billingFirstName.addTextChangedListener(afterTextChanged { editable ->
            subscriptionViewModel.onBillingFirstNameChange(editable.toString())
            billingFirstName.setSelection(editable.toString().length)
        })

        val billingLastName = binding.billingInfoWidget.billingInfoLastNameInput
        billingLastName.addTextChangedListener(afterTextChanged { editable ->
            subscriptionViewModel.onBillingLastNameChange(editable.toString())
            billingLastName.setSelection(editable.toString().length)
        })

        val streetAddress = binding.billingInfoWidget.billingInfoStreetAddressInput
        streetAddress.addTextChangedListener(afterTextChanged {editable ->
            subscriptionViewModel.onStreetAddressChange(editable.toString())
            streetAddress.setSelection(editable.toString().length)

        })

        val city = binding.billingInfoWidget.billingInfoCityInput
        city.addTextChangedListener(afterTextChanged {editable ->
            subscriptionViewModel.onCityChange(editable.toString())
            city.setSelection(editable.toString().length)
        })

        val state = binding.billingInfoWidget.billingInfoStateInput
        state.addTextChangedListener(afterTextChanged { editable ->
            subscriptionViewModel.onStateChange(editable.toString())
            state.setSelection(editable.toString().length)
        })

        val zipCode = binding.billingInfoWidget.billingInfoZipcodeInput
        zipCode.addTextChangedListener(afterTextChanged { editable ->
            subscriptionViewModel.onZipCodeChange(editable.toString())
            zipCode.setSelection(editable.toString().length)
        })
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, SubscriptionActivity::class.java)
    }
}