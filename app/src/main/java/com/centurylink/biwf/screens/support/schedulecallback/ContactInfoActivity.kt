package com.centurylink.biwf.screens.support.schedulecallback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.ContactInfoCoordinator
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.databinding.ActivityContactInfoBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
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
    }

    private fun initOnClicks() {
        binding.contactInfoNextBtn.setOnClickListener {
            viewModel.launchSelectTime()
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