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
        initViews()
        initOnClicks()
        initTextWatchers()
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
                bindings.networkStatusWifiButton.isActivated = it.isActive
                bindings.networkStatusWifiButtonText.isActivated = it.isActive
                bindings.networkStatusWifiImage.isActivated = it.isActive
                bindings.networkStatusWifiButtonText.text = getString(it.wifiNetworkButtonText)
                bindings.networkStatusWifiButtonActionText.text = getString(it.wifiButtonSubText)
                bindings.networkStatusGuestButton.isActivated = it.isActive
                bindings.networkStatusGuestButtonText.isActivated = it.isActive
                bindings.networkStatusGuestWifiImage.isActivated = it.isActive
                bindings.networkStatusGuestButtonText.text = getString(it.guestNetworkButtonText)
                bindings.networkStatusGuestButtonActionText.text = getString(it.guestNetworkButtonSubText)
            }
        }
        bindings.networkStatusWifiNameInput.setText(intent.getStringExtra(NETWORK_NAME))
    }

    private fun initOnClicks() {
        // will remove once rest of the network calls are implemented
        bindings.networkStatusDoneButton.setOnClickListener { validateNameAndPassword() }
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

    private fun initTextWatchers() {
        bindings.networkStatusWifiPasswordInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onPasswordValueChanged(it.toString())
                val passwordLength = bindings.networkStatusWifiPasswordInput.text.toString().length
                if (passwordLength == 0) {
                    bindings.fieldsMarkedRequiredWifiPassword.visibility = View.VISIBLE
                    bindings.networkStatusWifiPasswordLabel.text =
                        resources.getString(R.string.network_password_mandatory)
                    bindings.networkStatusWifiPasswordInput.background =
                        resources.getDrawable(R.drawable.background_thin_border_red)
                    bindings.networkStatusWifiPasswordLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.offline_red
                        )
                    )
                    bindings.networkStatusWifiPasswordLabel.typeface = Typeface.DEFAULT_BOLD
                    bindings.networkStatusWifiPasswordRestraintsLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.offline_red
                        )
                    )
                } else if (passwordLength < 8 || passwordLength > 63) {
                    bindings.fieldsMarkedRequiredWifiPassword.visibility = View.GONE
                    bindings.networkStatusWifiPasswordLabel.text =
                        resources.getString(R.string.network_password)
                    bindings.networkStatusWifiPasswordInput.background =
                        resources.getDrawable(R.drawable.background_thin_border)
                    bindings.networkStatusWifiPasswordLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                    bindings.networkStatusWifiPasswordLabel.typeface = Typeface.DEFAULT
                    bindings.networkStatusWifiPasswordRestraintsLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.offline_red
                        )
                    )
                } else if (passwordLength in 8..63) {
                    bindings.fieldsMarkedRequiredWifiPassword.visibility = View.GONE
                    bindings.networkStatusWifiPasswordLabel.text =
                        resources.getString(R.string.network_password)
                    bindings.networkStatusWifiPasswordInput.background =
                        resources.getDrawable(R.drawable.background_thin_border)
                    bindings.networkStatusWifiPasswordLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                    bindings.networkStatusWifiPasswordLabel.typeface = Typeface.DEFAULT
                    bindings.networkStatusWifiPasswordRestraintsLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                }
            }
        )
        bindings.networkStatusWifiNameInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onNameValueChanged(it.toString())
                val nameLength = bindings.networkStatusWifiNameInput.text.toString().length
                if (nameLength == 0) {
                    bindings.fieldsMarkedRequiredWifiName.visibility = View.VISIBLE
                    bindings.networkStatusWifiNameLabel.text =
                        resources.getString(R.string.network_name_mandatory)
                    bindings.networkStatusWifiNameLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.offline_red
                        )
                    )
                    bindings.networkStatusWifiNameInput.background =
                        resources.getDrawable(R.drawable.background_thin_border_red)
                    bindings.networkStatusWifiNameLabel.typeface = Typeface.DEFAULT_BOLD
                } else {
                    bindings.fieldsMarkedRequiredWifiName.visibility = View.GONE
                    bindings.networkStatusWifiNameLabel.text =
                        resources.getString(R.string.network_name)
                    bindings.networkStatusWifiNameInput.background =
                        resources.getDrawable(R.drawable.background_thin_border)
                    bindings.networkStatusWifiNameLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                    bindings.networkStatusWifiNameLabel.typeface = Typeface.DEFAULT
                    bindings.networkStatusWifiNameLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                }
            }
        )
        bindings.networkStatusGuestPasswordInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onPasswordValueChanged(it.toString())
                val passwordLength = bindings.networkStatusGuestPasswordInput.text.toString().length
                if (passwordLength == 0) {
                    bindings.fieldsMarkedRequiredGuestPassword.visibility = View.VISIBLE
                    bindings.networkStatusGuestPasswordLabel.text =
                        resources.getString(R.string.guest_network_password_mandatory)
                    bindings.networkStatusGuestPasswordInput.background =
                        resources.getDrawable(R.drawable.background_thin_border_red)
                    bindings.networkStatusGuestPasswordLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.offline_red
                        )
                    )
                    bindings.networkStatusGuestPasswordLabel.typeface = Typeface.DEFAULT_BOLD
                    bindings.networkStatusGuestPasswordRestraintsLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.offline_red
                        )
                    )
                } else if (passwordLength < 8 || passwordLength > 63) {
                    bindings.fieldsMarkedRequiredGuestPassword.visibility = View.GONE
                    bindings.networkStatusGuestPasswordLabel.text =
                        resources.getString(R.string.guest_network_password)
                    bindings.networkStatusGuestPasswordInput.background =
                        resources.getDrawable(R.drawable.background_thin_border)
                    bindings.networkStatusGuestPasswordLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                    bindings.networkStatusGuestPasswordLabel.typeface = Typeface.DEFAULT
                    bindings.networkStatusGuestPasswordRestraintsLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.offline_red
                        )
                    )
                } else if (passwordLength in 8..63) {
                    bindings.fieldsMarkedRequiredGuestPassword.visibility = View.GONE
                    bindings.networkStatusGuestPasswordLabel.text =
                        resources.getString(R.string.guest_network_password)
                    bindings.networkStatusGuestPasswordInput.background =
                        resources.getDrawable(R.drawable.background_thin_border)
                    bindings.networkStatusGuestPasswordLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                    bindings.networkStatusGuestPasswordLabel.typeface = Typeface.DEFAULT
                    bindings.networkStatusGuestPasswordRestraintsLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                }
            }
        )
        bindings.networkStatusGuestNameInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onNameValueChanged(it.toString())
                val nameLength = bindings.networkStatusGuestNameInput.text.toString().length
                if (nameLength == 0) {
                    bindings.fieldsMarkedRequiredGuestName.visibility = View.VISIBLE
                    bindings.networkStatusGuestNameLabel.text =
                        resources.getString(R.string.guest_network_name_mandatory)
                    bindings.networkStatusGuestNameLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.offline_red
                        )
                    )
                    bindings.networkStatusGuestNameInput.background =
                        resources.getDrawable(R.drawable.background_thin_border_red)
                    bindings.networkStatusGuestNameLabel.typeface = Typeface.DEFAULT_BOLD
                } else {
                    bindings.fieldsMarkedRequiredGuestName.visibility = View.GONE
                    bindings.networkStatusGuestNameLabel.text =
                        resources.getString(R.string.guest_network_name)
                    bindings.networkStatusGuestNameInput.background =
                        resources.getDrawable(R.drawable.background_thin_border)
                    bindings.networkStatusGuestNameLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                    bindings.networkStatusGuestNameLabel.typeface = Typeface.DEFAULT
                    bindings.networkStatusGuestNameLabel.setTextColor(
                        ContextCompat.getColor(
                            applicationContext,
                            R.color.font_color_medium_grey
                        )
                    )
                }
            }
        )
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
            }
            AlertDialog.BUTTON_NEGATIVE -> {
            }
        }
    }

    companion object {
        const val NETWORK_NAME: String = "NETWORK_NAME"
        fun newIntent(context: Context) = Intent(context, NetworkStatusActivity::class.java)
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, NetworkStatusActivity::class.java)
                .putExtra(NETWORK_NAME, bundle.getString(NETWORK_NAME))
        }
    }
}