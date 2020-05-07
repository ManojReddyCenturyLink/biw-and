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
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.afterTextChanged
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInfoBinding.inflate(layoutInflater)
        personalInfoCoordinator.observeThis(personalInfoViewModel.myState)
        setContentView(binding.root)
        navigator.observe(this)
        initViews()
        initTextWatchers()
    }

    /* Disable hardware back button */
    override fun onBackPressed() {
    }

    private fun initViews() {
        val screenTitle: String = getString(R.string.personal_info)

        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                binding.errors = personalInfoViewModel.error
                binding.lifecycleOwner = this.lifecycleOwner
                val errors = personalInfoViewModel.validateInput()
                if (!errors.hasErrors()) {
                    personalInfoViewModel.updatePassword()
                    finish()
                }
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

    private fun initTextWatchers() {
        binding.personalInfoPasswordInput.addTextChangedListener(
            afterTextChanged {
                personalInfoViewModel.onPasswordValueChanged(it.toString())
                binding.personalInfoPasswordInput.setSelection(binding.personalInfoPasswordInput.text.toString().length)
            }
        )
        binding.personalInfoConfirmPasswordInput.addTextChangedListener(
            afterTextChanged {
                personalInfoViewModel.onConfirmPasswordValueChanged(it.toString())
                binding.personalInfoConfirmPasswordInput.setSelection(binding.personalInfoConfirmPasswordInput.text.toString().length)
            }
        )
        binding.personalInfoPhoneNumberInput.addTextChangedListener(
            afterTextChanged { editable ->
                val validatedString = personalInfoViewModel.onPhoneNumberChanged(editable.toString())
                binding.personalInfoPhoneNumberInput.also {
                    /** remove the watcher  so you can not capture the affectation you are going to make, to avoid infinite loop on text change  */
                    it.removeTextChangedListener(this)
                    /** set the new text to the EditText  */
                    it.setText(validatedString.toString())
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

    companion object {
        const val PASSWORD_LAYOUT = "LAYOUT_PASSWORD"
        const val CONFIRM_PASSWORD_LAYOUT = "CONFIRM_PASSWORD_LAYOUT"
        fun newIntent(context: Context): Intent {
            return Intent(context, PersonalInfoActivity::class.java)
        }
    }
}