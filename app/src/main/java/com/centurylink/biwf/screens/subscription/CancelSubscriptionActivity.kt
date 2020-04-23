package com.centurylink.biwf.screens.subscription

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinator
import com.centurylink.biwf.databinding.ActivityCancelSubscriptionBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

class CancelSubscriptionActivity : BaseActivity() {

    companion object {
        const val REQUEST_TO_SUBSCRIPTION: Int = 1101
        fun newIntent(context: Context): Intent {
            return Intent(context, CancelSubscriptionActivity::class.java)
        }
    }

    @Inject
    lateinit var cancelSubscriptionCoordinator: CancelSubscriptionCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val cancelSubscriptionModel by lazy {
        ViewModelProvider(this, factory).get(CancelSubscriptionViewModel::class.java)
    }
    private lateinit var binding: ActivityCancelSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancelSubscriptionBinding.inflate(layoutInflater)
        cancelSubscriptionModel.apply {
            cancelSubscriptionDate.handleEvent { displayCancellationValidity(it) }
        }
        setContentView(binding.root)
        initHeaders()
        cancelSubscriptionModel.getCancellationValidity()
        cancelSubscriptionCoordinator.observeThis(cancelSubscriptionModel.myState)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        cancelSubscriptionCoordinator.navigator.activity = this
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.cancel_subscription_title)
        binding.activityHeaderView.subheaderCenterTitle.text = screenTitle
        binding.activityHeaderView.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.activityHeaderView.subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
        binding.activityHeaderView.subheaderRightActionTitle.setOnClickListener {
            setResult(Activity.RESULT_OK)
            this.finish()
        }
        binding.cancelSubscription.setOnClickListener { cancelSubscriptionModel.onNavigateToCancelSubscriptionDetails() }
    }

    @SuppressLint("StringFormatInvalid")
    private fun displayCancellationValidity(date: Date) {
        val validityDate = DateFormat.getDateInstance(DateFormat.LONG).format(date)
        binding.cancelSubscriptionContent.text =
            getString(R.string.manage_subscription_content, validityDate)
    }
}