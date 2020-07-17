package com.centurylink.biwf.screens.home.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
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

class PersonalInfoActivity : BaseActivity(), CustomDialogGreyTheme.DialogCallback,
    CustomDialogBlueTheme.ErrorDialogCallback {

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        viewModel.myState.observeWith(personalInfoCoordinator)
        setContentView(binding.root)
        navigator.observe(this)
        initViews()
        initTextWatchers()
    }

    override fun onBackPressed() {
        showPopUp()
    }

    private fun showPopUp() {
        CustomDialogGreyTheme(
            getString(R.string.save_changes_msg), "", getString(R.string.save), getString(R.string.discard))
            .show(fragmentManager, PersonalInfoActivity::class.simpleName)
    }

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
        binding.errors = viewModel.error
        binding.lifecycleOwner = this.lifecycleOwner
        viewModel.userPasswordFlow.observe {
            if (it.isEmpty()) {
                finish()
            } else {
                val msg = it
                if (msg.contains(getString(R.string.error_repeated_password), ignoreCase = true) ||
                    msg.contains(getString(R.string.error_invalid_password), ignoreCase = true) ||
                    msg.contains(getString(R.string.error_password_length), ignoreCase = true)
                ) {
                    CustomDialogBlueTheme(
                        getString(R.string.error_title),
                        it,
                        getString(R.string.discard_changes_and_close),
                        true
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
                        true
                    ).show(
                        fragmentManager,
                        callingActivity?.className
                    )
                }
            }
        }
        binding.ivQuestion.setOnClickListener {
            CustomDialogBlueTheme(
                getString(R.string.how_do_i_change_my_email),
                getString(R.string.personal_info_popup_msg),
                getString(R.string.ok_lowercase),
                false
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

    private fun validateInfoAndUpdatePassword() {
        val errors = viewModel.validateInput()
        if (!errors.hasErrors()) {
            viewModel.callUpdatePasswordApi()

        }
    }

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

    companion object {
        const val PASSWORD_LAYOUT = "LAYOUT_PASSWORD"
        const val CONFIRM_PASSWORD_LAYOUT = "CONFIRM_PASSWORD_LAYOUT"
        fun newIntent(context: Context): Intent {
            return Intent(context, PersonalInfoActivity::class.java)
        }
    }

    override fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                validateInfoAndUpdatePassword()
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                finish()
            }
        }
    }

    override fun onErrorDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                finish()
            }
        }
    }
}
