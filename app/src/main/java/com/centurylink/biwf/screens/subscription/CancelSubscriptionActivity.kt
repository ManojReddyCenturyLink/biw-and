package com.centurylink.biwf.screens.subscription

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityCancelSubscriptionBinding
import com.centurylink.biwf.screens.support.FAQActivity
import com.centurylink.biwf.utility.DaggerViewModelFactory
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

class CancelSubscriptionActivity : BaseActivity() {

    @Inject
    lateinit var cancelSubscriptionCoordinator: CancelSubscriptionCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    @Inject
    lateinit var navigator: Navigator

    private val cancelSubscriptionModel by lazy {
        ViewModelProvider(this, factory).get(CancelSubscriptionViewModel::class.java)
    }
    private lateinit var binding: ActivityCancelSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancelSubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)

        cancelSubscriptionModel.apply {
            cancelSubscriptionDate.handleEvent { displayCancellationValidity(it) }
        }
        cancelSubscriptionCoordinator.observeThis(cancelSubscriptionModel.myState)

        initHeaders()
        cancelSubscriptionModel.getCancellationValidity()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.cancel_subscription_title)
        binding.activityHeaderView.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener { finish() }
            subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
            subheaderRightActionTitle.setOnClickListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        binding.cancelSubscription
            .setOnClickListener { cancelSubscriptionModel.onNavigateToCancelSubscriptionDetails() }
    }

    @SuppressLint("StringFormatInvalid")
    private fun displayCancellationValidity(date: Date) {
        val validityDate = DateFormat.getDateInstance(DateFormat.LONG).format(date)
        binding.cancelSubscriptionContent.text =
            getString(R.string.manage_subscription_content, validityDate)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CancelSubscriptionDetailsActivity.REQUEST_TO__CANCEL_SUBSCRIPTION,
            FAQActivity.REQUEST_TO_HOME -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    companion object {
        const val REQUEST_TO_SUBSCRIPTION: Int = 1101

        fun newIntent(context: Context): Intent {
            return Intent(context, CancelSubscriptionActivity::class.java)
        }
    }
}