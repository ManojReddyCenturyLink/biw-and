package com.centurylink.biwf.screens.home.account

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.PersonalInfoCoordinator
import com.centurylink.biwf.databinding.ActivityPersonalInfoBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class PersonalInfoActivity : BaseActivity() {

    @Inject
    lateinit var personalInfoCoordinator: PersonalInfoCoordinator

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
        initViews()
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onResume() {
        super.onResume()
        personalInfoCoordinator.getNavigator().activity = this
    }

    private fun initViews() {
        val screenTitle: String = getString(R.string.personal_info)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.visibility = View.GONE
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener {
                validateFields()
            }
        }
        binding.layoutEmail.apply {
            tvLabel.text = resources.getString(R.string.email)
            tvErrorLabel.visibility = View.GONE
            edittextInput.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            etIcon.setImageResource(R.drawable.ic_question)
            etIcon.setOnClickListener {
                showPopup()
            }
        }
        binding.layoutPassword.apply {
            tvLabel.text = resources.getString(R.string.password)
            tvErrorLabel.text = resources.getString(R.string.password_mandatory)
            tvErrorLabel.visibility = View.GONE
            etIcon.setImageResource(R.drawable.ic_password_hide)
            etIcon.setOnClickListener {
                toggleTextVisibility()
            }
        }
        binding.layoutConfirmPassword.apply {
            tvLabel.text = resources.getString(R.string.confirm_password)
            tvErrorLabel.text = resources.getString(R.string.confirm_password_mandatory)
            tvErrorLabel.visibility = View.GONE
            etIcon.setImageResource(R.drawable.ic_password_hide)
            etIcon.setOnClickListener {
                toggleTextVisibility()
            }
        }
    }

    private fun showPopup() {
        //todo
    }

    private fun toggleTextVisibility() {
        //todo
    }

    private fun validateFields() {

        if(binding.layoutPassword.edittextInput.text.isNullOrEmpty()){
            binding.mandatoryFieldsLabel.visibility = View.VISIBLE
            binding.layoutPassword.tvErrorLabel.visibility = View.VISIBLE
            binding.layoutPassword.tvLabel.visibility = View.GONE
            binding.layoutPassword.edittextInput.background = getDrawable(R.drawable.background_thin_border_red)
        }
        if(binding.layoutConfirmPassword.edittextInput.text.isNullOrEmpty()){
            binding.mandatoryFieldsLabel.visibility = View.VISIBLE
            binding.layoutConfirmPassword.tvErrorLabel.visibility = View.VISIBLE
            binding.layoutConfirmPassword.tvLabel.visibility = View.GONE
            binding.layoutConfirmPassword.edittextInput.background = getDrawable(R.drawable.background_thin_border_red)
        }
        if(binding.layoutConfirmPassword.edittextInput.text != binding.layoutPassword.edittextInput.text){
            binding.mandatoryFieldsLabel.visibility = View.VISIBLE
            binding.layoutPassword.tvErrorLabel.visibility = View.VISIBLE
            binding.layoutPassword.tvLabel.visibility = View.GONE
            binding.layoutPassword.edittextInput.background = getDrawable(R.drawable.background_thin_border_red)

            binding.layoutConfirmPassword.tvErrorLabel.visibility = View.VISIBLE
            binding.layoutConfirmPassword.tvLabel.visibility = View.GONE
            binding.layoutConfirmPassword.edittextInput.background = getDrawable(R.drawable.background_thin_border_red)

            binding.layoutPassword.tvValidationError.visibility = View.VISIBLE
            binding.layoutConfirmPassword.tvValidationError.visibility = View.VISIBLE
        }
        if(binding.personalInfoPhoneNumberInput.text.isNullOrEmpty()){
            binding.mandatoryFieldsLabel.visibility = View.VISIBLE
            binding.phoneNumberErrorText.visibility = View.VISIBLE
            binding.phoneNumberText.visibility = View.INVISIBLE
            binding.personalInfoPhoneNumberInput.background = getDrawable(R.drawable.background_thin_border_red)
        }
        else{
            personalInfoViewModel.updatePassword()
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PersonalInfoActivity::class.java)
        }
    }
}