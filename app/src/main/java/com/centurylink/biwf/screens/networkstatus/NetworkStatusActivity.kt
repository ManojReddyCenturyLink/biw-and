package com.centurylink.biwf.screens.networkstatus

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityNetworkStatusBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.afterTextChanged
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import com.centurylink.biwf.widgets.GeneralErrorPopUp
import javax.inject.Inject

/**
 * Network status activity - this class handle common methods related to Network screen
 *
 * @constructor Create empty Network status activity
 */
class NetworkStatusActivity : BaseActivity() {
    private lateinit var bindings: ActivityNetworkStatusBinding

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val fragmentManager = supportFragmentManager

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(NetworkStatusViewModel::class.java)
    }

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindings = ActivityNetworkStatusBinding.inflate(layoutInflater)
        setContentView(bindings.root)
        initHeaders()
        initViews()
        initOnClicks()
        watchTextChanges()
        initEnableDisableEventClicks()
    }

    /**
     * Init headers - It will initialize screen headers
     *
     */
    private fun initHeaders() {
        var screenTitle: String = getString(R.string.network_status)
        bindings.incHeader.apply {
            subHeaderLeftIcon.visibility = View.GONE
            subheaderCenterTitle.text = screenTitle
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                if (viewModel.networkInfoComplete) {
                    validateNameAndPassword()
                }
            }
        }
    }

    /**
     * Init views - It will initialize the views
     *
     */
    private fun initViews() {
        setApiProgressViews(
            bindings.progressOverlay.root,
            bindings.retryOverlay.retryViewLayout,
            bindings.networkStatusScrollview,
            bindings.retryOverlay.root
        )
        viewModel.apply {
            progressViewFlow.observe {
                showProgress(it)
                if (it) {
                    bindings.incHeader.apply {
                        subheaderRightActionTitle.isClickable = false
                    }
                } else {
                    bindings.incHeader.apply {
                        subheaderRightActionTitle.isClickable = true
                    }
                }
            }
            modemDeviceID.observe {
                if (it) {
                    val serialNumber = intent.getStringExtra(DEVICE_ID)
                    bindings.networkStatusModemSerialNumber.text =
                        getString(R.string.serial_number, serialNumber)
                    bindings.networkStatusWifiNameInput.isEnabled = it
                    bindings.networkStatusWifiPasswordInput.isEnabled = it
                    bindings.networkStatusGuestNameInput.isEnabled = it
                    bindings.networkStatusGuestPasswordInput.isEnabled = it
                    bindings.incHeader.apply {
                        subheaderRightActionTitle.isClickable = true
                    }
                }
            }
            modemInfoFlow.observe {
                if (!it.apInfoList.isNullOrEmpty()) {
                    val deviceId = it.apInfoList[0].deviceId
                    if (!deviceId.isNullOrEmpty()) {
                        bindings.networkStatusModemSerialNumber.text =
                            getString(R.string.serial_number, deviceId)
                    }
                }
            }
            internetStatusFlow.observe {
                bindings.networkStatusInternetImageview.setImageDrawable(getDrawable(it.drawableId))
                bindings.networkStatusInternetStatus.text = getString(it.onlineStatus)
                bindings.networkStatusInternetStatusText.text = getString(it.subText)
                bindings.networkStatusModemImageview.setImageDrawable(getDrawable(it.drawableId))
                bindings.networkStatusModemStatus.text = getString(it.onlineStatus)
            }
            regularNetworkStatusFlow.observe {
                bindings.networkStatusWifiButton.isActivated = it.isNetworkEnabled
                bindings.networkStatusWifiButtonText.text = getString(it.networkStatusText)
                bindings.networkStatusWifiButtonText.setTextColor(getColor(it.networkStatusTextColor))
                bindings.networkStatusWifiImage.setImageDrawable(getDrawable(it.statusIcon))
                bindings.networkStatusWifiButtonActionText.text =
                    getString(it.networkStatusSubText)
                bindings.networkStatusGuestButtonText.isEnabled = it.isNetworkEnabled
                bindings.networkStatusWifiNameInput.isEnabled = it.isNetworkEnabled
                if (it.netWorkName.isNullOrEmpty()) {
                    bindings.networkStatusWifiNameInput.setText(intent.getStringExtra(REGULAR_WIFI_NAME))
                } else {
                    bindings.networkStatusWifiNameInput.setText(it.netWorkName)
                }
                bindings.networkStatusWifiPasswordInput.isEnabled = it.isNetworkEnabled
                if (it.networkPassword.isNullOrEmpty()) {
                    bindings.networkStatusWifiPasswordInput.setText(intent.getStringExtra(
                        REGULAR_WIFI_PASSWORD))
                } else {
                    bindings.networkStatusWifiPasswordInput.setText(it.networkPassword)
                }
            }
            guestNetworkStatusFlow.observe {
                bindings.networkStatusGuestButton.isActivated = it.isNetworkEnabled
                bindings.networkStatusGuestButtonText.text = getString(it.networkStatusText)
                bindings.networkStatusGuestButtonText.setTextColor(getColor(it.networkStatusTextColor))
                bindings.networkStatusGuestWifiImage.setImageDrawable(getDrawable(it.statusIcon))
                bindings.networkStatusGuestButtonText.isEnabled = it.isNetworkEnabled
                bindings.networkStatusGuestButtonActionText.text =
                    getString(it.networkStatusSubText)
                bindings.networkStatusGuestWifiImage.isActivated = it.isNetworkEnabled
                if (it.netWorkName.isNullOrEmpty()) {
                    bindings.networkStatusGuestNameInput.setText(intent.getStringExtra(
                        GUEST_WIFI_NAME))
                } else {
                    bindings.networkStatusGuestNameInput.setText(it.netWorkName)
                }
                bindings.networkStatusGuestNameInput.isEnabled = it.isNetworkEnabled
                if (it.networkPassword.isNullOrEmpty()) {
                    bindings.networkStatusGuestPasswordInput.setText(intent.getStringExtra(
                        GUEST_WIFI_PASSWORD))
                } else {
                    bindings.networkStatusGuestPasswordInput.setText(it.networkPassword)
                }
                bindings.networkStatusGuestPasswordInput.isEnabled = it.isNetworkEnabled
            }
            viewModel.error.observe {
                bindings.fieldsMarkedRequiredWifi.visibility =
                    if (it.containsKey("wifiNameFieldMandatory") || it.containsKey("wifiPasswordFieldMandatory")) View.VISIBLE else View.GONE
                bindings.networkStatusWifiNameLabel.setTextColor(
                    getColor(
                        if (it.containsKey("wifiNameError") && it.containsKey(
                                "wifiNameFieldMandatory"
                            )
                        ) R.color.strawberry else R.color.med_grey
                    )
                )
                bindings.networkStatusWifiNameLabel.text =
                    if (it.containsKey("wifiNameError") && it.containsKey("wifiNameFieldMandatory")) resources.getString(
                        R.string.network_name_mandatory
                    ) else resources.getString(
                        R.string.network_name
                    )
                bindings.networkStatusWifiNameLabel.typeface =
                    if (it.containsKey("wifiNameError") && it.containsKey("wifiNameFieldMandatory")) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                bindings.networkStatusWifiNameInput.background =
                    getDrawable(if (it.containsKey("wifiNameError") && it.containsKey("wifiNameFieldMandatory")) R.drawable.background_thin_border_red else R.drawable.background_thin_border)
                bindings.networkStatusWifiPasswordLabel.setTextColor(
                    getColor(
                        if (it.containsKey("wifiPasswordError") && it.containsKey(
                                "wifiPasswordFieldMandatory"
                            )
                        ) R.color.strawberry else R.color.med_grey
                    )
                )
                bindings.networkStatusWifiPasswordLabel.text =
                    if (it.containsKey("wifiPasswordError") && it.containsKey("wifiPasswordFieldMandatory")) resources.getString(
                        R.string.network_password_mandatory
                    ) else resources.getString(
                        R.string.network_password
                    )
                bindings.networkStatusWifiPasswordLabel.typeface =
                    if (it.containsKey("wifiPasswordError") && it.containsKey("wifiPasswordFieldMandatory")) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                bindings.networkStatusWifiPasswordInput.background =
                    getDrawable(if (it.containsKey("wifiPasswordError") && it.containsKey("wifiPasswordFieldMandatory")) R.drawable.background_thin_border_red else R.drawable.background_thin_border)
                bindings.networkStatusWifiPasswordRestraintsLabel.setTextColor(
                    getColor(
                        if (it.containsKey("wifiPasswordError") && it.containsKey("wifiPasswordFieldLength"))
                            R.color.strawberry else R.color.med_grey
                    )
                )
                bindings.fieldsMarkedRequiredGuest.visibility =
                    if (it.containsKey("guestNameFieldMandatory") || it.containsKey("guestPasswordFieldMandatory")) View.VISIBLE else View.GONE
                bindings.networkStatusGuestNameLabel.setTextColor(
                    getColor(
                        if (it.containsKey("guestNameError") && it.containsKey(
                                "guestNameFieldMandatory"
                            )
                        ) R.color.strawberry else R.color.med_grey
                    )
                )
                bindings.networkStatusGuestNameLabel.text =
                    if (it.containsKey("guestNameError") && it.containsKey("guestNameFieldMandatory")) resources.getString(
                        R.string.guest_network_name_mandatory
                    ) else resources.getString(
                        R.string.guest_network_name
                    )
                bindings.networkStatusGuestNameLabel.typeface =
                    if (it.containsKey("guestNameError") && it.containsKey("guestNameFieldMandatory")) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                bindings.networkStatusGuestNameInput.background =
                    getDrawable(if (it.containsKey("guestNameError") && it.containsKey("guestNameFieldMandatory")) R.drawable.background_thin_border_red else R.drawable.background_thin_border)
                bindings.networkStatusGuestPasswordLabel.setTextColor(
                    getColor(
                        if (it.containsKey("guestPasswordError") && it.containsKey(
                                "guestPasswordFieldMandatory"
                            )
                        ) R.color.strawberry else R.color.med_grey
                    )
                )
                bindings.networkStatusGuestPasswordLabel.text =
                    if (it.containsKey("guestPasswordError") && it.containsKey("guestPasswordFieldMandatory")) resources.getString(
                        R.string.guest_network_password_mandatory
                    ) else resources.getString(
                        R.string.guest_network_password
                    )
                bindings.networkStatusGuestPasswordLabel.typeface =
                    if (it.containsKey("guestPasswordError") && it.containsKey("guestPasswordFieldMandatory")) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                bindings.networkStatusGuestPasswordInput.background =
                    getDrawable(if (it.containsKey("guestPasswordError") && it.containsKey("guestPasswordFieldMandatory")) R.drawable.background_thin_border_red else R.drawable.background_thin_border)
                bindings.networkStatusGuestPasswordRestraintsLabel.setTextColor(
                    getColor(
                        if (it.containsKey("guestPasswordError") && it.containsKey("guestPasswordFieldLength"))
                            R.color.strawberry else R.color.med_grey
                    )
                )
            }
            errorSubmitValue.observe {
                if (it) {
                    showBlueThemePopUp()
                } else {
                    setResult(REQUEST_TO_HOME)
                    finish()
                }
            }

            errorMessageFlow.observe {
                displayGeneralError()
            }
        }
    }

    /**
     * Init on clicks - It will initialize the onclick listeners
     *
     */
    private fun initOnClicks() {
        // will remove once rest of the network calls are implemented
        bindings.ivPasswordVisibility.setOnClickListener { toggleNetworkTextVisibility() }
        bindings.ivGuestPasswordVisibility.setOnClickListener { toggleGuestTextVisibility() }
    }

    /**
     * Validate name and password - It is used to validate the network name and network password
     *
     */
    private fun validateNameAndPassword() {
        val errors = viewModel.validateInput()
        if (!errors.hasErrors()) {
            showAlertDialog()
        }
    }

    /**
     * Toggle network text visibility - It will handle network password visibility
     *
     */
    private fun toggleNetworkTextVisibility() {
        if (viewModel.togglePasswordVisibility()) {
            bindings.ivPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_show_password
                )
            )
            bindings.networkStatusWifiPasswordInput.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
        } else {
            bindings.ivPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_password_hide
                )
            )
            bindings.networkStatusWifiPasswordInput.transformationMethod =
                PasswordTransformationMethod.getInstance()
        }
    }

    /**
     * Toggle guest text visibility - It will handle guest network password visibility
     *
     */
    private fun toggleGuestTextVisibility() {
        if (viewModel.togglePasswordVisibility()) {
            bindings.ivGuestPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_show_password
                )
            )
            bindings.networkStatusGuestPasswordInput.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
        } else {
            bindings.ivGuestPasswordVisibility.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_password_hide
                )
            )
            bindings.networkStatusGuestPasswordInput.transformationMethod =
                PasswordTransformationMethod.getInstance()
        }
    }

    /**
     * Watch text changes - It is used to observe, text changes in network screen
     *
     */
    private fun watchTextChanges() {
        bindings.networkStatusWifiPasswordInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onWifiPasswordValueChanged(it.toString())
                bindings.networkStatusWifiPasswordInput.setSelection(bindings.networkStatusWifiPasswordInput.text.toString().length)
            }
        )
        bindings.networkStatusWifiNameInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onWifiNameValueChanged(it.toString())
                bindings.networkStatusWifiNameInput.setSelection(bindings.networkStatusWifiNameInput.text.toString().length)
            }
        )
        bindings.networkStatusGuestPasswordInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onGuestPasswordValueChanged(it.toString())
                bindings.networkStatusGuestPasswordInput.setSelection(bindings.networkStatusGuestPasswordInput.text.toString().length)
            }
        )
        bindings.networkStatusGuestNameInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onGuestNameValueChanged(it.toString())
                bindings.networkStatusGuestNameInput.setSelection(bindings.networkStatusGuestNameInput.text.toString().length)
            }
        )
    }

    /**
     * Init enable disable event clicks - It will handle enable and disable event click
     * listeners
     *
     */
    private fun initEnableDisableEventClicks() {
        bindings.networkStatusWifiButton.setOnClickListener {
            viewModel.wifiNetworkEnablement()
        }
        bindings.networkStatusGuestButton.setOnClickListener {
            viewModel.guestNetworkEnablement()
        }
    }

    /**
     * Show blue theme pop up - It shows the error dialog
     *
     */
    private fun showBlueThemePopUp() {
        CustomDialogBlueTheme(
            getString(R.string.error_title),
            getString(R.string.password_reset_error_msg),
            getString(
                R.string.discard_changes_and_close
            ),
            true,
            ::onDialogCallback
        ).show(
            supportFragmentManager,
            callingActivity?.className
        )
    }

    /**
     * On back pressed - This will handle back key click listeners
     *
     */
    override fun onBackPressed() {
        showGreyThemePopUp()
    }

    /**
     * Show grey theme pop up - It shows the alert dialog to save or discard changes
     *
     */
    private fun showGreyThemePopUp() {
        CustomDialogGreyTheme(
            getString(R.string.save_changes_msg),
            "",
            getString(R.string.save),
            getString(R.string.discard),
            ::onScreenExitConfirmationDialogCallback
        )
            .show(supportFragmentManager, NetworkStatusActivity::class.simpleName)
    }

    /**
     * On dialog callback- It will handle the dialog callback listeners
     *
     * @param buttonType - its return the which button is pressed negative or positive
     */
    private fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                viewModel.logDiscardChangesAndCloseClick()
                finish()
            }
        }
    }

    /**
     * Show alert dialog - It shows alert dialog
     *
     */
    private fun showAlertDialog() {
        CustomDialogGreyTheme(
            getString(R.string.save_changes_msg),
            "",
            getString(R.string.save),
            getString(R.string.discard),
            ::onScreenExitConfirmationDialogCallback
        ).show(
            supportFragmentManager,
            callingActivity?.className
        )
    }

    /**
     * On screen exit confirmation dialog callback- It will handle the on screen exit confirmation
     * dialog callback listeners
     *
     * @param buttonType - its return the which button is pressed negative or positive
     */
    private fun onScreenExitConfirmationDialogCallback(buttonType: Int) {
        when (buttonType) {
            // TODO - This has to be replaced with API calls
            AlertDialog.BUTTON_POSITIVE -> {
                val errors = viewModel.validateInput()
                if (!errors.hasErrors()) {
                    viewModel.onDoneClick()
                }
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                viewModel.logDiscardChangesClick()
                finish()
            }
        }
    }

    /**
     * Display general error - It shows general error dialog popup
     *
     */
    private fun displayGeneralError() {
        GeneralErrorPopUp.showGeneralErrorDialog(
            fragmentManager,
            callingActivity?.className
        )
    }

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        const val REQUEST_TO_HOME: Int = 101
        const val DEVICE_ID: String = "DEVICE_ID"
        const val REGULAR_WIFI_NAME: String = "REGULAR_WIFI_NAME"
        const val REGULAR_WIFI_PASSWORD: String = "REGULAR_WIFI_PASSWORD"
        const val GUEST_WIFI_NAME: String = "GUEST_WIFI_NAME"
        const val GUEST_WIFI_PASSWORD: String = "GUEST_WIFI_PASSWORD"
        fun newIntent(context: Context) = Intent(context, NetworkStatusActivity::class.java)
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, NetworkStatusActivity::class.java)
                .putExtra(DEVICE_ID, bundle.getString(DEVICE_ID))
                .putExtra(REGULAR_WIFI_NAME, bundle.getString(REGULAR_WIFI_NAME))
                .putExtra(REGULAR_WIFI_PASSWORD, bundle.getString(REGULAR_WIFI_PASSWORD))
                .putExtra(GUEST_WIFI_NAME, bundle.getString(GUEST_WIFI_NAME))
                .putExtra(GUEST_WIFI_PASSWORD, bundle.getString(GUEST_WIFI_PASSWORD))
        }
    }
}
