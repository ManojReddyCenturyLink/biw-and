package com.centurylink.biwf.screens.deviceusagedetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.UsageDetailsCoordinator
import com.centurylink.biwf.databinding.LayoutDevicesUsageInformationBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.getViewModel
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import javax.inject.Inject

class UsageDetailsActivity : BaseActivity() {

    @Inject
    lateinit var usageDetailsCoordinator: UsageDetailsCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var viewModelFactory: UsageDetailsViewModel.Factory

    private lateinit var binding: LayoutDevicesUsageInformationBinding

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
                viewModel.logDoneBtnClick()
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
                    setResult(REQUEST_TO_DEVICES)
                    finish()
                }
            }
            pauseUnpauseConnection.observe {
                binding.connectionStatusIcon.setImageDrawable(getDrawable(if (it) R.drawable.ic_3_bars else R.drawable.ic_network_off))
                binding.deviceConnectedBtn.background =
                    (getDrawable(if (it) R.drawable.light_blue_rounded_background else R.drawable.light_grey_rounded_background))
                binding.connectionStatusBtnText.text =
                    getString(if (it) R.string.device_connected else R.string.connection_paused)
                binding.tapToRetryText.text =
                    getString(if (it) R.string.tap_to_pause_connection else R.string.tap_to_resume_connection)
                binding.connectionStatusBtnText.setTextColor(getColor(if (it) R.color.blue else R.color.font_color_medium_grey))
            }
        }
        binding.nicknameDeviceNameInput.setText(screenTitle)
        binding.deviceConnectedBtn.setOnClickListener {
            viewModel.onDevicesConnectedClicked()
        }
        binding.removeDevicesBtn.setOnClickListener {
            viewModel.onRemoveDevicesClicked()
            showAlertDialog()
        }
    }

    private fun showAlertDialog() {
        CustomDialogGreyTheme(
            getString(
                R.string.remove_device_confirmation_title,
                intent.getStringExtra(HOST_NAME)
            ),
            getString(R.string.remove_device_confirmation_msg),
            getString(R.string.remove),
            getString(R.string.text_header_cancel),
            ::onDialogCallback
        ).show(
            supportFragmentManager,
            UsageDetailsActivity::class.simpleName
        )
    }

    private fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                viewModel.logRemoveConnection(true)
                viewModel.removeDevices(intent.getStringExtra(STA_MAC))
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                viewModel.logRemoveConnection(false)
            }
        }
    }

    companion object {
        val REQUEST_TO_DEVICES = 1341
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