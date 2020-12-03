package com.centurylink.biwf.screens.home.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.BuildConfig
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.PersonalInfoCoordinator
import com.centurylink.biwf.databinding.ActivityPersonalInfoBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.afterTextChanged
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import javax.inject.Inject

/**
 * Personal info activity - This class handle common methods related to personal information screen
 *
 * @constructor Create empty Personal info activity
 */
class PersonalInfoActivity : BaseActivity() {

    @Inject
    lateinit var personalInfoCoordinator: PersonalInfoCoordinator

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(PersonalInfoViewModel::class.java)
    }
    private lateinit var binding: ActivityPersonalInfoBinding
    private val fragmentManager = supportFragmentManager

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        viewModel.myState.observeWith(personalInfoCoordinator)
        setContentView(binding.root)
        navigator.observe(this)
        initViews()
        initTextWatchers()
    }

    /**
     * On back pressed - This will handle back key click listeners
     *
     */
    override fun onBackPressed() {
        showPopUp()
    }

    /**
     * Show pop up - It shows the alert dialog to save or discard changes
     *
     */
    private fun showPopUp() {
        CustomDialogGreyTheme(
            getString(R.string.save_changes_msg),
            "",
            getString(R.string.save),
            getString(R.string.discard),
            ::onScreenExitConfirmationDialogCallback
        )
            .show(fragmentManager, PersonalInfoActivity::class.simpleName)
    }

    /**
     * Init views - It initializes the views for personal activity UI screen
     *
     */
    private fun initViews() {
        val screenTitle: String = getString(R.string.personal_info)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                validateInfoAndUpdatePassword()
            }
        }
        binding.personalInfoEmailInput.text = intent.getStringExtra(USER_ID)
        val phoneNumber = formattedString(intent.getStringExtra(PHONE_NUMBER), '-', 3)
        val upadtedPhoneNumber = phoneNumber?.let { formattedString(it, '-', 7) }
        if (upadtedPhoneNumber != null) {
            viewModel.onPhoneNumberChanged(upadtedPhoneNumber)
        }
        binding.personalInfoPhoneNumberInput.setText(upadtedPhoneNumber)
        viewModel.error.observe {
            binding.mandatoryFieldsLabel.visibility =
                if (it.containsKey("fieldMandatory")) View.VISIBLE else View.GONE
            binding.personalInfoPasswordLabel.visibility =
                if (it.containsKey("passwordError")) View.GONE else View.VISIBLE
            binding.personalInfoPasswordErrorLabel.visibility =
                if (it.containsKey("passwordError")) View.VISIBLE else View.GONE
            binding.personalInfoConfirmPasswordLabel.visibility =
                if (it.containsKey("confirmPasswordError")) View.GONE else View.VISIBLE
            binding.personalInfoConfirmPasswordErrorLabel.visibility =
                if (it.containsKey("confirmPasswordError")) View.VISIBLE else View.GONE
            binding.errorPasswordDifferent.visibility =
                if (it.containsKey("passwordMismatchError")) View.VISIBLE else View.GONE
            binding.errorConfirmPasswordDifferent.visibility =
                if (it.containsKey("passwordMismatchError")) View.VISIBLE else View.GONE
            binding.phoneNumberText.visibility =
                if (it.containsKey("mobileNumberError") || it.containsKey("mobileNumberLengthError")) View.GONE else View.VISIBLE
            binding.phoneNumberErrorText.visibility =
                if (it.containsKey("mobileNumberError") || it.containsKey("mobileNumberLengthError")) View.VISIBLE else View.GONE
            binding.personalInfoPasswordInput.background =
                if (it.containsKey("passwordError") || it.containsKey("passwordLengthError")) getDrawable(R.drawable.background_thin_border_red) else getDrawable(
                    R.drawable.background_thin_border
                )
            binding.personalInfoConfirmPasswordInput.background =
                if (it.containsKey("confirmPasswordError") || it.containsKey("passwordLengthError")) getDrawable(R.drawable.background_thin_border_red) else getDrawable(
                    R.drawable.background_thin_border
                )
            binding.personalInfoPhoneNumberInput.background =
                if ((it.containsKey("mobileNumberError")) || (it.containsKey("mobileNumberLengthError"))) getDrawable(R.drawable.background_thin_border_red) else getDrawable(
                    R.drawable.background_thin_border
                )
            binding.personalInfoPhoneNumberInvalidError.visibility =
                if (it.containsKey("mobileNumberLengthError")) View.VISIBLE else View.GONE
            binding.errorPasswordLength.visibility =
                if ((it.containsKey("passwordLengthError")) && !(it.containsKey("passwordMismatchError"))) View.VISIBLE else View.GONE
        }
        viewModel.userPasswordFlow.observe {
            binding.incHeader.subheaderRightActionTitle.isEnabled = true
            if (it.isEmpty()) {
                viewModel.logResetPasswordSuccess()
                setResultToAccountFragment()
            } else {
                viewModel.logResetPasswordFailure()
                val msg = it
                if (msg.contains(getString(R.string.error_repeated_password), ignoreCase = true) ||
                    msg.contains(getString(R.string.error_invalid_password), ignoreCase = true) ||
                    msg.contains(getString(R.string.error_password_length), ignoreCase = true)
                ) {
                    CustomDialogBlueTheme(
                        getString(R.string.error_title),
                        it,
                        getString(R.string.ok),
                        true,
                        ::onDialogCallback
                    ).show(
                        fragmentManager,
                        callingActivity?.className
                    )
                } else {
                    CustomDialogBlueTheme(
                        getString(R.string.error_title),
                        getString(R.string.password_reset_error_msg),
                        getString(
                            R.string.discard_changes_and_close
                        ),
                        true,
                        ::onDialogCallbackError
                    ).show(
                        fragmentManager,
                        callingActivity?.className
                    )
                }
            }
        }
        binding.ivQuestion.setOnClickListener {
            viewModel.logUpdateEmailPopupClick()
            CustomDialogBlueTheme(
                getString(R.string.how_do_i_change_my_email),
                getString(R.string.personal_info_popup_msg, BuildConfig.MOBILE_NUMBER),
                getString(R.string.ok),
                false,
                ::onDialogCallback,
                true
            ).show(fragmentManager, callingActivity?.className)
        }
        binding.ivPasswordVisibility.setOnClickListener {
            toggleTextVisibility(
                viewModel.togglePasswordVisibility(),
                PASSWORD_LAYOUT
            )
        }
        binding.ivConfirmPasswordVisibility.setOnClickListener {
            toggleTextVisibility(
                viewModel.toggleConfirmPasswordVisibility(),
                CONFIRM_PASSWORD_LAYOUT
            )
        }
    }

    /**
     * Validate info and update password  - It is used to validate personal info screen information
     * and update personal account password accordingly
     *
     */
    private fun validateInfoAndUpdatePassword() {
        val errors = viewModel.validateInput()
        if (!errors.hasErrors()) {
            binding.incHeader.subheaderRightActionTitle.isEnabled = false
            viewModel.callUpdatePasswordApi()
        }
    }

    /**
     * Init text watchers - It is used to initialize text change listeners
     *
     */
    private fun initTextWatchers() {
        binding.personalInfoPasswordInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onPasswordValueChanged(it.toString())
                binding.personalInfoPasswordInput.setSelection(binding.personalInfoPasswordInput.text.toString().length)
            }
        )
        binding.personalInfoConfirmPasswordInput.addTextChangedListener(
            afterTextChanged {
                viewModel.onConfirmPasswordValueChanged(it.toString())
                binding.personalInfoConfirmPasswordInput.setSelection(binding.personalInfoConfirmPasswordInput.text.toString().length)
            }
        )
        binding.personalInfoPhoneNumberInput.addTextChangedListener(
            afterTextChanged { editable ->
                val validatedString =
                    viewModel.onPhoneNumberChanged(editable.toString())
                binding.personalInfoPhoneNumberInput.also {
                    /** remove the watcher  so you can not capture the affectation you are going to make, to avoid infinite loop on text change  */
                    it.removeTextChangedListener(this)
                    /** set the new text to the EditText  */
                    it.setText(validatedString)
                    /** bring the cursor to the end of input  */
                    it.setSelection(binding.personalInfoPhoneNumberInput.text.toString().length)
                    /* bring back the watcher and go on listening to change events */
                    it.addTextChangedListener(this)
                }
            }
        )
    }

    /**
     * Toggle text visibility - It will handle personal info password visibility
     *
     * @param togglePasswordVisibility - The boolean value for toggling password visibility
     * Its true to show password and false to hide password
     * @param layout - returns the selected layout
     */
    private fun toggleTextVisibility(togglePasswordVisibility: Boolean, layout: String) {
        if (togglePasswordVisibility) {
            if (layout == PASSWORD_LAYOUT) {
                binding.personalInfoPasswordInput.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.ivPasswordVisibility.setImageResource(R.drawable.ic_show_password)
            } else {
                binding.personalInfoConfirmPasswordInput.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.ivConfirmPasswordVisibility.setImageResource(R.drawable.ic_show_password)
            }
        } else {
            if (layout == PASSWORD_LAYOUT) {
                binding.personalInfoPasswordInput.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.ivPasswordVisibility.setImageResource(R.drawable.ic_password_hide)
            } else {
                binding.personalInfoConfirmPasswordInput.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.ivConfirmPasswordVisibility.setImageResource(R.drawable.ic_password_hide)
            }
        }
    }

    /**
     * On dialog callback - It will handle the dialog callback listeners
     *
     * @param buttonType - It returns the which button type pressed positive or negative
     */
    private fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                /** no-op **/
            }
        }
    }

    /**
     * On dialog callback - It will handle the dialog callback listeners
     *
     * @param buttonType - It returns the which button type pressed positive or negative
     */
    private fun onDialogCallbackError(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                finish()
            }
        }
    }

    /**
     * On screen exit confirmation dialog callback - It will handle the on screen exit confirmation
     * dialog callback listeners
     *
     * @param buttonType - It returns the which button type pressed positive or negative
     */
    private fun onScreenExitConfirmationDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                validateInfoAndUpdatePassword()
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                finish()
            }
        }
    }

    /**
     * Set result to account fragment - This method is used to update incoming changes in personal
     * info screen to account screen
     *
     */
    private fun setResultToAccountFragment() {
        val resultIntent = Intent()
        resultIntent.putExtra(PHONE_NUMBER, binding.personalInfoPhoneNumberInput.text.toString())
        setResult(REQUEST_TO_ACCOUNT_FROM_PERSONAL_INFO, resultIntent)
        finish()
    }

    /**
     * Formatted string - This method is used to format phone number string
     *
     * @param str - The  string to be formatted
     * @param ch - The character used to format string
     * @param position - The position where the character is added to format string
     * @return - returns formatted string
     */
    private fun formattedString(str: String, ch: Char, position: Int): String? {
        if (str.isNotEmpty())
            return (str.substring(0, position) + ch + str.substring(position))
        else
            return ""
    }

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        const val PASSWORD_LAYOUT = "LAYOUT_PASSWORD"
        const val CONFIRM_PASSWORD_LAYOUT = "CONFIRM_PASSWORD_LAYOUT"
        const val USER_ID = "USER_ID"
        const val PHONE_NUMBER = "PHONE_NUMBER"
        const val REQUEST_TO_ACCOUNT_FROM_PERSONAL_INFO: Int = 1101
        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, PersonalInfoActivity::class.java)
                .putExtra(USER_ID, bundle.getString(USER_ID))
                .putExtra(PHONE_NUMBER, bundle.getString(PHONE_NUMBER))
        }
    }
}
