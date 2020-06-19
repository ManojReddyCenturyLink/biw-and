package com.centurylink.biwf.screens.deviceusagedetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.BIWFApp
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.UsageDetailsCoordinator
import com.centurylink.biwf.databinding.LayoutDevicesUsageInformationBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class UsageDetailsActivity : BaseActivity() {

    @Inject
    lateinit var usageDetailsCoordinator: UsageDetailsCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator
    private val usageDetailsViewModel by lazy {
        ViewModelProvider(this, factory).get(UsageDetailsViewModel::class.java)
    }
    private lateinit var binding: LayoutDevicesUsageInformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutDevicesUsageInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (applicationContext as BIWFApp).dispatchingAndroidInjector.inject(this)
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.layoutTrafficDetails,
            binding.retryOverlay.root
        )
        navigator.observe(this)
        usageDetailsViewModel.apply {
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            myState.observeWith(usageDetailsCoordinator)
        }
        initViews()
    }

    override fun retryClicked() {
        usageDetailsViewModel.initApis()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initViews() {
        val screenTitle: String = "John’s work laptop"
        binding.activityHeaderView.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
        usageDetailsViewModel.apply {
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
        }
        binding.dailyUploadSpeed.text = usageDetailsViewModel.usageValueDaily.toString()
        binding.monthluUploadSpeed.text = usageDetailsViewModel.usageValueMonthly.toString()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, UsageDetailsActivity::class.java)
    }
}