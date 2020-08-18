package com.centurylink.biwf.screens.support.schedulecallback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityAdditionalInfoBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class AdditionalInfoActivity : BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    /*For future use*/
    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(AdditionalInfoViewModel::class.java)
    }
    private lateinit var binding: ActivityAdditionalInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdditionalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initHeaders()
        initOnClicks()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.additional_info_title)
        binding.incHeader.apply {
            subheaderCenterTitle.text = screenTitle
            subHeaderLeftIcon.setOnClickListener {
                viewModel.logBackButtonClick()
                finish() }
            subheaderRightActionTitle.text = getText(R.string.text_header_cancel)
            subheaderRightActionTitle.setOnClickListener {
                viewModel.logCancelButtonClick()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun initOnClicks() {
        binding.additionalInfoNextBtn.setOnClickListener { viewModel.logNextButtonClick() }
    }

    companion object {
        const val ADDITIONAL_INFO: String = "AdditionalInfo"
        const val REQUEST_TO_HOME: Int = 1100

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, AdditionalInfoActivity::class.java).putExtra(
                ADDITIONAL_INFO, bundle.getString(ADDITIONAL_INFO)
            )
        }
    }
}