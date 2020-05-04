package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.SupportCoordinator
import com.centurylink.biwf.databinding.ActivitySupportBinding
import com.centurylink.biwf.model.support.FaqTopicsItem
import com.centurylink.biwf.screens.subscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.subscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.support.adapter.SupportFAQAdapter
import com.centurylink.biwf.screens.support.adapter.SupportItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.observe
import javax.inject.Inject

class SupportActivity : BaseActivity(), SupportItemClickListener {

    @Inject
    lateinit var supportCoordinator: SupportCoordinator
    @Inject
    lateinit var factory: DaggerViewModelFactory
    @Inject
    lateinit var navigator: Navigator

    private val supportViewModel by lazy {
        ViewModelProvider(this, factory).get(SupportViewModel::class.java)
    }

    private lateinit var adapter: SupportFAQAdapter
    private lateinit var binding: ActivitySupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)

        supportViewModel.apply {
            faqLiveData.observe(this@SupportActivity, Observer {
                prepareRecyclerView(it)
            })
        }
        supportCoordinator.observeThis(supportViewModel.myState)
        init()
        getNotificationInformation()
    }

    override fun onFaqItemClick(itemFAQ: FaqTopicsItem) {
        supportViewModel.navigateToFAQList(itemFAQ)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CancelSubscriptionActivity.REQUEST_TO_SUBSCRIPTION -> {
                if (resultCode == CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT) {
                    setResult(CancelSubscriptionDetailsActivity.REQUEST_TO_ACCOUNT)
                    finish()
                } else if (resultCode == Activity.RESULT_OK) {
                    finish()
                }
            }
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
        binding.doneButtonSupport.setOnClickListener { finish() }

        binding.incTroubleshooting.restartModemButton.setOnClickListener { supportViewModel.restartModem() }
        binding.incTroubleshooting.runSpeedTestButton.setOnClickListener { supportViewModel.runSpeedTest() }
        binding.incTroubleshooting.supportVisitWebsite.setOnClickListener { }

        binding.incContactUs.liveChatTextview.setOnClickListener { supportViewModel.setManageSubscription() }
        binding.incContactUs.scheduleCallbackTextview.setOnClickListener { supportViewModel.launchScheduleCallback() }
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

    companion object {
        const val REQUEST_TO_HOME: Int = 12200

        fun newIntent(context: Context): Intent {
            return Intent(context, SupportActivity::class.java)
        }
    }
}
