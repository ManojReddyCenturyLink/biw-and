package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.SupportCoordinator
import com.centurylink.biwf.databinding.ActivitySupportBinding
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.support.adapter.SupportFAQAdapter
import com.centurylink.biwf.screens.support.adapter.SupportItemClickListener
import com.centurylink.biwf.service.impl.workmanager.ModemRebootMonitorService
import com.centurylink.biwf.utility.DaggerViewModelFactory
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

    override val viewModel by lazy {
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
        viewModel.myState.observeWith(supportCoordinator)
        initLiveChat()
        initViews()
        observeViews()
    }

    override fun onFaqItemClick(item: String) {
        viewModel.navigateToFAQList(item)
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

    override fun retryClicked() {
        showProgress(true)
        viewModel.initApis()
    }

    private fun observeViews() {
        viewModel.apply {
            faqSectionInfo.observe {
                prepareRecyclerView(it.questionMap)
            }
        }
    }

    private fun initViews() {
        setApiProgressViews(
            binding.progressOverlay.root,
            binding.retryOverlay.retryViewLayout,
            binding.supportScrollView,
            binding.retryOverlay.root
        )
        viewModel.apply {
            uploadSpeed.observe { binding.incTroubleshooting.uploadSpeed.text = it }
            downloadSpeed.observe { binding.incTroubleshooting.downloadSpeed.text = it }
            latestSpeedTest.observe { binding.incTroubleshooting.lastSpeedTestTime.text = it }
            progressVisibility.observe {
                binding.incTroubleshooting.uploadSpeed.visibility = if (it) View.INVISIBLE else View.VISIBLE
                binding.incTroubleshooting.downloadSpeed.visibility = if (it) View.INVISIBLE else View.VISIBLE
                binding.incTroubleshooting.downloadProgressIcon.visibility = if (it) View.VISIBLE else View.INVISIBLE
                binding.incTroubleshooting.uploadProgressIcon.visibility = if (it) View.VISIBLE else View.INVISIBLE
            }
            modemResetButtonState.observe {
                binding.incTroubleshooting.rebootModemButton.isActivated = it
            }
            speedTestButtonState.observe {
                binding.incTroubleshooting.runSpeedTestButton.isActivated = it
            }
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            detailedRebootStatusFlow.observe { rebootStatus ->
                when (rebootStatus) {
                    ModemRebootMonitorService.RebootState.READY -> {
                        setRebootButtonVisibility(false)
                    }
                    ModemRebootMonitorService.RebootState.ONGOING -> {
                        setRebootButtonVisibility(true)
                    }
                    ModemRebootMonitorService.RebootState.SUCCESS -> {
                        setRebootButtonVisibility(false)
                        showModemRebootSuccessDialog()
                    }
                    ModemRebootMonitorService.RebootState.ERROR -> {
                        setRebootButtonVisibility(false)
                        showModemRebootErrorDialog()
                    }
                }
            }
        }
        binding.supportFaqTopicsRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.doneButtonSupport.setOnClickListener {
            viewModel.logDoneButtonClick()
            finish()
        }

        binding.incTroubleshooting.apply {
            rebootModemButton.setOnClickListener {
                viewModel.rebootModem()
            }
            runSpeedTestButton.setOnClickListener { viewModel.startSpeedTest() }
            supportVisitWebsite.setOnClickListener {
                viewModel.logVisitWebsite()
                //TODO Add Website feature when url is available
            }
        }

        binding.incContactUs.liveChatTextview.setOnClickListener {
            viewModel.logLiveChatLaunch()
            chatUIClient?.startChatSession(
                this
            )
        }
        binding.incContactUs.scheduleCallbackRow.setOnClickListener { viewModel.launchScheduleCallback() }
    }

    private fun initLiveChat() {
        val chatConfiguration =
            ChatConfiguration.Builder(ORG_ID, BUTTON_ID, DEPLOYMENT_ID, AGENT_POD).build()

        ChatUI.configure(ChatUIConfiguration.create(chatConfiguration)).createClient(this)
            .onResult { _, uiClient ->
                chatUIClient = uiClient
            }
    }

    private fun prepareRecyclerView(list: List<String>) {
        adapter = SupportFAQAdapter(this, this, list)
        binding.supportFaqTopicsRecyclerview.adapter = adapter
    }

    private fun setRebootButtonVisibility(restarting: Boolean) {
        binding.incTroubleshooting.rebootModemButton.visibility = if (restarting) View.GONE else View.VISIBLE
        binding.incTroubleshooting.rebootingModemButton.root.visibility = if (restarting) View.VISIBLE else View.GONE
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
