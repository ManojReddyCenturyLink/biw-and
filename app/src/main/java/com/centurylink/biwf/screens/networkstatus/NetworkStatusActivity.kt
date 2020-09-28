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
import javax.inject.Inject

class NetworkStatusActivity : BaseActivity() {
    private lateinit var bindings: ActivityNetworkStatusBinding

    @Inject
    lateinit var factory: DaggerViewModelFactory

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(NetworkStatusViewModel::class.java)
    }

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

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.network_status)
        bindings.incHeader.apply {
            subHeaderLeftIcon.visibility = View.GONE
            subheaderCenterTitle.text = screenTitle
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                if(viewModel.isApiComplete) {
                  validateNameAndPassword()
                }
            }
        }
    }

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
                bindings.networkStatusWifiNameInput.setText(it.netWorkName)
                bindings.networkStatusWifiPasswordInput.isEnabled = it.isNetworkEnabled
                bindings.networkStatusWifiPasswordInput.setText(it.networkPassword)
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
                bindings.networkStatusGuestNameInput.setText(it.netWorkName)
                bindings.networkStatusGuestNameInput.isEnabled = it.isNetworkEnabled
                bindings.networkStatusGuestPasswordInput.setText(it.networkPassword)
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
        }
    }

    private fun initOnClicks() {
        // will remove once rest of the network calls are implemented
        bindings.ivPasswordVisibility.setOnClickListener { toggleNetworkTextVisibility() }
        bindings.ivGuestPasswordVisibility.setOnClickListener { toggleGuestTextVisibility() }
    }

    private fun validateNameAndPassword() {
        val errors = viewModel.validateInput()
        if (!errors.hasErrors()) {
            showAlertDialog()
        }
    }

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

    private fun initEnableDisableEventClicks() {
        bindings.networkStatusWifiButton.setOnClickListener {
            viewModel.wifiNetworkEnablement()
        }
        bindings.networkStatusGuestButton.setOnClickListener {
            viewModel.guestNetworkEnablement()
        }
    }

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

    override fun onBackPressed() {
        showGreyThemePopUp()
    }

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

    private fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                viewModel.logDiscardChangesAndCloseClick()
                finish()
            }
        }
    }

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

    companion object {
        const val NETWORK_NAME: String = "NETWORK_NAME"
        const val REQUEST_TO_HOME: Int = 101
        fun newIntent(context: Context) = Intent(context, NetworkStatusActivity::class.java)
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, NetworkStatusActivity::class.java)
                .putExtra(NETWORK_NAME, bundle.getString(NETWORK_NAME))
        }
    }
}