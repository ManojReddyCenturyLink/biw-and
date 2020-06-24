package com.centurylink.biwf.screens.deviceusagedetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
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
        navigator.observe(this)
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.layoutTrafficDetails,
            binding.retryOverlay.root
        )
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
        val screenTitle = intent.getStringExtra(HOST_NAME)
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
            updateStaMacValue(intent.getStringExtra(STA_MAC))
            initApis()
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            uploadSpeedDaily.observe { binding.dailyUploadSpeed.text = it }
            uploadSpeedMonthly.observe { binding.monthlyUploadSpeed.text = it }
            downloadSpeedDaily.observe { binding.dailyDownloadSpeed.text = it }
            downloadSpeedMonthly.observe { binding.monthlyDownloadSpeed.text = it }
        }
        binding.deviceConnectedBtn.setOnClickListener { usageDetailsViewModel.onDevicesConnectedClicked() }
    }

    companion object {
        const val STA_MAC = "STA_MAC"
        const val HOST_NAME = "HOST_NAME"

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, UsageDetailsActivity::class.java).putExtra(
                STA_MAC, bundle.getString(STA_MAC)
            ).putExtra(HOST_NAME, bundle.getString(HOST_NAME))
        }
    }
}