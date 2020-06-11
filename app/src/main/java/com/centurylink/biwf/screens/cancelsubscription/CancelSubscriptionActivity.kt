package com.centurylink.biwf.screens.cancelsubscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.CancelSubscriptionCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityCancelSubscriptionBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
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
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.cancelSubscriptionView,
            binding.retryOverlay.root
        )
        cancelSubscriptionModel.apply {
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            cancelSubscriptionDate.observe { displayCancellationValidity(it.subscriptionEndDate!!) }
        }
        cancelSubscriptionModel.myState.observeWith(cancelSubscriptionCoordinator)
        initHeaders()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        val screenTitle: String = getString(R.string.cancel_subscription_title)
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

    private fun displayCancellationValidity(date: String) {
        binding.cancelSubscriptionContent.text =
            getString(R.string.manage_subscription_content, date)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CancelSubscriptionDetailsActivity.REQUEST_TO_CANCEL_SUBSCRIPTION -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
                } else if (resultCode == CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT) {
                    setResult(CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT)
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