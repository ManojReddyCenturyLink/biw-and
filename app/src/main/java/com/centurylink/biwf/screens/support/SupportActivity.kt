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
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.support.adapter.SupportFAQAdapter
import com.centurylink.biwf.screens.support.adapter.SupportItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.utility.observe
import com.salesforce.android.chat.core.ChatConfiguration
import com.salesforce.android.chat.ui.ChatUI
import com.salesforce.android.chat.ui.ChatUIClient
import com.salesforce.android.chat.ui.ChatUIConfiguration
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
    private lateinit var chatUIClient: ChatUIClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySupportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navigator.observe(this)
        supportViewModel.myState.observeWith(supportCoordinator)
        initLiveChat()
        initViews()
        observeViews()

    }

    private fun observeViews() {
        supportViewModel.apply {
            faqSectionInfo.observe {
                prepareRecyclerView(it.questionMap)
            }
        }
    }

    override fun onFaqItemClick(item: String) {
        supportViewModel.navigateToFAQList(item)
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

    private fun initViews() {
        binding.supportFaqTopicsRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.doneButtonSupport.setOnClickListener { finish() }

        binding.incTroubleshooting.apply {
            restartModemButton.setOnClickListener { supportViewModel.restartModem() }
            runSpeedTestButton.setOnClickListener { supportViewModel.runSpeedTest() }
            supportVisitWebsite.setOnClickListener {
                //TODO Add Website feature when url is available
            }
        }

        binding.incContactUs.liveChatTextview.setOnClickListener { chatUIClient?.startChatSession(this) }
        binding.incContactUs.scheduleCallbackRow.setOnClickListener { supportViewModel.launchScheduleCallback() }
    }

    private fun initLiveChat() {
        val chatConfiguration = ChatConfiguration.Builder(ORG_ID, BUTTON_ID, DEPLOYMENT_ID, AGENT_POD).build()

        ChatUI.configure(ChatUIConfiguration.create(chatConfiguration)).createClient(this)
            .onResult { _, uiClient ->
                chatUIClient = uiClient
            }
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

    private fun prepareRecyclerView(list: List<String>) {
        adapter = SupportFAQAdapter(this, this, list)
        binding.supportFaqTopicsRecyclerview.adapter = adapter
    }

    companion object {
        const val REQUEST_TO_HOME: Int = 12200
        const val AGENT_POD = "d.la1-c1cs-ord.salesforceliveagent.com"
        const val ORG_ID = "00Df0000002HOQc"
        const val DEPLOYMENT_ID = "572f0000000Cauc"
        const val BUTTON_ID = "573f000000000zz"

        fun newIntent(context: Context): Intent {
            return Intent(context, SupportActivity::class.java)
        }
    }
}
