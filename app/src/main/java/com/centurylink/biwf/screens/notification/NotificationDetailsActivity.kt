package com.centurylink.biwf.screens.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityNotifcationDetailsBinding
import com.centurylink.biwf.screens.common.CustomWebFragment
import com.centurylink.biwf.utility.DaggerViewModelFactory
import javax.inject.Inject

/**
 * Notification details activity - Activity for displaying the Notification Details in a WebView
 *
 * @constructor Create empty Notification details activity
 */
class NotificationDetailsActivity : BaseActivity() {

    /**
     * Companion - It is initialized when the class is loaded.
     *
     * @constructor Create empty Companion
     */
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

    /**
     * On create - Called when the activity is first created
     *
     *@param savedInstanceState - Bundle: If the activity is being re-initialized after previously
     * being shut down then this Bundle contains the data it most recently supplied in
     * onSaveInstanceState(Bundle). Note: Otherwise it is null. This value may be null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityNotifcationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setActivityHeight()
        initFragment()
        initHeaders()
    }

    /**
     * Init headers - It will initialize screen headers
     *
     */
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

    /**
     * On back pressed - This will handle back key click listeners
     *
     */
    override fun onBackPressed() {
        finish()
    }

    /**
     * Init fragment - It will initialize Notification Details in a WebView
     *
     */
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
