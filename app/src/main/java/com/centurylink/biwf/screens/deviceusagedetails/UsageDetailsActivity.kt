package com.centurylink.biwf.screens.deviceusagedetails

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.UsageDetailsCoordinator
import com.centurylink.biwf.databinding.LayoutDevicesUsageInformationBinding
import com.centurylink.biwf.model.devices.DeviceConnectionStatus
import com.centurylink.biwf.model.devices.DevicesData
import com.centurylink.biwf.screens.networkstatus.ModemUtils
import com.centurylink.biwf.utility.AppUtil
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.getViewModel
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import javax.inject.Inject

/**
 * Usage details activity - this class handle common methods related to devices screen
 *
 * @constructor Create empty Usage details activity
 */
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

    private lateinit var deviceData: DevicesData

    private val fragmentManager = supportFragmentManager

    override val viewModel by lazy {
        getViewModel<UsageDetailsViewModel>(
            viewModelFactory.withInput(
                intent.getSerializableExtra(
                    DEVICE_INFO
                ) as DevicesData
            )
        )
    }

    /**
     * On create - Called when the activity is first created
     *
     * @param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LayoutDevicesUsageInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        initViews()
    }

    /**
     * Retry clicked - this handles retry click listeners
     *
     */
    override fun retryClicked() {
        viewModel.initApis()
    }

    /**
     * On back pressed - this handles back key click listeners
     *
     */
    override fun onBackPressed() {
        setResult(REQUEST_TO_DEVICES)
        finish()
    }

    /**
     * Init views - it will initialises the views
     *
     */
    private fun initViews() {
        deviceData = intent.getSerializableExtra(DEVICE_INFO) as DevicesData
        val nickName = if (deviceData.mcAfeeName.isNotEmpty()) {
            deviceData.mcAfeeName
        } else {
            deviceData.hostName ?: ""
        }
        binding.subheaderCenterTitle.text = nickName
        binding.subheaderRightActionTitle.text = getText(R.string.done)
        binding.subheaderRightActionTitle.setOnClickListener {
            val nickname = if (binding.nicknameDeviceNameInput.text.toString()
                    .isNotEmpty()
            ) binding.nicknameDeviceNameInput.text.toString() else nickName
            validateNickName(nickname)
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
            showErrorPopup.observe {
                if (it) {
                    showAlertDialog(true)
                } else {
                    setResult(REQUEST_TO_DEVICES)
                    finish()
                }
            }
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
                val isModemStatus = intent.getBooleanExtra(MODEM_STATUS, false)
                if (!isModemStatus) {
                    it.deviceConnectionStatus = DeviceConnectionStatus.MODEM_OFF
                    binding.deviceConnectedBtn.background =
                        (getDrawable(R.drawable.light_gray_rounded_borderless_background))
                    binding.connectionStatusBtnText.text =
                        getString(R.string.not_connected)
                    binding.tapToRetryText.text =
                        getString(R.string.connect_device_pause_unpause)
                    binding.connectionStatusBtnText.setTextColor(getColor(R.color.med_grey))
                } else {
                    when (it.deviceConnectionStatus) {
                        DeviceConnectionStatus.PAUSED -> {
                            binding.deviceConnectedBtn.background =
                                getDrawable(R.drawable.light_grey_rounded_background)
                            binding.connectionStatusBtnText.text = getString(R.string.connection_paused)
                            binding.connectionStatusBtnText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                            binding.connectionStatusIcon.visibility = View.VISIBLE
                            binding.tapToRetryText.text = getString(R.string.tap_to_resume_connection)
                            binding.connectionStatusBtnText.setTextColor(getColor(R.color.dark_grey))
                        }
                        DeviceConnectionStatus.DEVICE_CONNECTED -> {
                            binding.deviceConnectedBtn.background =
                                getDrawable(R.drawable.light_blue_rounded_background)
                            binding.connectionStatusBtnText.text = getString(R.string.device_connected)
                            binding.connectionStatusBtnText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                            binding.connectionStatusIcon.visibility = View.VISIBLE
                            binding.tapToRetryText.text = getString(R.string.tap_to_pause_connection)
                            binding.connectionStatusBtnText.setTextColor(getColor(R.color.purple))
                        }
                        DeviceConnectionStatus.FAILURE -> {
                            binding.deviceConnectedBtn.background =
                                getDrawable(R.drawable.light_blue_rounded_background)
                            binding.connectionStatusBtnText.text = ""
                            binding.connectionStatusBtnText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_icon_reload, 0, 0, 0)
                            binding.connectionStatusIcon.visibility = View.GONE
                            binding.tapToRetryText.text =
                                getString(R.string.error_loading_device_status)
                        }
                        else -> {}
                    }
                }
                binding.connectionStatusIcon.setImageDrawable(
                    getDrawable(
                        ModemUtils.getConnectionStatusIcon(
                            it
                        )
                    )
                )
            }
        }
        binding.nicknameDeviceNameInput.hint = nickName
        binding.deviceConnectedBtn.setOnClickListener {
        viewModel.retryStatus = !AppUtil.isOnline(this@UsageDetailsActivity)
        viewModel.onDevicesConnectedClicked()
        }
        binding.removeDevicesBtn.setOnClickListener {
            viewModel.onRemoveDevicesClicked()
            showAlertDialog(false)
        }
        binding.nicknameDeviceNameInput.onFocusChangeListener = OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                hideKeyboard()
            }
        }
    }

    /**
     * Validate nick name - It will validate the name
     *
     * @param nickname - returns the formatted name string
     */
    private fun validateNickName(nickname: String) {
        if (viewModel.validateInput(nickname)) {
            CustomDialogBlueTheme(
                title = getString(R.string.error_title),
                message = getString(R.string.error_nickname_field),
                buttonText = getString(R.string.discard_changes_and_close),
                isErrorPopup = true,
                callback = ::onErrorDialogCallback
            ).show(
                fragmentManager,
                callingActivity?.className
            )
        } else {
            viewModel.onDoneBtnClick(nickname)
        }
    }

    /**
     * Hide keyboard - It handle the hide keyboard logic
     *
     */
    private fun hideKeyboard() {
        val imm: InputMethodManager =
            this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.nicknameDeviceNameInput.windowToken, 0)
    }

    /**
     * Hide keyboard - It show the alert dialog
     *
     */
    private fun showAlertDialog(displayBlueDialog: Boolean) {
        if (displayBlueDialog) {
            CustomDialogBlueTheme(
                title = getString(R.string.error_title),
                message = getString(R.string.password_reset_error_msg),
                buttonText = getString(R.string.discard_changes_and_close),
                isErrorPopup = true,
                callback = ::onErrorDialogCallback
            ).show(
                fragmentManager,
                callingActivity?.className
            )
        } else {
            CustomDialogGreyTheme(
                getString(
                    R.string.remove_device_confirmation_title,
                    deviceData.mcAfeeName
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
    }

    /**
     * On dialog callback- it handle the dialog callback listeners
     *
     * @param buttonType - its return the which button is pressed negative or positive
     */
    private fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                viewModel.logRemoveConnection(true)
                viewModel.removeDevices(deviceData.stationMac!!)
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                viewModel.logRemoveConnection(false)
            }
        }
    }

    /**
     * On error dialog callback - t handle the dialog callback listeners
     *
     * @param buttonType - its return the which button is pressed negative or positive
     */
    private fun onErrorDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    companion object {
        const val REQUEST_TO_DEVICES = 1341
        const val DEVICE_INFO = "DEVICE_INFO"
        const val MODEM_STATUS = "MODEM_STATUS"
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, UsageDetailsActivity::class.java)
                .putExtra(DEVICE_INFO, bundle.getSerializable(DEVICE_INFO))
                .putExtra(MODEM_STATUS, bundle.getBoolean(MODEM_STATUS, false))
        }
    }
}
