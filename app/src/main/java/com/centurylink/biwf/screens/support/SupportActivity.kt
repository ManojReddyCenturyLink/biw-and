package com.centurylink.biwf.screens.support

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.WorkManager
import com.centurylink.biwf.R
import com.centurylink.biwf.base.BaseActivity
import com.centurylink.biwf.coordinators.Navigator
import com.centurylink.biwf.coordinators.SupportCoordinator
import com.centurylink.biwf.databinding.ActivitySupportBinding
import com.centurylink.biwf.repos.ModemRebootRepository
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionActivity
import com.centurylink.biwf.screens.cancelsubscription.CancelSubscriptionDetailsActivity
import com.centurylink.biwf.screens.support.adapter.SupportFAQAdapter
import com.centurylink.biwf.screens.support.adapter.SupportItemClickListener
import com.centurylink.biwf.utility.DaggerViewModelFactory
import com.centurylink.biwf.widgets.CustomDialogBlueTheme
import com.centurylink.biwf.widgets.CustomDialogGreyTheme
import com.salesforce.android.chat.core.ChatConfiguration
import com.salesforce.android.chat.ui.ChatUI
import com.salesforce.android.chat.ui.ChatUIClient
import com.salesforce.android.chat.ui.ChatUIConfiguration
import javax.inject.Inject

class SupportActivity : BaseActivity(), SupportItemClickListener,
    CustomDialogGreyTheme.DialogCallback, CustomDialogBlueTheme.ErrorDialogCallback {

    @Inject
    lateinit var supportCoordinator: SupportCoordinator

    @Inject
    lateinit var factory: DaggerViewModelFactory

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var workManager: WorkManager

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

    override fun retryClicked() {
        showProgress(true)
        supportViewModel.initApis()
    }

    private fun observeViews() {
        supportViewModel.apply {
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
        supportViewModel.apply {
            uploadSpeed.observe { binding.incTroubleshooting.uploadSpeed.text = it }
            downloadSpeed.observe { binding.incTroubleshooting.downloadSpeed.text = it }
            latestSpeedTest.observe { binding.incTroubleshooting.lastSpeedTestTime.text = it }
            progressVisibility.observe {
                binding.incTroubleshooting.uploadSpeed.visibility = if (it) View.INVISIBLE else View.VISIBLE
                binding.incTroubleshooting.downloadSpeed.visibility = if (it) View.INVISIBLE else View.VISIBLE
                binding.incTroubleshooting.downloadProgressIcon.visibility = if (it) View.VISIBLE else View.INVISIBLE
                binding.incTroubleshooting.uploadProgressIcon.visibility = if (it) View.VISIBLE else View.INVISIBLE
                binding.incTroubleshooting.runSpeedTestButton.isActivated = !it
            }
            progressViewFlow.observe { showProgress(it) }
            errorMessageFlow.observe { showRetry(it.isNotEmpty()) }
            modemRebootStatusFlow.observe { rebootStatus ->
                when (rebootStatus) {
                    ModemRebootRepository.Companion.RebootState.READY -> {
                        setRebootButtonVisibility(false)
                    }
                    ModemRebootRepository.Companion.RebootState.ONGOING -> {
                        setRebootButtonVisibility(true)
                    }
                    ModemRebootRepository.Companion.RebootState.SUCCESS -> {
                        setRebootButtonVisibility(false)
                        showModemRebootSuccessDialog()
                    }
                    ModemRebootRepository.Companion.RebootState.ERROR -> {
                        setRebootButtonVisibility(false)
                        showModemRebootErrorDialog()
                    }
                }
            }
        }
        binding.supportFaqTopicsRecyclerview.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.doneButtonSupport.setOnClickListener { finish() }

        binding.incTroubleshooting.apply {
            rebootModemButton.setOnClickListener { supportViewModel.rebootModem() }
            runSpeedTestButton.setOnClickListener { supportViewModel.startSpeedTest() }
            supportVisitWebsite.setOnClickListener {
                //TODO Add Website feature when url is available
            }
        }

        binding.incContactUs.liveChatTextview.setOnClickListener {
            chatUIClient?.startChatSession(
                this
            )
        }
        binding.incContactUs.scheduleCallbackRow.setOnClickListener { supportViewModel.launchScheduleCallback() }
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

    // TODO - Extract to more central location as needed
    private fun showModemRebootSuccessDialog() {
        workManager.pruneWork()
        CustomDialogBlueTheme(
            title = getString(R.string.modem_reboot_success_title),
            message = getString(R.string.modem_reboot_success_message),
            buttonText = getString(R.string.modem_reboot_success_button),
            isErrorPopup = false
        ).show(
            supportFragmentManager,
            callingActivity?.className
        )
    }

    // TODO - Extract to more central location as needed
    private fun showModemRebootErrorDialog() {
        workManager.pruneWork()
        CustomDialogGreyTheme(
            title = getString(R.string.modem_reboot_error_title),
            message = getString(R.string.modem_reboot_error_message),
            positiveText = getString(R.string.modem_reboot_error_button_positive),
            negativeText = getString(R.string.modem_reboot_error_button_negative)
        ).show(
            supportFragmentManager,
            callingActivity?.className
        )
    }

    // Button press callbacks for the Modem Reboot Error Dialog
    override fun onDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                supportViewModel.onRetryModemRebootClicked()
            }
            AlertDialog.BUTTON_NEGATIVE -> {
                supportViewModel.onCancelModemRebootClicked()
            }
        }
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

    override fun onErrorDialogCallback(buttonType: Int) {
        when (buttonType) {
            AlertDialog.BUTTON_POSITIVE -> {
                finish()
            }
        }
    }
}
