package com.centurylink.biwf.screens.support.schedulecallback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.ContactInfoCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityContactInfoBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.afterTextChanged
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import javax.inject.Inject

/**
 * Contact info activity - This class handle common methods related to contact screen
 *
 * @constructor Create empty Contact info activity
 */
class ContactInfoActivity : BaseActivity() {

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
    private lateinit var customerCareOption: String
    private lateinit var additionalInfo: String
    private lateinit var phoneNumber: String

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactInfoBinding.inflate(layoutInflater)
        navigator.observe(this)
        viewModel.myState.observeWith(contactInfoCoordinator)
        setContentView(binding.root)
        initViews()
        initHeaders()
        initOnClicks()
    }

    /**
     * On back pressed - This will handle back key click listeners
     *
     */
    override fun onBackPressed() {
        finish()
    }

    /**
     * Retry clicked - This will handle retry click listener
     *
     */
    override fun retryClicked() {
        showProgress(true)
        viewModel.initContactApiCall()
    }

    /**
     * Init views - It will initializes the views
     *
     */
    private fun initViews() {
        customerCareOption = intent.getStringExtra(CUSTOMER_CARE_OPTION)
        additionalInfo = intent.getStringExtra(ADDITIONAL_INFO)
        viewModel.accountDetailsInfo.observe {
            binding.contactInfoExistingUser.contactInfoPhoneNumber.text =
                viewModel.uiAccountDetails.cellPhone?.let {
                    formattedString(it)
                }
            }
        viewModel.progressViewFlow.observe { showProgress(it) }
        viewModel.errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.constraintLayout,
            binding.retryOverlay.root
        )
        val isExistingUser = intent.getBooleanExtra(IS_EXISTING_USER, false)
        if (isExistingUser) {
            viewModel.isExistingUserWithPhoneNumberState.observe {
                binding.contactInfoExistingUser.layoutContactInfoExistingUserWithPhoneNumber.visibility =
                    if (it) View.VISIBLE else View.GONE
                binding.contactInfoExistingUser.layoutContactInfoExistingUserWithoutPhoneNumber.visibility =
                    if (it) View.GONE else View.VISIBLE

                if (it) {
                    formatPhoneNumber(binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput)
                } else {
                    formatPhoneNumber(binding.contactInfoExistingUser.contactInfoWithOutPhoneNumberInput)
                }
            }
        } else {
            binding.contactNewUser.root.isVisible = true
            binding.contactInfoExistingUser.root.isVisible = false
            formatPhoneNumber(binding.contactNewUser.contactInfoPhoneNumberInput)
        }
    }

    /**
     * Formatted string - This will format string if it's not empty
     *
     * @param str - string to be formatted
     * @return - returns formatted string
     */
    private fun formattedString(str: String): String? {
        return if (str.isNotEmpty()) {
            val substring1 = str.substring(1, 4)
            val substring2 = str.substring(6)
            substring1.plus("-").plus(substring2)
        } else
            ""
    }

    /**
     * Init headers - It will initialize screen headers
     *
     */
    private fun initHeaders() {
        val screenTitle: String = getString(R.string.contact_info)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                finish()
            }
            subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
            subheaderRightActionTitle.setOnClickListener {
                setResult(Activity.RESULT_OK)
                    finish()
            }
        }

        viewModel.error.observe {
            val isExistingUser = intent.getBooleanExtra(IS_EXISTING_USER, false)
            if (isExistingUser) {
                binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.background =
                    if (it.containsKey("mobileNumberError")) getDrawable(R.drawable.ic_selected_error) else getDrawable(
                        R.drawable.ic_selected
                    )
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
            } else {
                binding.contactNewUser.errorValidPhoneNumberNewUser.visibility =
                    if (it.containsKey("mobileNumberError")) View.VISIBLE else View.GONE
                binding.contactNewUser.contactInfoPhoneNumberInput.background =
                    if (it.containsKey("mobileNumberError")) getDrawable(R.drawable.background_thin_border_red) else getDrawable(
                        R.drawable.background_thin_border
                    )
            }
        }
    }

    /**
     * Format phone number - This is used to format the phone number
     *
     * @param edit - The editview to be formatted
     */
    private fun formatPhoneNumber(edit: EditText) {
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
                    it.setSelection(edit.text.toString().length)
                    /* bring back the watcher and go on listening to change events */
                    it.addTextChangedListener(this)
                }
            }
        )
    }

    /**
     * Init on clicks - It will initializes click listeners
     *
     */
    private fun initOnClicks() {
        binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumber.setOnClickListener {
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.isEnabled = false
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.text = null
            binding.contactInfoExistingUser.errorValidPhoneNumber.visibility = View.GONE
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.background =
                getDrawable(R.drawable.background_thin_border)
            binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.background =
                getDrawable(R.drawable.ic_radio_empty)
        }
        binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.setOnClickListener {
            binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.background =
                getDrawable(R.drawable.ic_selected)
            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.isEnabled = true
        }
        binding.contactInfoNextBtn.setOnClickListener {
            val isExistingUser = intent.getBooleanExtra(IS_EXISTING_USER, false)
            if (isExistingUser) {
                if (viewModel.isExistingUserWithPhoneNumber) {
                    // existing user with phone number
                    if (binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumberInput.isChecked) {
                        // existing user with user input phone number
                        phoneNumber =
                            binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.text.toString()
                        validatePhoneNumber()
                    } else if (binding.contactInfoExistingUser.contactInfoSelectRadioBtnPhoneNumber.isChecked) {
                        // existing user with default phone number
                        phoneNumber =
                            binding.contactInfoExistingUser.contactInfoPhoneNumber.text.toString()
                        viewModel.launchSelectTime(customerCareOption, additionalInfo, phoneNumber)
                    }
                } else if (!viewModel.isExistingUserWithPhoneNumber) {
                    // existing user without phone number
                    phoneNumber =
                        binding.contactInfoExistingUser.contactInfoWithOutPhoneNumberInput.text.toString()
                    validatePhoneNumber()
                }
            } else {
                // non existing user
                phoneNumber = binding.contactNewUser.contactInfoPhoneNumberInput.text.toString()
                validatePhoneNumber()
            }
        }
        binding.contactNewUser.ivQuestion.setOnClickListener {
            CustomDialogBlueTheme(
                getString(R.string.email_question_popup_heading),
                getString(R.string.email_question_popup_text),
                getString(
                    R.string.ok
                ),
                true,
                ::onErrorDialogCallback
            ).show(
                supportFragmentManager,
                callingActivity?.className
            )
        }
    }

    /**
     * On error dialog callback - It will handle the error dialog callback listeners
     *
     * @param buttonType - It returns which button is pressed negative or positive
     */
    private fun onErrorDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
            }
        }
    }

    /**
     * Validate phone number - It is used to validate error states
     *
     */
    private fun validatePhoneNumber() {
        val errors = viewModel.validateInput()
        if (!errors.hasErrors()) {
            viewModel.launchSelectTime(customerCareOption, additionalInfo, phoneNumber)
        }
    }

    /**
     * On activity result - Called when an activity you launched exits, giving you the requestCode
     * you started it with, the resultCode it returned and any additional data from it.
     *
     * @param requestCode - It is originally supplied to startActivityForResult(), allowing
     * to identify result code came from.
     * @param resultCode - It is returned by the child activity through its setResult().
     * @param data - It will return result data to the caller activity.
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TO_HOME -> {
                if (resultCode == Activity.RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    val isExistingUser = intent.getBooleanExtra(IS_EXISTING_USER, false)
                    if (isExistingUser) {
                        binding.contactInfoExistingUser.contactInfoWithPhoneNumberInput.text.clear()
                        binding.contactInfoExistingUser.contactInfoWithOutPhoneNumberInput.text.clear()
                    } else {
                        binding.contactNewUser.contactInfoFirstNameInput.text.clear()
                        binding.contactNewUser.contactInfoLastNameInput.text.clear()
                        binding.contactNewUser.contactInfoEmailInput.text.clear()
                        binding.contactNewUser.contactInfoPhoneNumberInput.text.clear()
                    }
                }
            }
        }
    }

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
    companion object {
        const val IS_EXISTING_USER = "isExistingUser"
        const val CUSTOMER_CARE_OPTION = "CustomerCareOption"
        const val ADDITIONAL_INFO = "AdditionalInfo"
        const val REQUEST_TO_HOME: Int = 1100

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, ContactInfoActivity::class.java)
                .putExtra(IS_EXISTING_USER, bundle.getBoolean(IS_EXISTING_USER))
                .putExtra(CUSTOMER_CARE_OPTION, bundle.getString(CUSTOMER_CARE_OPTION))
                .putExtra(ADDITIONAL_INFO, bundle.getString(ADDITIONAL_INFO))
        }
    }
}
