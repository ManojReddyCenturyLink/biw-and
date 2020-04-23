package com.centurylink.biwf.screens.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.CancelSubscriptionsDetailsCoordinator
import com.centurylink.biwf.databinding.ActivityCancelSubscriptionDetailsBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class CancelSubscriptionDetailsActivity : BaseActivity() {

    companion object {
        const val REQUEST_TO__CANCEL_SUBSCRIPTION: Int = 11101
        fun newIntent(context: Context): Intent {
            return Intent(context, CancelSubscriptionDetailsActivity::class.java)
        }
    }

    @Inject
    lateinit var cancelSubscriptionDetailsCoordinator: CancelSubscriptionsDetailsCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val cancelSubscriptionDetailsModel by lazy {
        ViewModelProvider(this, factory).get(CancelSubscriptionDetailsViewModel::class.java)
    }
    private lateinit var binding: ActivityCancelSubscriptionDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCancelSubscriptionDetailsBinding.inflate(layoutInflater)
        cancelSubscriptionDetailsModel.apply {

        }
        setContentView(binding.root)
        initHeaders()
        initViews()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        cancelSubscriptionDetailsCoordinator.navigator.activity = this
    }

    private fun initHeaders() {
        binding.activityHeaderView.subheaderCenterTitle.text =
            getString(R.string.cancel_subscription_details_title)
        binding.activityHeaderView.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.activityHeaderView.subheaderRightActionTitle.text =
            getText(R.string.text_header_cancel)
        binding.activityHeaderView.subheaderRightActionTitle.setOnClickListener {
            setResult(Activity.RESULT_OK)
            this.finish()
        }
    }

    private fun initViews(){
    }
}