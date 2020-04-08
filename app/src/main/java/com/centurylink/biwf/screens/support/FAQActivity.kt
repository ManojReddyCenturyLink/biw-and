package com.centurylink.biwf.screens.support

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.screens.notification.NotificationActivity

class FAQActivity : BaseActivity() {
    companion object {
        fun newIntent(context: Context) = Intent(context, FAQActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.TransparentActivity)
        super.onCreate(savedInstanceState)
    }
}