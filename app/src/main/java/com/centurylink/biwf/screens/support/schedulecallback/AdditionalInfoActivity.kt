package com.centurylink.biwf.screens.support.schedulecallback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.AdditionalInfoCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityAdditionalInfoBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class AdditionalInfoActivity : BaseActivity() {

    @Inject
    lateinit var additionalInfoCoordinator: AdditionalInfoCoordinator

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    /*For future use*/
    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(AdditionalInfoViewModel::class.java)
    }
    private lateinit var binding: ActivityAdditionalInfoBinding
    private var isExistingUser: Boolean = false
    private lateinit var customerCareOption: String
    private lateinit var additionalInfo: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdditionalInfoBinding.inflate(layoutInflater)
        navigator.observe(this)
        viewModel.myState.observeWith(additionalInfoCoordinator)
        setContentView(binding.root)
        initHeaders()
        initOnClicks()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        customerCareOption = intent.getStringExtra(ADDITIONAL_INFO)
        val screenTitle: String = getString(R.string.additional_info_title)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                viewModel.logBackButtonClick()
                finish()
            }
            subheaderRightActionTitle.text = getText(R.string.additional_info_cancel)
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logCancelButtonClick()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun initOnClicks() {
        binding.additionalInfoNextBtn.setOnClickListener {
           viewModel.logNextButtonClick()
            additionalInfo = binding.additionalInfoInput.text.toString()
            isExistingUser = intent.getBooleanExtra(IS_EXISTING_USER, false)
            viewModel.launchContactInfo(isExistingUser, customerCareOption, additionalInfo)
       }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_TO_HOME -> {
               if (resultCode == Activity.RESULT_OK) {
                    setResult(RESULT_OK)
                    finish()
               } else if(resultCode == Activity.RESULT_CANCELED) {
                   binding.additionalInfoInput.text.clear()
               }
            }
        }
    }

    companion object {
        const val ADDITIONAL_INFO: String = "AdditionalInfo"
        const val REQUEST_TO_HOME: Int = 1100
        const val IS_EXISTING_USER = "isExistingUser"

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, AdditionalInfoActivity::class.java)
                .putExtra(ADDITIONAL_INFO, bundle.getString(ADDITIONAL_INFO))
                .putExtra(IS_EXISTING_USER, bundle.getBoolean(IS_EXISTING_USER))
        }
    }
}