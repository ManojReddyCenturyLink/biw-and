package com.centurylink.biwf.screens.support.schedulecallback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.ContactInfoCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityContactInfoBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.afterTextChanged
import com.google.firebase.crashlytics.internal.common.CommonUtils.hideKeyboard
import javax.inject.Inject

class ContactInfoActivity: BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var contactInfoCoordinator: ContactInfoCoordinator

    @Inject
    lateinit var navigator: Navigator

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(ContactInfoViewModel::class.java)
    }
    private lateinit var binding: ActivityContactInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactInfoBinding.inflate(layoutInflater)
        navigator.observe(this)
        viewModel.myState.observeWith(contactInfoCoordinator)
        setContentView(binding.root)
        initHeaders()
        initOnClicks()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.contact_info)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                viewModel.logBackButtonClick()
                finish()
            }
            subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logCancelButtonClick()
                setResult(Activity.RESULT_OK)
                    finish()
            }
        }

        viewModel.error.observe {
            binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.background =
                if (it.containsKey("mobileNumberError")) getDrawable(R.drawable.ic_selected_error) else getDrawable(R.drawable.ic_selected)
            binding.contactInfoExistingUser.errorValidPhoneNumber.visibility =
                if (it.containsKey("mobileNumberError")) View.VISIBLE else View.GONE
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.background =
                if (it.containsKey("mobileNumberError")) getDrawable(R.drawable.background_thin_border_red) else getDrawable(
                    R.drawable.background_thin_border
                )
            binding.contactInfoExistingUser.contactInfoWithOutPhoneNumberInput.background =
                if (it.containsKey("mobileNumberError")) getDrawable(R.drawable.background_thin_border_red) else getDrawable(
                    R.drawable.background_thin_border
                )
        }
        val editText1 =  binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput
        val editText2 =  binding.contactInfoExistingUser.contactInfoWithOutPhoneNumberInput
       formatPhoneNumber(editText1)
       formatPhoneNumber(editText2)
    }

    private fun formatPhoneNumber(edit : EditText)
    {
        edit.addTextChangedListener(
        afterTextChanged { editable ->
            val validatedString =
                viewModel.onPhoneNumberChanged(editable.toString())
                edit.also {
                /** remove the watcher  so you can not capture the affectation you are going to make, to avoid infinite loop on text change  */
                /** remove the watcher  so you can not capture the affectation you are going to make, to avoid infinite loop on text change  */
                it.removeTextChangedListener(this)
                /** set the new text to the EditText  */
                /** set the new text to the EditText  */
                it.setText(validatedString)
                /** bring the cursor to the end of input  */
                /** bring the cursor to the end of input  */
                it.setSelection( edit.text.toString().length)
                /* bring back the watcher and go on listening to change events */
                it.addTextChangedListener(this)
            }
        }
        )
    }

    private fun initOnClicks() {
        binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumber.setOnClickListener {
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.isEnabled=false
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.text=null
            binding.contactInfoExistingUser.errorValidPhoneNumber.visibility = View.GONE
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.background=getDrawable(R.drawable.background_thin_border)
            binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.background = getDrawable(R.drawable.ic_radio_empty)
        }
        binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.setOnClickListener {
            binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.background = getDrawable(R.drawable.ic_selected)
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.isEnabled=true
        }
        binding.contactInfoNextBtn.setOnClickListener {
            if(binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.isChecked || binding.contactInfoExistingUser.contactInfoWithOutPhoneNumberInput.text.isNullOrEmpty())
                validatePhoneNUmber()
            else
            viewModel.launchSelectTime()
        }
    }

    private fun validatePhoneNUmber() {
        val errors = viewModel.validateInput()
        if (!errors.hasErrors()) {
            viewModel.launchSelectTime()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TO_HOME -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
                }
            }
        }
    }

    companion object {
        const val CONTACT_INFO: String = "ContactInfo"
        const val REQUEST_TO_HOME: Int = 1100

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, ContactInfoActivity::class.java)
                .putExtra(CONTACT_INFO, bundle.getString(CONTACT_INFO))
        }
    }
}