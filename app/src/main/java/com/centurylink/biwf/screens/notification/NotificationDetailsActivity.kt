package com.centurylink.biwf.screens.notification

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.databinding.ActivityNotifcationDetailsBinding
import com.centurylink.biwf.screens.common.CustomWebFragment

/**
 * Activity for displaying the Notification Details in a WebView
 */
class NotificationDetailsActivity : BaseActivity() {

    private lateinit var binding: ActivityNotifcationDetailsBinding

    private val manager = supportFragmentManager;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotifcationDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setHeightofActivity()
        initFragment()
        initView()
    }

    private fun initView() {
        binding.ivBackIcon.setOnClickListener { finish() }
        binding.ivCloseIcon.setOnClickListener { finish() }
    }

    private fun initFragment() {
        val transaction = manager.beginTransaction()
        val fragment =
            CustomWebFragment.newInstance("https://www.centurylink.com/business.html", true)
        transaction.replace(R.id.containerLayout, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, NotificationDetailsActivity::class.java)
    }
}