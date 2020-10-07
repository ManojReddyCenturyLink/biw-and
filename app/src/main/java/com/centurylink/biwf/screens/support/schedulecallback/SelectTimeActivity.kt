package com.centurylink.biwf.screens.support.schedulecallback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivitySelectTimeBinding
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

class SelectTimeActivity: BaseActivity() {

    @Inject
    lateinit var factory: DaggerViewModelFactory

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(SelectTimeViewModel::class.java)
    }
    private lateinit var binding: ActivitySelectTimeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectTimeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initHeaders()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.select_time)
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
    }

    companion object {
        const val SELECT_TIME: String = "SelectTime"

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, SelectTimeActivity::class.java)
                .putExtra(SELECT_TIME, bundle.getString(SELECT_TIME))
        }
    }
}