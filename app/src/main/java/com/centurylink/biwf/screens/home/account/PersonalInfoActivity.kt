package com.centurylink.biwf.screens.home.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.PersonalInfoCoordinator
import com.centurylink.biwf.databinding.ActivityPersonalInfoBinding
import com.centurylink.biwf.model.UserDetails
import com.centurylink.biwf.utility.DaggerViewModelFactory
import kotlinx.android.synthetic.main.widget_info_popup.view.*
import javax.inject.Inject

class PersonalInfoActivity : BaseActivity() {

    @Inject
    lateinit var personalInfoCoordinator: PersonalInfoCoordinator
    @Inject
    lateinit var navigator: Navigator
    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val personalInfoViewModel by lazy {
        ViewModelProvider(this, factory).get(PersonalInfoViewModel::class.java)
    }
    private lateinit var binding: ActivityPersonalInfoBinding
    private var userDetails: UserDetails? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        personalInfoCoordinator.observeThis(personalInfoViewModel.myState)
        setContentView(binding.root)
        navigator.observe(this)
        initViews()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initViews() {
        val screenTitle: String = getString(R.string.personal_info)
        userDetails?.password  = binding.personalInfoPasswordInput.text.toString()
        userDetails?.confirmPassword  = binding.personalInfoConfirmPasswordInput.text.toString()
        userDetails?.mobileNumber = binding.personalInfoPhoneNumberInput.text.toString()
        //val errors = personalInfoViewModel.validateInput(userDetails)

        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
//                if (errors.hasErrors()) {
//                    validateFields()
//                } else {
//                    personalInfoViewModel.updatePassword()
//                    finish()
//                }
            }
        }
        binding.ivQuestion.setOnClickListener {
            showPopup()
        }
        binding.ivPasswordVisibility.setOnClickListener {
            toggleTextVisibility(
                personalInfoViewModel.togglePasswordVisibility(),
                PASSWORD_LAYOUT
            )
        }
        binding.ivConfirmPasswordVisibility.setOnClickListener {
            toggleTextVisibility(
                personalInfoViewModel.toggleConfirmPasswordVisibility(),
                CONFIRM_PASSWORD_LAYOUT
            )
        }
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

    private fun showPopup() {
        val builder = AlertDialog.Builder(this)
        val viewGroup = null
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.widget_info_popup,
            viewGroup, false
        )
        builder.setView(dialogView)
        val alertDialog = builder.create()
        dialogView.popup_cancel_btn.setOnClickListener { alertDialog.dismiss() }
        dialogView.popup_ok_button.setOnClickListener { alertDialog.dismiss() }
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()
    }

    private fun validateFields() {
        var checks = personalInfoViewModel.error
        when {
            checks.value?.get("mandatoryFieldError")!! -> {
                binding.mandatoryFieldsLabel.visibility = View.VISIBLE
            }
            checks.value?.get("mobileNumberError")!! -> {
                binding.personalInfoPhoneNumberInput.background =
                    getDrawable(R.drawable.background_thin_border_red)
                binding.phoneNumberErrorText.visibility = View.VISIBLE
                binding.phoneNumberText.visibility = View.GONE
            }
            checks.value?.get("passwordError")!! || checks.value?.get("passwordMismatchError")!! -> {
                binding.personalInfoPasswordInput.background =
                    getDrawable(R.drawable.background_thin_border_red)
                binding.personalInfoPasswordErrorLabel.visibility = View.VISIBLE
                binding.personalInfoPasswordLabel.visibility = View.GONE
            }
            checks.value?.get("confirmPasswordError")!! || checks.value?.get("passwordMismatchError")!! -> {
                binding.personalInfoConfirmPasswordInput.background =
                    getDrawable(R.drawable.background_thin_border_red)
                binding.personalInfoConfirmPasswordErrorLabel.visibility = View.VISIBLE
                binding.personalInfoConfirmPasswordLabel.visibility = View.GONE
            }
            checks.value?.get("passwordMismatchError")!! -> {
                binding.errorPasswordDifferent.visibility = View.VISIBLE
                binding.errorConfirmPasswordDifferent.visibility = View.VISIBLE
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
}