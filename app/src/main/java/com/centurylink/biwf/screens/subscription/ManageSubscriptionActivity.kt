package com.centurylink.biwf.screens.subscription

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.ManageSubscriptionCoordinator
import com.centurylink.biwf.databinding.ActivityManageSubscriptionBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class ManageSubscriptionActivity : BaseActivity() {

    companion object {
        const val requestToSubscription: Int = 1101
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, ManageSubscriptionActivity::class.java)
        }
    }

    @Inject
    lateinit var manageSubscriptionCoordinator: ManageSubscriptionCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val manageSubscriptionViewModel by lazy {
        ViewModelProvider(this, factory).get(ManageSubscriptionViewModel::class.java)
    }
    private lateinit var binding: ActivityManageSubscriptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityManageSubscriptionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setHeightofActivity()
        initHeaders()
    }
    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        manageSubscriptionCoordinator.navigator.activity = this
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.manage_subscription_title)
        binding.activityHeaderView.subHeaderTitle.text = screenTitle
        binding.activityHeaderView.subHeaderLeftIcon.setOnClickListener { this.finish() }
        binding.activityHeaderView.subHeaderRightIcon.setOnClickListener {
            setResult(Activity.RESULT_OK)
            this.finish()
        }
    }
}