package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.SupportCoordinator
import com.centurylink.biwf.databinding.ActivitySupportBinding
import com.centurylink.biwf.model.support.FaqTopicsItem
import com.centurylink.biwf.screens.subscription.ManageSubscriptionActivity
import com.centurylink.biwf.screens.support.adapter.SupportFAQAdapter
import com.centurylink.biwf.screens.support.adapter.SupportItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.observe
import kotlinx.android.synthetic.main.widget_contact_us_section.view.*
import kotlinx.android.synthetic.main.widget_header_component.view.*
import kotlinx.android.synthetic.main.widget_troubleshooting.view.*
import javax.inject.Inject

class SupportActivity : BaseActivity(), SupportItemClickListener {

    companion object {
        fun newIntent(context: Context) = Intent(context, SupportActivity::class.java)
    }

    @Inject
    lateinit var supportCoordinator: SupportCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory

    private val supportViewModel by lazy {
        ViewModelProvider(this, factory).get(SupportViewModel::class.java)
    }

    private lateinit var adapter: SupportFAQAdapter
    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportViewModel.apply {
            faqLiveData.observe(this@SupportActivity, Observer {
                prepareRecyclerView(it)
            })
        }
        supportCoordinator.observeThis(supportViewModel.myState)
        init()
        getNotificationInformation()
    }

    override fun onResume() {
        super.onResume()
        supportCoordinator.navigator.activity = this
    }

    override fun onFaqItemClick(itemFAQ: FaqTopicsItem) {
        supportViewModel.navigateToFAQList(itemFAQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ManageSubscriptionActivity.REQUEST_TO_SUBSCRIPTION,
            FAQActivity.REQUEST_TO_HOME -> {
                if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun init() {
        binding.supportFaqTopicsRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.incHeader.root.header_title.setText(R.string.support)
        binding.incHeader.root.header_closeIcon.setOnClickListener { finish() }

        binding.incTroubleshooting.root.restart_modem_button.setOnClickListener { supportViewModel.restartModem() }
        binding.incTroubleshooting.root.run_speed_test_button.setOnClickListener { supportViewModel.runSpeedTest() }
        binding.incTroubleshooting.root.support_visit_website.setOnClickListener { }

        binding.incContactUs.root.live_chat_textview.setOnClickListener { supportViewModel.setManageSubscription() }
        binding.incContactUs.root.schedule_callback_textview.setOnClickListener { }
        binding.incContactUs.root.support_call_us_link.setOnClickListener { supportViewModel.callUs() }
    }

    private fun getNotificationInformation() {
        supportViewModel.getResponseData().observe(this) {
            when {
                it.status.isLoading() -> {
                }
                it.status.isSuccessful() -> {
                    supportViewModel.displaySortedNotifications(it.data!!.faqTopics)
                }
                it.status.isError() -> {
                }
            }
        }
    }

    private fun prepareRecyclerView(list: MutableList<FaqTopicsItem>) {
        adapter = SupportFAQAdapter(this, this, list)
        binding.supportFaqTopicsRecyclerview.adapter = adapter
    }
}
