package com.centurylink.biwf.screens.notification

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityNotifcationDetailsBinding
import com.centurylink.biwf.screens.common.CustomWebFragment

/**
 * Activity for displaying the Notification Details in a WebView
 */
class NotificationDetailsActivity : BaseActivity() {

    companion object {
        const val launchFromHome: String = "launchType"
        const val urlToLaunch: String = "launchurl"
        const val requestToDismiss = 1000

        fun newIntent(context: Context, bundle: Bundle): Intent {
            return Intent(context, NotificationDetailsActivity::class.java)
                .putExtra(launchFromHome, bundle.getBoolean(launchFromHome))
                .putExtra(urlToLaunch, bundle.getString(urlToLaunch))
        }
    }

    private val manager = supportFragmentManager
    private lateinit var binding: ActivityNotifcationDetailsBinding
    private var url: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
        binding = ActivityNotifcationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setHeightofActivity()
        initFragment()
        initView()
    }

    private fun initView() {
        val displayBackIcon = intent.getBooleanExtra(launchFromHome, false)
        if (displayBackIcon) {
            binding.notificationDetailsBackIcon.visibility = View.VISIBLE
        } else {
            binding.notificationDetailsBackIcon.visibility = View.GONE
        }
        binding.notificationDetailsBackIcon.setOnClickListener { finish() }
        binding.notificationDetailsCloseIcon.setOnClickListener {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun initFragment() {
        url = intent.getStringExtra(urlToLaunch)
        val transaction = manager.beginTransaction()
        val fragment =
            CustomWebFragment.newInstance(url!!)
        transaction.replace(R.id.notification_details_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}