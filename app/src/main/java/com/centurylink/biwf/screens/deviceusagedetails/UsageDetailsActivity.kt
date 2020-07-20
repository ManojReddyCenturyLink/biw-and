package com.centurylink.biwf.screens.deviceusagedetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.UsageDetailsCoordinator
import com.centurylink.biwf.databinding.LayoutDevicesUsageInformationBinding
import com.centurylink.biwf.screens.home.account.PersonalInfoActivity
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.getViewModel
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import javax.inject.Inject

class UsageDetailsActivity : BaseActivity(), CustomDialogGreyTheme.DialogCallback {

    @Inject
    lateinit var usageDetailsCoordinator: UsageDetailsCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: UsageDetailsViewModel.Factory

    private lateinit var binding: LayoutDevicesUsageInformationBinding
    private val fragmentManager = supportFragmentManager

    override val viewModel by lazy {
        getViewModel<UsageDetailsViewModel>(viewModelFactory.withInput(intent.getStringExtra(STA_MAC)))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutDevicesUsageInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        initViews()
    }

    override fun retryClicked() {
        viewModel.initApis()
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
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.layoutTrafficDetails,
            binding.retryOverlay.root
        )
        viewModel.apply {
            initApis()
            myState.observeWith(usageDetailsCoordinator)
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            uploadSpeedDaily.observe { binding.dailyUploadSpeed.text = it }
            uploadSpeedMonthly.observe { binding.biweeklyUploadSpeed.text = it }
            downloadSpeedDaily.observe { binding.dailyDownloadSpeed.text = it }
            downloadSpeedMonthly.observe { binding.biweeklyDownloadSpeed.text = it }
            uploadSpeedDailyUnit.observe { binding.dailyUploadSpeedUnit.text = it }
            uploadSpeedMonthlyUnit.observe { binding.uploadSpeedUnitBiweekly.text = it }
            downloadSpeedDailyUnit.observe { binding.downloadSpeedUnitDaily.text = it }
            downloadSpeedMonthlyUnit.observe { binding.downloadSpeedUnitBiweekly.text = it }
            removeDevices.observe {
                if (it) {
                    setResult(100)
                    finish()
                }
            }
        }
        binding.deviceConnectedBtn.setOnClickListener { viewModel.onDevicesConnectedClicked() }
        binding.removeDevicesBtn.setOnClickListener { showAlertDialog() }
    }

    private fun showAlertDialog() {
        CustomDialogGreyTheme(
            getString(
                R.string.remove_device_confirmation_title,
                intent.getStringExtra(VENDOR_NAME)
            ),
            getString(R.string.remove_device_confirmation_msg),
            getString(R.string.remove),
            getString(
                R.string.text_header_cancel
            )
        ).show(
            fragmentManager,
            UsageDetailsActivity::class.simpleName
        )
    }

    override fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                viewModel.removeDevices(intent.getStringExtra(STA_MAC))
            }
            AlertDialog.BUTTON_NEGATIVE -> {
            }
        }
    }


    companion object {

        const val STA_MAC = "STA_MAC"
        const val HOST_NAME = "HOST_NAME"
        const val VENDOR_NAME = "VENDOR_NAME"
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, UsageDetailsActivity::class.java)
                .putExtra(STA_MAC, bundle.getString(STA_MAC))
                .putExtra(HOST_NAME, bundle.getString(HOST_NAME))
                .putExtra(VENDOR_NAME, bundle.getString(VENDOR_NAME))
        }
    }
}