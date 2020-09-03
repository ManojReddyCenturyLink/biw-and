package com.centurylink.biwf.screens.notification

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.base.BaseViewModel
import com.centurylink.biwf.databinding.ActivityNotifcationDetailsBinding
import com.centurylink.biwf.screens.common.CustomWebFragment
import com.centurylink.biwf.screens.support.SupportViewModel
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

/**
 * Activity for displaying the Notification Details in a WebView
 */
class NotificationDetailsActivity : BaseActivity() {

    companion object {
        const val LAUNCH_FROM_HOME: String = "launchType"
        const val URL_TO_LAUNCH: String = "launchurl"
        const val REQUEST_TO_DISMISS = 1000

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, NotificationDetailsActivity::class.java)
                .putExtra(LAUNCH_FROM_HOME, bundle.getBoolean(LAUNCH_FROM_HOME))
                .putExtra(URL_TO_LAUNCH, bundle.getString(URL_TO_LAUNCH))
        }
    }

    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val manager = supportFragmentManager
    private lateinit var binding: ActivityNotifcationDetailsBinding
    private var url: String? = null

    override val viewModel by lazy {
        ViewModelProvider(this, factory).get(NotificationDetailsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityNotifcationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActivityHeight()
        initFragment()
        initHeaders()
    }

    private fun initHeaders() {
        var screenTitle: String = getString(R.string.notification_details)
        binding.incHeader.apply {
            subHeaderLeftIcon.visibility = View.VISIBLE
            subheaderCenterTitle.text = screenTitle
            subheaderRightActionTitle.text = getText(R.string.done)
            subheaderRightActionTitle.setOnClickListener { finish() }
            subHeaderLeftIcon.setOnClickListener { finish()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initFragment() {
        url = intent.getStringExtra(URL_TO_LAUNCH)
        val transaction = manager.beginTransaction()
        val fragment =
            CustomWebFragment.newInstance(url!!)
        transaction.replace(R.id.notification_details_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}